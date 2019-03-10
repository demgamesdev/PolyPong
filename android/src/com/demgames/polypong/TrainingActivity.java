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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


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

        final TextView infoTextView = (TextView) findViewById(R.id.trainingTextView);
        final CheckBox resumeCheckBox = (CheckBox) findViewById(R.id.resumeCheckBox);
        SeekBar layersSeekBar = (SeekBar) findViewById(R.id.layersSeekBar);
        SeekBar ballsSeekBar = (SeekBar) findViewById(R.id.ballsSeekBar);
        globals.getAI().infoTextView = infoTextView;

        layersSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                infoTextView.setText("Layers set to " + progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                /*Toast.makeText(TrainingActivity.this, "Layernumber set to:" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();*/
            }
        });

        ballsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                infoTextView.setText("Balls set to " + progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                /*Toast.makeText(TrainingActivity.this, "Layernumber set to:" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();*/
            }
        });

        String agentName = getIntent().getStringExtra("agentname");
        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "training";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = ballsSeekBar.getProgress();
                globals.getGameVariables().numberOfPlayers = 2;
                globals.getGameVariables().setBalls(true);
                globals.getGameVariables().setBats();

                globals.getGameVariables().aiState = false;
                globals.getGameVariables().gravityState = true;

                globals.getGameVariables().inputs.clear();
                globals.getGameVariables().outputs.clear();

                Intent startGame = new Intent(getApplicationContext(), GDXGameLauncher.class);
                startGame.putExtra("agentname",agentName);
                startActivity(startGame);
            }
        });

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globals.getAI().loadData(agentName);

                if(resumeCheckBox.isChecked()) {
                    globals.getAI().loadModel(agentName);
                } else {
                    int[] n_units = new int[layersSeekBar.getProgress()+2];
                    n_units[0] = globals.getAI().dataSet.numInputs(); //inputs dimension
                    n_units[1] = 10; //units per ball from convolution
                    n_units[n_units.length-1] = globals.getAI().dataSet.numOutcomes();
                    for(int l=2;l<n_units.length-1;l++) {
                        n_units[l] = 10; //hidden units
                    }
                    System.out.println("Input size " + n_units[0]);
                    globals.getAI().buildModel(agentName,n_units);
                }

                globals.getAI().train(10000,true);
                //model.save();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globals.getAI().loadModel(agentName);
                globals.getGameVariables().model = globals.getAI().model;
                //model.test(trainingSet.dataSet);

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "testing";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = ballsSeekBar.getProgress();
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
