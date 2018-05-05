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

import com.demgames.polypong.network.sendclasses.SendSettings;
import com.esotericsoftware.kryonet.Connection;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class ServerActivity extends AppCompatActivity {

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
        globalVariables.setConnectState(false);
        globalVariables.setReadyStateState(false);
        globalVariables.setGameLaunched(false);

        globalVariables.setIpAdressList(new String[] {});
        globalVariables.setConnectionList(new Connection[]{});
        globalVariables.setRemoteIpAdress(null);

        globalVariables.setMyPlayerScreen(0);


        //IP Suche
        MyTaskServer= new MyTask();
        MyTaskServer.execute();

        globalVariables.setBalls(true);


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
            globalVariables.getServer().stop();
        }
        return super.onKeyDown(keyCode, event);
    }


    /********* Thread Function - Searching IP and displaying *********/
    //Zeigt die IP Adresse an während dem Suchen
    class MyTask extends AsyncTask<Void,Void,Void>{

        Globals globalVariables = (Globals) getApplicationContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (ServerActivity.this, R.layout.listview, globalVariables.getIpAdressList());
        final TextView myIpTextView = (TextView) findViewById(R.id.IPtextView);


        @Override
        protected Void doInBackground(Void... voids) {

            //kryostuff--------------------------------------
            globalVariables.getServer().start();
            try {
                globalVariables.getServer().bind(globalVariables.getMyPort(), globalVariables.getMyPort());
            } catch (IOException e) {
                e.printStackTrace();
            }

            globalVariables.setServerListener(getApplicationContext());

            globalVariables.getServer().addListener(globalVariables.getServerListener());

            globalVariables.registerKryoClasses(globalVariables.getServer().getKryo());

            Log.d(TAG, "doInBackground: Anfang Suche");

            while(!globalVariables.getConnectState() && !isCancelled()){
                //sendHostConnect();
                publishProgress();
                try {
                    Thread.currentThread().sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));

                if(globalVariables.getConnectionList().length!=0 ) {
                    Log.d(TAG,"Connectionlist not empty");
                } else {
                    Log.d(TAG,"Connectionlist empty");
                }

                myIpTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfIp(globalVariables.getMyIpAdress())) {
                            myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getMyIpAdress());
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

            while(!globalVariables.getReadyState() && !isCancelled()) {

            }

            if(!isCancelled()) {
                globalVariables.getServer().removeListener(globalVariables.getServerListener());
                Intent startGame = new Intent(getApplicationContext(), GameLaunchActivity.class);
                //Intent startGame = new Intent(getApplicationContext(), GameLaunchActivity.class);
                startGame.putExtra("mode", "host");
                startActivity(startGame);
                //globalVariables.myThread.stop();
                globalVariables.setGameLaunched(true);
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
            //globalVariables.setSearchConnecState(true);
            ServerLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //Toast.makeText(Client.this, globalVariables.getMyIpList().get(i), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onItemClick: " + Integer.toString(i));
                    Log.d(TAG, "onItemClick: " + globalVariables.getIpAdressList().get(i));
                    Toast.makeText(ServerActivity.this, "Zu \"" + globalVariables.getIpAdressList().get(i) + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                    globalVariables.setConnectState(true);
                    globalVariables.setRemoteIpAdress(globalVariables.getIpAdressList().get(i));

                    SendSettings mySettings=new SendSettings();
                    //mySettings.connectionList=globalVariables.getConnectionList();
                    mySettings.ballsPositions=globalVariables.getBallsPositions();
                    mySettings.ballsVelocities=globalVariables.getBallsVelocities();
                    mySettings.ballsSizes=globalVariables.getBallsSizes();
                    mySettings.gameMode=globalVariables.getGameMode();
                    mySettings.gravityState=globalVariables.getGravityState();
                    mySettings.attractionState=globalVariables.getAttractionState();
                    globalVariables.getConnectionList()[0].sendTCP(mySettings);

                    /*SendBallsKinetics ballPacket= new SendBallsKinetics();
                    ballPacket.ballsPositions=new PVector[]{new PVector(-1,100),new PVector(2,-10009)};
                    globalVariables.getConnectionList()[0].sendTCP(ballPacket);*/

                    globalVariables.setReadyStateState(true);
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
