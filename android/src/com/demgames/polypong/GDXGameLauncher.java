package com.demgames.polypong;

import com.demgames.miscclasses.SendClasses.*;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;


public class GDXGameLauncher extends AndroidApplication {
    private static final String TAG = "GDXGameLauncher" ;
    public static Activity GDXGAME;
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		GDXGAME=this;
		super.onCreate(savedInstanceState);

        final Globals globals = (Globals) getApplicationContext();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        if(getIntent().getStringExtra("gamemode").equals("classic")) {
            ClassicGame game = new ClassicGame(globals, getIntent().getIntExtra("myplayernumber",0),getIntent().getIntExtra("numberofplayers",2),
                    getIntent().getIntExtra("numberofballs",1),getIntent().getBooleanExtra("gravitystate",false),
                    getIntent().getBooleanExtra("attractionstate",false),getIntent().getStringExtra("mode"),
                    getIntent().getBooleanExtra("agentmode",false),displayMetrics.heightPixels,displayMetrics.widthPixels);
            if(getIntent().getBooleanExtra("agentmode",false)) {
                globals.getAgent().loadModel(getIntent().getStringExtra("agentname"));
                //globals.getGameVariables().model = globals.getNeuralNetwork().model;
            }
            initialize(game, config);
        } else if(getIntent().getStringExtra("gamemode").equals("pong")) {
            ClassicGame game = new ClassicGame(globals, getIntent().getIntExtra("myplayernumber",0),getIntent().getIntExtra("numberofplayers",2),
                    getIntent().getIntExtra("numberofballs",1),getIntent().getBooleanExtra("gravitystate",false),
                    getIntent().getBooleanExtra("attractionstate",false),getIntent().getStringExtra("mode"),
                    getIntent().getBooleanExtra("agentmode",false),displayMetrics.heightPixels,displayMetrics.widthPixels);
            initialize(game, config);
        } else if(getIntent().getStringExtra("gamemode").equals("training") || getIntent().getStringExtra("gamemode").equals("testing")) {
            TrainingGame game = new TrainingGame(globals, getIntent().getIntExtra("myplayernumber",0),getIntent().getIntExtra("numberofplayers",2),
                    getIntent().getIntExtra("numberofballs",1),getIntent().getBooleanExtra("gravitystate",false),
                    getIntent().getBooleanExtra("attractionstate",false),getIntent().getStringExtra("mode"),
                    getIntent().getBooleanExtra("agentmode",false),displayMetrics.heightPixels,displayMetrics.widthPixels);
            initialize(game, config);
        }
	}

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy: Activity destroyed");

        Globals globals=(Globals)getApplicationContext();
        //TODO for more players

        if(getIntent().getStringExtra("gamemode").equals("training")) {


            Log.d(TAG, "input length "+globals.getAgent().inputs.size());
            //System.out.println(globals.getGameVariables().inputs);

            globals.getAgent().createDataSet(globals.getAgent().inputs,globals.getAgent().outputs);
            globals.getAgent().saveDataSet(getIntent().getStringExtra("dataname"));
        }


        /*for(DataSetRow dataRow : ds.dataSet.getRows()) {
            System.out.print("Input: " + Arrays.toString(dataRow.getInput()));
            System.out.println(" Output: " + Arrays.toString(dataRow.getDesiredOutput()));

        }*/

        if(!(getIntent().getStringExtra("gamemode").equals("training") || getIntent().getStringExtra("gamemode").equals("testing"))) {
            globals.getComm().shutdownServer();
            globals.getComm().shutdownAllClients();
        }

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Globals globals=(Globals)getApplicationContext();
        if (globals.getComm().gameHasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            if(globals.getComm().gameState ==1) {
                globals.getComm().clientConnectionStatesMap.put(getIntent().getIntExtra("myplayernumber",0) ,4);

                globals.getComm().sendObjectToAllClients(new SendConnectionState(getIntent().getIntExtra("myplayernumber",0),4), "tcp");
            }
        } else {
            if(globals.getComm().gameState ==1) {
                globals.getComm().clientConnectionStatesMap.put(getIntent().getIntExtra("myplayernumber",0) ,5);

                globals.getComm().sendObjectToAllClients(new SendConnectionState(getIntent().getIntExtra("myplayernumber",0),5), "tcp");
            }
        }
    }
}
