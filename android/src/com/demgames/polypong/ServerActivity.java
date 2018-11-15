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

import com.esotericsoftware.kryonet.Connection;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;



public class ServerActivity extends AppCompatActivity{

    private static final String TAG = "ServerActivity";
    private MyTask MyTaskServer;

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

        globalVariables.getNetworkVariables().ipAdressList=new ArrayList<String>(Arrays.asList(new String[] {}));
        globalVariables.getNetworkVariables().connectionList=new ArrayList<Connection>(Arrays.asList(new Connection[] {}));

        globalVariables.getSettingsVariables().myPlayerScreen=0;


        //IP Suche
        MyTaskServer= new MyTask();
        MyTaskServer.execute();

        globalVariables.getGameVariables().setBalls(true);


    }

    @Override
    protected void onDestroy() {

        MyTaskServer.cancel(true);
        Log.d(TAG, "onDestroy: MyTask canceled");
        final Globals globalVariables=(Globals) getApplication();
        //globalVariables.getServer().stop();
        //Log.d(TAG, "onDestroy: kryoserver stopped");
        //globalVariables.setSearchConnecState(false);
        //Toast.makeText(Client.this, "Suche wird beendet", Toast.LENGTH_SHORT).show();
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
            globalVariables.getNetworkVariables().server.stop();
        }
        return super.onKeyDown(keyCode, event);
    }


    /********* Thread Function - Searching IP and displaying *********/
    //Zeigt die IP Adresse an während dem Suchen
    class MyTask extends AsyncTask<Void,Void,Void>{

        Globals globalVariables = (Globals) getApplicationContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (ServerActivity.this, R.layout.listview, globalVariables.getNetworkVariables().ipAdressList);
        final TextView myIpTextView = (TextView) findViewById(R.id.IPtextView);


        @Override
        protected Void doInBackground(Void... voids) {

            //kryostuff--------------------------------------
            globalVariables.getNetworkVariables().server.start();
            try {
                globalVariables.getNetworkVariables().server.bind(globalVariables.getNetworkVariables().tcpPort,globalVariables.getNetworkVariables().udpPort);
            } catch (IOException e) {
                e.printStackTrace();
            }

            globalVariables.setGlobalListener(getApplicationContext());

            globalVariables.getNetworkVariables().server.addListener(globalVariables.getGlobalListener());

            globalVariables.registerKryoClasses(globalVariables.getNetworkVariables().server.getKryo());

            Log.d(TAG, "doInBackground: Anfang Suche");

            while(globalVariables.getSettingsVariables().connectionState==0 && !isCancelled()){
                //sendHostConnect();
                publishProgress();
                try {
                    Thread.currentThread().sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                globalVariables.getNetworkVariables().myIpAdress=wifiIpAddress(getApplicationContext());

                if(globalVariables.getNetworkVariables().connectionList.size()!=0 ) {
                    Log.d(TAG,"Connectionlist not empty");
                } else {
                    Log.d(TAG,"Connectionlist empty");
                }

                myIpTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfIp(globalVariables.getNetworkVariables().myIpAdress)) {
                            myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getNetworkVariables().myIpAdress);
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
            globalVariables.getNetworkVariables().connectionList.get(0).sendTCP(sendConnectionState);

            globalVariables.getSettingsVariables().connectionState=3;

            if(!isCancelled()) {
                startActivity(new Intent(getApplicationContext(), GDXGameLauncher.class));
                //globalVariables.myThread.stop();
                MyTaskServer.cancel(true);
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
                    Log.d(TAG, "onItemClick: " + globalVariables.getNetworkVariables().ipAdressList.get(i));
                    Toast.makeText(ServerActivity.this, "Zu \"" + globalVariables.getNetworkVariables().ipAdressList.get(i) + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                    globalVariables.getNetworkVariables().remoteIpAdress=globalVariables.getNetworkVariables().ipAdressList.get(i);

                    Globals.SendVariables.SendSettings mySettings=new Globals.SendVariables.SendSettings();
                    //mySettings.connectionList=globalVariables.getConnectionList();
                    mySettings.ballsPositions=globalVariables.getGameVariables().ballsPositions;
                    mySettings.ballsVelocities=globalVariables.getGameVariables().ballsVelocities;
                    mySettings.ballsSizes=globalVariables.getGameVariables().ballsSizes;
                    mySettings.gameMode=globalVariables.getSettingsVariables().gameMode;
                    mySettings.gravityState=globalVariables.getGameVariables().gravityState;
                    mySettings.attractionState=globalVariables.getGameVariables().attractionState;
                    mySettings.ballsDisplayStates=globalVariables.getGameVariables().ballDisplayStates;
                    globalVariables.getNetworkVariables().connectionList.get(0).sendTCP(mySettings);

                    globalVariables.getSettingsVariables().connectionState=1;
                    /*SendBallsKinetics ballPacket= new SendBallsKinetics();
                    ballPacket.ballsPositions=new PVector[]{new PVector(-1,100),new PVector(2,-10009)};
                    globalVariables.getConnectionList()[0].sendTCP(ballPacket);*/
                }
            });
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(globalVariables.getUpdateListViewState()) {
                adapter.notifyDataSetChanged();
                globalVariables.setUpdateListViewState(false);
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
