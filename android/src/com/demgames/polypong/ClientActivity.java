package com.demgames.polypong;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.os.StrictMode;
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
import android.os.Vibrator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ClientActivity extends AppCompatActivity{

    private static final String TAG = "Client";
    private ClientTask clientListUpdateTask;

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

        //networkstuff
        globalVariables.getSettingsVariables().connectionState=0;

        globalVariables.getSettingsVariables().discoveryIpAdresses =new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getSettingsVariables().ipAdresses =new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getSettingsVariables().discoveryPlayerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getSettingsVariables().playerNames =new ArrayList<String>(Arrays.asList(new String[] {}));

        globalVariables.getSettingsVariables().startServerThread();
        globalVariables.getSettingsVariables().startDiscoveryClientThread();


        globalVariables.setListeners(getApplicationContext());
        globalVariables.getSettingsVariables().server.addListener(globalVariables.getServerListener());
        //globalVariables.getSettingsVariables().discoveryClient.addListener(globalVariables.getClientListener());


        try {
            globalVariables.getSettingsVariables().server.bind(globalVariables.getSettingsVariables().tcpPort,globalVariables.getSettingsVariables().udpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //--------------------------------------------------


        //Thread für den Verbindungsaufbau
        clientListUpdateTask = new ClientTask();
        clientListUpdateTask.execute();

    }



    @Override
    protected void onDestroy() {

        clientListUpdateTask.cancel(true);
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
            globalVariables.getSettingsVariables().server.stop();
            globalVariables.getSettingsVariables().discoveryClient.stop();
            globalVariables.getSettingsVariables().stopClients();
        }
        return super.onKeyDown(keyCode, event);
    }



    /********* Thread Function - Searching IP and displaying *********/
    class ClientTask extends AsyncTask<Void,Void,Void> {

        Globals globalVariables = (Globals) getApplicationContext();
        ArrayAdapter<String> ClientListViewAdapter = new ArrayAdapter<String>
                (ClientActivity.this, R.layout.listview, globalVariables.getSettingsVariables().discoveryIpAdresses);
        TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        EditText manualIpEditText = (EditText) findViewById(R.id.manualIpEditText);
        Button manualIpButton = (Button) findViewById(R.id.manualIpButton);



        @Override
        protected Void doInBackground(Void... voids) {
            //Background Thread

            Log.d(TAG, "doInBackground: Anfang Suche");

            List<InetAddress> discoveryHosts;
            while (globalVariables.getSettingsVariables().connectionState == 0 && !isCancelled()) {
                //sendClientConnect();
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                discoveryHosts=globalVariables.getSettingsVariables().discoveryClient.discoverHosts(globalVariables.getSettingsVariables().udpPort,500);
                for (int i = 0; i < discoveryHosts.toArray().length; i++) {
                    String tempIpAdress = discoveryHosts.toArray()[i].toString();
                    tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length());
                    Log.d("discovery", tempIpAdress);
                    globalVariables.getSettingsVariables().addDiscoveryIpToList(tempIpAdress);
                }
                globalVariables.getSettingsVariables().myIpAdress=wifiIpAddress(getApplicationContext());

                myIpTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfIp(globalVariables.getSettingsVariables().myIpAdress)) {
                            myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getSettingsVariables().myIpAdress);

                        } else {
                            myIpTextView.setText("Unable to get Ip-Adress");
                        }

                    }
                });

                publishProgress();
            }


            Log.d(TAG, "doInBackground: Ende Suche");

            Log.d(TAG, "onPostExecute: Anfang Settings Senden");

            while(globalVariables.getSettingsVariables().connectionState == 1 && !isCancelled()) {

            }

            while(!(globalVariables.getSettingsVariables().connectionState==3) && !isCancelled()) {

            }

            if(!isCancelled()) {
                startActivity(new Intent(getApplicationContext(), GDXGameLauncher.class));
                //globalVariables.myThread.stop();
                clientListUpdateTask.cancel(true);
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
                ListView ClientListView = (ListView) findViewById(R.id.ClientListView);
                ClientListView.setAdapter(ClientListViewAdapter);
                //globalVariables.setSearchConnecState(true);
                final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                ClientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Toast.makeText(Client.this, globalVariables.getMyIpList().get(i), Toast.LENGTH_SHORT).show();
                        vib.vibrate(50);
                        Log.d(TAG, "onItemClick: " + Integer.toString(i));
                        Log.d(TAG, "onItemClick: " + globalVariables.getSettingsVariables().discoveryIpAdresses.get(i));
                        Toast.makeText(ClientActivity.this, "Zu \"" + globalVariables.getSettingsVariables().discoveryIpAdresses.get(i) + "\" wird verbunden", Toast.LENGTH_SHORT).show();

                        //storeIP = globalVariables.getSettingsVariables().remoteIpAdress;
                        //storeIPAdress();
                        try {
                            globalVariables.getSettingsVariables().discoveryClient.connect(5000,globalVariables.getSettingsVariables().discoveryIpAdresses.get(i),globalVariables.getSettingsVariables().tcpPort,globalVariables.getSettingsVariables().udpPort);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        globalVariables.getSettingsVariables().connectionState=1;

                        //sendClientConnect();
                    }
                });

                manualIpButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(checkIfIp(manualIpEditText.getText().toString())) {
                            Toast.makeText(ClientActivity.this, "Zu \"" + manualIpEditText.getText().toString() + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                            vib.vibrate(50);
                            globalVariables.getSettingsVariables().manualConnectIpAdress=manualIpEditText.getText().toString();
                            //storeIP = globalVariables.getSettingsVariables().remoteIpAdress;
                            //storeIPAdress();

                            try {
                                globalVariables.getSettingsVariables().discoveryClient.connect(5000, globalVariables.getSettingsVariables().manualConnectIpAdress, globalVariables.getSettingsVariables().tcpPort, globalVariables.getSettingsVariables().udpPort);
                                Globals.SendVariables.SendConnectionRequest sendConnectionRequest = new IGlobals.SendVariables.SendConnectionRequest();
                                sendConnectionRequest.myPlayerName=globalVariables.getSettingsVariables().myPlayerName;

                                globalVariables.getSettingsVariables().discoveryClient.sendTCP(sendConnectionRequest);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            globalVariables.getSettingsVariables().connectionState = 1;
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

            if(globalVariables.getSettingsVariables().updateListViewState) {
                ClientListViewAdapter.notifyDataSetChanged();
                globalVariables.getSettingsVariables().updateListViewState=false;
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

    //Loads latest myPlayerName entry from internal Storage

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

            EditText manualIpEditText = (EditText) findViewById(R.id.manualIpEditText);
            manualIpEditText.setText(stringBuffer.toString());

            //if (globalVariables.getSettingsVariables().addDiscoveryIpToList(stringBuffer.toString())) {
                //globalVariables.setUpdateListViewState(true);
            //}
            //storedIPadress.setText(stringBuffer.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}