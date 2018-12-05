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

public interface IGlobals {
    static final String TAG = "IGlobals";

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
                    this.balls[i].ballRadius = (rand.nextFloat()+0.5f)*0.03f * this.factor;
                    this.balls[i].ballPositionX = (rand.nextFloat()-0.5f)*0.8f * this.factor;
                    this.balls[i].ballPositionY = ((rand.nextFloat()-1f)*0.6f-0.2f)*this.factor;
                    this.balls[i].ballVelocityX = 0;
                    this.balls[i].ballVelocityY = 0;
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

        public ServerThread serverThread;
        public ClientThread[] clientThreads;
        public ClientThread discoveryClientThread;

        public int gameMode;
        public boolean hasFocus;

        public int setupConnectionState =0;
        public boolean updateListViewState;

        public Object threadObjectLock;

        public int myPlayerNumber;
        public int numberOfPlayers;

        SettingsVariables() {

            this.tcpPort=12000;
            this.udpPort=12001;

            this.resetArrayLists();

            this.updateListViewState=false;

            //TODO adapt number
            this.clientConnectionStates=new int[10];
            this.hasFocus=true;

            this.threadObjectLock = new Object();
        }

        public void startServerThread() {
            this.serverThread = new ServerThread("serverThread",this.tcpPort,this.udpPort);
            this.registerKryoClasses(this.serverThread.getServer().getKryo());
            this.serverThread.start();
            this.serverThread.bind();

        }

        public void startDiscoveryClientThread() {
            this.discoveryClientThread = new ClientThread("discoveryClientThread",this.tcpPort,this.udpPort);
            this.registerKryoClasses(this.discoveryClientThread.getClient().getKryo());
            this.discoveryClientThread.start();
        }

        public void connectDiscoveryClient(String ipAdress_) {
            this.discoveryClientThread.connect(ipAdress_);
        }

        public void startAllClientThreads() {
            this.clientThreads = new ClientThread[this.numberOfPlayers];
            for(int i=0; i<this.numberOfPlayers; i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i] = new ClientThread("clientThread "+i,this.tcpPort,this.udpPort);
                    this.registerKryoClasses(this.clientThreads[i].getClient().getKryo());
                    this.clientThreads[i].start();
                }
            }
        }

        public void setAllClientListeners(Listener listener) {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].getClient().addListener(listener);
                }
            }
        }

        public void connectAllClients() {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].connect(this.ipAdresses.get(i));
                    //this.clientThreads[i].start();

                }
            }
        }

        public void sendToAllClients(Object object, String protocol) {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].sendObject(object, protocol);
                }
            }
        }

        public void shutdownAllClients() {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].shutdownClient();
                }
            }
        }

        public boolean checkAllClientConnectionStates(int state) {
            if(this.clientThreads==null){
                return(false);
            }
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(!(this.clientConnectionStates[i]==state)) {
                    com.esotericsoftware.minlog.Log.debug("client"+i+" not in state "+state);
                    return(false);
                }
            }
            com.esotericsoftware.minlog.Log.debug("all clients in state "+state);
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
            myKryo.register(SendVariables.SendSettings.class);
            myKryo.register(SendVariables.SendConnectionState.class);
            myKryo.register(SendVariables.SendConnectionRequest.class);
            myKryo.register(SendVariables.SendFrequents.class);
            myKryo.register(SendVariables.SendFieldChange.class);
        }

        public boolean addDiscoveryIpToList(String IpAdress){
            if(!this.discoveryIpAdresses.contains(IpAdress)){
                this.discoveryIpAdresses.add(IpAdress);
                this.discoveryIsChecked.add(false);
                this.updateListViewState=true;
                //Log.d("addiptolist",IpAdress +" added");
                return(true);
            }
            return(false);
        }

        public boolean addDiscoveryPlayerNameToList(String playerName){
            this.discoveryPlayerNames.add(playerName);
            return(false);
        }

        public void resetArrayLists() {
            this.discoveryIpAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.ipAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.playerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryPlayerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryIsChecked =new ArrayList<Boolean>(Arrays.asList(new Boolean[]{}));
        }

        public class ServerThread extends Thread {
            private Server server;
            private String ipAdress;
            private int tcpPort, udpPort, udpPendingSize;
            private boolean isRunnning;
            private boolean tcpPending;
            private boolean udpPending;
            private boolean bindPending;

            private String threadName;

            public ServerThread(String threadName_, int tcpPort_, int udpPort_) {
                this.threadName=threadName_;
                this.server = new Server(10240,10240);
                this.server.start();

                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;
                this.isRunnning=true;
                this.bindPending=false;

            }

            public void run() {
                while (this.isRunnning) {
                    if(this.bindPending) {
                        try {
                            this.server.bind(this.tcpPort, this.udpPort);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        this.bindPending = false;
                    }
                }
                this.server.stop();

            }

            public void bind() {
                this.bindPending=true;
            }

            public void shutdownServer() {
                this.isRunnning=false;
            }

            public Server getServer() {
                return(this.server);
            }
        }

        public class ClientThread extends Thread {
            private Client client;
            private String ipAdress;
            private int tcpPort, udpPort;
            private boolean isRunnning;
            private boolean tcpPending;
            private boolean udpPending;
            private boolean connectionPending;
            private List<Object> tcpPendingObjects;
            private Object udpPendingObject;
            private long referenceTime;
            private int udpSendTimer;
            //private ArrayList<Object> udpPendingObjects;
            //private ArrayList<Object> tempUdpPendingObjects;

            private String threadName;

            public ClientThread(String threadName_, int tcpPort_, int udpPort_) {
                this.threadName=threadName_;
                this.client = new Client(10240,10240);
                this.client.start();

                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;
                this.isRunnning=true;
                this.tcpPending=false;
                this.udpPending=false;
                this.connectionPending=false;

                this.tcpPendingObjects = new ArrayList<Object>();
                this.udpPendingObject = new Object();


                this.udpSendTimer = 100;
                this.referenceTime = System.currentTimeMillis();

                //this.udpPendingObjects = new ArrayList<Object>(Arrays.asList(new Object[] {}));

            }

            public void run() {
                while (this.isRunnning) {
                    if (this.connectionPending) {
                        try {
                            this.client.connect(5000, this.ipAdress, this.tcpPort, this.udpPort);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        this.connectionPending = false;
                    }
                    try {
                        if (this.tcpPending) {
                            synchronized (this.tcpPendingObjects) {
                                this.tcpPending = false;
                                for(int i=0;i<this.tcpPendingObjects.size();i++){
                                    this.client.sendTCP(this.tcpPendingObjects.get(i));
                                }
                                this.tcpPendingObjects = new ArrayList();
                            }
                        }
                        if(System.currentTimeMillis() - this.referenceTime > this.udpSendTimer) {
                            if (this.udpPending) {
                                synchronized (this.udpPendingObject) {
                                    this.udpPending = false;
                                    SendVariables.SendFrequents sendFrequents = (SendVariables.SendFrequents)this.udpPendingObject;
                                    for(int i=0;i<sendFrequents.balls.length;i++){
                                        Gdx.app.debug(TAG, "sendfrequents ball " + Integer.toString(sendFrequents.balls[i].ballNumber));
                                    }
                                    this.client.sendUDP(this.udpPendingObject);
                                }

                                this.referenceTime = System.currentTimeMillis();
                            }
                        }

                    }catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                }
                this.client.stop();

            }

            void connect(String ipAdress_) {
                this.ipAdress=ipAdress_;
                this.connectionPending=true;

            }

            public void sendObject(Object object, String protocol) {
                try {
                    if (protocol.equals("tcp")) {
                        synchronized (this.tcpPendingObjects) {
                            this.tcpPendingObjects.add(object);
                            this.tcpPending = true;
                        }

                    } else if (protocol.equals("udp")) {
                        synchronized (this.udpPendingObject) {
                            this.udpPendingObject = object;
                            this.udpPending = true;
                        }

                    }
                }catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            public void shutdownClient() {
                this.isRunnning=false;
            }

            public Client getClient() {
                return(this.client);
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

        static public class SendConnectionRequest {
            public String myPlayerName;
        }

        static public class SendFrequents {
            public int myPlayerNumber;
            public Ball[] balls;
            public Bat bat;
            public int[] scores;
        }

        static public class SendFieldChange {
            public int myPlayerNumber;

            public Ball[] balls;
            /*public int[] ballNumbers;
            public int[] ballPlayerFields;
            public Vector2[] ballPositions;
            public Vector2[] ballVelocities;*/
        }
    }

    class Ball{
        public int ballNumber;
        public int ballPlayerField;

        public float ballRadius;
        public int ballDisplayState;
        public float ballPositionX;
        public float ballPositionY;
        public float ballVelocityX;
        public float ballVelocityY;
        public float ballAngle;
        public float ballAngularVelocity;

        Ball() {

        }
    }

    class Bat{
        public float batPositionX;
        public float batPositionY;
        public float batVelocityX;
        public float batVelocityY;
        public float batAngle;
        public float batAngularVelocity;

        Bat() {

        }
    }

    GameVariables getGameVariables();
    SettingsVariables getSettingsVariables();
}
