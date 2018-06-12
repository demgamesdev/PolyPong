package com.demgames.polypong;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.PowerManager;
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
    private static final String TAG = "MyActivity";

    //Always On
    protected PowerManager.WakeLock mWakeLock;
    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    String Name;
    String file_name = "name_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



         /* will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);


        final Button startHostButton = (Button) findViewById(R.id.startHostButton);
        final Button startClientButton = (Button) findViewById(R.id.startClientButton);


        readName();

        startHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getName()) {
                    Intent startHost = new Intent(getApplicationContext(), OptionsActivity.class);
                    startActivity(startHost);
                }
            }

        });

        startClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getName()) {
                    Intent startClient = new Intent(getApplicationContext(), ClientActivity.class);
                    startActivity(startClient);
                    //myThread.stop();
                }




            }
        });
    }

    boolean getName(){
        Globals globalVariables = (Globals) getApplicationContext();
        EditText YourName = (EditText) findViewById(R.id.nameEditText);
        Name = YourName.getText().toString();
        if (YourName.getText().toString().matches("")){
            Context context = getApplicationContext();
            CharSequence text = "Name ist ungültig!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            Log.e(TAG, "MainActivity getName: Kein name eingegeben");
            return false;
        }
        else{

            writeName();

            //Name in Globals Speichern
            String[] name = new String[2];
            name[0]=YourName.getText().toString();
            globalVariables.getSettingsVariables().playerNamesList=new ArrayList<String>(Arrays.asList(name));
            List<String> supplierNames1 = new ArrayList<String>();
            supplierNames1 = globalVariables.getSettingsVariables().playerNamesList;
            Log.d(TAG, "MainActivity getName: "+ supplierNames1.get(0));
            return true;
        }


    }


    //Saves latest Name entry onto internal Storage
    public void writeName(){
        try {
            FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
            fileOutputStream.write(Name.getBytes());
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Loads latest Name entry from internal Storage
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