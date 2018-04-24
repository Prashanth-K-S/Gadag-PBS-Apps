package com.mytrintrin.www.pbs_gadag;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by siteurl on 19/1/18.
 */

public class API {

    public static final String BaseURL ="http://52.187.178.113:5678";


    public static void createsocket()
    {

        //To create socket
           Socket mSocket;
        {
            try {
                mSocket = IO.socket(API.BaseURL);
                mSocket.on("connect", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.d("socket","connected");
                    }
                });
            } catch (URISyntaxException e) {}
        }
    }




}
