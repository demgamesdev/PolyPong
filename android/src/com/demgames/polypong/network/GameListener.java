package com.demgames.polypong.network;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.demgames.polypong.GDXGameLauncher;
import com.demgames.polypong.Globals;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

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
        GDXGameLauncher.GDXGAME.finish();
    }

    @Override
    public void received(Connection connection,Object object) {
        //Log.d(TAG, "Package received.");

        if(object instanceof Globals.SendVariables.SendBallKinetics) {
            //Log.d(TAG, "ball received");
            Globals.SendVariables.SendBallKinetics ballKinetics=(Globals.SendVariables.SendBallKinetics)object;
            int ballNumber=ballKinetics.ballNumber;
            int ballPlayerScreen=ballKinetics.ballPlayerScreen;
            Vector2 ballPosition=ballKinetics.ballPosition;
            Vector2 ballVelocity=ballKinetics.ballVelocity;

            //Log.d(TAG, "ball "+Integer.toString(ballNumber)+" updated to x "+Float.toString(ballPosition.x));

            //globalVariables.getGameVariables().ballsPlayerScreens[ballNumber]=ballPlayerScreen;
            globalVariables.getGameVariables().ballsPositions[ballNumber]=new Vector2(1-ballPosition.x,2-ballPosition.y);
            globalVariables.getGameVariables().ballsVelocities[ballNumber]=new Vector2(-ballVelocity.x,-ballVelocity.y);


        } else if(object instanceof Globals.SendVariables.SendBallScreenChange) {

            Globals.SendVariables.SendBallScreenChange ballScreenChange=(Globals.SendVariables.SendBallScreenChange)object;
            int ballNumber=ballScreenChange.ballNumber;
            int ballPlayerScreen = ballScreenChange.ballPlayerScreen;
            Vector2 ballPosition=ballScreenChange.ballPosition;
            Vector2 ballVelocity=ballScreenChange.ballVelocity;

            globalVariables.getGameVariables().ballsPositions[ballNumber]=new Vector2(1-ballPosition.x,2-ballPosition.y);
            globalVariables.getGameVariables().ballsVelocities[ballNumber]=new Vector2(-ballVelocity.x,-ballVelocity.y);
            globalVariables.getGameVariables().ballsPlayerScreens[ballNumber]=ballPlayerScreen;

            Log.d(TAG, "ball "+Integer.toString(ballNumber)+" screenchange");

        } else if(object instanceof Globals.SendVariables.SendBat) {
            //Log.d(TAG,"received Bat");
            Globals.SendVariables.SendBat bat=(Globals.SendVariables.SendBat)object;
            Vector2 batPosition=bat.batPosition;
            float batOrientation=bat.batOrientation;

            globalVariables.getGameVariables().batPosition=batPosition;
            globalVariables.getGameVariables().batOrientation=batOrientation;

        } else if(object instanceof Globals.SendVariables.SendScore) {
            Log.d(TAG,"received Score");
            Globals.SendVariables.SendScore score=(Globals.SendVariables.SendScore)object;
            int myScore = score.myScore;
            int otherScore=score.otherScore;

            globalVariables.getGameVariables().myScore=myScore;
            globalVariables.getGameVariables().otherScore=otherScore;


        }
    }
}
