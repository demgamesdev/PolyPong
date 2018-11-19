package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.demgames.polypong.Globals;
import com.demgames.polypong.IGlobals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.ArrayList;
import java.util.Arrays;

public class ServerListener extends Listener{

    Globals globalVariables;

    public ServerListener(Context myContext) {
        globalVariables=(Globals)myContext;
    }

    private static final String TAG = "ServerListener";

    @Override
    public void connected(Connection connection) {
        String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
        Log.e(TAG, tempIpAdress+" connected.");
        //globalVariables.getSettingsVariables().addDiscoveryConnectionToList(connection);
    }

    @Override
    public void disconnected(Connection connection) {
        /*String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];*/
        Log.e(TAG, " disconnected.");
        //GDXGameLauncher.GDXGAME.finish();
    }

    @Override
    public void received(Connection connection,Object object) {
        Log.d(TAG, "Package received.");

        if(object instanceof Globals.SendVariables.SendBallScreenChange) {
            //Log.d(TAG,"screenchange received");
            Globals.SendVariables.SendBallScreenChange ballScreenChange=(Globals.SendVariables.SendBallScreenChange)object;

            for (int i =0; i<ballScreenChange.ballNumbers.length;i++) {
                globalVariables.getGameVariables().ballsPlayerScreens[ballScreenChange.ballNumbers[i]]=ballScreenChange.ballPlayerFields[i];
                globalVariables.getGameVariables().ballsPositions[ballScreenChange.ballNumbers[i]]=new Vector2(-ballScreenChange.ballPositions[i].x,-ballScreenChange.ballPositions[i].y);
                globalVariables.getGameVariables().ballsVelocities[ballScreenChange.ballNumbers[i]]=new Vector2(-ballScreenChange.ballVelocities[i].x,-ballScreenChange.ballVelocities[i].y);
            }

            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");

        }else if(object instanceof Globals.SendVariables.SendBallGoal) {
            //Log.d(TAG, "ballkinetics received");
            Globals.SendVariables.SendBallGoal ballGoal=(Globals.SendVariables.SendBallGoal)object;

            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" updated to x "+Float.toString(ballPosition.x));
            globalVariables.getGameVariables().playerScores[(globalVariables.getSettingsVariables().myPlayerNumber +1)%2]=ballGoal.playerScores[(globalVariables.getSettingsVariables().myPlayerNumber +1)%2];
            for (int i =0; i<ballGoal.ballNumbers.length;i++) {
                globalVariables.getGameVariables().ballDisplayStates[ballGoal.ballNumbers[i]]=false;
                Log.d(TAG, "ball "+Integer.toString(ballGoal.ballNumbers[i])+" in goal");
                //TODO score
            }



        } else if(object instanceof Globals.SendVariables.SendBallKinetics) {
            //Log.d(TAG, "ballkinetics received");
            Globals.SendVariables.SendBallKinetics ballKinetics=(Globals.SendVariables.SendBallKinetics)object;

            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" updated to x "+Float.toString(ballPosition.x));
            for (int i =0; i<ballKinetics.ballNumbers.length;i++) {
                globalVariables.getGameVariables().ballsPlayerScreens[ballKinetics.ballNumbers[i]]=ballKinetics.ballPlayerFields[i];
                globalVariables.getGameVariables().ballsPositions[ballKinetics.ballNumbers[i]]=new Vector2(-ballKinetics.ballPositions[i].x,-ballKinetics.ballPositions[i].y);
                globalVariables.getGameVariables().ballsVelocities[ballKinetics.ballNumbers[i]]=new Vector2(-ballKinetics.ballVelocities[i].x,-ballKinetics.ballVelocities[i].y);
            }



        } else if(object instanceof Globals.SendVariables.SendBat) {
            //Log.d(TAG,"received Bat");
            Globals.SendVariables.SendBat bat=(Globals.SendVariables.SendBat)object;
            globalVariables.getGameVariables().batPositions[bat.batPlayerField]=bat.batPosition.cpy().scl(-1f);
            //globalVariables.getGameVariables().batVelocities[bat.batPlayerField]=bat.batVelocity.cpy().scl(-1f);
            globalVariables.getGameVariables().batOrientations[bat.batPlayerField]=bat.batOrientation;

        } else if(object instanceof Globals.SendVariables.SendScore) {
            Log.d(TAG,"received Score");
            Globals.SendVariables.SendScore score=(Globals.SendVariables.SendScore)object;



        } else if(object instanceof Globals.SendVariables.SendSettings) {
            Log.d(TAG,"received settings");
            Globals.SendVariables.SendSettings settings=(Globals.SendVariables.SendSettings)object;

            globalVariables.getSettingsVariables().myPlayerNumber=settings.yourPlayerNumber;
            globalVariables.getSettingsVariables().numberOfPlayers=settings.numberOfPlayers;
            globalVariables.getSettingsVariables().ipAdresses=new ArrayList<String>(Arrays.asList(settings.ipAdresses));
            globalVariables.getSettingsVariables().playerNames=new ArrayList<String>(Arrays.asList(settings.playerNames));

            Log.d(TAG,"received settings "+globalVariables.getSettingsVariables().ipAdresses.get(0));
            Log.d(TAG,"received settings "+globalVariables.getSettingsVariables().ipAdresses.get(1));
            //globalVariables.getSettingsVariables().playerNames=settings.playerNames;

            globalVariables.getGameVariables().numberOfBalls=settings.ballsPositions.length;
            globalVariables.getGameVariables().setBalls(false);
            globalVariables.getSettingsVariables().gameMode=settings.gameMode;
            globalVariables.getGameVariables().gravityState=settings.gravityState;
            globalVariables.getGameVariables().attractionState=settings.attractionState;

            for (int i=0; i<settings.ballsPositions.length;i++) {
                globalVariables.getGameVariables().ballsPositions[i]=new Vector2(-settings.ballsPositions[i].x,-settings.ballsPositions[i].y);
                globalVariables.getGameVariables().ballsVelocities[i]=new Vector2(-settings.ballsVelocities[i].x,-settings.ballsVelocities[i].y);
                globalVariables.getGameVariables().ballsPlayerScreens[i]=0;
                globalVariables.getGameVariables().ballsSizes[i]=settings.ballsSizes[i];
                globalVariables.getGameVariables().ballDisplayStates[i]=settings.ballsDisplayStates[i];
                Log.d(TAG,"x "+Float.toString(globalVariables.getGameVariables().ballsPositions[i].x)+", y "+Float.toString(globalVariables.getGameVariables().ballsPositions[i].y));
                /*tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                Log.e(TAG, "Connection: "+ tempIpAdress);*/
            }

            globalVariables.getSettingsVariables().discoveryClient.stop();

            globalVariables.getSettingsVariables().startGameThreads();
            globalVariables.getSettingsVariables().connectClients();
            globalVariables.getSettingsVariables().setClientListeners(globalVariables.getClientListener());


            globalVariables.getSettingsVariables().connectionState=2;

            IGlobals.SendVariables.SendConnectionState sendConnectionState=new IGlobals.SendVariables.SendConnectionState();
            sendConnectionState.connectionState=2;
            globalVariables.getSettingsVariables().sendToClients(sendConnectionState,"tcp");

        }else if(object instanceof Globals.SendVariables.SendConnectionState) {
            Log.d(TAG, "Connectionstate received.");
            Globals.SendVariables.SendConnectionState connectionState=(Globals.SendVariables.SendConnectionState)object;
            globalVariables.getSettingsVariables().connectionState=connectionState.connectionState;

        } else if(object instanceof Globals.SendVariables.SendConnectionRequest) {
            Globals.SendVariables.SendConnectionRequest connectionRequest=(Globals.SendVariables.SendConnectionRequest)object;

            String tempIpAdress=connection.getRemoteAddressTCP().toString();
            tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
            Log.e(TAG, tempIpAdress+" connectionrequest of "+ connectionRequest.myPlayerName);


            globalVariables.getSettingsVariables().addDiscoveryIpToList(tempIpAdress);
            globalVariables.getSettingsVariables().addDiscoveryPlayerNameToList(connectionRequest.myPlayerName);
        }
    }
}
