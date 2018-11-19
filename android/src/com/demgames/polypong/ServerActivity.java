package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;
import android.os.Vibrator;

import com.demgames.polypong.network.ClientListener;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;



public class ServerActivity extends AppCompatActivity{

    private static final String TAG = "ServerActivity";
    private MyTask serverListUpdateTask;

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

        setContentView(R.layout.activity_server);

        /***Decklarationen***/

        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.getSettingsVariables().connectionState=0;

        globalVariables.getSettingsVariables().discoveryIpAdresses =new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getSettingsVariables().ipAdresses =new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getSettingsVariables().discoveryPlayerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getSettingsVariables().playerNames =new ArrayList<String>(Arrays.asList(new String[] {}));

        globalVariables.getSettingsVariables().startServerThread();

        globalVariables.setListeners(getApplicationContext());
        globalVariables.getSettingsVariables().server.addListener(globalVariables.getServerListener());

        try {
            globalVariables.getSettingsVariables().server.bind(globalVariables.getSettingsVariables().tcpPort,globalVariables.getSettingsVariables().udpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        globalVariables.getSettingsVariables().myPlayerNumber =0;


        //IP Suche
        serverListUpdateTask = new MyTask();
        serverListUpdateTask.execute();

        globalVariables.getGameVariables().setBalls(true);


    }

    @Override
    protected void onDestroy() {

        serverListUpdateTask.cancel(true);
        Log.d(TAG, "onDestroy: MyTask canceled");
        final Globals globalVariables=(Globals) getApplication();
        Log.d(TAG, "onDestroy: updatethread interrupted");

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
            String[] parts = teststring.split("\\.");
            if (parts.length == 4) {
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
            globalVariables.getSettingsVariables().stopClients();

        }
        return super.onKeyDown(keyCode, event);
    }


    /********* Thread Function - Searching IP and displaying *********/
    //Zeigt die IP Adresse an während dem Suchen
    class MyTask extends AsyncTask<Void,Void,Void>{

        Globals globalVariables = (Globals) getApplicationContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (ServerActivity.this, R.layout.listview, globalVariables.getSettingsVariables().discoveryIpAdresses);
        final TextView myIpTextView = (TextView) findViewById(R.id.IPtextView);


        @Override
        protected Void doInBackground(Void... voids) {

            //kryostuff--------------------------------------

            Log.d(TAG, "doInBackground: Anfang Suche");

            while(globalVariables.getSettingsVariables().connectionState==0 && !isCancelled()){
                //sendHostConnect();
                publishProgress();
                try {
                    Thread.currentThread().sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                globalVariables.getSettingsVariables().myIpAdress=wifiIpAddress(getApplicationContext());

                myIpTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfIp(globalVariables.getSettingsVariables().myIpAdress)) {
                            myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getSettingsVariables().myIpAdress);
                        }
                        else {
                            myIpTextView.setText("Unable to get Ip-Adress");
                        }
                    }
                });
            }

            /*while(!globalVariables.getReadyState()){
                Log.d(TAG, "doInBackground: Sende Settings");
                //sendSettings();
            }*/



            Log.d(TAG, "doInBackground: Ende Suche");

            while(globalVariables.getSettingsVariables().connectionState==1 && !isCancelled()) {

            }

            IGlobals.SendVariables.SendConnectionState sendConnectionState=new IGlobals.SendVariables.SendConnectionState();
            sendConnectionState.connectionState=3;
            globalVariables.getSettingsVariables().sendToClients(sendConnectionState,"tcp");

            globalVariables.getSettingsVariables().connectionState=3;

            if(!isCancelled()) {
                startActivity(new Intent(getApplicationContext(), GDXGameLauncher.class));
                //globalVariables.myThread.stop();
                serverListUpdateTask.cancel(true);
                finish();

                Log.d(TAG, "Game started");
            } else {
                Log.d(TAG,"skipped do in background due to cancelling");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            ListView ServerLV = (ListView) findViewById(R.id.serverListView);
            ServerLV.setAdapter(adapter);
            final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            //globalVariables.setSearchConnecState(true);
            ServerLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //Toast.makeText(Client.this, globalVariables.getMyIpList().get(i), Toast.LENGTH_SHORT).show();
                    v.vibrate(50);
                    Log.d(TAG, "onItemClick: " + Integer.toString(i));
                    Log.d(TAG, "onItemClick: " + globalVariables.getSettingsVariables().discoveryIpAdresses.get(i));
                    Toast.makeText(ServerActivity.this, "Zu \"" + globalVariables.getSettingsVariables().discoveryIpAdresses.get(i) + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                    //Generalize
                    globalVariables.getSettingsVariables().ipAdresses.add(globalVariables.getSettingsVariables().myIpAdress);
                    globalVariables.getSettingsVariables().ipAdresses.add(globalVariables.getSettingsVariables().discoveryIpAdresses.get(i));

                    globalVariables.getSettingsVariables().numberOfPlayers=globalVariables.getSettingsVariables().ipAdresses.size();

                    globalVariables.getSettingsVariables().startGameThreads();
                    globalVariables.getSettingsVariables().connectClients();
                    globalVariables.getSettingsVariables().setClientListeners(globalVariables.getClientListener());

                    globalVariables.getSettingsVariables().playerNames.add("test1");
                    globalVariables.getSettingsVariables().playerNames.add("test2");


                    Globals.SendVariables.SendSettings settings=new Globals.SendVariables.SendSettings();

                    settings.yourPlayerNumber=1;
                    settings.numberOfPlayers=globalVariables.getSettingsVariables().numberOfPlayers;
                    settings.ipAdresses=globalVariables.getSettingsVariables().ipAdresses.toArray(new String[0]);
                    settings.playerNames=globalVariables.getSettingsVariables().playerNames.toArray(new String[0]);

                    settings.ballsPositions=globalVariables.getGameVariables().ballsPositions;
                    settings.ballsVelocities=globalVariables.getGameVariables().ballsVelocities;
                    settings.ballsSizes=globalVariables.getGameVariables().ballsSizes;
                    settings.gameMode=globalVariables.getSettingsVariables().gameMode;
                    settings.gravityState=globalVariables.getGameVariables().gravityState;
                    settings.attractionState=globalVariables.getGameVariables().attractionState;
                    settings.ballsDisplayStates=globalVariables.getGameVariables().ballDisplayStates;

                    globalVariables.getSettingsVariables().sendToClients(settings,"tcp");
                    //globalVariables.getSettingsVariables().connections.get(0).sendTCP(settings);

                    globalVariables.getSettingsVariables().connectionState=1;
                    /*SendBallsKinetics ballPacket= new SendBallsKinetics();
                    ballPacket.ballsPositions=new PVector[]{new PVector(-1,100),new PVector(2,-10009)};
                    globalVariables.getConnectionList()[0].sendTCP(ballPacket);*/
                }
            });
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(globalVariables.getSettingsVariables().updateListViewState) {
                adapter.notifyDataSetChanged();
                globalVariables.getSettingsVariables().updateListViewState=false;
                //
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: canceld");
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void Void) {


            Log.d(TAG, "onPostExecute:  MyTask Abgeschlossen");

        }
        ServerActivity m_activity = null;
    }


}
