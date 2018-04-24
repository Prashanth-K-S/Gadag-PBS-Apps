package com.mytrintrin.www.pbs_gadag;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.mytrintrin.www.pbs_gadag.Application.MyApplication;
import com.mytrintrin.www.pbs_gadag.Services.ConnectivityReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {


    ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mProgressDlg;
    private static final int LoctionPermissionCode =1;
    ArrayList<String> deviceaddress = new ArrayList<>() ;
    private Dialog alertDialog;

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
        setContentView(R.layout.activity_main);

        mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();
        mProgressDlg = new ProgressDialog(this);
        alertDialog = new Dialog(this);

        if (mBluetoothAdapter == null) {
            showUnsupported();
        }

        if (mBluetoothAdapter.isEnabled()) {
            //To ask location permission
            LocationPermission();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1000);
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);

        //To check internet connection
        checkConnection();

    }

    private void showUnsupported() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Bluetooth");
        builder.setMessage("Bluetooth is unsupported by this device");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finishAffinity();
            }
        });
        builder.show();
    }

    //Requesting Run Time permission for location
    private void LocationPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION
                }, LoctionPermissionCode);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LoctionPermissionCode:
                if (grantResults.length > 0) {
                    boolean LocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (LocationPermission) {
                        scanbluetoothdevices();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    //permission ends

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    /*@Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        mSocket.disconnect();
        super.onDestroy();
    }*/

    private void scanbluetoothdevices() {
        if(isGPSEnabled(this)) {
            mBluetoothAdapter.startDiscovery();
            mProgressDlg.setMessage("Scanning...");
            mProgressDlg.setCancelable(false);
            mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mBluetoothAdapter.cancelDiscovery();
                }
            });
            mProgressDlg.show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Location")
                    .setMessage("Location is not enable.")
                    .setPositiveButton("Enable",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    public boolean isGPSEnabled(Context mContext)
    {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            if(LocationManager.MODE_CHANGED_ACTION.equals(action))
            {
                if(isGPSEnabled(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "Location On", Toast.LENGTH_SHORT).show();
                    scanbluetoothdevices();
                }
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                        scanbluetoothdevices();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                /*deviceaddress = new ArrayList<>()*/;
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //senddevicesdetailstoserver();
                mSocket.emit("bluetoothList", deviceaddress);
                mProgressDlg.dismiss();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String name= device.getName();
                String address = device.getAddress().toString();
                if(name.contains("NokeLock")) {
                    if(!(deviceaddress.contains(address))) {
                        mDeviceList.add(device);
                        deviceaddress.add(address);
                    }
                       // Log.d("arraysize", String.valueOf(mDeviceList.size()));
                }
            }
        }
    };

    private void senddevicesdetailstoserver() {
        mSocket.emit("bluetoothList", deviceaddress);
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
            mSocket.on("cycleList", onBluetoothResponseListener);
            if(alertDialog.isShowing())
            {
                alertDialog.dismiss();
            }
        } else {
            shownointernetdialog();
        }
    }

    //To get register response from socket
    private Emitter.Listener onBluetoothResponseListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("Bluetooth Response",data.toString());
                    try {
                        String status = data.getString("status");
                        if(status.equals("0"))
                        {

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


