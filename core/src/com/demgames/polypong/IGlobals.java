package com.demgames.polypong;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IGlobals {
    String TAG = "IGlobals";

    class GameVariables {

        public int numberOfBalls;

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

        public int myPlayerNumber;
        public int numberOfPlayers;

        public List<double[]> inputs= new ArrayList<double[]>();
        public List<double[]> outputs = new ArrayList<double[]>();

        public MultiLayerNetwork model;
        public boolean aiState=false;

        GameVariables() {
            this.gameState = 0;
            friction=0.1f;

            gravityState=false;
            attractionState=false;
        }

        public void setBalls(boolean setupState) {
            Random rand = new Random();
            this.balls = new Ball[this.numberOfBalls];
            this.ballPlayerFields = new int[this.numberOfBalls];
            this.ballUpdateStates = new boolean[this.numberOfBalls];

            for (int i = 0; i < this.numberOfBalls; i++) {
                this.balls[i] = new Ball();
                if (setupState) {
                    this.balls[i].ballNumber = i;
                    this.balls[i].ballPlayerField = i%numberOfPlayers;
                    //this.balls[i].ballRadius = (rand.nextFloat()*0.2f+0.8f)*0.05f * this.factor;
                    this.balls[i].ballRadius = 0.04f;
                    this.balls[i].ballPosition = new Vector2(0,0);
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
                this.bats[i].batPosition = new Vector2(0,0);
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

        public ServerThread serverThread;
        public ClientThread[] clientThreads;
        public ClientThread discoveryClientThread;

        public List<Player> playerList;

        public String gameMode;
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

            this.discoveryRequest = new SendVariables.SendDiscoveryRequest();
        }

        public void setMyPlayerName(String myPlayerName_){
            this.myPlayerName = myPlayerName_;
            this.discoveryRequest.myPlayerName = myPlayerName_;
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

        public void sendBallToAllClients(ClassicGameObjects.Ball ball) {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i].addToBallsMap(ball);
                }
            }
        }

        public void sendInfoToAllClients(ClassicGameObjects.Bat bat, ConcurrentHashMap<Integer,Integer> ballDisplayStatesMap, int[] scores) {
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
            myKryo.register(SendVariables.SendGameInfo.class);
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
                //Gdx.app.debug(TAG,player.connectionIpAdress + " vs " + this.playerList.get(i).connectionIpAdress);
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
            private int tcpPort, udpPort;
            private boolean isRunnning;
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
            private String connectionIpAdress;
            private int tcpPort, udpPort;
            private boolean isRunnning;

            private long connectionReferenceTime,discoveryReferenceTime, sendGameInfoReferenceTime;
            private int connectionTimer,discoveryTimer, sendGameInfoTimer;

            private List<Object> tcpPendingObjects;
            private List<Object> udpPendingObjects;
            private List<String> discoveryList;

            private boolean connectionPending;
            private boolean tcpPending;
            private boolean udpPending;
            private boolean discoveryPending;


            private boolean sendGameInfoTcp;
            private SendVariables.SendGameInfo sendGameInfo;

            private Multimap<String,Object> sendMultiMap;
            private Map<String,Object> sendInfoMap;
            private Map<Integer,Object> sendBallsMap;

            private final Object sendGameInfoThreadLock = new Object();
            private final Object discoveryThreadLock = new Object();

            private String threadName;

            private Ball tempBall;
            private Bat tempBat;

            public ClientThread(String threadName_, int tcpPort_, int udpPort_) {
                this.threadName=threadName_;
                this.client = new Client(10240,10240);
                this.client.start();

                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;
                this.isRunnning=true;

                this.connectionReferenceTime = System.currentTimeMillis();
                this.discoveryReferenceTime = System.currentTimeMillis();
                this.sendGameInfoReferenceTime = System.currentTimeMillis();

                this.connectionTimer = 0;
                this.discoveryTimer = 50;
                this.sendGameInfoTimer = 50;

                this.tcpPendingObjects = new ArrayList<Object>();
                this.udpPendingObjects = new ArrayList<Object>();
                this.discoveryList = new ArrayList<String>();

                this.connectionPending=false;
                this.tcpPending = false;
                this.udpPending = false;
                this.discoveryPending=false;

                this.sendGameInfoTcp = false;
                this.sendGameInfo = new SendVariables.SendGameInfo();
                this.sendGameInfo.gameInfoMap = new ConcurrentHashMap<String, Object>();

                this.sendMultiMap = Multimaps.synchronizedMultimap(HashMultimap.<String, Object>create());
                this.sendInfoMap = new ConcurrentHashMap<String, Object>();
                this.sendBallsMap = new ConcurrentHashMap<Integer, Object>();

                this.tempBall = new Ball();
                this.tempBat = new Bat();



                //this.udpPendingObjects = new ArrayList<Object>(Arrays.asList(new Object[] {}));

            }

            public void run() {
                while (this.isRunnning) {
                    if(System.currentTimeMillis() - this.connectionReferenceTime > this.connectionTimer) {
                        if (this.connectionPending) {
                            synchronized (connectionThreadLock) {
                                try {
                                    this.client.connect(5000, this.connectionIpAdress, this.tcpPort, this.udpPort);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                this.connectionPending = false;
                            }
                            this.connectionReferenceTime=System.currentTimeMillis();
                        }
                    }
                    try {
                        if(System.currentTimeMillis() - this.discoveryReferenceTime > this.discoveryTimer) {
                            if (this.discoveryPending) {
                                synchronized (this.discoveryThreadLock) {
                                    for (String ipAdress : this.discoveryList) {
                                        try {
                                            this.client.connect(5000, ipAdress, this.tcpPort, this.udpPort);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        this.client.sendUDP(discoveryRequest);
                                    }
                                    this.discoveryPending = false;
                                    this.discoveryList.clear();
                                }
                                this.discoveryReferenceTime = System.currentTimeMillis();
                            }
                        }


                        if (this.tcpPending) {
                            synchronized (sendThreadLock) {
                                for(Object object : this.tcpPendingObjects){
                                    this.client.sendTCP(object);
                                }
                                this.tcpPendingObjects.clear();
                                this.tcpPending = false;
                            }
                        }

                        if (this.udpPending) {
                            synchronized (sendThreadLock) {
                                for(Object object : this.udpPendingObjects){
                                    this.client.sendUDP(object);
                                }
                                this.udpPendingObjects.clear();
                                this.udpPending = false;
                            }
                        }

                        if(System.currentTimeMillis() - this.sendGameInfoReferenceTime > this.sendGameInfoTimer) {
                            synchronized (this.sendGameInfoThreadLock) {
                                this.sendGameInfo.gameInfoMap.put("myplayernumber", myPlayerNumber);
                                if (this.sendInfoMap.get("balldisplaystates") != null) {
                                    this.sendGameInfo.gameInfoMap.put("balldisplaystates", this.sendInfoMap.get("balldisplaystates"));
                                }

                                if (this.sendInfoMap.get("scores") != null) {
                                    this.sendGameInfo.gameInfoMap.put("scores", this.sendInfoMap.get("scores"));
                                }

                                if (this.sendInfoMap.get("bat") != null) {
                                    this.sendGameInfo.gameInfoMap.put("bat", this.sendInfoMap.get("bat"));
                                }
                                if (this.sendBallsMap.values().size() != 0) {
                                    this.sendGameInfo.gameInfoMap.put("balls", this.sendBallsMap);
                                }

                                if (this.sendGameInfoTcp) {
                                    this.client.sendTCP(this.sendGameInfo);
                                    this.sendGameInfoTcp = false;
                                } else {
                                    this.client.sendUDP(this.sendGameInfo);
                                }

                                this.sendBallsMap.clear();
                                this.sendInfoMap.clear();
                                this.sendGameInfo.gameInfoMap.clear();
                                //Thread.sleep(this.sendGameInfoTimer);
                            }
                            this.sendGameInfoReferenceTime = System.currentTimeMillis();
                        }

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
                this.client.stop();

            }

            void connect(String ipAdress_) {
                if(!ipAdress_.equals("127.0.0.1") && !ipAdress_.equals(myIpAdress)) {
                    synchronized (connectionThreadLock) {
                        this.connectionIpAdress = ipAdress_;
                        this.connectionPending = true;
                    }
                }
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

            public void addToBallsMap(ClassicGameObjects.Ball ball){
                this.tempBall = new Ball();
                this.tempBall.ballNumber = ball.ballNumber;
                this.tempBall.ballPlayerField = ball.tempPlayerField;
                this.tempBall.ballDisplayState = ball.ballDisplayState;

                if (this.tempBall.ballDisplayState == 1) {
                    this.tempBall.ballPosition = ball.ballBody.getPosition();
                    this.tempBall.ballVelocity = ball.ballBody.getLinearVelocity();
                    this.tempBall.ballAngle = ball.ballBody.getAngle();
                    this.tempBall.ballAngularVelocity = ball.ballBody.getAngularVelocity();
                }
                synchronized (this.sendGameInfoThreadLock) {
                    if (this.tempBall.ballPlayerField != myPlayerNumber) this.sendGameInfoTcp = true;
                    this.sendBallsMap.put(this.tempBall.ballNumber, this.tempBall);
                }
            }

            public void sendFrequentInfo(ClassicGameObjects.Bat bat, ConcurrentHashMap<Integer,Integer> ballDisplayStatesMap, int[] scores){
                synchronized (this.sendGameInfoThreadLock) {
                    this.sendInfoMap.put("myplayernumber", myPlayerNumber);
                    this.sendInfoMap.put("scores", scores);
                    this.sendInfoMap.put("balldisplaystates", ballDisplayStatesMap);
                }
                this.tempBat = new Bat();
                this.tempBat.batPosition = bat.batBody.getPosition();
                this.tempBat.batVelocity = bat.batBody.getLinearVelocity();
                this.tempBat.batAngle = bat.batBody.getAngle();
                this.tempBat.batAngularVelocity = bat.batBody.getAngularVelocity();

                synchronized (this.sendGameInfoThreadLock) {
                    this.sendInfoMap.put("bat", this.tempBat);
                }
            }

            public void sendDicoveryRequest(String ipAdress_) {
                if(!ipAdress_.equals("127.0.0.1") && !ipAdress_.equals(myIpAdress)) {
                    synchronized (this.discoveryThreadLock){
                        if(!this.discoveryList.contains(ipAdress_)){
                            this.discoveryList.add(ipAdress_);
                        }
                        this.discoveryPending = true;
                    }
                }
            }

            public void shutdownClient() {
                this.isRunnning=false;
            }

            public Client getClient() {
                return(this.client);
            }
        }

        //
    }

    class SendVariables {
        static public class SendSettings {
            public int numberOfPlayers;
            public int yourPlayerNumber;
            public String[] ipAdresses;
            public String[] playerNames;

            public Ball[] balls;

            public String gameMode;
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

        static public class SendGameInfo {
            public ConcurrentHashMap<String,Object> gameInfoMap;

            /*public int myPlayerNumber;

            public Bat bat;
            public ConcurrentHashMap<Integer,Integer> ballDisplayStatesMap;
            public int[] scores;

            public Ball[] balls;*/
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
