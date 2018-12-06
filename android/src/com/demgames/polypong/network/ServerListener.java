package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.MathUtils;
import com.demgames.polypong.Globals;
import com.demgames.polypong.IGlobals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.ArrayList;
import java.util.Arrays;

public class ServerListener extends Listener{

    Globals globals;

    public ServerListener(Context context) {
        globals =(Globals)context;
    }

    private static final String TAG = "ServerListener";

    @Override
    public void connected(Connection connection) {
        String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
        Log.e(TAG, tempIpAdress+" connected.");
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, " disconnected.");
    }

    @Override
    public void received(Connection connection,Object object) {
        synchronized (globals.getSettingsVariables().threadObjectLock){
            if(object instanceof Globals.SendVariables.SendFrequentBall) {
                Globals.SendVariables.SendFrequentBall sendFrequentBall =(Globals.SendVariables.SendFrequentBall)object;

                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFrequentBall.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));
                /*Log.d(TAG, "rotateRad "+Float.toString(rotateRad));

                Log.d(TAG, "ball " + Integer.toString(sendFrequentBall.ballNumber) + " displayState " + Integer.toString(sendFrequentBall.ballDisplayState) +
                        " playerfield " + Integer.toString(sendFrequentBall.myPlayerNumber));*/

                for (int i = 0; i < sendFrequentBall.numberOfSendBalls; i++) {
                    globals.getGameVariables().ballPlayerFields[sendFrequentBall.ballNumbers[i]] = sendFrequentBall.ballPlayerFields[i];
                    globals.getGameVariables().balls[sendFrequentBall.ballNumbers[i]].ballDisplayState = sendFrequentBall.ballDisplayStates[i];

                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents angle " + Float.toString(sendFieldChange.ballAngles[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents position " + Float.toString(sendFieldChange.ballPositionsX[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents velocity " + Float.toString(sendFieldChange.ballVelocitiesX[i]));
                    if (sendFrequentBall.ballDisplayStates[i] == 1) {
                        globals.getGameVariables().balls[sendFrequentBall.ballNumbers[i]].ballPositionX = sendFrequentBall.ballPositionsX[i] * MathUtils.cos(rotateRad) - sendFrequentBall.ballPositionsY[i] * MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequentBall.ballNumbers[i]].ballPositionY = sendFrequentBall.ballPositionsY[i] * MathUtils.cos(rotateRad) + sendFrequentBall.ballPositionsX[i] * MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequentBall.ballNumbers[i]].ballVelocityX = sendFrequentBall.ballVelocitiesX[i] * MathUtils.cos(rotateRad) - sendFrequentBall.ballVelocitiesY[i] * MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequentBall.ballNumbers[i]].ballVelocityY = sendFrequentBall.ballVelocitiesY[i] * MathUtils.cos(rotateRad) + sendFrequentBall.ballVelocitiesX[i] * MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequentBall.ballNumbers[i]].ballAngle = sendFrequentBall.ballAngles[i] + rotateRad;
                        globals.getGameVariables().balls[sendFrequentBall.ballNumbers[i]].ballAngularVelocity = sendFrequentBall.ballAngularVelocities[i];
                    }
                    globals.getGameVariables().ballUpdateStates[sendFrequentBall.ballNumbers[i]] = true;
                }


            } else if(object instanceof Globals.SendVariables.SendFrequentBat) {
                Globals.SendVariables.SendFrequentBat sendFrequentBat = (Globals.SendVariables.SendFrequentBat) object;

                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFrequentBat.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));

                globals.getGameVariables().bats[sendFrequentBat.myPlayerNumber].batPositionX = sendFrequentBat.batPositionX*MathUtils.cos(rotateRad)-sendFrequentBat.batPositionY*MathUtils.sin(rotateRad);
                globals.getGameVariables().bats[sendFrequentBat.myPlayerNumber].batPositionY = sendFrequentBat.batPositionY*MathUtils.cos(rotateRad)+sendFrequentBat.batPositionX*MathUtils.sin(rotateRad);
                globals.getGameVariables().bats[sendFrequentBat.myPlayerNumber].batVelocityX = sendFrequentBat.batVelocityX*MathUtils.cos(rotateRad)-sendFrequentBat.batVelocityX*MathUtils.sin(rotateRad);
                globals.getGameVariables().bats[sendFrequentBat.myPlayerNumber].batVelocityY = sendFrequentBat.batVelocityY*MathUtils.cos(rotateRad)+sendFrequentBat.batVelocityX*MathUtils.sin(rotateRad);
                globals.getGameVariables().bats[sendFrequentBat.myPlayerNumber].batAngle= sendFrequentBat.batAngle + rotateRad;
                globals.getGameVariables().bats[sendFrequentBat.myPlayerNumber].batAngularVelocity= sendFrequentBat.batAngularVelocity;
                globals.getGameVariables().batUpdateStates[sendFrequentBat.myPlayerNumber] = true;


            } else if(object instanceof IGlobals.SendVariables.SendFrequentInfo) {
                IGlobals.SendVariables.SendFrequentInfo sendFrequentInfo = (IGlobals.SendVariables.SendFrequentInfo) object;

                globals.getGameVariables().playerScores[sendFrequentInfo.myPlayerNumber] = sendFrequentInfo.scores[sendFrequentInfo.myPlayerNumber];


            }else if (object instanceof IGlobals.SendVariables.SendFieldChange) {
                IGlobals.SendVariables.SendFieldChange sendFieldChange = (IGlobals.SendVariables.SendFieldChange) object;

                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFieldChange.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));
                //Log.d(TAG, "degrees " + rotateRad/MathUtils.PI*180);

                for (int i = 0; i < sendFieldChange.numberOfSendBalls; i++) {
                    globals.getGameVariables().ballPlayerFields[sendFieldChange.ballNumbers[i]] = sendFieldChange.ballPlayerFields[i];
                    globals.getGameVariables().balls[sendFieldChange.ballNumbers[i]].ballDisplayState = sendFieldChange.ballDisplayStates[i];

                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents angle " + Float.toString(sendFieldChange.ballAngles[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents position " + Float.toString(sendFieldChange.ballPositionsX[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents velocity " + Float.toString(sendFieldChange.ballVelocitiesX[i]));
                    globals.getGameVariables().balls[sendFieldChange.ballNumbers[i]].ballPositionX= sendFieldChange.ballPositionsX[i]*MathUtils.cos(rotateRad)-sendFieldChange.ballPositionsY[i]*MathUtils.sin(rotateRad);
                    globals.getGameVariables().balls[sendFieldChange.ballNumbers[i]].ballPositionY= sendFieldChange.ballPositionsY[i]*MathUtils.cos(rotateRad)+sendFieldChange.ballPositionsX[i]*MathUtils.sin(rotateRad);
                    globals.getGameVariables().balls[sendFieldChange.ballNumbers[i]].ballVelocityX = sendFieldChange.ballVelocitiesX[i]*MathUtils.cos(rotateRad)-sendFieldChange.ballVelocitiesY[i]*MathUtils.sin(rotateRad);
                    globals.getGameVariables().balls[sendFieldChange.ballNumbers[i]].ballVelocityY = sendFieldChange.ballVelocitiesY[i]*MathUtils.cos(rotateRad)+sendFieldChange.ballVelocitiesX[i]*MathUtils.sin(rotateRad);
                    globals.getGameVariables().balls[sendFieldChange.ballNumbers[i]].ballAngle= sendFieldChange.ballAngles[i] + rotateRad;
                    globals.getGameVariables().balls[sendFieldChange.ballNumbers[i]].ballAngularVelocity = sendFieldChange.ballAngularVelocities[i];

                    globals.getGameVariables().ballUpdateStates[sendFieldChange.ballNumbers[i]] = true;
                }

                /*for (int i = 0; i < sendFieldChange.balls.length; i++) {
                    Log.d(TAG, "fieldchange of ball " + sendFieldChange.balls[i].ballNumber + " received");
                    globals.getGameVariables().ballPlayerFields[sendFieldChange.balls[i].ballNumber] = sendFieldChange.balls[i].ballPlayerField;
                    globals.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballDisplayState = sendFieldChange.balls[i].ballDisplayState;

                    globals.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballPositionX= sendFieldChange.balls[i].ballPositionX*MathUtils.cos(rotateRad)-sendFieldChange.balls[i].ballPositionY*MathUtils.sin(rotateRad);
                    globals.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballPositionY= sendFieldChange.balls[i].ballPositionY*MathUtils.cos(rotateRad)+sendFieldChange.balls[i].ballPositionX*MathUtils.sin(rotateRad);
                    globals.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballVelocityX = sendFieldChange.balls[i].ballVelocityX*MathUtils.cos(rotateRad)-sendFieldChange.balls[i].ballVelocityY*MathUtils.sin(rotateRad);
                    globals.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballVelocityY = sendFieldChange.balls[i].ballVelocityY*MathUtils.cos(rotateRad)+sendFieldChange.balls[i].ballVelocityX*MathUtils.sin(rotateRad);

                    globals.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballAngle= sendFieldChange.balls[i].ballAngle + rotateRad;
                    globals.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballAngularVelocity = sendFieldChange.balls[i].ballAngularVelocity;

                    globals.getGameVariables().ballUpdateStates[sendFieldChange.balls[i].ballNumber] = true;
            /*Log.d(TAG, "ballposition x " + sendFieldChange.ballPositions[i].rotateRad(rotateRad).x +
                    " y " + sendFieldChange.ballPositions[i].rotateRad(rotateRad).y);
                }

                Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");*/

            } else if(object instanceof Globals.SendVariables.SendSettings) {
                if(globals.getSettingsVariables().setupConnectionState == 1) {
                    Log.d(TAG, "received settings");
                    Globals.SendVariables.SendSettings settings = (Globals.SendVariables.SendSettings) object;

                    globals.getSettingsVariables().myPlayerNumber = settings.yourPlayerNumber;
                    globals.getSettingsVariables().numberOfPlayers = settings.numberOfPlayers;
                    globals.getGameVariables().myPlayerNumber = settings.yourPlayerNumber;
                    globals.getGameVariables().numberOfPlayers = settings.numberOfPlayers;

                    globals.getSettingsVariables().ipAdresses = new ArrayList<String>(Arrays.asList(settings.ipAdresses));
                    globals.getSettingsVariables().playerNames = new ArrayList<String>(Arrays.asList(settings.playerNames));

                    Log.d(TAG, "settings yourPlayerNumber: " + settings.yourPlayerNumber);
                    Log.d(TAG, "myPlayerNumber: " + globals.getSettingsVariables().myPlayerNumber);

                    for (int i = 0; i < globals.getSettingsVariables().ipAdresses.size(); i++) {
                        Log.d(TAG, "received ip adresses " + globals.getSettingsVariables().ipAdresses.get(i));
                    }

                    //globals.getSettingsVariables().playerNames=settings.playerNames;

                    globals.getGameVariables().numberOfBalls = settings.balls.length;
                    globals.getSettingsVariables().gameMode = settings.gameMode;
                    globals.getGameVariables().gravityState = settings.gravityState;
                    globals.getGameVariables().attractionState = settings.attractionState;

                    globals.getGameVariables().setBalls(false);
                    globals.getGameVariables().setBats();

                    float rotateRad = (-2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * globals.getSettingsVariables().myPlayerNumber);

                    for (int i = 0; i < settings.balls.length; i++) {
                        globals.getGameVariables().balls[i].ballDisplayState= settings.balls[i].ballDisplayState;
                        globals.getGameVariables().balls[i].ballRadius = settings.balls[i].ballRadius;
                        globals.getGameVariables().balls[settings.balls[i].ballNumber].ballPositionX= settings.balls[i].ballPositionX*MathUtils.cos(rotateRad)-settings.balls[i].ballPositionY*MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[settings.balls[i].ballNumber].ballPositionY= settings.balls[i].ballPositionY*MathUtils.cos(rotateRad)+settings.balls[i].ballPositionX*MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[settings.balls[i].ballNumber].ballVelocityX = settings.balls[i].ballVelocityX*MathUtils.cos(rotateRad)-settings.balls[i].ballVelocityY*MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[settings.balls[i].ballNumber].ballVelocityY = settings.balls[i].ballVelocityY*MathUtils.cos(rotateRad)+settings.balls[i].ballVelocityX*MathUtils.sin(rotateRad);

                        globals.getGameVariables().balls[i].ballAngle = settings.balls[i].ballAngle+rotateRad;
                        globals.getGameVariables().balls[i].ballAngularVelocity= settings.balls[i].ballAngularVelocity;

                        //globals.getGameVariables().ballPlayerFields[i]=0;
                        //Log.d(TAG,"x "+Float.toString(globals.getGameVariables().ballPositions[i].x)+", y "+Float.toString(globals.getGameVariables().ballPositions[i].y));
    /*tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
    Log.e(TAG, "Connection: "+ tempIpAdress);*/
                    }

                    globals.getSettingsVariables().discoveryClientThread.shutdownClient();

                    Log.d(TAG, "Connection of discoveryClient ended");

                    globals.getSettingsVariables().startAllClientThreads();
                    globals.getSettingsVariables().setAllClientListeners(globals.getClientListener());
                    globals.getSettingsVariables().connectAllClients();

                    globals.getSettingsVariables().setupConnectionState = 2;

                    IGlobals.SendVariables.SendConnectionState sendConnectionState = new IGlobals.SendVariables.SendConnectionState();
                    sendConnectionState.myPlayerNumber = globals.getSettingsVariables().myPlayerNumber;
                    sendConnectionState.connectionState = 2;
                    globals.getSettingsVariables().sendToAllClients(sendConnectionState, "tcp");
                }

            } else if(object instanceof Globals.SendVariables.SendConnectionState) {
                Globals.SendVariables.SendConnectionState connectionState=(Globals.SendVariables.SendConnectionState) object;


                Log.d(TAG, "Connectionstate " + connectionState.connectionState+ " received from player " + connectionState.myPlayerNumber);
                globals.getSettingsVariables().clientConnectionStates[connectionState.myPlayerNumber] = connectionState.connectionState;


            } else if(object instanceof Globals.SendVariables.SendConnectionRequest) {
                Globals.SendVariables.SendConnectionRequest connectionRequest=(Globals.SendVariables.SendConnectionRequest) object;

                String tempIpAdress=connection.getRemoteAddressTCP().toString();
                tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                Log.e(TAG, tempIpAdress+" connectionrequest of "+ connectionRequest.myPlayerName);

                globals.getSettingsVariables().addDiscoveryIpToList(tempIpAdress);
                globals.getSettingsVariables().addDiscoveryPlayerNameToList(connectionRequest.myPlayerName);
            }
        }

    }
}
