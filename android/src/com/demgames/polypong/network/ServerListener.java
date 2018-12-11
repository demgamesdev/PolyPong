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
import java.util.Map;

public class ServerListener extends Listener{

    Globals globals;

    public ServerListener(Context context) {
        globals =(Globals)context;
    }

    private static final String TAG = "ServerListener";

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
    }

    @Override
    public void received(Connection connection,Object object) {
        if(object instanceof Globals.SendVariables.SendFrequentBalls) {
            synchronized (globals.getSettingsVariables().receiveThreadLock) {
                Globals.SendVariables.SendFrequentBalls sendFrequentBalls = (Globals.SendVariables.SendFrequentBalls) object;

                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFrequentBalls.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));
                //Log.d(TAG, "rotateRad " + Float.toString(rotateRad));

            /*Log.d(TAG, "ball " + Integer.toString(sendFrequentBalls.ballNumber) + " displayState " + Integer.toString(sendFrequentBalls.ballDisplayState) +
                    " playerfield " + Integer.toString(sendFrequentBalls.myPlayerNumber));*/

                for (Map.Entry<Integer, IGlobals.Ball> ball : sendFrequentBalls.frequentBallsMap.entrySet()) {
                    globals.getGameVariables().ballPlayerFields[ball.getKey()] = ball.getValue().ballPlayerField;
                    globals.getGameVariables().balls[ball.getKey()].ballDisplayState = ball.getValue().ballDisplayState;

                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents angle " + Float.toString(sendFieldChange.ballAngles[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents position " + Float.toString(sendFieldChange.ballPositionsX[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents velocity " + Float.toString(sendFieldChange.ballVelocitiesX[i]));
                    if (ball.getValue().ballDisplayState == 1) {
                        globals.getGameVariables().balls[ball.getKey()].ballPosition = ball.getValue().ballPosition.rotateRad(rotateRad);
                        globals.getGameVariables().balls[ball.getKey()].ballVelocity = ball.getValue().ballVelocity.rotateRad(rotateRad);
                        globals.getGameVariables().balls[ball.getKey()].ballAngle = ball.getValue().ballAngle + rotateRad;
                        globals.getGameVariables().balls[ball.getKey()].ballAngularVelocity = ball.getValue().ballAngularVelocity;
                    }
                    globals.getGameVariables().ballUpdateStates[ball.getKey()] = true;
                }
            }

        } else if(object instanceof IGlobals.SendVariables.SendFrequentInfo) {
            synchronized (globals.getSettingsVariables().receiveThreadLock) {
                IGlobals.SendVariables.SendFrequentInfo sendFrequentInfo = (IGlobals.SendVariables.SendFrequentInfo) object;

                globals.getGameVariables().playerScores[sendFrequentInfo.myPlayerNumber] = sendFrequentInfo.scores[sendFrequentInfo.myPlayerNumber];

                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFrequentInfo.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));

                globals.getGameVariables().bats[sendFrequentInfo.myPlayerNumber].batPosition = sendFrequentInfo.bat.batPosition.rotateRad(rotateRad);
                globals.getGameVariables().bats[sendFrequentInfo.myPlayerNumber].batVelocity = sendFrequentInfo.bat.batVelocity.rotateRad(rotateRad);
                globals.getGameVariables().bats[sendFrequentInfo.myPlayerNumber].batAngle = sendFrequentInfo.bat.batAngle + rotateRad;
                globals.getGameVariables().bats[sendFrequentInfo.myPlayerNumber].batAngularVelocity = sendFrequentInfo.bat.batAngularVelocity;
                globals.getGameVariables().batUpdateStates[sendFrequentInfo.myPlayerNumber] = true;

                for (Map.Entry<Integer, Integer> ball : sendFrequentInfo.ballDisplayStatesMap.entrySet()) {
                    globals.getGameVariables().balls[ball.getKey()].ballDisplayState = ball.getValue();
                    //Log.d(TAG, "ball " + Integer.toString(ball.getKey()) + " displayState " + Integer.toString(ball.getValue()));
                    globals.getGameVariables().ballUpdateStates[ball.getKey()] = true;
                }
            }

        }else if (object instanceof IGlobals.SendVariables.SendFieldChangeBalls) {
            synchronized (globals.getSettingsVariables().receiveThreadLock) {
                Globals.SendVariables.SendFieldChangeBalls sendFieldChangeBalls = (Globals.SendVariables.SendFieldChangeBalls) object;

                float rotateRad = (2f * MathUtils.PI / globals.getSettingsVariables().numberOfPlayers * (sendFieldChangeBalls.myPlayerNumber - globals.getSettingsVariables().myPlayerNumber));
                Log.d(TAG, "rotateRad " + Float.toString(rotateRad));



                for (Map.Entry<Integer, IGlobals.Ball> ball : sendFieldChangeBalls.fieldChangeBallsMap.entrySet()) {
                    globals.getGameVariables().ballPlayerFields[ball.getKey()] = ball.getValue().ballPlayerField;
                    globals.getGameVariables().balls[ball.getKey()].ballDisplayState = ball.getValue().ballDisplayState;

                    Log.d(TAG, "ball " + Integer.toString(ball.getKey()) + " playerfield " + Integer.toString(ball.getValue().ballPlayerField) + " sendfieldchangeball received");

                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents angle " + Float.toString(sendFieldChange.ballAngles[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents position " + Float.toString(sendFieldChange.ballPositionsX[i]));
                    //Log.d(TAG, "ball " + Integer.toString(sendFieldChange.ballNumbers[i]) + " sendFrequents velocity " + Float.toString(sendFieldChange.ballVelocitiesX[i]));
                    globals.getGameVariables().balls[ball.getKey()].ballPosition = ball.getValue().ballPosition.rotateRad(rotateRad);
                    globals.getGameVariables().balls[ball.getKey()].ballVelocity = ball.getValue().ballVelocity.rotateRad(rotateRad);
                    globals.getGameVariables().balls[ball.getKey()].ballAngle = ball.getValue().ballAngle + rotateRad;
                    globals.getGameVariables().balls[ball.getKey()].ballAngularVelocity = ball.getValue().ballAngularVelocity;
                    globals.getGameVariables().ballUpdateStates[ball.getKey()] = true;
                }
            }

        } else if(object instanceof Globals.SendVariables.SendSettings) {
            synchronized (globals.getSettingsVariables().receiveThreadLock) {
                if (globals.getSettingsVariables().setupConnectionState == 1) {
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
                        globals.getGameVariables().balls[i].ballDisplayState = settings.balls[i].ballDisplayState;
                        globals.getGameVariables().balls[i].ballRadius = settings.balls[i].ballRadius;
                        globals.getGameVariables().balls[settings.balls[i].ballNumber].ballPosition = settings.balls[i].ballPosition.rotateRad(rotateRad);
                        globals.getGameVariables().balls[settings.balls[i].ballNumber].ballVelocity = settings.balls[i].ballVelocity.rotateRad(rotateRad);
                        globals.getGameVariables().balls[i].ballAngle = settings.balls[i].ballAngle + rotateRad;
                        globals.getGameVariables().balls[i].ballAngularVelocity = settings.balls[i].ballAngularVelocity;

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
                    globals.getSettingsVariables().sendObjectToAllClients(sendConnectionState, "tcp");
                }
            }

        } else if(object instanceof Globals.SendVariables.SendConnectionState) {
            synchronized (globals.getSettingsVariables().receiveThreadLock) {
                Globals.SendVariables.SendConnectionState connectionState = (Globals.SendVariables.SendConnectionState) object;


                Log.d(TAG, "Connectionstate " + connectionState.connectionState + " received from player " + connectionState.myPlayerNumber);
                globals.getSettingsVariables().clientConnectionStates[connectionState.myPlayerNumber] = connectionState.connectionState;
            }


        } else if(object instanceof Globals.SendVariables.SendConnectionRequest) {
            synchronized (globals.getSettingsVariables().receiveThreadLock) {
                Globals.SendVariables.SendConnectionRequest connectionRequest = (Globals.SendVariables.SendConnectionRequest) object;

                String tempIpAdress = connection.getRemoteAddressTCP().toString();
                tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length()).split(":")[0];
                Log.e(TAG, tempIpAdress + " connectionrequest of " + connectionRequest.myPlayerName);

                globals.getSettingsVariables().addDiscoveryIpToList(tempIpAdress);
                globals.getSettingsVariables().addDiscoveryPlayerNameToList(connectionRequest.myPlayerName);

                IGlobals.Player tempPlayer = new IGlobals.Player();
                tempPlayer.ipAdress = tempIpAdress;
                tempPlayer.name = connectionRequest.myPlayerName;

                if(globals.getSettingsVariables().addPlayerToList(tempPlayer)) {
                    globals.getSettingsVariables().updateListViewState = true;
                } else {
                    Log.e(TAG, tempIpAdress + " already in playerlist");
                }

                //globals.getSettingsVariables().updateListViewState = true;
            }

        } else if(object instanceof Globals.SendVariables.SendDiscoveryRequest) {
            if(globals.getSettingsVariables().myPlayerNumber==0) {
                synchronized (globals.getSettingsVariables().receiveThreadLock) {
                    Globals.SendVariables.SendDiscoveryRequest discoveryRequest = (Globals.SendVariables.SendDiscoveryRequest) object;

                    String tempIpAdress = connection.getRemoteAddressTCP().toString();
                    tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length()).split(":")[0];
                    Log.e(TAG, tempIpAdress + " discoveryrequest of " + discoveryRequest.myPlayerName);

                    IGlobals.SendVariables.SendDiscoveryResponse sendDiscoveryResponse = new IGlobals.SendVariables.SendDiscoveryResponse();
                    sendDiscoveryResponse.myPlayerName = globals.getSettingsVariables().myPlayerName;
                    connection.sendUDP(sendDiscoveryResponse);
                }
            }
        }
    }

}
