package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.demgames.polypong.Globals;
import com.demgames.polypong.network.sendclasses.SendSettings;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import processing.core.PVector;

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
        Log.e(TAG, "disconnected.");
    }

    @Override
    public void received(Connection connection,Object object) {
        Log.e(TAG, "Package received.");

        if(object instanceof SendSettings) {
            Log.d(TAG,"received settings");
            SendSettings mySettings=(SendSettings)object;
            PVector[] ballsPositions=mySettings.ballsPositions;
            PVector[] ballsVelocities=mySettings.ballsVelocities;
            float[] ballsSizes=mySettings.ballsSizes;
            int gameMode=mySettings.gameMode;
            boolean gravityState=mySettings.gravityState;
            boolean attractionState=mySettings.attractionState;

            globalVariables.setNumberOfBalls(ballsPositions.length);
            globalVariables.setBalls(false);
            globalVariables.setGameMode(gameMode);
            globalVariables.setGravityState(gravityState);
            globalVariables.setAttractionState(attractionState);

            for (int i=0; i<ballsPositions.length;i++) {
                globalVariables.setBallPosition(i,ballsPositions[i]);
                globalVariables.setBallVelocity(i,ballsVelocities[i]);
                globalVariables.setBallPlayerScreen(i,0);
                globalVariables.setBallSize(i,ballsSizes[i]);
                Log.d(TAG,"x "+Float.toString(globalVariables.getBallsPositions()[i].x)+", y "+Float.toString(globalVariables.getBallsPositions()[i].y));
                /*tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                Log.e(TAG, "Connection: "+ tempIpAdress);*/
            }

            globalVariables.setReadyStateState(true);
        }
    }
}
