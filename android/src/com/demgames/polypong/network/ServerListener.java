package com.demgames.polypong.network;

import com.demgames.polypong.Globals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import android.content.Context;
import android.util.Log;

public class ServerListener extends Listener{

    Globals globalVariables;

    public ServerListener(Context myContext) {
        globalVariables=(Globals) myContext;
    }

    private static final String TAG = "ServerListener";

    @Override
    public void connected(Connection connection) {
        String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
        Log.e(TAG, tempIpAdress+" connected.");
        globalVariables.getNetworkVariables().addToConnectionList(connection);

        if (globalVariables.getNetworkVariables().addIpTolist(tempIpAdress)) {
            globalVariables.setUpdateListViewState(true);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, "disconnected.");
    }

    @Override
    public void received(Connection connection,Object object) {
        Log.e(TAG, "Package received.");

    }

    public void removeListener(){

    }
}
