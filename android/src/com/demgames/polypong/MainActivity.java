package com.demgames.polypong;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.content.Intent;
import android.os.PowerManager;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


         /* will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "polypong:mywakelocktag");
        this.mWakeLock.acquire();



        final Button startHostButton = (Button) findViewById(R.id.startHostButton);
        final Button startClientButton = (Button) findViewById(R.id.startClientButton);
        final Button manageButton = (Button) findViewById(R.id.manageButton);
        final EditText myPlayerNameEditText = (EditText) findViewById(R.id.nameEditText);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.ttf");
        startHostButton.setTypeface(typeface);
        startClientButton.setTypeface(typeface);
        manageButton.setTypeface(typeface);
        myPlayerNameEditText.setTypeface(typeface);

        String fileName = "myplayername";

        readNameToEditText(fileName,myPlayerNameEditText);

        //create data folders
        String [] directoryNames = new String[]{"agents","data"};
        for(String dir : directoryNames) {
            File projDir = new File(getFilesDir().getAbsolutePath() + File.separator + dir);
            if (!projDir.exists())
                projDir.mkdirs();
        }


        startHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myPlayerName = getMyPlayerName(myPlayerNameEditText);
                if (myPlayerName != null) {
                    writeName(fileName,myPlayerName);
                    Intent startHostOptionsActivity = new Intent(getApplicationContext(), HostOptionsActivity.class);
                    startHostOptionsActivity.putExtra("myplayername",myPlayerName);
                    startHostOptionsActivity.putExtra("networkmode","host");
                    startActivity(startHostOptionsActivity);
                }
            }

        });

        startClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myPlayerName = getMyPlayerName(myPlayerNameEditText);
                if (myPlayerName != null) {
                    writeName(fileName,myPlayerName);
                    Intent startClientActivity = new Intent(getApplicationContext(), ClientActivity.class);
                    startClientActivity.putExtra("myplayername",myPlayerName);
                    startClientActivity.putExtra("networkmode","client");
                    startActivity(startClientActivity);
                    //myThread.stop();
                }




            }
        });

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myPlayerName = getMyPlayerName(myPlayerNameEditText);
                if (myPlayerName != null) {
                    writeName(fileName,myPlayerName);
                    Intent startManageActivity = new Intent(getApplicationContext(), ManageActivity.class);
                    startManageActivity.putExtra("myplayername",myPlayerName);
                    startActivity(startManageActivity);
                    //myThread.stop();
                }




            }
        });
    }

    String getMyPlayerName(EditText editText){
        String myPlayerName = editText.getText().toString();
        if (myPlayerName.matches("")){
            CharSequence message = "myPlayerName ist ungültig!";
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            toast.show();
            Log.e(TAG, "myPlayerName not okay");

            return null;
        }
        else{

            Log.d(TAG, "myPlayerName okay");
            return myPlayerName;
        }
    }


    //Saves latest myPlayerName entry onto internal Storage
    public void writeName(String fileName, String name){
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileName, MODE_PRIVATE);
            fileOutputStream.write(name.getBytes());
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Loads latest myPlayerName entry from internal Storage
    public void readNameToEditText(String fileName, EditText editText){
        try {
            String Message;
            FileInputStream fileInputStream = openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message = bufferedReader.readLine())!=null){
                //removed newline
                stringBuffer.append(Message);
            }

            editText.setText(stringBuffer.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}