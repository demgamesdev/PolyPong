package com.demgames.polypong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public interface IGlobals {
    String TAG = "IGlobals";

    class GameVariables {

        public int numberOfBalls;

        public float width, height;
        public int gameState;

        public Ball[] balls;
        public Bat[] bats;

        public int[] ballPlayerFields;
        public boolean[] ballUpdateStates;
        public boolean[] batUpdateStates;

        public float friction;

        public boolean gravityState;
        public boolean attractionState;

        public int[] playerScores;

        private float factor =1;

        public int myPlayerNumber;
        public int numberOfPlayers;

        GameVariables() {
            this.gameState = 0;
            friction=0.1f;

            gravityState=false;
            attractionState=false;
        }

        public void setBalls(boolean randomPosition) {
            Random rand = new Random();
            this.balls = new Ball[this.numberOfBalls];
            this.ballPlayerFields = new int[this.numberOfBalls];
            this.ballUpdateStates = new boolean[this.numberOfBalls];

            for (int i = 0; i < this.numberOfBalls; i++) {
                this.balls[i] = new Ball();
                if (randomPosition) {
                    this.balls[i].ballNumber = i;
                    this.balls[i].ballRadius = (rand.nextFloat()*0.2f+0.8f)*0.05f * this.factor;
                    this.balls[i].ballPosition = new Vector2((rand.nextFloat()-0.5f)*0.8f * this.factor,((rand.nextFloat()-1f)*0.6f-0.2f)*this.factor);
                    this.balls[i].ballVelocity = new Vector2(0,0);
                    this.balls[i].ballAngle = 0f;
                    this.balls[i].ballAngularVelocity = 0f;

                }
                //this.balls[i].ballDisplayState=1;
                this.ballUpdateStates[i]=false;
            }
        }

        public void setBats() {
            this.bats = new Bat[this.numberOfPlayers];
            this.batUpdateStates = new boolean[this.numberOfPlayers];

            for (int i = 0; i < this.numberOfPlayers; i++) {
                this.bats[i]= new Bat();
                this.batUpdateStates[i] = false;
            }
        }
    }


    class SettingsVariables {
        public String myPlayerName;
        public String myIpAdress;
        public String manualConnectIpAdress;
        int tcpPort,udpPort;

        public List<String> discoveryIpAdresses;
        public List<Boolean> discoveryIsChecked;
        public List<String> ipAdresses;
        public List<String> playerNames;
        public List<String> discoveryPlayerNames;
        public int[] clientConnectionStates;
        private SendVariables.SendDiscoveryRequest discoveryRequest;

        public ServerRunnable serverRunnable;
        public ClientRunnable[] clientRunnables;
        public ClientRunnable discoveryClientRunnable;

        public ThreadPoolExecutor networkThreadPool;

        public List<Player> playerList;

        public int gameMode;
        public boolean hasFocus;

        public int setupConnectionState =0;
        public boolean updateListViewState;

        public static final Object receiveThreadLock = new Object();
        public static final Object sendThreadLock = new Object();
        public static final Object connectionThreadLock = new Object();

        public int myPlayerNumber;
        public int numberOfPlayers;

        public int testNumber;

        SettingsVariables() {

            this.tcpPort=12000;
            this.udpPort=12001;

            this.resetObjects();

            this.updateListViewState=false;

            //TODO adapt number
            this.clientConnectionStates=new int[10];
            this.hasFocus=true;

            this.discoveryRequest = new SendVariables.SendDiscoveryRequest();

            this.networkThreadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();

            this.testNumber = 0;
        }

        public void setMyPlayerName(String myPlayerName_){
            this.myPlayerName = myPlayerName_;
            this.discoveryRequest.myPlayerName = myPlayerName_;
        }

        public void startServerThread() {
            this.serverRunnable = new ServerRunnable();
            this.registerKryoClasses(this.serverRunnable.getServer().getKryo());
            this.serverRunnable.getServer().start();//startServer();
            //this.networkThreadPool.submit(this.serverRunnable);
            this.serverRunnable.setupServerBind(this.tcpPort,this.udpPort);
            this.networkThreadPool.submit(this.serverRunnable);
        }

        public void startDiscoveryClientThread() {
            this.discoveryClientRunnable = new ClientRunnable();
            this.registerKryoClasses(this.discoveryClientRunnable.getClient().getKryo());
            this.discoveryClientRunnable.getClient().start();//.startClient();
            //this.networkThreadPool.submit(this.discoveryClientRunnable);
        }

        public void connectDiscoveryClient(String ipAdress_) {
            this.discoveryClientRunnable.setupClientConnection(ipAdress_, this.tcpPort, this.udpPort);
            this.networkThreadPool.submit(this.discoveryClientRunnable);
        }

        public void discoveryRequest(String ipAdress_) {
            if(!ipAdress_.equals("127.0.0.1") && !ipAdress_.equals(myIpAdress)) {
                this.discoveryClientRunnable.setupDiscoveryRequest(ipAdress_, this.tcpPort, this.udpPort, this.discoveryRequest);
                this.networkThreadPool.submit(this.discoveryClientRunnable);
            }
        }

        public void startAllClientThreads() {
            com.esotericsoftware.minlog.Log.debug("number of players "+this.numberOfPlayers);
            this.clientRunnables = new ClientRunnable[this.numberOfPlayers];
            for(int i=0; i<this.numberOfPlayers; i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientRunnables[i] = new ClientRunnable();
                    this.registerKryoClasses(this.clientRunnables[i].getClient().getKryo());
                    this.clientRunnables[i].getClient().start();//.startClient();
                    com.esotericsoftware.minlog.Log.debug("client "+i + "started");
                    this.testNumber = i;
                    //this.networkThreadPool.submit(this.clientRunnables[i]);
                }
            }
        }

        public void setAllClientListeners(Listener listener) {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clientRunnables[i].getClient().addListener(listener);
                }
            }
        }

        public void connectAllClients() {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientRunnables[i].setupClientConnection(this.ipAdresses.get(i), this.tcpPort, this.udpPort);
                    this.networkThreadPool.submit(this.clientRunnables[i]);
                    com.esotericsoftware.minlog.Log.debug("client "+i + " connected");
                    //this.clientThreads[i].start();

                }
            }
        }

        public void sendObjectClient(ClientRunnable clientRunnable_, Object object, String protocol) {
            if(protocol.equals("tcp")) {
                clientRunnable_.setupClientTCPSend(object);
                this.networkThreadPool.submit(clientRunnable_);

            } else if (protocol.equals("udp")) {
                clientRunnable_.setupClientUDPSend(object);
                this.networkThreadPool.submit(clientRunnable_);
            }
        }

        public void sendObjectToAllClients(Object object, String protocol) {
            if(protocol.equals("tcp")) {
                for(int i=0;i<this.numberOfPlayers;i++) {
                    if(i!=this.myPlayerNumber) {
                        this.clientRunnables[i].setupClientTCPSend(object);
                        this.networkThreadPool.submit(this.clientRunnables[i]);
                    }
                }
            } else if (protocol.equals("udp")) {
                for(int i=0;i<this.numberOfPlayers;i++) {
                    if(i!=this.myPlayerNumber) {
                        this.clientRunnables[i].setupClientUDPSend(object);
                        this.networkThreadPool.submit(this.clientRunnables[i]);
                    }
                }
            }
        }

        public void shutdownAllClients() {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clientRunnables[i].getClient().stop();
                }
            }
        }

        public boolean checkAllClientConnectionStates(int state) {
            if(this.clientRunnables ==null){
                return(false);
            }
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(!(this.clientConnectionStates[i]==state)) {
                    com.esotericsoftware.minlog.Log.debug("client"+i+" not in state "+state);
                    return(false);
                }
            }
            com.esotericsoftware.minlog.Log.debug("all clientRunnables in state "+state);
            return(true);
        }

        public void registerKryoClasses(Kryo myKryo) {
            myKryo.register(float.class);
            myKryo.register(float[].class);
            myKryo.register(int.class);
            myKryo.register(int[].class);
            myKryo.register(String.class);
            myKryo.register(String[].class);
            myKryo.register(boolean.class);
            myKryo.register(boolean[].class);
            myKryo.register(Connection.class);
            myKryo.register(Connection[].class);
            myKryo.register(Object.class);
            myKryo.register(Object[].class);
            myKryo.register(Ball.class);
            myKryo.register(Ball[].class);
            myKryo.register(Bat.class);
            myKryo.register(Bat[].class);
            myKryo.register(com.badlogic.gdx.math.Vector2.class);
            myKryo.register(com.badlogic.gdx.math.Vector2[].class);
            myKryo.register(ConcurrentHashMap.class);
            myKryo.register(SendVariables.SendSettings.class);
            myKryo.register(SendVariables.SendConnectionState.class);
            myKryo.register(SendVariables.SendConnectionRequest.class);
            myKryo.register(SendVariables.SendDiscoveryRequest.class);
            myKryo.register(SendVariables.SendDiscoveryResponse.class);
            myKryo.register(SendVariables.SendFrequentBalls.class);
            myKryo.register(SendVariables.SendFrequentInfo.class);
            myKryo.register(SendVariables.SendFieldChangeBalls.class);
        }

        public boolean addDiscoveryIpToList(String IpAdress){
            if(!this.discoveryIpAdresses.contains(IpAdress)){
                this.discoveryIpAdresses.add(IpAdress);
                this.discoveryIsChecked.add(false);
                //Log.d("addiptolist",IpAdress +" added");
                return(true);
            }
            return(false);
        }

        public boolean addPlayerToList(Player player){
            for(int i=0;i<this.playerList.size();i++) {
                //Gdx.app.debug(TAG,player.ipAdress + " vs " + this.playerList.get(i).ipAdress);
                if(player.ipAdress.equals(this.playerList.get(i).ipAdress)) {
                    return(false);
                }
            }
            this.playerList.add(player);
            return(true);
        }

        public boolean addDiscoveryPlayerNameToList(String playerName){
            this.discoveryPlayerNames.add(playerName);
            return(false);
        }

        public void resetObjects() {
            this.discoveryIpAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.ipAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.playerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryPlayerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryIsChecked =new ArrayList<Boolean>(Arrays.asList(new Boolean[]{}));
            this.playerList = new ArrayList<Player>();

            this.networkThreadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        }

        public final class ServerRunnable implements Runnable{
            private int tcpPort;
            private int udpPort;
            private Server server;

            private String runState;

            ServerRunnable() {
                this.server = new Server(10240,10240);
                this.runState ="";
            }


            @Override
            public void run() {
                if (this.runState.equals("serverstart")){
                    this.server.start();

                } else if(this.runState.equals("serverbind")){
                    try{
                        this.server.bind(this.tcpPort, this.udpPort);
                    }catch (IOException e ) {
                        e.printStackTrace();
                    }

                }

            }
            public synchronized void startServer(){
                this.runState = "serverstart";
            }

            public synchronized void setupServerBind(int tcpPort_, int udpPort_){
                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;
                this.runState = "serverbind";
            }

            public Server getServer() {
                return this.server;
            }
        }

        public final class ClientRunnable implements Runnable{
            private Client client;
            private int tcpPort;
            private int udpPort;
            private String ipAdress;
            private Object sendObject;

            private String runState;

            ClientRunnable() {
                this.client = new Client(10240,10240);
                this.runState ="";
            }


            @Override
            public void run() {
                if (this.runState.equals("clientstart")){
                    this.client.start();

                } else if (this.runState.equals("clientconnection")){
                    try{
                        this.client.connect(5000, this.ipAdress, this.tcpPort, this.udpPort);
                    }catch (IOException e ) {
                        e.printStackTrace();
                    }

                } else if (this.runState.equals("clientsendtcp")){
                    try{
                        this.client.connect(5000, this.ipAdress, this.tcpPort, this.udpPort);
                    }catch (IOException e ) {
                        e.printStackTrace();
                    }
                    this.client.sendTCP(this.sendObject);

                } else if (this.runState.equals("clientsendudp")){
                    this.client.sendUDP(this.sendObject);

                }else if (this.runState.equals("clientdiscoveryrequest")){
                    try{
                        this.client.connect(5000, this.ipAdress, this.tcpPort, this.udpPort);
                    }catch (IOException e ) {
                        e.printStackTrace();
                    }
                    this.client.sendTCP(this.sendObject);

                }

            }

            public synchronized void startClient(){
                this.runState = "clientstart";
            }

            public synchronized void setupClientConnection(String ipAdress_, int tcpPort_, int udpPort_){
                this.ipAdress = ipAdress_;
                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;

                this.runState = "clientconnection";
            }

            public synchronized void setupClientTCPSend(Object object_) {
                this.sendObject = object_;

                this.runState = "clientsendtcp";
            }

            public synchronized void setupClientUDPSend(Object object_) {
                this.sendObject = object_;

                this.runState = "clientsendudp";
            }

            public synchronized void setupDiscoveryRequest(String ipAdress_, int tcpPort_, int udpPort_, Object object_) {
                this.ipAdress = ipAdress_;
                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;

                this.sendObject = object_;
                this.runState = "clientdiscoveryrequest";
            }

            public Client getClient() {
                return this.client;
            }
        }
    }

    class SendVariables {
        static public class SendSettings {
            public int numberOfPlayers;
            public int yourPlayerNumber;
            public String[] ipAdresses;
            public String[] playerNames;

            public Ball[] balls;

            public int gameMode;
            public boolean gravityState;
            public boolean attractionState;
        }

        static public class SendConnectionState {
            public int myPlayerNumber;
            public int connectionState;
        }

        static public class SendDiscoveryRequest {
            public String myPlayerName;
        }

        static public class SendDiscoveryResponse {
            public String myPlayerName;
        }

        static public class SendConnectionRequest {
            public String myPlayerName;
        }

        static public class SendFrequentBalls {
            public int myPlayerNumber;

            public ConcurrentHashMap<Integer, Ball> frequentBallsMap;
        }

        static public class SendFrequentInfo {
            public int myPlayerNumber;
            public Bat bat;
            public ConcurrentHashMap<Integer,Integer> ballDisplayStatesMap;
            public int[] scores;
        }



        static public class SendFieldChangeBalls {
            public int myPlayerNumber;

            public ConcurrentHashMap<Integer, Ball> fieldChangeBallsMap;
        }
    }

    class Ball{
        public int ballNumber;
        public int ballPlayerField;

        public float ballRadius;
        public int ballDisplayState;
        public Vector2 ballPosition;
        public Vector2 ballVelocity;
        public float ballAngle;
        public float ballAngularVelocity;
    }

    class Bat{
        public Vector2 batPosition;
        public Vector2 batVelocity;
        public float batAngle;
        public float batAngularVelocity;
    }

    class Player {
        public String name;
        public String ipAdress;
    }

    GameVariables getGameVariables();
    SettingsVariables getSettingsVariables();
}
