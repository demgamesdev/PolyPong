package com.demgames.polypong;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class HostOptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "HostOptionsActivity";
    private String gameMode = "classic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_hostoptions);

        final Globals globalVariables = (Globals) getApplicationContext();

        final Button submitButton = (Button) findViewById(R.id.hostOptionsSubmitButton);

        final SeekBar ballsSeekBar = (SeekBar) findViewById(R.id.hostOptionsBallsSeekBar);
        final TextView ballsTextView = (TextView) findViewById(R.id.hostOptionsBallsTextView);

        final SeekBar frictionSeekBar = (SeekBar) findViewById(R.id.frictionSeekBar);
        final TextView frictionTextView = (TextView) findViewById(R.id.frictionTextView);

        final CheckBox gravityCheckBox = (CheckBox) findViewById(R.id.gravitycheckBox);
        final CheckBox attractionCheckBox = (CheckBox) findViewById(R.id.attractcheckBox);

        final Spinner gamemode = (Spinner) findViewById(R.id.gamemodeSpinner);
        gamemode.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> gameModeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.gamemodes, R.layout.spinner_item);
        gameModeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gamemode.setAdapter(gameModeSpinnerAdapter);

        ballsTextView.setText( getString(R.string.numballs) + Integer.toString(ballsSeekBar.getProgress()));

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHostActivity = new Intent(getApplicationContext(), HostActivity.class);
                startHostActivity.putExtra("numberofballs",ballsSeekBar.getProgress());
                startHostActivity.putExtra("friction",frictionSeekBar.getProgress());
                startHostActivity.putExtra("gravitystate",gravityCheckBox.isChecked());
                startHostActivity.putExtra("attractionstate",attractionCheckBox.isChecked());
                startHostActivity.putExtra("gamemode",gameMode);
                startActivity(startHostActivity);

            }
        });



        ballsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ballsTextView.setText(getString(R.string.numballs) + Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        frictionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                frictionTextView.setText(Float.toString(((float)progress/10000)*2) + " Reibung");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i){
            case 0:
                this.gameMode = "classic";
                Log.d(TAG, "onItemSelected: gameMode classic");
                break;
            case 1:
                this.gameMode = "pong";
                Log.d(TAG, "onItemSelected: gameMode pong");
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        this.gameMode="classic";
    }
}
