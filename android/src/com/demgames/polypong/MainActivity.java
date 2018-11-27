package com.demgames.polypong;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.PowerManager;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //Always On
    protected PowerManager.WakeLock mWakeLock;
    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        //turn off debug maybe causing fc
        StrictMode.allowThreadDiskReads();
        super.onDestroy();
    }

    String myPlayerName;
    String file_name = "name_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



         /* will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "polypong:mywakelocktag");
        this.mWakeLock.acquire();

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);


        final Globals globalVariables = (Globals) getApplicationContext();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        globalVariables.getGameVariables().height= displayMetrics.heightPixels;
        globalVariables.getGameVariables().width = displayMetrics.widthPixels;

        Log.d(TAG, "screen width "+globalVariables.getGameVariables().width + " height "+globalVariables.getGameVariables().height);


        final Button startHostButton = (Button) findViewById(R.id.startHostButton);
        final Button startClientButton = (Button) findViewById(R.id.startClientButton);

        readName();

        startHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getMyPlayerName()) {
                    Intent startHost = new Intent(getApplicationContext(), OptionsActivity.class);
                    startActivity(startHost);
                }
            }

        });

        startClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getMyPlayerName()) {
                    Intent startClient = new Intent(getApplicationContext(), ClientActivity.class);
                    startActivity(startClient);
                    //myThread.stop();
                }




            }
        });
    }

    boolean getMyPlayerName(){
        Globals globalVariables = (Globals) getApplicationContext();
        EditText YourName = (EditText) findViewById(R.id.nameEditText);
        myPlayerName = YourName.getText().toString();
        if (myPlayerName.matches("")){
            Context context = getApplicationContext();
            CharSequence text = "myPlayerName ist ungültig!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            Log.e(TAG, "myPlayerName: Kein name eingegeben");
            return false;
        }
        else{
            globalVariables.getSettingsVariables().myPlayerName= myPlayerName;

            writeName();

            Log.d(TAG, "myPlayerName: "+ globalVariables.getSettingsVariables().myPlayerName);
            return true;
        }


    }


    //Saves latest myPlayerName entry onto internal Storage
    public void writeName(){
        try {
            FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
            fileOutputStream.write(myPlayerName.getBytes());
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Loads latest myPlayerName entry from internal Storage
    public void readName(){
        try {
            String Message;
            EditText YourName = (EditText) findViewById(R.id.nameEditText);
            FileInputStream fileInputStream = openFileInput(file_name);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message = bufferedReader.readLine())!=null){
                stringBuffer.append(Message + "\n");
            }

            YourName.setText(stringBuffer.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}