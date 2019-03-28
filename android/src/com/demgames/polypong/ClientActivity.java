package com.demgames.polypong;

import com.demgames.miscclasses.SendClasses.*;
import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.ServerListener;

import android.app.AlertDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;
import android.os.Vibrator;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.List;


public class ClientActivity extends AppCompatActivity{

    private static final String TAG = "ClientActivity";
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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_client);

        final Globals globals = (Globals) getApplicationContext();

        //networkstuff
        globals.getComm().setSetupConnectionState(0);
        //setReceived temporarily to 1 != 0

        globals.getComm().resetLists();

        globals.getComm().startServerThread(new ServerListener(getApplicationContext(),getIntent().getStringExtra("myplayername"),getIntent().getStringExtra("networkmode")));
        globals.getComm().startDiscoveryClientThread(new ClientListener(getApplicationContext(),getIntent().getStringExtra("myplayername"),getIntent().getStringExtra("networkmode")));



        //--------------------------------------------------


        //Thread für den Verbindungsaufbau
        clientListUpdateTask = new ClientTask();
        clientListUpdateTask.execute();

    }



    @Override
    protected void onDestroy() {

        clientListUpdateTask.cancel(true);
        Log.d(TAG, "onDestroy: UpdateTask beendet");
        Log.d(TAG, "onDestroy: Activity geschlossen");

        super.onDestroy();
    }

    /********* OTHER FUNCTIONS *********/


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            Globals globals = (Globals) getApplicationContext();
            globals.getComm().shutdownServer();
            globals.getComm().shutdownDiscoveryClient();
            globals.getComm().shutdownAllClients();
        }
        return super.onKeyDown(keyCode, event);
    }



    /********* Thread Function - Searching IP and displaying *********/
    class ClientTask extends AsyncTask<Void,Void,Void> {

        Globals globals = (Globals) getApplicationContext();

       CustomAdapters.PlayerArrayAdapter clientListViewAdapter = new  CustomAdapters.PlayerArrayAdapter(ClientActivity.this,R.layout.item_choice_multiple,
               R.id.choiceMultipleTextView, globals.getComm().discoveryPlayers);

        TextView myIpAdressTextView = (TextView) findViewById(R.id.IpAdressTextView);
        EditText manualIpEditText = (EditText) findViewById(R.id.manualIpEditText);
        Button manualIpButton = (Button) findViewById(R.id.manualIpButton);
        String myIpAdress;

        final View mView = getLayoutInflater().inflate(R.layout.dialog_choose_agent,null,false);
        final ListView availableAgentsListView = (ListView) mView.findViewById(R.id.availableAgentsListView);
        final Button selfPlayButton = (Button) mView.findViewById(R.id.selfPlayButton);

        final AlertDialog.Builder makeDialog = new AlertDialog.Builder(ClientActivity.this);
        AlertDialog alertDialog;

        List<String> agentsList = new ArrayList<>();
        List<String> agentsNameList = new ArrayList<>();
        ArrayAdapter agentsAdapter = new ArrayAdapter<>(getApplication(),R.layout.item_textview, R.id.listViewtextView,agentsNameList);

        final ClientTask updateTask = this;

        @Override
        protected void onPreExecute() {

            loadIPAdress();

            ListView ClientListView = (ListView) findViewById(R.id.ClientListView);
            ClientListView.setAdapter(clientListViewAdapter);
            final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            ClientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //Toast.makeText(Client.this, globals.getMyIpList().get(i), Toast.LENGTH_SHORT).show();
                    vib.vibrate(50);
                    if(checkIfIp(myIpAdress)) {
                        Log.d(TAG, "onItemClick: " + Integer.toString(i));
                        Log.d(TAG, "onItemClick: " + globals.getComm().discoveryPlayers.get(i).name);
                        Toast.makeText(ClientActivity.this, "Zu \"" + globals.getComm().discoveryPlayers.get(i).name + "\" wird verbunden", Toast.LENGTH_SHORT).show();

                        //storeIPAdress();
                        globals.getComm().connectDiscoveryClient(globals.getComm().discoveryPlayers.get(i).ipAdress, myIpAdress);
                        //globals.getSettingsVariables().discoveryClient.connect(5000,globals.getSettingsVariables().discoveryIpAdresses.get(i),globals.getSettingsVariables().tcpPort,globals.getSettingsVariables().udpPort);

                        SendConnectionRequest sendConnectionRequest = new SendConnectionRequest();
                        sendConnectionRequest.myPlayerName = getIntent().getStringExtra("myplayername");
                        globals.getComm().sendDiscoveryClientObject(sendConnectionRequest, "tcp");

                        globals.getComm().setSetupConnectionState(1);
                    } else {

                    }
                    //sendClientConnect();
                }
            });

            manualIpButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(checkIfIp(manualIpEditText.getText().toString()) && checkIfIp(myIpAdress)) {
                        Toast.makeText(ClientActivity.this, "Zu \"" + manualIpEditText.getText().toString() + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                        vib.vibrate(50);
                        String manualConnectIpAdress=manualIpEditText.getText().toString();
                        //storeIP = globals.getSettingsVariables().remoteIpAdress;
                        //storeIPAdress();

                        globals.getComm().connectDiscoveryClient(manualConnectIpAdress,myIpAdress);
                        //globals.getSettingsVariables().discoveryClient.connect(5000,globals.getSettingsVariables().discoveryIpAdresses.get(i),globals.getSettingsVariables().tcpPort,globals.getSettingsVariables().udpPort);

                        globals.getComm().sendDiscoveryClientObject(new SendConnectionRequest(getIntent().getStringExtra("myplayername")),"tcp");

                        globals.getComm().setSetupConnectionState(1);
                    } else {
                        Toast.makeText(ClientActivity.this, "Enter valid Ip-adress", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            availableAgentsListView.setAdapter(agentsAdapter);
            makeDialog.setView(mView);
            alertDialog = makeDialog.create();


            selfPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent startGDXGameLauncher = new Intent(getApplicationContext(), GDXGameLauncher.class);
                    startGDXGameLauncher.putExtra("myplayername",getIntent().getStringExtra("myplayername"));
                    startGDXGameLauncher.putExtra("myplayernumber",globals.getComm().myPlayerNumber);
                    startGDXGameLauncher.putExtra("numberofplayers",globals.getComm().playerMap.size());
                    startGDXGameLauncher.putExtra("numberofballs",globals.getComm().balls.length);
                    startGDXGameLauncher.putExtra("gravitystate",globals.getComm().gravityState);
                    startGDXGameLauncher.putExtra("attractionstate",globals.getComm().attractionState);
                    startGDXGameLauncher.putExtra("gamemode",globals.getComm().gameMode);
                    startGDXGameLauncher.putExtra("agentmode",false);
                    startGDXGameLauncher.putExtra("mode","normal");


                    startActivity(startGDXGameLauncher);
                    alertDialog.dismiss();
                    updateTask.cancel(true);
                    finish();
                }

            });

            availableAgentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    globals.setupAgent(getApplicationContext());
                    Intent startGDXGameLauncher = new Intent(getApplicationContext(), GDXGameLauncher.class);
                    startGDXGameLauncher.putExtra("myplayername",getIntent().getStringExtra("myplayername"));
                    startGDXGameLauncher.putExtra("myplayernumber",globals.getComm().myPlayerNumber);
                    startGDXGameLauncher.putExtra("numberofplayers",globals.getComm().playerMap.size());
                    startGDXGameLauncher.putExtra("numberofballs",globals.getComm().balls.length);
                    startGDXGameLauncher.putExtra("gravitystate",globals.getComm().gravityState);
                    startGDXGameLauncher.putExtra("attractionstate",globals.getComm().attractionState);
                    startGDXGameLauncher.putExtra("gamemode",globals.getComm().gameMode);
                    startGDXGameLauncher.putExtra("agentmode",true);
                    startGDXGameLauncher.putExtra("mode","normal");
                    startGDXGameLauncher.putExtra("agentname",agentsList.get(i));
                    alertDialog.dismiss();
                    startActivity(startGDXGameLauncher);
                    updateTask.cancel(true);
                    finish();


                }
            });


        }


        @Override
        protected Void doInBackground(Void... voids) {
            //Background Thread

            Log.d(TAG, "doInBackground: Anfang Suche");

            List<InetAddress> discoveryHosts;

            /** start dicovery loop */
            while (globals.getComm().setupConnectionState == 0 && !isCancelled()) {
                //sendClientConnect();
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                myIpAdress= getMyIpAdress(getApplicationContext());

                discoveryHosts= globals.getComm().discoverHosts();
                String tempIpAdress;
                for (int i = 0; i < discoveryHosts.toArray().length; i++) {
                    tempIpAdress = discoveryHosts.toArray()[i].toString();
                    tempIpAdress = tempIpAdress.substring(1, tempIpAdress.length());
                    Log.d("discovered ", tempIpAdress);
                    globals.getComm().addDiscoveryHost(tempIpAdress);
                }
                for(String ipAdress : globals.getComm().discoveryHostList) {
                    globals.getComm().discoveryRequest(getIntent().getStringExtra("myplayername"),ipAdress,myIpAdress);
                }

                publishProgress();
            }


            Log.d(TAG, "doInBackground: Discovery finished, now connecting");

            /** host chosen, waiting for response */
            while(globals.getComm().setupConnectionState == 1 && !isCancelled()) {

            }

            /** response received, connecting to all clients */
            globals.getComm().shutdownDiscoveryClient();
            globals.getComm().startAllClientThreads(globals.getComm().myPlayerNumber, globals.getComm().playerMap.size(), new ClientListener(getApplicationContext(),
                    getIntent().getStringExtra("myplayername"),getIntent().getStringExtra("networkmode")));
            globals.getComm().connectAllClients(globals.getComm().playerMap,myIpAdress);

            globals.getComm().sendObjectToAllClients(new SendConnectionState(globals.getComm().myPlayerNumber,2), "tcp");
            globals.getComm().clientConnectionStatesMap.put(globals.getComm().myPlayerNumber,2);
            Log.d(TAG, "doInBackground: connectionstate 2 sent");

            /** waiting for ready of host */
            while(!(globals.getComm().clientConnectionStatesMap.get(0) ==3) && !isCancelled()) {

            }

            globals.getComm().sendObjectToAllClients(new SendConnectionState(globals.getComm().myPlayerNumber,3),"tcp");
            globals.getComm().clientConnectionStatesMap.put((globals.getComm().myPlayerNumber),3);

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

            /** start game*/
            return null;
        }



        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: Asynctask canceled");
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //Neue IP Adresse wird in die Listview geschrieben

            clientListViewAdapter.notifyDataSetChanged();

            myIpAdressTextView.post(new Runnable() {
                @Override
                public void run() {


                }
            });


            if (checkIfIp(myIpAdress)) {
                myIpAdressTextView.setText("Deine IP-Adresse lautet: " + myIpAdress);

            } else {
                myIpAdressTextView.setText("Unable to get Ip-Adress");
            }

        }

        //@Override
        protected void onPostExecute(Void Void) {
            if (!isCancelled()) {
                alertDialog.show();
            }
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

            //if (globals.getSettingsVariables().addDiscoveryIpToList(stringBuffer.toString())) {
                //globals.setUpdateListViewState(true);
            //}
            //storedIPadress.setText(stringBuffer.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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