package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.demgames.polypong.Globals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

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

        if(object instanceof Globals.SendVariables.SendSettings) {
            Log.d(TAG,"received settings");
            Globals.SendVariables.SendSettings mySettings=(Globals.SendVariables.SendSettings)object;
            Vector2[] ballsPositions=mySettings.ballsPositions;
            Vector2[] ballsVelocities=mySettings.ballsVelocities;
            float[] ballsSizes=mySettings.ballsSizes;
            int gameMode=mySettings.gameMode;
            boolean gravityState=mySettings.gravityState;
            boolean attractionState=mySettings.attractionState;

            globalVariables.getGameVariables().numberOfBalls=ballsPositions.length;
            globalVariables.getGameVariables().setBalls(false);
            globalVariables.getSettingsVariables().gameMode=gameMode;
            globalVariables.getGameVariables().gravityState=gravityState;
            globalVariables.getGameVariables().attractionState=attractionState;

            for (int i=0; i<ballsPositions.length;i++) {
                globalVariables.getGameVariables().ballsPositions[i]=new Vector2(1-ballsPositions[i].x,2-ballsPositions[i].y);
                globalVariables.getGameVariables().ballsVelocities[i]=new Vector2(-ballsVelocities[i].x,-ballsVelocities[i].y);
                globalVariables.getGameVariables().ballsPlayerScreens[i]=0;
                globalVariables.getGameVariables().ballsSizes[i]=ballsSizes[i];
                Log.d(TAG,"x "+Float.toString(globalVariables.getGameVariables().ballsPositions[i].x)+", y "+Float.toString(globalVariables.getGameVariables().ballsPositions[i].y));
                /*tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                Log.e(TAG, "Connection: "+ tempIpAdress);*/
            }

            globalVariables.getSettingsVariables().readyState=true;
        }
    }
}
