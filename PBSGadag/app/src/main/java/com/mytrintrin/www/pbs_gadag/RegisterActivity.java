package com.mytrintrin.www.pbs_gadag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.mytrintrin.www.pbs_gadag.Application.MyApplication;
import com.mytrintrin.www.pbs_gadag.Services.ConnectivityReceiver;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class RegisterActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private Toolbar mToolbar;
    private Dialog alertDialog;
    private MaterialEditText mName,mLastName,mEmail,mPassword,mConfirmPassword,mAadhar,mPhone;
    private RelativeLayout mRootLayout;
    private CheckBox mShowPassword;

    //Regex for email vaildation
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    //Regex for password vaildation
    private static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}";

    //To create socket
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(API.BaseURL);
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mToolbar = findViewById(R.id.registertoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Intializing views
        mName = findViewById(R.id.edtName);
        mLastName = findViewById(R.id.edtLastName);
        mEmail = findViewById(R.id.edtEmail);
        mPassword = findViewById(R.id.edtPassword);
        mConfirmPassword = findViewById(R.id.edtConfirmPassword);
        mAadhar = findViewById(R.id.edtAadharNo);
        mPhone = findViewById(R.id.edtMobile);
        mRootLayout = findViewById(R.id.registerrootlayout);
        alertDialog = new Dialog(this);
        mShowPassword = findViewById(R.id.cbShowPwd);
        mShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else {
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        //To check internet connection
        checkConnection();
    }


    //To validate register credentials
    public void validatecredentials(View view)
    {

        if (TextUtils.isEmpty(mName.getText().toString().trim())) {
            mName.setError("Please Enter Name");
            Snackbar.make(mRootLayout, "Please Enter Name", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!mEmail.getText().toString().trim().matches(EMAIL_PATTERN)) {
            mEmail.setError("Please Enter Valid Email");
            Snackbar.make(mRootLayout, "Please Enter Valid Email", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mPassword.getText().toString().trim())) {
            mPassword.setError("Please Enter Password");
            Snackbar.make(mRootLayout, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mConfirmPassword.getText().toString().trim())) {
            mConfirmPassword.setError("Please Enter Confirm Password");
            Snackbar.make(mRootLayout, "Please Enter Confirm Password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!mPassword.getText().toString().trim().equals(mConfirmPassword.getText().toString().trim())) {
            mPassword.setError("Password didn't match");
            mConfirmPassword.setError("Password didn't match");
            Snackbar.make(mRootLayout, "Password didn't match", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mAadhar.getText().toString().trim())||(mAadhar.getText().toString().trim().length()<12)) {
            mAadhar.setError("Invalid Aadhar Number");
            Snackbar.make(mRootLayout, "Invalid Aadhar Number", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if(!mPassword.getText().toString().trim().matches(PASSWORD_PATTERN)||!mConfirmPassword.getText().toString().trim().matches(PASSWORD_PATTERN))
        {
            Snackbar.make(mRootLayout, "Invalid Password", Snackbar.LENGTH_SHORT).show();
            showalertdialog(getString(R.string.password),getString(R.string.passworderrormsg));
            return;
        }

        if(TextUtils.isEmpty(mPhone.getText().toString().trim()))
        {
            mPhone.setError("Invalid Mobile Number");
            Snackbar.make(mRootLayout, "Invalid Mobile Number", Snackbar.LENGTH_SHORT).show();
            return;
        }

        registerUser();
    }

    private void registerUser() {

        JSONObject registerobject = new JSONObject();
        try {
            registerobject.put("name",mName.getText().toString().trim());
            registerobject.put("lastname",mLastName.getText().toString().trim());
            registerobject.put("email",mEmail.getText().toString().trim());
            registerobject.put("password",mPassword.getText().toString().trim());
            registerobject.put("aadhar",mAadhar.getText().toString().trim());
            registerobject.put("mobile",mPhone.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("register", registerobject);
    }

    //To get register response from socket
    private Emitter.Listener onRegisterListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            RegisterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("Register Response",data.toString());
                    try {
                        String status = data.getString("status");
                        if(status.equals("0"))
                        {
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //To disconnect socket
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        //mSocket.off("register", onNewMessage);
    }

    //To show password alert dialog
    private void showalertdialog(String title,String message) {

        AlertDialog.Builder errordialog = new AlertDialog.Builder(RegisterActivity.this);
        errordialog.setIcon(R.mipmap.ic_launcher);
        errordialog.setTitle(title);
        errordialog.setMessage(message);
        errordialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        errordialog.setCancelable(false);
        errordialog.show();
    }


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
            mSocket.on("register", onRegisterListener);
            if(alertDialog.isShowing())
            {
                alertDialog.dismiss();
            }
        } else {
            shownointernetdialog();
        }
    }

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
