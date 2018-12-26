package com.demgames.polypong;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.PowerManager;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
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

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


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
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "polypong:mywakelocktag");
        this.mWakeLock.acquire();

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);


        final Button startHostButton = (Button) findViewById(R.id.startHostButton);
        final Button startClientButton = (Button) findViewById(R.id.startClientButton);
        final TextView WelcomeScreen = (TextView) findViewById(R.id.Welcome_Text);
        final EditText YourName = (EditText) findViewById(R.id.nameEditText);

        WelcomeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YourName.setVisibility(View.VISIBLE);
                WelcomeScreen.setVisibility(View.INVISIBLE);
            }
        });

        //Erkennt wenn enter bei der Nameneingabe gedrückt wird
        //Wenn der
        YourName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (YourName.getText().toString().matches("")){

                }
                else{
                    NameEditToTextView();
                }
                return false;
            }
        });

        readName();

        welcomescreen();

        startHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getName()) {
                    NameEditToTextView();
                    Intent startHost = new Intent(getApplicationContext(), Game_Selection.class);
                    startActivity(startHost);
                }
            }

        });

        startClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getName()) {
                    NameEditToTextView();
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

            //Name in Globals gespeichert
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
                stringBuffer.append(Message);
            }
            YourName.setText(stringBuffer.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void NameEditToTextView(){

        final TextView WelcomeScreen = (TextView) findViewById(R.id.Welcome_Text);
        final EditText YourName = (EditText) findViewById(R.id.nameEditText);

        YourName.setVisibility(View.INVISIBLE);
        WelcomeScreen.setVisibility(View.VISIBLE);
        welcomescreen();
        Name = YourName.getText().toString();
        writeName();
    }


    //Überprüft ob ein name schon gespeichert ist und wählt Wilkommenscreen aus
    public void welcomescreen(){
        EditText YourName = (EditText) findViewById(R.id.nameEditText);
        TextView Welcome = (TextView) findViewById(R.id.Welcome_Text);
        View Test = (View) findViewById(R.id.username_text_input_layout);
        //ImageButton EditNameBtn = (ImageButton) findViewById(R.id.editNameBtn);

        //Edittext wird unsichtbar
        if (!YourName.getText().toString().matches("")){
            YourName.setVisibility(View.INVISIBLE);
            Test.setVisibility(View.INVISIBLE);
            Welcome.setVisibility(View.VISIBLE);

            //Unterstreicht den Text
            SpannableString content = new SpannableString(getString(R.string.welcomeScreen) + " " + YourName.getText().toString() + "!");
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            Welcome.setText(content);
        }
        //Willkommenscreen wird unsichtbar
        else{
            Welcome.setVisibility(View.INVISIBLE);
            //EditNameBtn.setVisibility(View.INVISIBLE);
        }
    }
}