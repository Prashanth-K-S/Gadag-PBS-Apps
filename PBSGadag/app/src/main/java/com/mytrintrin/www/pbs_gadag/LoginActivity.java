package com.mytrintrin.www.pbs_gadag;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.mytrintrin.www.pbs_gadag.Application.MyApplication;
import com.mytrintrin.www.pbs_gadag.Services.ConnectivityReceiver;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class LoginActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private ImageView mLoginLogo;
    private MaterialEditText mLoginEmail,mLoginPassword;
    private Toolbar mToolbar;
    private CheckBox mShowPassword;
    private Dialog alertDialog;
    String email,password;
    RelativeLayout LoginRootLayout;
    //Regex for email vaildation
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


    //To create socket
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(API.BaseURL);
        } catch (URISyntaxException e) {
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginLogo = findViewById(R.id.iv_loginlogo);
        Glide.with(this).load(R.drawable.trintrin).into(mLoginLogo);
        mToolbar = findViewById(R.id.logintoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoginEmail = findViewById(R.id.edtEmail);
        mLoginPassword = findViewById(R.id.edtPassword);
        mShowPassword = findViewById(R.id.cbShowPwd);
        alertDialog = new Dialog(this);
        LoginRootLayout = findViewById(R.id.loginrootlayout);

        mShowPassword = findViewById(R.id.cbShowPwd);

        //To show/hide password
        mShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else {
                    mLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        checkConnection();
    }

    //To validate login credentials
    public void validatecredentials(View view)
    {
        email = mLoginEmail.getText().toString().trim();
        password = mLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            mLoginEmail.setError("Please Enter Email");
            Snackbar.make(LoginRootLayout, "Please Enter Email", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!email.matches(EMAIL_PATTERN)) {
            mLoginEmail.setError("Please Enter Valid Email");
            Snackbar.make(LoginRootLayout, "Please Enter Valid Email", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mLoginPassword.setError("Please Enter Password");
            Snackbar.make(LoginRootLayout, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        //Method to signin
        Signinwithmail(email, password);

    }

    //To display forgot password dialog
    public void showforgotpassworddialog(View view)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
        dialog.setTitle("Forgot Password");
        dialog.setMessage("Please enter registered email");

        LayoutInflater inflater = LayoutInflater.from(this);
        View forgot_layout = inflater.inflate(R.layout.forgot_layout, null);
        final MaterialEditText edtEmail = forgot_layout.findViewById(R.id.edtEmail);

        dialog.setView(forgot_layout);

        dialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(edtEmail.getText().toString().matches(EMAIL_PATTERN)) {
                    JSONObject forgotobject = new JSONObject();
                    try {
                        forgotobject.put("email", edtEmail.getText().toString().trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("forgotPassword", forgotobject);
                }
                else
                {
                    Snackbar.make(LoginRootLayout, "Please Enter Valid Email", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /*//To disconnect socket
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }*/

    //To check internet connection manually
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //To connect to socket
            mSocket.connect();
            mSocket.on("login", onLoginListener);
            mSocket.on("forgotPassword", onForgotPasswordListener);
            if(alertDialog.isShowing())
            {
                alertDialog.dismiss();
            }
        } else {
            shownointernetdialog();
        }
    }

    //To sign with email/phone and password
    private void Signinwithmail(String email, String password) {

        JSONObject loginobject = new JSONObject();
        try {
            loginobject.put("email",email);
            loginobject.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("login", loginobject);
    }


    //To get login response from socket
    private Emitter.Listener onLoginListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    //Log.d("Login Response",data.toString());
                    try {
                        String status = data.getString("status");
                        if(status.equals("0"))
                        {
                            Toast.makeText(LoginActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            finish();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Invalid Login Credentials", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //To get forgot password response from socket
    private Emitter.Listener onForgotPasswordListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                   // Log.d("Forgot Response",data.toString());
                    try {
                        String status = data.getString("status");
                        if(status.equals("0"))
                        {
                            Toast.makeText(LoginActivity.this, "Password reset Link sent to your mail", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Invalid mail id", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //To show no internet dialog
    private void shownointernetdialog() {
        //alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.nointernet);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        Button retry = alertDialog.findViewById(R.id.exit_btn);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                //checkConnection();
                finishAffinity();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }
}
