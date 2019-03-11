package com.demgames.polypong;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class GDXGameLauncher extends AndroidApplication {
    private static final String TAG = "GDXGameLauncher" ;
    public static Activity GDXGAME;
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		GDXGAME=this;
		super.onCreate(savedInstanceState);

        final Globals globals = (Globals) getApplicationContext();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        globals.getGameVariables().playerScores=new int[globals.getSettingsVariables().numberOfPlayers];

        if(globals.getSettingsVariables().gameMode.equals("classic")) {
            ClassicGame game = new ClassicGame(globals);
            initialize(game, config);
        } else if(globals.getSettingsVariables().gameMode.equals("pong")) {
            ClassicGame game = new ClassicGame(globals);
            initialize(game, config);
        } else if(globals.getSettingsVariables().gameMode.equals("training") || globals.getSettingsVariables().gameMode.equals("testing")) {
            TrainingGame game = new TrainingGame(globals);
            initialize(game, config);
        }
	}

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy: Activity destroyed");

        Globals globals=(Globals)getApplicationContext();
        //TODO for more players

        if(globals.getSettingsVariables().gameMode.equals("training")) {


            Log.d(TAG, "input length "+globals.getGameVariables().inputs.size());
            //System.out.println(globals.getGameVariables().inputs);

        /*for(int i=0;i<globals.getGameVariables().inputs.size();i++) {
            ds.add(globals.getGameVariables().inputs.get(i),globals.getGameVariables().outputs.get(i));
        }*/
            globals.getAI().createDataSet(getIntent().getStringExtra("dataname"),globals.getGameVariables().inputs,globals.getGameVariables().outputs);
            globals.getAI().saveData();
        }


        /*for(DataSetRow dataRow : ds.dataSet.getRows()) {
            System.out.print("Input: " + Arrays.toString(dataRow.getInput()));
            System.out.println(" Output: " + Arrays.toString(dataRow.getDesiredOutput()));

        }*/

        if(globals.getSettingsVariables().serverThread != null) {
            globals.getSettingsVariables().serverThread.shutdownServer();
            globals.getSettingsVariables().shutdownAllClients();
        }

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Globals globalVariables=(Globals)getApplicationContext();
        globalVariables.getSettingsVariables().hasFocus = hasFocus;
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            if(globalVariables.getGameVariables().gameState ==1) {
                globalVariables.getSettingsVariables().clientConnectionStates[globalVariables.getSettingsVariables().myPlayerNumber] =4;
                IGlobals.SendVariables.SendConnectionState sendConnectionState = new IGlobals.SendVariables.SendConnectionState();
                sendConnectionState.myPlayerNumber = globalVariables.getSettingsVariables().myPlayerNumber;
                sendConnectionState.connectionState = 4;
                globalVariables.getSettingsVariables().sendObjectToAllClients(sendConnectionState, "tcp");
            }
        } else {
            if(globalVariables.getGameVariables().gameState ==1) {
                globalVariables.getSettingsVariables().clientConnectionStates[globalVariables.getSettingsVariables().myPlayerNumber]=5;
                IGlobals.SendVariables.SendConnectionState sendConnectionState = new IGlobals.SendVariables.SendConnectionState();
                sendConnectionState.myPlayerNumber = globalVariables.getSettingsVariables().myPlayerNumber;
                sendConnectionState.connectionState = 5;
                globalVariables.getSettingsVariables().sendObjectToAllClients(sendConnectionState, "tcp");
            }
        }
    }
}
