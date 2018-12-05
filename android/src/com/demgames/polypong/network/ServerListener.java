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
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, " disconnected.");
    }

    @Override
    public void received(Connection connection,Object object) {
        if(true) {
            if(object instanceof Globals.SendVariables.SendClass) {
                Globals.SendVariables.SendClass sendClass = (Globals.SendVariables.SendClass) object;

                for(int obj=0;obj<sendClass.sendObjects.length;obj++) {

                    if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendFrequents) {
                        Globals.SendVariables.SendFrequents sendFrequents=(Globals.SendVariables.SendFrequents)sendClass.sendObjects[obj];

                        float rotateRad = (2f * MathUtils.PI / globalVariables.getSettingsVariables().numberOfPlayers * (sendFrequents.myPlayerNumber - globalVariables.getSettingsVariables().myPlayerNumber));
                        Log.d(TAG, "rotateRad "+Float.toString(rotateRad));
                        //Log.d(TAG, "ball kinetics 0 x "+ ballKinetics.ballPositions[0].x +" y "+ballKinetics.ballPositions[0].y);
                        for (int i = 0; i < sendFrequents.balls.length; i++) {
                            try {
                                if(sendFrequents.balls[i]!=null) {
                                    globalVariables.getGameVariables().ballPlayerFields[sendFrequents.balls[i].ballNumber] = sendFrequents.balls[i].ballPlayerField;
                                    globalVariables.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballDisplayState = sendFrequents.balls[i].ballDisplayState;
                                    Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " displayState " + Integer.toString(sendFrequents.balls[i].ballDisplayState) +
                                            " playerfield " + Integer.toString(sendFrequents.myPlayerNumber));
                                    if (sendFrequents.balls[i].ballDisplayState == 1) {
                                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " sendFrequents angle " + Float.toString(sendFrequents.balls[i].ballAngle));
                                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " sendFrequents position " + Float.toString(sendFrequents.balls[i].ballPosition.x));
                                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " sendFrequents velocity " + Float.toString(sendFrequents.balls[i].ballVelocity.x));
                                        globalVariables.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballPosition= sendFrequents.balls[i].ballPosition.rotateRad(rotateRad);
                                        globalVariables.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballVelocity = sendFrequents.balls[i].ballVelocity.rotateRad(rotateRad);
                                        globalVariables.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballAngle= sendFrequents.balls[i].ballAngle + rotateRad;
                                        globalVariables.getGameVariables().balls[sendFrequents.balls[i].ballNumber].ballAngularVelocity = sendFrequents.balls[i].ballAngularVelocity;

                                        Log.d(TAG, "ball " + Integer.toString(sendFrequents.balls[i].ballNumber) + " globals angle " + Float.toString(globalVariables.getGameVariables().balls[i].ballAngle));
                                    }
                                    globalVariables.getGameVariables().ballUpdateStates[sendFrequents.balls[i].ballNumber] = true;
                                } else {
                                    Log.d(TAG, "ball null");

                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }


                        }

                        try {
                            globalVariables.getGameVariables().bats[sendFrequents.myPlayerNumber].batPosition = sendFrequents.bat.batPosition.rotateRad(rotateRad);
                            globalVariables.getGameVariables().bats[sendFrequents.myPlayerNumber].batVelocity = sendFrequents.bat.batVelocity.rotateRad(rotateRad);
                            globalVariables.getGameVariables().bats[sendFrequents.myPlayerNumber].batAngle= sendFrequents.bat.batAngle + rotateRad;
                            globalVariables.getGameVariables().bats[sendFrequents.myPlayerNumber].batAngularVelocity= sendFrequents.bat.batAngularVelocity;
                            globalVariables.getGameVariables().batUpdateStates[sendFrequents.myPlayerNumber] = true;

                            globalVariables.getGameVariables().playerScores[sendFrequents.myPlayerNumber] = sendFrequents.scores[sendFrequents.myPlayerNumber];
                        }catch (NullPointerException e) {
                                e.printStackTrace();
                        }




                    }else if (sendClass.sendObjects[obj] instanceof IGlobals.SendVariables.SendFieldChange) {
                        IGlobals.SendVariables.SendFieldChange sendFieldChange = (IGlobals.SendVariables.SendFieldChange) sendClass.sendObjects[obj];

                        float rotateRad = (2f * MathUtils.PI / globalVariables.getSettingsVariables().numberOfPlayers * (sendFieldChange.myPlayerNumber - globalVariables.getSettingsVariables().myPlayerNumber));
                        //Log.d(TAG, "degrees " + rotateRad/MathUtils.PI*180);
                        for (int i = 0; i < sendFieldChange.balls.length; i++) {
                            Log.d(TAG, "fieldchange of ball " + sendFieldChange.balls[i].ballNumber + " received");
                            globalVariables.getGameVariables().ballPlayerFields[sendFieldChange.balls[i].ballNumber] = sendFieldChange.balls[i].ballPlayerField;
                            globalVariables.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballDisplayState = sendFieldChange.balls[i].ballDisplayState;

                            globalVariables.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballPosition= sendFieldChange.balls[i].ballPosition.rotateRad(rotateRad);
                            globalVariables.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballVelocity = sendFieldChange.balls[i].ballVelocity.rotateRad(rotateRad);
                            globalVariables.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballAngle= sendFieldChange.balls[i].ballAngle + rotateRad;
                            globalVariables.getGameVariables().balls[sendFieldChange.balls[i].ballNumber].ballAngularVelocity = sendFieldChange.balls[i].ballAngularVelocity;

                            globalVariables.getGameVariables().ballUpdateStates[sendFieldChange.balls[i].ballNumber] = true;
                        /*Log.d(TAG, "ballposition x " + sendFieldChange.ballPositions[i].rotateRad(rotateRad).x +
                                " y " + sendFieldChange.ballPositions[i].rotateRad(rotateRad).y);*/
                        }

                        //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");

                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendSettings) {
                        if(globalVariables.getSettingsVariables().setupConnectionState == 1) {
                            Log.d(TAG, "received settings");
                            Globals.SendVariables.SendSettings settings = (Globals.SendVariables.SendSettings) sendClass.sendObjects[obj];

                            globalVariables.getSettingsVariables().myPlayerNumber = settings.yourPlayerNumber;
                            globalVariables.getSettingsVariables().numberOfPlayers = settings.numberOfPlayers;
                            globalVariables.getGameVariables().myPlayerNumber = settings.yourPlayerNumber;
                            globalVariables.getGameVariables().numberOfPlayers = settings.numberOfPlayers;

                            globalVariables.getSettingsVariables().ipAdresses = new ArrayList<String>(Arrays.asList(settings.ipAdresses));
                            globalVariables.getSettingsVariables().playerNames = new ArrayList<String>(Arrays.asList(settings.playerNames));

                            Log.d(TAG, "settings yourPlayerNumber: " + settings.yourPlayerNumber);
                            Log.d(TAG, "myPlayerNumber: " + globalVariables.getSettingsVariables().myPlayerNumber);

                            for (int i = 0; i < globalVariables.getSettingsVariables().ipAdresses.size(); i++) {
                                Log.d(TAG, "received ip adresses " + globalVariables.getSettingsVariables().ipAdresses.get(i));
                            }

                            //globalVariables.getSettingsVariables().playerNames=settings.playerNames;

                            globalVariables.getGameVariables().numberOfBalls = settings.balls.length;
                            globalVariables.getSettingsVariables().gameMode = settings.gameMode;
                            globalVariables.getGameVariables().gravityState = settings.gravityState;
                            globalVariables.getGameVariables().attractionState = settings.attractionState;

                            globalVariables.getGameVariables().setBalls(false);
                            globalVariables.getGameVariables().setBats();

                            float rotateRad = (-2f * MathUtils.PI / globalVariables.getSettingsVariables().numberOfPlayers * globalVariables.getSettingsVariables().myPlayerNumber);

                            for (int i = 0; i < settings.balls.length; i++) {
                                globalVariables.getGameVariables().balls[i].ballDisplayState= settings.balls[i].ballDisplayState;
                                globalVariables.getGameVariables().balls[i].ballRadius = settings.balls[i].ballRadius;
                                globalVariables.getGameVariables().balls[i].ballPosition = settings.balls[i].ballPosition.rotateRad(rotateRad);
                                globalVariables.getGameVariables().balls[i].ballVelocity = settings.balls[i].ballVelocity.rotateRad(rotateRad);
                                globalVariables.getGameVariables().balls[i].ballAngle = settings.balls[i].ballAngle+rotateRad;
                                globalVariables.getGameVariables().balls[i].ballAngularVelocity= settings.balls[i].ballAngularVelocity;

                                //globalVariables.getGameVariables().ballPlayerFields[i]=0;
                                //Log.d(TAG,"x "+Float.toString(globalVariables.getGameVariables().ballPositions[i].x)+", y "+Float.toString(globalVariables.getGameVariables().ballPositions[i].y));
            /*tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
            Log.e(TAG, "Connection: "+ tempIpAdress);*/
                            }

                            globalVariables.getSettingsVariables().discoveryClientThread.shutdownClient();

                            Log.d(TAG, "Connection of discoveryClient ended");

                            globalVariables.getSettingsVariables().startAllClientThreads();
                            globalVariables.getSettingsVariables().setAllClientListeners(globalVariables.getClientListener());
                            globalVariables.getSettingsVariables().connectAllClients();

                            globalVariables.getSettingsVariables().setupConnectionState = 2;

                            IGlobals.SendVariables.SendConnectionState sendConnectionState = new IGlobals.SendVariables.SendConnectionState();
                            sendConnectionState.myPlayerNumber = globalVariables.getSettingsVariables().myPlayerNumber;
                            sendConnectionState.connectionState = 2;
                            globalVariables.getSettingsVariables().sendToAllClients(sendConnectionState, "tcp");
                        }

                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendConnectionState) {
                        Globals.SendVariables.SendConnectionState connectionState=(Globals.SendVariables.SendConnectionState)sendClass.sendObjects[obj];


                        Log.d(TAG, "Connectionstate received from player " + connectionState.myPlayerNumber);
                        globalVariables.getSettingsVariables().clientConnectionStates[connectionState.myPlayerNumber] = connectionState.connectionState;


                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendConnectionRequest) {
                        Globals.SendVariables.SendConnectionRequest connectionRequest=(Globals.SendVariables.SendConnectionRequest)sendClass.sendObjects[obj];

                        String tempIpAdress=connection.getRemoteAddressTCP().toString();
                        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                        Log.e(TAG, tempIpAdress+" connectionrequest of "+ connectionRequest.myPlayerName);

                        globalVariables.getSettingsVariables().addDiscoveryIpToList(tempIpAdress);
                        globalVariables.getSettingsVariables().addDiscoveryPlayerNameToList(connectionRequest.myPlayerName);
                    }

                }


            } else {
                //Log.d(TAG, "Some Package received.");
            }
        }
    }
}
