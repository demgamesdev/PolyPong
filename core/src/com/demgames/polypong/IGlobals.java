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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
                    this.balls[i].ballRadius = (rand.nextFloat()+0.5f)*0.03f * this.factor;
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

        public ServerThread serverThread;
        public ClientThread[] clientThreads;
        public ClientThread discoveryClientThread;

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

        SettingsVariables() {

            this.tcpPort=12000;
            this.udpPort=12001;

            this.resetArrayLists();

            this.updateListViewState=false;

            //TODO adapt number
            this.clientConnectionStates=new int[10];
            this.hasFocus=true;
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

        public void sendObjectToAllClients(Object object, String protocol) {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].addObjectToProtocolSendList(object, protocol);
                }
            }
        }

        public void sendFrequentBallToAllClient(ClassicGameObjects.Ball ball) {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].addToFrequentBallsMap(ball);
                }
            }
        }

        public void sendFieldChangeBallToAllClients(ClassicGameObjects.Ball ball) {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].addToFieldChangeBallsMap(ball);
                }
            }
        }

        public void sendFrequentInfoToAllClients(ClassicGameObjects.Bat bat, ConcurrentHashMap<Integer,Integer> ballDisplayStatesMap, int[] scores) {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].sendFrequentInfo(bat,ballDisplayStatesMap, scores);
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

        public void resetArrayLists() {
            this.discoveryIpAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.ipAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.playerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryPlayerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryIsChecked =new ArrayList<Boolean>(Arrays.asList(new Boolean[]{}));
            this.playerList = new ArrayList<Player>();
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
            private boolean connectionPending;
            private long sendFrequentBallsReferenceTime,sendFieldChangeBallsReferenceTime,sendFrequentInfoReferenceTime;
            private int sendFrequentBallsTimer,sendFieldChangeBallsTimer,sendFrequentInfoTimer;

            private List<Object> tcpPendingObjects;
            private List<Object> udpPendingObjects;
            private boolean tcpPending;
            private boolean udpPending;

            private SendVariables.SendFrequentBalls sendFrequentBalls;
            private SendVariables.SendFieldChangeBalls sendFieldChangeBalls;
            private SendVariables.SendFrequentInfo sendFrequentInfo;

            private ConcurrentHashMap<Integer, Ball> frequentBallsMap;
            private ConcurrentHashMap<Integer, Ball> fieldChangeBallsMap;

            private final Object sendFrequentBallsThreadLock = new Object();
            private final Object sendFieldChangeBallsThreadLock = new Object();
            private final Object sendFrequentInfoThreadLock = new Object();


            private boolean frequentBallsPending;
            private boolean fieldChangeBallsPending;
            private boolean frequentInfoPending;

            private String threadName;

            public ClientThread(String threadName_, int tcpPort_, int udpPort_) {
                this.threadName=threadName_;
                this.client = new Client(10240,10240);
                this.client.start();

                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;
                this.isRunnning=true;
                this.connectionPending=false;

                this.sendFrequentBallsReferenceTime = System.currentTimeMillis();
                this.sendFieldChangeBallsReferenceTime = System.currentTimeMillis();
                this.sendFrequentInfoReferenceTime = System.currentTimeMillis();

                this.sendFrequentBallsTimer = 50;
                this.sendFieldChangeBallsTimer = 50;
                this.sendFrequentInfoTimer = 50;

                this.tcpPendingObjects = new ArrayList<Object>();
                this.udpPendingObjects = new ArrayList<Object>();
                this.tcpPending = false;
                this.udpPending = false;

                this.sendFrequentBalls = new SendVariables.SendFrequentBalls();
                this.sendFieldChangeBalls = new SendVariables.SendFieldChangeBalls();
                this.sendFrequentInfo = new SendVariables.SendFrequentInfo();


                this.frequentBallsMap = new ConcurrentHashMap();
                this.fieldChangeBallsMap = new ConcurrentHashMap();

                this.frequentBallsPending = false;
                this.fieldChangeBallsPending = false;
                this.frequentInfoPending = false;



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
                            synchronized (sendThreadLock) {
                                for(int i=0;i<this.tcpPendingObjects.size();i++){
                                    this.client.sendTCP(this.tcpPendingObjects.get(i));
                                }
                                this.tcpPendingObjects = new ArrayList();
                                this.tcpPending = false;
                            }
                        }

                        if (this.udpPending) {
                            synchronized (sendThreadLock) {
                                for(int i=0;i<this.udpPendingObjects.size();i++){
                                    this.client.sendUDP(this.udpPendingObjects.get(i));
                                }
                                this.tcpPendingObjects = new ArrayList();
                                this.udpPending = false;
                            }
                        }

                        if(this.fieldChangeBallsPending){
                            if(System.currentTimeMillis() - this.sendFieldChangeBallsReferenceTime > this.sendFieldChangeBallsTimer) {
                                synchronized (this.sendFieldChangeBallsThreadLock) {
                                    this.sendFieldChangeBalls.myPlayerNumber = myPlayerNumber;
                                    this.sendFieldChangeBalls.fieldChangeBallsMap = this.fieldChangeBallsMap;

                                    this.client.sendTCP(this.sendFieldChangeBalls);
                                    this.fieldChangeBallsMap.clear();
                                    this.fieldChangeBallsPending = false;
                                }
                                this.sendFieldChangeBallsReferenceTime = System.currentTimeMillis();
                            }
                        }

                        if(this.frequentBallsPending){
                            if(System.currentTimeMillis() - this.sendFrequentBallsReferenceTime > this.sendFrequentBallsTimer) {
                                synchronized(this.sendFrequentBallsThreadLock) {
                                    this.sendFrequentBalls.myPlayerNumber = myPlayerNumber;
                                    this.sendFrequentBalls.frequentBallsMap = this.frequentBallsMap;

                                    this.client.sendUDP(this.sendFrequentBalls);
                                    this.frequentBallsMap.clear();
                                    this.frequentBallsPending = false;
                                }
                                this.sendFrequentBallsReferenceTime = System.currentTimeMillis();
                            }
                        }

                        if(this.frequentInfoPending) {
                            if(System.currentTimeMillis() - this.sendFrequentInfoReferenceTime > this.sendFrequentInfoTimer) {
                                synchronized(this.sendFrequentInfoThreadLock) {

                                    this.client.sendUDP(this.sendFrequentInfo);
                                    this.frequentInfoPending = false;
                                }
                                this.sendFrequentInfoReferenceTime = System.currentTimeMillis();
                            }
                        }

                    }catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
                this.client.stop();

            }

            void connect(String ipAdress_) {
                this.ipAdress=ipAdress_;
                this.connectionPending=true;

            }

            public void addObjectToProtocolSendList(Object object, String protocol) {
                try {
                    if (protocol.equals("tcp")) {
                        synchronized (sendThreadLock) {
                            this.tcpPendingObjects.add(object);
                            this.tcpPending = true;
                        }

                    } else if (protocol.equals("udp")) {
                        synchronized (sendThreadLock) {
                            this.udpPendingObjects.add(object);
                            this.udpPending = true;
                        }

                    }
                }catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            public void addToFrequentBallsMap(ClassicGameObjects.Ball ball){
                synchronized (this.sendFrequentBallsThreadLock) {
                    Ball tempBall = new Ball();
                    tempBall.ballNumber = new Integer(ball.ballNumber);
                    tempBall.ballPlayerField = new Integer(myPlayerNumber);
                    tempBall.ballDisplayState = new Integer(ball.ballDisplayState);

                    if (tempBall.ballDisplayState == 1) {
                        tempBall.ballPosition = new Vector2(ball.ballBody.getPosition());
                        tempBall.ballVelocity = new Vector2(ball.ballBody.getLinearVelocity());
                        tempBall.ballAngle = new Float(ball.ballBody.getAngle());
                        tempBall.ballAngularVelocity = new Float(ball.ballBody.getAngularVelocity());
                    }

                    this.frequentBallsMap.put(tempBall.ballNumber, tempBall);
                    this.frequentBallsPending = true;
                }
            }

            public void addToFieldChangeBallsMap(ClassicGameObjects.Ball ball){
                synchronized (this.sendFieldChangeBallsThreadLock) {
                    Ball tempBall = new Ball();
                    tempBall.ballNumber = new Integer(ball.ballNumber);
                    tempBall.ballPlayerField = new Integer(ball.tempPlayerField);
                    tempBall.ballDisplayState = new Integer(ball.ballDisplayState);

                    tempBall.ballPosition = new Vector2(ball.ballBody.getPosition());
                    tempBall.ballVelocity = new Vector2(ball.ballBody.getLinearVelocity());
                    tempBall.ballAngle = new Float(ball.ballBody.getAngle());
                    tempBall.ballAngularVelocity = new Float(ball.ballBody.getAngularVelocity());

                    this.fieldChangeBallsMap.put(tempBall.ballNumber, tempBall);
                    this.fieldChangeBallsPending = true;
                }

            }

            public void sendFrequentInfo(ClassicGameObjects.Bat bat, ConcurrentHashMap<Integer,Integer> ballDisplayStatesMap, int[] scores){
                synchronized (this.sendFrequentInfoThreadLock) {
                    this.sendFrequentInfo.myPlayerNumber = new Integer(myPlayerNumber);

                    this.sendFrequentInfo.bat = new Bat();
                    this.sendFrequentInfo.bat.batPosition = new Vector2(bat.batBody.getPosition());
                    this.sendFrequentInfo.bat.batVelocity = new Vector2(bat.batBody.getLinearVelocity());
                    this.sendFrequentInfo.bat.batAngle = new Float(bat.batBody.getAngle());
                    this.sendFrequentInfo.bat.batAngularVelocity = new Float(bat.batBody.getAngularVelocity());

                    this.sendFrequentInfo.ballDisplayStatesMap = new ConcurrentHashMap<Integer, Integer>(ballDisplayStatesMap);

                    this.sendFrequentInfo.scores = new int[scores.length];
                    for (int i = 0; i < scores.length; i++) {
                        this.sendFrequentInfo.scores[i] = scores[i];
                    }
                    this.frequentInfoPending = true;
                }

            }

            public void sendDicoveryRequest(String ipAdress_, SendVariables.SendDiscoveryRequest discoveryRequest) {
                try {
                    this.client.connect(5000, ipAdress_, this.tcpPort, this.udpPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.client.sendUDP(discoveryRequest);
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
