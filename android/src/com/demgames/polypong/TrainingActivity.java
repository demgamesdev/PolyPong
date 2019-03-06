package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;


public class TrainingActivity extends AppCompatActivity {

    private static final String TAG = "trainingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_training);

        final Globals globals = (Globals) getApplicationContext();

        final Button genButton = (Button) findViewById(R.id.genButton);
        final Button trainButton = (Button) findViewById(R.id.trainButton);
        final Button testButton = (Button) findViewById(R.id.testButton);

        TextView trainingTextView = (TextView) findViewById(R.id.trainingTextView);
        final CheckBox resumeCheckBox = (CheckBox) findViewById(R.id.resumeCheckBox);
        globals.getAI().infoTextView = trainingTextView;

        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "training";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = 1;
                globals.getGameVariables().numberOfPlayers = 2;
                globals.getGameVariables().setBalls(true);
                globals.getGameVariables().setBats();

                globals.getGameVariables().aiState = false;
                globals.getGameVariables().gravityState = true;

                globals.getGameVariables().inputs.clear();
                globals.getGameVariables().outputs.clear();
                Intent startGame = new Intent(getApplicationContext(), GDXGameLauncher.class);
                startActivity(startGame);
            }
        });

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globals.getAI().loadData("test");

                if(resumeCheckBox.isChecked()) {
                    globals.getAI().loadModel("test");
                } else {
                    globals.getAI().buildModel("test",new int[]{globals.getAI().dataSet.numInputs(),20,20,globals.getAI().dataSet.numOutcomes()});
                }

                globals.getAI().train(10000,true);
                //model.save();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globals.getAI().loadModel("test");
                globals.getGameVariables().model = globals.getAI().model;
                //model.test(trainingSet.dataSet);

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "testing";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = 1;
                globals.getGameVariables().numberOfPlayers = 2;
                globals.getGameVariables().setBalls(true);
                globals.getGameVariables().setBats();

                globals.getGameVariables().aiState = true;
                globals.getGameVariables().gravityState = true;
                Intent startGame = new Intent(getApplicationContext(), GDXGameLauncher.class);
                startActivity(startGame);

            }
        });
    }






    @Override
    protected void onDestroy() {
        Globals globals = (Globals) getApplicationContext();

        try{
            globals.getAI().trainingTask.cancel(true);
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
