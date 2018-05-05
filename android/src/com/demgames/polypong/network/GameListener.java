package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.demgames.polypong.Globals;
import com.demgames.polypong.network.sendclasses.SendBallKinetics;
import com.demgames.polypong.network.sendclasses.SendBallScreenChange;
import com.demgames.polypong.network.sendclasses.SendBat;
import com.demgames.polypong.network.sendclasses.SendScore;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import processing.core.PVector;

import com.demgames.polypong.GameLaunchActivity;

public class GameListener extends Listener{

    Globals globalVariables;

    public GameListener(Context myContext) {
        globalVariables=(Globals)myContext;
    }

    private static final String TAG = "GameListener";

    @Override
    public void connected(Connection connection) {
        String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
        Log.e(TAG, tempIpAdress+" connected.");
    }

    @Override
    public void disconnected(Connection connection) {
        /*String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];*/
        Log.e(TAG, " disconnected.");
        GameLaunchActivity.GLA.finish();
    }

    @Override
    public void received(Connection connection,Object object) {
        //Log.d(TAG, "Package received.");

        if(object instanceof SendBallKinetics) {
            //Log.d(TAG, "ball received");
            SendBallKinetics ballKinetics=(SendBallKinetics)object;
            int ballNumber=ballKinetics.ballNumber;
            PVector ballPosition=ballKinetics.ballPosition;
            PVector ballVelocity=ballKinetics.ballVelocity;

            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" updated to x "+Float.toString(ballPosition.x));


            globalVariables.setBallPosition(ballNumber,ballPosition);
            globalVariables.setBallVelocity(ballNumber,ballVelocity);


        } else if(object instanceof SendBallScreenChange) {

            SendBallScreenChange ballScreenChange=(SendBallScreenChange)object;
            int ballNumber=ballScreenChange.ballNumber;
            PVector ballPosition=ballScreenChange.ballPosition;
            PVector ballVelocity=ballScreenChange.ballVelocity;

            globalVariables.setBallPosition(ballNumber,ballPosition);
            globalVariables.setBallVelocity(ballNumber,ballVelocity);
            globalVariables.setBallPlayerScreen(ballNumber,globalVariables.getMyPlayerScreen());

            Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");

        } else if(object instanceof SendBat) {
            //Log.d(TAG,"received Bat");
            SendBat bat=(SendBat)object;
            PVector batPosition=bat.batPosition;
            float batOrientation=bat.batOrientation;

            globalVariables.setBatPosition(batPosition);
            globalVariables.setBatOrientation(batOrientation);

        } else if(object instanceof SendScore) {
            Log.d(TAG,"received Score");
            SendScore score=(SendScore)object;
            int myScore = score.myScore;
            int otherScore=score.otherScore;

            globalVariables.setMyScore(myScore);
            globalVariables.setOtherScore(myScore);


        }
    }
}
