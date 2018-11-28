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
        try {
            if(object instanceof Globals.SendVariables.SendClass) {
                Globals.SendVariables.SendClass sendClass = (Globals.SendVariables.SendClass) object;

                for(int obj=0;obj<sendClass.sendObjects.length;obj++) {
                    if (sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendBallScreenChange) {
                        Globals.SendVariables.SendBallScreenChange ballScreenChange = (Globals.SendVariables.SendBallScreenChange) sendClass.sendObjects[obj];

                        synchronized (globalVariables.getGameVariables()) {
                        float rotateRad = (2f * MathUtils.PI / globalVariables.getSettingsVariables().numberOfPlayers * (ballScreenChange.myPlayerNumber - globalVariables.getSettingsVariables().myPlayerNumber));
                        //Log.d(TAG, "degrees " + rotateRad/MathUtils.PI*180);
                            for (int i = 0; i < ballScreenChange.ballNumbers.length; i++) {
                                Log.d(TAG, "fieldchange of ball " + ballScreenChange.ballNumbers[i] + " received");
                                globalVariables.getGameVariables().updateBallStates[ballScreenChange.ballNumbers[i]]=true;
                                globalVariables.getGameVariables().ballsPlayerScreens[ballScreenChange.ballNumbers[i]] = ballScreenChange.ballPlayerFields[i];
                                globalVariables.getGameVariables().ballsPositions[ballScreenChange.ballNumbers[i]] = globalVariables.getGameVariables().upScaleVector(ballScreenChange.ballPositions[i]).rotateRad(rotateRad);
                                globalVariables.getGameVariables().ballsVelocities[ballScreenChange.ballNumbers[i]] = globalVariables.getGameVariables().upScaleVector(ballScreenChange.ballVelocities[i]).rotateRad(rotateRad);
                            /*Log.d(TAG, "ballposition x " + globalVariables.getGameVariables().upScaleVector(ballScreenChange.ballPositions[i]).rotateRad(rotateRad).x +
                                    " y " + globalVariables.getGameVariables().upScaleVector(ballScreenChange.ballPositions[i]).rotateRad(rotateRad).y);*/
                            }
                        }

                        //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");

                    } else if (sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendBallGoal) {
                        //Log.d(TAG, "ballgoal received");
                        Globals.SendVariables.SendBallGoal ballGoal = (Globals.SendVariables.SendBallGoal) sendClass.sendObjects[obj];

                        synchronized (globalVariables.getGameVariables()) {
                            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" updated to x "+Float.toString(ballPosition.x));
                            globalVariables.getGameVariables().playerScores[ballGoal.myPlayerNumber] = ballGoal.playerScores[ballGoal.myPlayerNumber];
                            for (int i = 0; i < ballGoal.ballNumbers.length; i++) {
                                globalVariables.getGameVariables().updateBallStates[ballGoal.ballNumbers[i]] = true;
                                globalVariables.getGameVariables().ballDisplayStates[ballGoal.ballNumbers[i]] = false;
                                //Log.d(TAG, "ball "+Integer.toString(ballGoal.ballNumbers[i])+" in goal");
                                //TODO score
                            }
                        }


                    }else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendBallKinetics) {
                        //Log.d(TAG, "ballkinetics received");
                        //TODO investigate nullpointer exception in udp
                        Globals.SendVariables.SendBallKinetics ballKinetics=(Globals.SendVariables.SendBallKinetics)sendClass.sendObjects[obj];

                        synchronized (globalVariables.getGameVariables()) {
                            float rotateRad = (2f * MathUtils.PI / globalVariables.getSettingsVariables().numberOfPlayers * (ballKinetics.myPlayerNumber - globalVariables.getSettingsVariables().myPlayerNumber));


                            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" updated to x "+Float.toString(ballPosition.x));
                            //Log.d(TAG, "ball kinetics 0 x "+ ballKinetics.ballPositions[0].x +" y "+ballKinetics.ballPositions[0].y);
                            for (int i = 0; i < ballKinetics.ballNumbers.length; i++) {
                                globalVariables.getGameVariables().updateBallStates[ballKinetics.ballNumbers[i]] = true;
                                globalVariables.getGameVariables().ballsPlayerScreens[ballKinetics.ballNumbers[i]] = ballKinetics.ballPlayerFields[i];
                                globalVariables.getGameVariables().ballsPositions[ballKinetics.ballNumbers[i]] = globalVariables.getGameVariables().upScaleVector(ballKinetics.ballPositions[i]).rotateRad(rotateRad);
                                globalVariables.getGameVariables().ballsVelocities[ballKinetics.ballNumbers[i]] = globalVariables.getGameVariables().upScaleVector(ballKinetics.ballVelocities[i]).rotateRad(rotateRad);
                            }
                        }



                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendBat) {
                        //TODO investigate nullpointer exception in udp

                        Globals.SendVariables.SendBat bat=(Globals.SendVariables.SendBat)sendClass.sendObjects[obj];

                        synchronized (globalVariables.getGameVariables()) {
                            float rotateRad=(2f*MathUtils.PI/globalVariables.getSettingsVariables().numberOfPlayers*(bat.myPlayerNumber-globalVariables.getSettingsVariables().myPlayerNumber));

                            //Log.d(TAG,"received Bat at x " + globalVariables.getGameVariables().batPositions[bat.batPlayerField].x + " y " + globalVariables.getGameVariables().batPositions[bat.batPlayerField].y);

                            globalVariables.getGameVariables().batPositions[bat.batPlayerField]=globalVariables.getGameVariables().upScaleVector(bat.batPosition).rotateRad(rotateRad);
                            //globalVariables.getGameVariables().batVelocities[bat.batPlayerField]=bat.batVelocity.cpy().scl(-1f);
                            globalVariables.getGameVariables().batOrientations[bat.batPlayerField]=bat.batOrientation+rotateRad;
                        }


                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendScore) {
                        Log.d(TAG,"received Score");
                        Globals.SendVariables.SendScore score=(Globals.SendVariables.SendScore)sendClass.sendObjects[obj];

                        synchronized (globalVariables.getGameVariables()) {

                        }



                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendSettings) {
                        Log.d(TAG,"received settings");
                        Globals.SendVariables.SendSettings settings=(Globals.SendVariables.SendSettings)sendClass.sendObjects[obj];

                        synchronized (globalVariables.getSettingsVariables()) {
                            synchronized (globalVariables.getGameVariables()) {
                                globalVariables.getSettingsVariables().myPlayerNumber = settings.yourPlayerNumber;
                                globalVariables.getSettingsVariables().numberOfPlayers = settings.numberOfPlayers;
                                globalVariables.getSettingsVariables().ipAdresses = new ArrayList<String>(Arrays.asList(settings.ipAdresses));
                                globalVariables.getSettingsVariables().playerNames = new ArrayList<String>(Arrays.asList(settings.playerNames));

                                Log.d(TAG, "settings yourPlayerNumber: " + settings.yourPlayerNumber);
                                Log.d(TAG, "myPlayerNumber: " + globalVariables.getSettingsVariables().myPlayerNumber);

                                for (int i = 0; i < globalVariables.getSettingsVariables().ipAdresses.size(); i++) {
                                    Log.d(TAG, "received ip adresses " + globalVariables.getSettingsVariables().ipAdresses.get(i));
                                }

                                //globalVariables.getSettingsVariables().playerNames=settings.playerNames;

                                globalVariables.getGameVariables().numberOfBalls = settings.ballsPositions.length;
                                globalVariables.getSettingsVariables().gameMode = settings.gameMode;
                                globalVariables.getGameVariables().gravityState = settings.gravityState;
                                globalVariables.getGameVariables().attractionState = settings.attractionState;

                                globalVariables.getGameVariables().setBalls(false);
                                globalVariables.getGameVariables().setBats(globalVariables.getSettingsVariables().numberOfPlayers);

                                float rotateRad = (-2f * MathUtils.PI / globalVariables.getSettingsVariables().numberOfPlayers * globalVariables.getSettingsVariables().myPlayerNumber);

                                for (int i = 0; i < settings.ballsPositions.length; i++) {
                                    globalVariables.getGameVariables().ballsPositions[i] = globalVariables.getGameVariables().upScaleVector(settings.ballsPositions[i]).rotateRad(rotateRad);
                                    globalVariables.getGameVariables().ballsVelocities[i] = globalVariables.getGameVariables().upScaleVector(settings.ballsVelocities[i]).rotateRad(rotateRad);
                                    //globalVariables.getGameVariables().ballsPlayerScreens[i]=0;
                                    globalVariables.getGameVariables().ballsSizes[i] = settings.ballsSizes[i];
                                    globalVariables.getGameVariables().ballDisplayStates[i] = settings.ballsDisplayStates[i];
                                    //Log.d(TAG,"x "+Float.toString(globalVariables.getGameVariables().ballsPositions[i].x)+", y "+Float.toString(globalVariables.getGameVariables().ballsPositions[i].y));
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
                        }

                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendConnectionState) {
                        Globals.SendVariables.SendConnectionState connectionState=(Globals.SendVariables.SendConnectionState)sendClass.sendObjects[obj];

                        synchronized (globalVariables.getSettingsVariables()) {
                            Log.d(TAG, "Connectionstate received from player " + connectionState.myPlayerNumber);
                            globalVariables.getSettingsVariables().clientConnectionStates[connectionState.myPlayerNumber] = connectionState.connectionState;
                        }

                    } else if(sendClass.sendObjects[obj] instanceof Globals.SendVariables.SendConnectionRequest) {
                        Globals.SendVariables.SendConnectionRequest connectionRequest=(Globals.SendVariables.SendConnectionRequest)sendClass.sendObjects[obj];

                        String tempIpAdress=connection.getRemoteAddressTCP().toString();
                        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
                        Log.e(TAG, tempIpAdress+" connectionrequest of "+ connectionRequest.myPlayerName);

                        synchronized (globalVariables.getSettingsVariables()) {
                            globalVariables.getSettingsVariables().addDiscoveryIpToList(tempIpAdress);
                            globalVariables.getSettingsVariables().addDiscoveryPlayerNameToList(connectionRequest.myPlayerName);
                        }
                    }

                }


            } else {
                //Log.d(TAG, "Some Package received.");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
