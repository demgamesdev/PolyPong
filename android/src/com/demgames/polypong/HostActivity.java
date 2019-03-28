package com.demgames.polypong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;
import android.os.Vibrator;

import com.demgames.miscclasses.GameObjectClasses.*;
import com.demgames.miscclasses.SendClasses.*;
import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.ServerListener;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HostActivity extends AppCompatActivity{

    private static final String TAG = "HostActivity";
    private UpdateTask searchClientsUpdateTask;
    private String myIpAdress;
    private ListView serverListView;

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
        setContentView(R.layout.activity_host);

        /***Decklarationen***/

        final Globals globals = (Globals) getApplicationContext();
        final Button startGameButton= (Button) findViewById(R.id.hostStartGameButton);
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        globals.getComm().setSetupConnectionState(0);
        globals.getComm().resetLists();
        globals.getComm().startServerThread(new ServerListener(getApplicationContext(),getIntent().getStringExtra("myplayername"),getIntent().getStringExtra("networkmode")));


        //IP Suche
        searchClientsUpdateTask = new UpdateTask();
        searchClientsUpdateTask.execute();


        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globals.getComm().resetPlayerMap();
                globals.getComm().playerMap.put(0,new Player(getIntent().getStringExtra("myplayername"),myIpAdress));

                /** add checked clients to player map*/
                SparseBooleanArray checkedClientsIndices = serverListView.getCheckedItemPositions();
                int playerIndex =1; //starting with 0 for host
                for(int i=0; i<globals.getComm().discoveryPlayers.size();i++) {
                    if(checkedClientsIndices.get(i)){
                        globals.getComm().playerMap.put(playerIndex,new Player(globals.getComm().discoveryPlayers.get(i)));
                        playerIndex++;
                    }
                }


                if(globals.getComm().playerMap.size()>1) {
                    vibrator.vibrate(50);

                    globals.getComm().initGame(0,getIntent().getIntExtra("numberofballs",1),globals.getComm().playerMap.size(),
                            getIntent().getStringExtra("gamemode"),getIntent().getBooleanExtra("gravitystate",false),
                            getIntent().getBooleanExtra("attractionstate", false),true);


                    Toast.makeText(HostActivity.this, "Connecting to " + Integer.toString(globals.getComm().playerMap.size()- 1)
                            + " players", Toast.LENGTH_SHORT).show();

                    /**i.e. not yet pressed on start game*/
                    if(globals.getComm().setupConnectionState<2) {
                        globals.getComm().startAllClientThreads(0,globals.getComm().playerMap.size(),new ClientListener(getApplicationContext(),getIntent().getStringExtra("myplayername"),
                                getIntent().getStringExtra("networkmode")));
                    }
                    globals.getComm().connectAllClients(globals.getComm().playerMap,myIpAdress);
                    /**connected to all clients*/
                    Log.d(TAG, "Connecting to all clients.");

                    /**prepare base settings*/
                    SendSettings sendSettings = new SendSettings();
                    sendSettings = new SendSettings();
                    sendSettings.yourPlayerNumber = 0;
                    sendSettings.numberOfPlayers = globals.getComm().playerMap.size();
                    sendSettings.playerMap = globals.getComm().playerMap;
                    sendSettings.balls = globals.getComm().balls;
                    sendSettings.gameMode = getIntent().getStringExtra("gamemode");
                    sendSettings.gravityState = getIntent().getBooleanExtra("gravitystate",false);
                    sendSettings.attractionState = getIntent().getBooleanExtra("attractionstate",false);


                    for (int i = 1; i < globals.getComm().playerMap.size(); i++) {
                        globals.getComm().sendObjectToClient(new SendSettings(sendSettings,i), "tcp",i);
                    }

                    globals.getComm().clientConnectionStatesMap.put(0,2);
                    globals.getComm().setupConnectionState = 2;

                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        searchClientsUpdateTask.cancel(true);
        Log.d(TAG, "onDestroy: UpdateTask canceled");
        final Globals globalVariables=(Globals) getApplication();
        Log.d(TAG, "onDestroy: updatethread interrupted");

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            Globals globalVariables = (Globals) getApplicationContext();
            globalVariables.getComm().shutdownServer();
            globalVariables.getComm().shutdownAllClients();

        }
        return super.onKeyDown(keyCode, event);
    }


    /********* Thread Function - Searching IP and displaying *********/
    class UpdateTask extends AsyncTask<Void,Void,Void>{

        Globals globals = (Globals) getApplicationContext();
        //ArrayAdapter<String> serverListViewAdapter;
        CustomAdapters.PlayerArrayAdapter serverListViewAdapter;

        final TextView myIpTextView = (TextView) findViewById(R.id.HostIpTextView);
        final View mView = getLayoutInflater().inflate(R.layout.dialog_choose_agent,null,false);
        final ListView availableAgentsListView = (ListView) mView.findViewById(R.id.availableAgentsListView);
        final Button selfPlayButton = (Button) mView.findViewById(R.id.selfPlayButton);

        final AlertDialog.Builder makeDialog = new AlertDialog.Builder(HostActivity.this);
        AlertDialog alertDialog;

        List<String> agentsList = new ArrayList<>();
        List<String> agentsNameList = new ArrayList<>();
        ArrayAdapter agentsAdapter = new ArrayAdapter<>(getApplication(),R.layout.item_textview, R.id.listViewtextView,agentsNameList);


        final UpdateTask updateTask = this;

        @Override
        protected void onPreExecute() {
            serverListView = (ListView) findViewById(R.id.hostFoundClientsListView);
            serverListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            serverListViewAdapter = new CustomAdapters.PlayerArrayAdapter(HostActivity.this,R.layout.item_choice_multiple,R.id.choiceMultipleTextView, globals.getComm().discoveryPlayers);
            //serverListViewAdapter = new ClientPlayerArrayAdapter(HostActivity.this, R.layout.serverlistview_row, R.id.connectionCheckedTextView,globals.getSettingsVariables().discoveryIpAdresses);

            serverListView.setAdapter(serverListViewAdapter);
            availableAgentsListView.setAdapter(agentsAdapter);
            makeDialog.setView(mView);
            alertDialog = makeDialog.create();


            selfPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent startGDXGameLauncher = new Intent(getApplication(),GDXGameLauncher.class);
                    startGDXGameLauncher.putExtra("myplayername",getIntent().getStringExtra("myplayername"));
                    startGDXGameLauncher.putExtra("myplayernumber",0);
                    startGDXGameLauncher.putExtra("numberofplayers",globals.getComm().playerMap.size());
                    startGDXGameLauncher.putExtra("numberofballs",getIntent().getIntExtra("numberofballs",1));
                    startGDXGameLauncher.putExtra("gravitystate",getIntent().getBooleanExtra("gravitystate",false));
                    startGDXGameLauncher.putExtra("attractionstate",getIntent().getBooleanExtra("attractionstate",false));
                    startGDXGameLauncher.putExtra("gamemode",getIntent().getStringExtra("gamemode"));
                    startGDXGameLauncher.putExtra("mode","normal");
                    startGDXGameLauncher.putExtra("agentmode",false);
                    alertDialog.dismiss();
                    startActivity(startGDXGameLauncher);
                    updateTask.cancel(true);
                    finish();
                }

            });

            availableAgentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    globals.setupAgent(getApplicationContext());
                    Intent startGDXGameLauncher = new Intent(getApplication(),GDXGameLauncher.class);
                    startGDXGameLauncher.putExtra("myplayername",getIntent().getStringExtra("myplayername"));
                    startGDXGameLauncher.putExtra("myplayernumber",0);
                    startGDXGameLauncher.putExtra("numberofplayers",globals.getComm().playerMap.size());
                    startGDXGameLauncher.putExtra("numberofballs",getIntent().getIntExtra("numberofballs",1));
                    startGDXGameLauncher.putExtra("gravitystate",getIntent().getBooleanExtra("gravitystate",false));
                    startGDXGameLauncher.putExtra("attractionstate",getIntent().getBooleanExtra("attractionstate",false));
                    startGDXGameLauncher.putExtra("gamemode",getIntent().getStringExtra("gamemode"));
                    startGDXGameLauncher.putExtra("mode","normal");
                    startGDXGameLauncher.putExtra("agentmode",true);
                    startGDXGameLauncher.putExtra("agentname",agentsList.get(i));
                    alertDialog.dismiss();
                    startActivity(startGDXGameLauncher);
                    updateTask.cancel(true);
                    finish();


                }
            });

            //globals.setSearchConnecState(true);


            /*serverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onItemClick: " + Integer.toString(i));
                    Log.d(TAG, "onItemClick: " + globals.getComm().discoveryPlayers.get(i).ipAdress);
                    CheckedTextView checkedTextView = (CheckedTextView) view;
                    globals.getSettingsVariables().discoveryIsChecked.setReceived(i,checkedTextView.isChecked());

                }
            });*/


        }

        @Override
        protected Void doInBackground(Void... voids) {

            //kryostuff--------------------------------------

            Log.d(TAG, "doInBackground: Anfang Suche");

            /** waiting for connection request*/
            while(globals.getComm().setupConnectionState ==0 && !isCancelled()){
                //sendHostConnect();

                try {
                    Thread.currentThread().sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                myIpAdress=getMyIpAdress(getApplicationContext());
                publishProgress();
            }


            Log.d(TAG, "doInBackground: discovery ended");

            /**wait for all clients to receive settings*/
            while(!globals.getComm().checkAllClientConnectionStates(2) && !isCancelled()) {
                Log.d(TAG, "doInBackground: not all clients in state 2");
            }

            /**send ready for game*/
            globals.getComm().sendObjectToAllClients(new SendConnectionState(0,3),"tcp");

            globals.getComm().clientConnectionStatesMap.put(0,3);

            if(!isCancelled()) {
                File agentsDir = new File(getApplication().getFilesDir().getAbsolutePath() + File.separator + "agents");
                String[] agentFiles = agentsDir.list();
                for(int i = 0; i<agentFiles.length;i++) {
                    String[] tempSplit1 = agentFiles[i].split("\\.");

                    String[] tempSplit2 = tempSplit1[0].split("_");
                    if(tempSplit2[2].equals(Integer.toString(getIntent().getIntExtra("numberofballs",1)))) {
                        agentsList.add(tempSplit1[0]);
                        agentsNameList.add(tempSplit2[0]+" (" + tempSplit2[3]+")");
                    }

                }
                agentsAdapter.notifyDataSetChanged();

                Log.d(TAG, "Game started");
            } else {
                Log.d(TAG,"skipped do in background due to cancelling");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            serverListViewAdapter.notifyDataSetChanged();

            if (checkIfIp(myIpAdress)) {
                myIpTextView.setText("Deine IP-Adresse lautet: " + myIpAdress);
            }
            else {
                myIpTextView.setText("Unable to get Ip-Adress");
            }

            myIpTextView.post(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: canceld");
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void Void) {
            alertDialog.show();

            Log.d(TAG, "onPostExecute:  UpdateTask Abgeschlossen");

        }
    }

    /********* OTHER FUNCTIONS *********/

    protected String getMyIpAdress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAdress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAdress = Integer.reverseBytes(ipAdress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAdress).toByteArray();
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

    boolean checkIfIp(String string) {
        if(string != null) {
            String[] parts = string.split("\\."); //String wird bei jedem Punkt gesplittet
            if (parts.length == 4) {                        //String muss aus 4 Teilen bestehen
                return (true);
            }
        }

        return (false);

    }


}
