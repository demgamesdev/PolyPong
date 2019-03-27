package com.demgames.polypong.network;

import com.demgames.miscclasses.GameObjectClasses.*;
import com.demgames.miscclasses.SendClasses.*;

import android.content.Context;
import android.util.Log;

import com.demgames.polypong.GDXGameLauncher;
import com.demgames.polypong.Globals;
import com.demgames.polypong.IGlobals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientListener extends Listener{

    private Globals globals;
    private String myPlayerName;
    private String networkMode;

    public ClientListener(Context context, String myPlayerName_, String networkMode_) {
        this.globals =(Globals)context;
        this.myPlayerName = myPlayerName_;
        this.networkMode = networkMode_;
    }

    private static final String TAG = "ClientListener";

    @Override
    public void connected(Connection connection) {
        try {
            synchronized (globals.getComm().connectionThreadLock) {
                String tempIpAdress=connection.getRemoteAddressTCP().toString();
                tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                Log.e(TAG, tempIpAdress+" connected.");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, " disconnected.");
        synchronized (globals.getComm().connectionThreadLock) {
            if(GDXGameLauncher.GDXGAME!=null) {
                GDXGameLauncher.GDXGAME.finish();
            }
        }
    }

    @Override
    public void received(Connection connection,Object object) {
        Log.d(TAG, "Package received.");
        if(object instanceof SendDiscoveryResponse) {
            try {
                synchronized (globals.getComm().receiveThreadLock) {
                    synchronized (globals.getComm().connectionThreadLock) {
                        SendDiscoveryResponse discoveryResponse = (SendDiscoveryResponse) object;

                        String tempIpAdress = connection.getRemoteAddressTCP().toString();
                        tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length()).split(":")[0];
                        Log.e(TAG, tempIpAdress + " discoveryresponse of " + discoveryResponse.myPlayerName);

                        globals.getComm().addDiscoveryPlayer(discoveryResponse.myPlayerName,tempIpAdress);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }


    }
}
