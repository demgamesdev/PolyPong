package com.demgames.polypong;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public interface IGlobals {

    class GameVariables {

        public int numberOfBalls;

        public Vector2[] ballsPositions;
        public Vector2[] ballsVelocities;
        public int[] ballsPlayerScreens;
        public float[] ballsSizes;
        public boolean[] ballDisplayStates;
        public boolean[] updateBallStates;

        public float width, height;
        //TODO generalize to more players
        public Vector2[] batPositions;
        //public Vector2[] batVelocities = new Vector2[2];
        public float[] batOrientations;

        public float friction;

        public boolean gravityState;
        public boolean attractionState;

        public int[] playerScores;

        GameVariables() {
            numberOfBalls=1;
            friction=0.1f;

            gravityState=false;
            attractionState=true;
        }

        public void setBalls(boolean randomPosition) {
            Random rand = new Random();
            this.ballsPositions = new Vector2[this.numberOfBalls];
            this.ballsVelocities = new Vector2[this.numberOfBalls];
            this.ballsPlayerScreens = new int[this.numberOfBalls];
            this.ballsSizes = new float[this.numberOfBalls];
            this.ballDisplayStates = new boolean[this.numberOfBalls];
            this.updateBallStates = new boolean[this.numberOfBalls];


            if (randomPosition) {
                for (int i = 0; i < this.numberOfBalls; i++) {
                    this.ballsPositions[i] = this.upScaleVector(new Vector2((rand.nextFloat()-0.5f)*0.8f, (rand.nextFloat()-1f)*0.6f-0.2f));
                    this.ballsVelocities[i] = this.upScaleVector(new Vector2(0, 0));
                    this.ballsPlayerScreens[i] = 0;
                    this.ballsSizes[i] = rand.nextFloat();
                    this.ballDisplayStates[i]=true;
                    this.updateBallStates[i]=false;
                }
            }
        }

        public void setBats(int numberOfPlayers) {
            this.batPositions=new Vector2[numberOfPlayers];
            this.batOrientations= new float[numberOfPlayers];
        }

        public Vector2 downScaleVector(Vector2 vector) {
            return(new Vector2(vector.x/width,vector.y/height));
        }

        public Vector2 upScaleVector(Vector2 vector) {
            return(new Vector2(vector.x*width,vector.y*height));
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

        public int setupConnectionState =0;
        public boolean updateListViewState;

        public int myPlayerNumber;
        public int numberOfPlayers=0;

        SettingsVariables() {

            this.tcpPort=12000;
            this.udpPort=12001;

            this.resetArrayLists();

            this.updateListViewState=false;

            //TODO adapt number
            this.clientConnectionStates=new int[10];
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
            myKryo.register(Integer.class);
            myKryo.register(Integer[].class);
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
            myKryo.register(com.badlogic.gdx.math.Vector2.class);
            myKryo.register(com.badlogic.gdx.math.Vector2[].class);
            myKryo.register(SendVariables.SendClass.class);
            myKryo.register(SendVariables.SendSettings.class);
            myKryo.register(SendVariables.SendConnectionState.class);
            myKryo.register(SendVariables.SendConnectionRequest.class);
            myKryo.register(SendVariables.SendBallKinetics.class);
            myKryo.register(SendVariables.SendBallScreenChange.class);
            myKryo.register(SendVariables.SendBallGoal.class);
            myKryo.register(SendVariables.SendBat.class);
            myKryo.register(SendVariables.SendScore.class);
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
            private int tcpPort, udpPort, udpPendingSize;
            private boolean isRunnning;
            private boolean tcpPending;
            private boolean udpPending;
            private boolean connectionPending;
            private List<Object> tcpPendingObjects;
            private Object udpPendingObject;
            private SendVariables.SendClass tcpSendClass;
            private SendVariables.SendClass udpSendClass;
            private long referenceTime;
            private int updateTime;
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
                this.udpPendingSize=2;

                this.tcpPendingObjects = Collections.synchronizedList(new ArrayList());
                this.udpPendingObject = new Object();
                this.tcpSendClass = new SendVariables.SendClass();
                this.udpSendClass = new SendVariables.SendClass();


                this.updateTime = 0;
                this.referenceTime = System.currentTimeMillis();

                //this.udpPendingObjects = new ArrayList<Object>(Arrays.asList(new Object[] {}));

            }

            public void run() {
                while (this.isRunnning) {
                    if(System.currentTimeMillis() - this.referenceTime > this.updateTime) {
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
                                    this.tcpSendClass.sendObjects = this.tcpPendingObjects.toArray(new Object[0]);
                                    this.tcpPendingObjects = Collections.synchronizedList(new ArrayList());
                                }
                                this.client.sendTCP(this.tcpSendClass);
                                this.tcpPending = false;
                            }

                            if (this.udpPending) {
                                synchronized (this.udpPendingObject) {
                                    this.udpSendClass = (SendVariables.SendClass) this.udpPendingObject;
                                }
                                this.client.sendUDP(this.udpSendClass);
                                this.udpPending = false;
                            }

                        }catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        this.referenceTime = System.currentTimeMillis();
                    }

                }
                this.client.stop();

            }

            void connect(String ipAdress_) {
                this.ipAdress=ipAdress_;
                this.connectionPending=true;

            }

            public void sendObject(Object object, String protocol) {
                if(protocol.equals("tcp")) {
                    try {
                        synchronized (this.tcpPendingObjects) {
                            this.tcpPendingObjects.add(object);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    this.tcpPending=true;

                } else if(protocol.equals("udp")) {
                    try {
                        this.udpPendingObject = object;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    this.udpPending=true;
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
        static public class SendClass {
            public Object[] sendObjects;
        }

        static public class SendSettings {
            public int numberOfPlayers;
            public int yourPlayerNumber;
            public String[] ipAdresses;
            public String[] playerNames;

            public Vector2[] ballsPositions;
            public Vector2[] ballsVelocities;
            public float[] ballsSizes;
            public boolean[] ballsDisplayStates;
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

        static public class SendBallKinetics {
            public int myPlayerNumber;
            public Integer[] ballNumbers;
            public Integer[] ballPlayerFields;
            public Vector2[] ballPositions;
            public Vector2[] ballVelocities;
        }

        static public class SendBat {
            public int myPlayerNumber;
            public  int batPlayerField;
            public Vector2 batPosition;
            //public Vector2 batVelocity;
            public float batOrientation;
        }

        static public class SendBallScreenChange {
            public int myPlayerNumber;
            public Integer[] ballNumbers;
            public Integer[] ballPlayerFields;
            public Vector2[] ballPositions;
            public Vector2[] ballVelocities;
        }

        static public class SendBallGoal {
            public int myPlayerNumber;
            public Integer[] ballNumbers;
            public int[] playerScores;
        }


        static public class SendScore {
            public int myScore;
            public int otherScore;
        }
    }

    GameVariables getGameVariables();
    SettingsVariables getSettingsVariables();
}
