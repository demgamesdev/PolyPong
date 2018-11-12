package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.demgames.polypong.GDXGameLauncher;
import com.demgames.polypong.Globals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class GlobalListener extends Listener{

    Globals globalVariables;

    public GlobalListener(Context myContext) {
        globalVariables=(Globals)myContext;
    }

    private static final String TAG = "GlobalListener";

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
        /*String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];*/
        Log.e(TAG, " disconnected.");
        GDXGameLauncher.GDXGAME.finish();
    }

    @Override
    public void received(Connection connection,Object object) {
        //Log.d(TAG, "Package received.");

        if(object instanceof Globals.SendVariables.SendBallScreenChange) {
            //Log.d(TAG,"screenchange received");
            Globals.SendVariables.SendBallScreenChange ballScreenChange=(Globals.SendVariables.SendBallScreenChange)object;

            for (int i =0; i<ballScreenChange.ballNumbers.length;i++) {
                globalVariables.getGameVariables().ballsPlayerScreens[ballScreenChange.ballNumbers[i]]=ballScreenChange.ballPlayerScreens[i];
                globalVariables.getGameVariables().ballsPositions[ballScreenChange.ballNumbers[i]]=new Vector2(-ballScreenChange.ballPositions[i].x,-ballScreenChange.ballPositions[i].y);
                globalVariables.getGameVariables().ballsVelocities[ballScreenChange.ballNumbers[i]]=new Vector2(-ballScreenChange.ballVelocities[i].x,-ballScreenChange.ballVelocities[i].y);
            }

            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");

        } else if(object instanceof Globals.SendVariables.SendBallKinetics) {
            //Log.d(TAG, "ballkinetics received");
            Globals.SendVariables.SendBallKinetics ballKinetics=(Globals.SendVariables.SendBallKinetics)object;

            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" updated to x "+Float.toString(ballPosition.x));
            for (int i =0; i<ballKinetics.ballNumbers.length;i++) {
                globalVariables.getGameVariables().ballsPlayerScreens[ballKinetics.ballNumbers[i]]=ballKinetics.ballPlayerScreens[i];
                globalVariables.getGameVariables().ballsPositions[ballKinetics.ballNumbers[i]]=new Vector2(-ballKinetics.ballPositions[i].x,-ballKinetics.ballPositions[i].y);
                globalVariables.getGameVariables().ballsVelocities[ballKinetics.ballNumbers[i]]=new Vector2(-ballKinetics.ballVelocities[i].x,-ballKinetics.ballVelocities[i].y);
            }



        } else if(object instanceof Globals.SendVariables.SendBat) {
            //Log.d(TAG,"received Bat");
            Globals.SendVariables.SendBat bat=(Globals.SendVariables.SendBat)object;
            globalVariables.getGameVariables().batPositions[bat.batPlayerScreen]=bat.batPosition.cpy().scl(-1f);
            //globalVariables.getGameVariables().batVelocities[bat.batPlayerScreen]=bat.batVelocity.cpy().scl(-1f);
            globalVariables.getGameVariables().batOrientations[bat.batPlayerScreen]=bat.batOrientation;

        } else if(object instanceof Globals.SendVariables.SendScore) {
            Log.d(TAG,"received Score");
            Globals.SendVariables.SendScore score=(Globals.SendVariables.SendScore)object;
            int myScore = score.myScore;
            int otherScore=score.otherScore;

            globalVariables.getGameVariables().myScore=myScore;
            globalVariables.getGameVariables().otherScore=otherScore;


        } else if(object instanceof Globals.SendVariables.SendSettings) {
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
                globalVariables.getGameVariables().ballsPositions[i]=new Vector2(-ballsPositions[i].x,-ballsPositions[i].y);
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
