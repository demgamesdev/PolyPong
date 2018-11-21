package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.demgames.polypong.GDXGameLauncher;
import com.demgames.polypong.Globals;
import com.demgames.polypong.IGlobals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.ArrayList;
import java.util.Arrays;

public class ClientListener extends Listener{

    Globals globalVariables;

    public ClientListener(Context myContext) {
        globalVariables=(Globals)myContext;
    }

    private static final String TAG = "ClientListener";

    @Override
    public void connected(Connection connection) {
        String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
        Log.e(TAG, tempIpAdress+" connected.");
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, " disconnected.");
        GDXGameLauncher.GDXGAME.finish();
    }

    @Override
    public void received(Connection connection,Object object) {
        //Log.d(TAG, "Package received.");


    }
}
