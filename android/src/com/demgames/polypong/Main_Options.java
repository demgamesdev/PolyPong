package com.demgames.polypong;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main_Options extends AppCompatActivity {
    private static final String TAG = "Main_Options";

    String file_name = "main_options";
    String dark_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__options);


        Switch DarkMode = (Switch) findViewById(R.id.DarkMode);
        final ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.id);

        LoadOptions();

        DarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    dark_mode="1";
                    layout.setBackgroundColor(Color.parseColor("#000000"));
                }
                else{
                    dark_mode="0";
                    layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient));
                }
                SavesOptions();
            }
        });



    }

    public void SavesOptions(){
        try {
            FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
            fileOutputStream.write(dark_mode.getBytes());
            fileOutputStream.close();
            Log.d(TAG, "Optionen Laden: "+ dark_mode);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void LoadOptions(){
        final ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.id);
        try {

            String Message;
            FileInputStream fileInputStream = openFileInput(file_name);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message = bufferedReader.readLine())!=null){
                stringBuffer.append(Message);
            }

            Log.d(TAG, "Optionen Laden: "+ Message);

            if (Message=="1"){
                layout.setBackgroundColor(Color.parseColor("#000000"));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
