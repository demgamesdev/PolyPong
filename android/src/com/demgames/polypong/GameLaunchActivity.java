package com.demgames.polypong;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;
import android.widget.FrameLayout;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.demgames.polypong.ClassicGame;

import processing.android.PFragment;
import processing.android.CompatUtils;
import processing.core.PApplet;

public class GameLaunchActivity extends AppCompatActivity {
    public static Activity GLA;

    private PApplet sketch;
    private static final String TAG = "MyActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GLA=this;
        super.onCreate(savedInstanceState);
        final Globals globalVariables = (Globals) getApplicationContext();

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Log.d(TAG, "onCreate: Entscheide welches spiel gestartet wird");
        Log.d(TAG, "onCreate: Spiel: " + globalVariables.getGameMode());

        globalVariables.setGameListener(getApplicationContext());

        if(getIntent().getExtras().getString("mode").equals("host")) {
            globalVariables.getServer().addListener(globalVariables.getGameListener());
        } else if(getIntent().getExtras().getString("mode").equals("client")) {
            globalVariables.getClient().addListener(globalVariables.getGameListener());
        }


        if (globalVariables.getGameMode()==1){
            Log.d(TAG, "onCreate: Patys sketch gestartet");
            sketch = new Sketch(getIntent().getExtras().getString("mode"));
            PFragment fragment = new PFragment(sketch);
            fragment.setView(frame, this);

        } else if (globalVariables.getGameMode()==2){
            startActivity(new Intent(getApplicationContext(), AndroidLauncher.class));
            Log.d(TAG, "onCreate: Raphaels sketch gestartet");
        }

        else if(globalVariables.getGameMode()==0){
            Log.e(TAG, "onCreate: Spiel = 0 (Übertragung der Settings von Server an Client überprüfen)");
        }

        else{
            Log.e(TAG, "onCreate: Spiel nicht definiert");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (sketch != null) {
            sketch.onNewIntent(intent);
        }
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy: Activity destroyed");

        Globals globalVariables=(Globals)getApplicationContext();

        if(getIntent().getExtras().getString("mode").equals("host")) {
            globalVariables.getServer().stop();
        } else if(getIntent().getExtras().getString("mode").equals("client")) {
            globalVariables.getClient().stop();
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

