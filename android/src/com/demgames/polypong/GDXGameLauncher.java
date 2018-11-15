package com.demgames.polypong;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
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

        final Globals globalVariables = (Globals) getApplicationContext();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        ClassicGame classicGame = new ClassicGame(globalVariables);
		initialize(classicGame, config);
	}

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy: Activity destroyed");

        Globals globalVariables=(Globals)getApplicationContext();
        //TODO for more players
        globalVariables.getGameVariables().playerScores=new int[2];

        if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
            globalVariables.getNetworkVariables().server.stop();
        } else {
            globalVariables.getNetworkVariables().client.stop();
        }

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
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
        }
    }
}
