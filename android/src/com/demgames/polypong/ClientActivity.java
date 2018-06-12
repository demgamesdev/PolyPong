package com.demgames.polypong;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.os.StrictMode;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Connection;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;


public class ClientActivity extends AppCompatActivity{

    private static final String TAG = "Client";
    private MyTaskClient MyTaskClient;

    String file_name = "IPAdressfile";
    String storeIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Todo proper network handling in asynctask or thread
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }


        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_client);

        /***Deklarationen***/
        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.getSettingsVariables().connectState=false;
        globalVariables.getSettingsVariables().readyState=false;
        globalVariables.getSettingsVariables().gameLaunched=false;

        globalVariables.getNetworkVariables().ipAdressList=new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getNetworkVariables().connectionList=new ArrayList<Connection>(Arrays.asList(new Connection[] {}));

        globalVariables.getSettingsVariables().myPlayerScreen=1;

        //--------------------------------------------------


        //Thread für den Verbindungsaufbau
        MyTaskClient = new MyTaskClient();
        MyTaskClient.execute();

    }



    @Override
    protected void onDestroy() {

        MyTaskClient.cancel(true);
        Log.d(TAG, "onDestroy: MyTask beendet");
        final Globals globalVariables=(Globals) getApplication();
        //globalVariables.getClient().stop();
        //Log.d(TAG, "onDestroy: Kryoclient stopped");
        Log.d(TAG, "onDestroy: Activity geschlossen");

        super.onDestroy();
    }

    /********* OTHER FUNCTIONS *********/


    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            //ipAddressString = null;
            ipAddressString = "192.168.43.1";
        }
        return ipAddressString;
    }

    boolean checkIfIp(String teststring) {
        if(teststring != null) {
            String[] parts = teststring.split("\\."); //String wird bei jedem Punkt gesplittet
            if (parts.length == 4) {                        //String muss aus 4 Teilen bestehen
                return (true);
            }
        }

        return (false);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            Globals globalVariables = (Globals) getApplicationContext();
            globalVariables.getNetworkVariables().client.stop();
        }
        return super.onKeyDown(keyCode, event);
    }



    /********* Thread Function - Searching IP and displaying *********/
    class MyTaskClient extends AsyncTask<Void,Void,Void> {

        Globals globalVariables = (Globals) getApplicationContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (ClientActivity.this, R.layout.listview, globalVariables.getNetworkVariables().ipAdressList);
        TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        EditText manualIpEditText = (EditText) findViewById(R.id.manualIpEditText);
        Button manualIpButton = (Button) findViewById(R.id.manualIpButton);

        @Override
        protected Void doInBackground(Void... voids) {
            //Background Thread

            //kryostuff--------------------------------------
            globalVariables.getNetworkVariables().client.start();

            globalVariables.setClientListener(getApplicationContext());
            globalVariables.getNetworkVariables().client.addListener(globalVariables.getClientListener());

            globalVariables.registerKryoClasses(globalVariables.getNetworkVariables().client.getKryo());

            Log.d(TAG, "doInBackground: Anfang Suche");


            while (!globalVariables.getSettingsVariables().connectState&& !isCancelled()) {
                //sendClientConnect();
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                globalVariables.setHostsList(globalVariables.getNetworkVariables().client.discoverHosts(globalVariables.getNetworkVariables().myPort,1000));
                if(globalVariables.getHostsList().toArray().length!=0) {
                    for (int i = 0; i < globalVariables.getHostsList().toArray().length; i++) {
                        String tempIPAdress = globalVariables.getHostsList().toArray()[i].toString();
                        tempIPAdress = tempIPAdress.substring(1, tempIPAdress.length());
                        Log.d("discovery", tempIPAdress);
                        if (globalVariables.getNetworkVariables().addIpTolist(tempIPAdress)) {
                            globalVariables.setUpdateListViewState(true);
                        }
                    }
                }
                globalVariables.getNetworkVariables().myIpAdress=wifiIpAddress(getApplicationContext());

                myIpTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfIp(globalVariables.getNetworkVariables().myIpAdress)) {
                            myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getNetworkVariables().myIpAdress);
                            //IP Adresse wird in die Liste Hinzugefügt
                            //globalVariables.addIpTolist(globalVariables.getMyIpAdress());

                        } else {
                            myIpTextView.setText("Unable to get Ip-Adress");
                        }
                        //globalVariables.updateListView();

                    }
                });

                publishProgress();
            }


            Log.d(TAG, "doInBackground: Ende Suche");

            Log.d(TAG, "onPostExecute: Anfang Settings Senden");

            while(!globalVariables.getSettingsVariables().readyState && !isCancelled()) {

            }

            if(!isCancelled()) {
                globalVariables.getNetworkVariables().client.removeListener(globalVariables.getClientListener());
                startActivity(new Intent(getApplicationContext(), GDXGameLauncher.class));
                //globalVariables.myThread.stop();
                globalVariables.getSettingsVariables().gameLaunched=true;
                MyTaskClient.cancel(true);
                finish();

                Log.d(TAG, "onPostExecute: Ende Settings Senden");

                Log.d(TAG, "onPostExecute:  MyTask Abgeschlossen");
            } else {
                Log.d(TAG,"skipped do in background due to cancelling");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

                loadIPAdress();

                //Vor dem Thread Initialisierung
                ListView ClientLV = (ListView) findViewById(R.id.ClientListView);
                ClientLV.setAdapter(adapter);
                //globalVariables.setSearchConnecState(true);
                final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                ClientLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Toast.makeText(Client.this, globalVariables.getMyIpList().get(i), Toast.LENGTH_SHORT).show();
                        v.vibrate(50);
                        Log.d(TAG, "onItemClick: " + Integer.toString(i));
                        Log.d(TAG, "onItemClick: " + globalVariables.getNetworkVariables().ipAdressList.get(i));
                        Toast.makeText(ClientActivity.this, "Zu \"" + globalVariables.getNetworkVariables().ipAdressList.get(i) + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                        globalVariables.getSettingsVariables().connectState=true;
                        globalVariables.getNetworkVariables().remoteIpAdress=globalVariables.getNetworkVariables().ipAdressList.get(i);
                        storeIP = globalVariables.getNetworkVariables().remoteIpAdress;
                        storeIPAdress();

                        try {
                            globalVariables.getNetworkVariables().client.connect(5000,globalVariables.getNetworkVariables().remoteIpAdress,globalVariables.getNetworkVariables().myPort,globalVariables.getNetworkVariables().myPort);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //sendClientConnect();
                    }
                });

                manualIpButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(checkIfIp(manualIpEditText.getText().toString())) {
                            Toast.makeText(ClientActivity.this, "Zu \"" + manualIpEditText.getText().toString() + "\" wird verbunden", Toast.LENGTH_SHORT).show();

                            try {
                                globalVariables.getNetworkVariables().client.connect(5000, manualIpEditText.getText().toString(), globalVariables.getNetworkVariables().myPort, globalVariables.getNetworkVariables().myPort);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            globalVariables.getNetworkVariables().remoteIpAdress=manualIpEditText.getText().toString();
                            globalVariables.getSettingsVariables().connectState=true;
                        } else {
                            Toast.makeText(ClientActivity.this, "Gültige Ip-Adresse eingeben", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: Asynctask canceled");
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //Neue IP Adresse wird in die Listview geschrieben

            if(globalVariables.getUpdateListViewState()) {
                adapter.notifyDataSetChanged();
                globalVariables.setUpdateListViewState(false);
                //
            }

        }

        //@Override
        protected void onPostExecute(Void Void) {
            ClientActivity m_activity = null;
        }

    }


    //Saves latest connected IP Adress in local storage
    public void storeIPAdress(){
        try {
            FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
            fileOutputStream.write(storeIP.getBytes());
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Loads latest Name entry from internal Storage

    public void loadIPAdress(){
        Globals globalVariables = (Globals) getApplicationContext();
        try {
            String Message;
            FileInputStream fileInputStream = openFileInput(file_name);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message = bufferedReader.readLine())!=null){
                stringBuffer.append(Message);
            }

            if (globalVariables.getNetworkVariables().addIpTolist(stringBuffer.toString())) {
                globalVariables.setUpdateListViewState(true);
            }
            //storedIPadress.setText(stringBuffer.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}