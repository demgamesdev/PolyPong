package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.demgames.polypong.GDXGameLauncher;
import com.demgames.polypong.Globals;
import com.demgames.polypong.IGlobals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientListener extends Listener{

    Globals globals;

    public ClientListener(Context myContext) {
        globals =(Globals)myContext;
    }

    private static final String TAG = "ClientListener";

    @Override
    public void connected(Connection connection) {
        synchronized (globals.getSettingsVariables().connectionThreadLock) {
            String tempIpAdress=connection.getRemoteAddressTCP().toString();
            tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
            Log.e(TAG, tempIpAdress+" connected.");
        }
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, " disconnected.");
        synchronized (globals.getSettingsVariables().connectionThreadLock) {
            if(GDXGameLauncher.GDXGAME!=null) {
                GDXGameLauncher.GDXGAME.finish();
            }
        }
    }

    @Override
    public void received(Connection connection,Object object) {
        Log.d(TAG, "Package received.");
        if(object instanceof Globals.SendVariables.SendDiscoveryResponse) {
            synchronized (globals.getSettingsVariables().receiveThreadLock) {
                synchronized (globals.getSettingsVariables().connectionThreadLock) {
                    Globals.SendVariables.SendDiscoveryResponse discoveryResponse = (Globals.SendVariables.SendDiscoveryResponse) object;

                    if(connection!=null) {
                        String tempIpAdress = connection.getRemoteAddressTCP().toString();
                        tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length()).split(":")[0];
                        Log.e(TAG, tempIpAdress + " discoveryresponse of " + discoveryResponse.myPlayerName);
                        IGlobals.Player tempPlayer = new IGlobals.Player();
                        tempPlayer.ipAdress = tempIpAdress;
                        tempPlayer.name = discoveryResponse.myPlayerName;

                        if (globals.getSettingsVariables().addPlayerToList(tempPlayer)) {
                            globals.getSettingsVariables().updateListViewState = true;
                        } else {
                            Log.e(TAG, tempIpAdress + " already in playerlist");
                        }
                    }
                }
            }
        }


    }
}
