package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.MathUtils;
import com.demgames.miscclasses.GameObjectClasses.*;
import com.demgames.miscclasses.SendClasses.*;
import com.demgames.polypong.Globals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerListener extends Listener{

    private Globals globals;
    private String networkMode;
    private String myPlayerName;

    public ServerListener(Context context,String myPlayerName_,String networkMode_) {
        this.globals =(Globals)context;
        this.myPlayerName = myPlayerName_;
        this.networkMode = networkMode_;

    }

    private static final String TAG = "ServerListener";

    @Override
    public void connected(Connection connection) {
        try{
            synchronized (globals.getComm().connectionThreadLock) {
                String tempIpAdress=connection.getRemoteAddressTCP().toString();
                tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                Log.e(TAG, tempIpAdress+" connected.");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, " disconnected.");
    }

    @Override
    public void received(Connection connection,Object object) {
        try{
            if(object instanceof SendGameInfo) {
                synchronized (globals.getComm().receiveThreadLock) {
                    SendGameInfo sendGameInfo = (SendGameInfo) object;

                    if(sendGameInfo.gameInfoMap.get("myplayernumber")!=null) {
                        int tempMyPlayerNumber = (int)sendGameInfo.gameInfoMap.get("myplayernumber");
                        if (sendGameInfo.gameInfoMap.get("scores") != null) {
                            globals.getComm().playerScores[tempMyPlayerNumber] = ((int[]) (sendGameInfo.gameInfoMap.get("scores")))[tempMyPlayerNumber];
                        }


                        float rotateRad = (2f * MathUtils.PI / globals.getComm().numberOfPlayers * (tempMyPlayerNumber - globals.getComm().myPlayerNumber));

                        if(sendGameInfo.gameInfoMap.get("bat")!=null) {
                            globals.getComm().bats[tempMyPlayerNumber].setReceived((Bat)sendGameInfo.gameInfoMap.get("bat"),rotateRad);
                            globals.getComm().bats[tempMyPlayerNumber].batUpdateState = true;
                        }
                        if(sendGameInfo.gameInfoMap.get("balls")!=null) {
                            for (Map.Entry<Integer, Ball> ball : ((ConcurrentHashMap<Integer,Ball>)sendGameInfo.gameInfoMap.get("balls")).entrySet()) {
                                globals.getComm().balls[ball.getKey()].ballDisplayState = ball.getValue().ballDisplayState;

                                Log.d(TAG, "ball " + ball.getKey() + " displaystate " +  ball.getValue().ballDisplayState);
                                Log.d(TAG, "ball playerfield " + Integer.toString(ball.getValue().ballPlayerField) + " tempplayerfield " + Integer.toString(ball.getValue().balltempPlayerField));
                                if (ball.getValue().ballDisplayState == 1) {
                                   // Log.d(TAG, "ball " + Integer.toString(ball.ballNumber) + " sendFrequents position " + Float.toString(ball.ballPosition.x));

                                    globals.getComm().balls[ball.getKey()].setReceived(ball.getValue(),rotateRad);
                                }
                                globals.getComm().balls[ball.getKey()].ballUpdateState = true;
                            }
                        }
                    }

                }

            } else if(object instanceof SendSettings) {
                synchronized (globals.getComm().receiveThreadLock) {
                    if (globals.getComm().setupConnectionState == 1) {
                        Log.d(TAG, "received settings");
                        SendSettings settings = (SendSettings) object;


                        globals.getComm().setPlayerMap(settings.playerMap);

                        for (int i : globals.getComm().playerMap.keySet()) {
                            Log.d(TAG, "received " + globals.getComm().playerMap.get(i).name +  " with ip adress " + globals.getComm().playerMap.get(i).ipAdress);
                        }

                        globals.getComm().initGame(settings.yourPlayerNumber,settings.balls.length,settings.numberOfPlayers,settings.gameMode,
                                settings.gravityState,settings.attractionState,false);

                        float rotateRad = (-2f * MathUtils.PI / globals.getComm().numberOfPlayers * globals.getComm().myPlayerNumber);
                        for (int i = 0; i < settings.balls.length; i++) {

                            globals.getComm().balls[i].setReceived(settings.balls[i],rotateRad);
                            Log.d(TAG, "settings ball " + i + " displaystate " +  settings.balls[i].ballDisplayState);

                            //globals.getGameVariables().ballPlayerFields[i]=0;
                            //Log.d(TAG,"x "+Float.toString(globals.getGameVariables().ballPositions[i].x)+", y "+Float.toString(globals.getGameVariables().ballPositions[i].y));
    /*tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
    Log.e(TAG, "Connection: "+ tempIpAdress);*/
                        }

                        Log.d(TAG, "settings yourPlayerNumber: " + settings.yourPlayerNumber);
                        Log.d(TAG, "myPlayerNumber: " + globals.getComm().myPlayerNumber);
                        globals.getComm().setSetupConnectionState(2);

                    }
                }

            } else if(object instanceof SendConnectionState) {
                synchronized (globals.getComm().receiveThreadLock) {
                    SendConnectionState connectionState = (SendConnectionState) object;

                    Log.d(TAG, "Connectionstate " + connectionState.connectionState + " received from player " + connectionState.myPlayerNumber);
                    globals.getComm().clientConnectionStatesMap.put((connectionState.myPlayerNumber),connectionState.connectionState);
                }


            } else if(object instanceof SendConnectionRequest) {
                synchronized (globals.getComm().receiveThreadLock) {
                    SendConnectionRequest connectionRequest = (SendConnectionRequest) object;

                    String tempIpAdress = connection.getRemoteAddressTCP().toString();
                    tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length()).split(":")[0];
                    Log.e(TAG, tempIpAdress + " connectionrequest of " + connectionRequest.myPlayerName);

                    globals.getComm().addDiscoveryPlayer(connectionRequest.myPlayerName,tempIpAdress);
                }

            } else if(object instanceof SendDiscoveryRequest) {
                if(this.networkMode.equals("host")) {
                    synchronized (globals.getComm().receiveThreadLock) {
                        SendDiscoveryRequest discoveryRequest = (SendDiscoveryRequest) object;

                        String tempIpAdress = connection.getRemoteAddressTCP().toString();
                        tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length()).split(":")[0];
                        Log.e(TAG, tempIpAdress + " discoveryrequest of " + discoveryRequest.myPlayerName);

                        connection.sendUDP(new SendDiscoveryResponse(this.myPlayerName));
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
