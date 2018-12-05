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
            if(object instanceof Globals.SendVariables.SendFrequents) {
                Globals.SendVariables.SendFrequents sendFrequents=(Globals.SendVariables.SendFrequents)object;
                IGlobals.Ball[] tempBalls = new IGlobals.Ball[sendFrequents.balls.length];


                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFrequents.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));
                Log.d(TAG, "rotateRad "+Float.toString(rotateRad));
                //Log.d(TAG, "ball kinetics 0 x "+ ballKinetics.ballPositions[0].x +" y "+ballKinetics.ballPositions[0].y);
                for (int i = 0; i < sendFrequents.balls.length; i++) {
                    tempBalls[i] = sendFrequents.balls[i];
                    Log.d(TAG, "ball " + Integer.toString(tempBalls[i].ballNumber) + " displayState " + Integer.toString(tempBalls[i].ballDisplayState) +
                        " playerfield " + Integer.toString(sendFrequents.myPlayerNumber));
                    globals.getGameVariables().ballPlayerFields[sendFrequents.balls[i].ballNumber] = sendFrequents.balls[i].ballPlayerField;
                    globals.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballDisplayState = sendFrequents.balls[i].ballDisplayState;
                    if (sendFrequents.balls[i].ballDisplayState == 1) {
                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " sendFrequents angle " + Float.toString(sendFrequents.balls[i].ballAngle));
                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " sendFrequents position " + Float.toString(sendFrequents.balls[i].ballPositionX));
                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " sendFrequents velocity " + Float.toString(sendFrequents.balls[i].ballVelocityX));
                        globals.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballPositionX= sendFrequents.balls[i].ballPositionX*MathUtils.cos(rotateRad)-sendFrequents.balls[i].ballPositionY*MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballPositionY= sendFrequents.balls[i].ballPositionY*MathUtils.cos(rotateRad)+sendFrequents.balls[i].ballPositionX*MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballVelocityX = sendFrequents.balls[i].ballVelocityX*MathUtils.cos(rotateRad)-sendFrequents.balls[i].ballVelocityY*MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballVelocityY = sendFrequents.balls[i].ballVelocityY*MathUtils.cos(rotateRad)+sendFrequents.balls[i].ballVelocityX*MathUtils.sin(rotateRad);
                        globals.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballAngle= sendFrequents.balls[i].ballAngle + rotateRad;
                        globals.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballAngularVelocity = sendFrequents.balls[i].ballAngularVelocity;

                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " globals angle " + Float.toString(globals.getGameVariables().balls[i].ballAngle));
                    }
                    globals.getGameVariables().ballUpdateStates[sendFrequents.balls[i].ballNumber] = true;


                }

                    globals.getGameVariables().bats[sendFrequents.myPlayerNumber].batPositionX = sendFrequents.bat.batPositionX*MathUtils.cos(rotateRad)-sendFrequents.bat.batPositionY*MathUtils.sin(rotateRad);
                    globals.getGameVariables().bats[sendFrequents.myPlayerNumber].batPositionY = sendFrequents.bat.batPositionY*MathUtils.cos(rotateRad)+sendFrequents.bat.batPositionX*MathUtils.sin(rotateRad);
                    globals.getGameVariables().bats[sendFrequents.myPlayerNumber].batVelocityX = sendFrequents.bat.batVelocityX*MathUtils.cos(rotateRad)-sendFrequents.bat.batVelocityX*MathUtils.sin(rotateRad);
                    globals.getGameVariables().bats[sendFrequents.myPlayerNumber].batVelocityY = sendFrequents.bat.batVelocityY*MathUtils.cos(rotateRad)+sendFrequents.bat.batVelocityX*MathUtils.sin(rotateRad);
                    globals.getGameVariables().bats[sendFrequents.myPlayerNumber].batAngle= sendFrequents.bat.batAngle + rotateRad;
                    globals.getGameVariables().bats[sendFrequents.myPlayerNumber].batAngularVelocity= sendFrequents.bat.batAngularVelocity;
                    globals.getGameVariables().batUpdateStates[sendFrequents.myPlayerNumber] = true;

                    globals.getGameVariables().playerScores[sendFrequents.myPlayerNumber] = sendFrequents.scores[sendFrequents.myPlayerNumber];




            }else if (object instanceof IGlobals.SendVariables.SendFieldChange) {
                IGlobals.SendVariables.SendFieldChange sendFieldChange = (IGlobals.SendVariables.SendFieldChange) object;

                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFieldChange.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));
                //Log.d(TAG, "degrees " + rotateRad/MathUtils.PI*180);
                for (int i = 0; i < sendFieldChange.balls.length; i++) {
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
                    " y " + sendFieldChange.ballPositions[i].rotateRad(rotateRad).y);*/
                }

                //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");

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


                Log.d(TAG, "Connectionstate received from player " + connectionState.myPlayerNumber);
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
