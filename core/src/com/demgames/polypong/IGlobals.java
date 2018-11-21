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


            if (randomPosition) {
                for (int i = 0; i < this.numberOfBalls; i++) {
                    this.ballsPositions[i] = this.upScaleVector(new Vector2((rand.nextFloat()-0.5f)*0.8f, (rand.nextFloat()-1f)*0.6f-0.2f));
                    this.ballsVelocities[i] = this.upScaleVector(new Vector2(0, 0));
                    this.ballsPlayerScreens[i] = 0;
                    this.ballsSizes[i] = rand.nextFloat();
                    this.ballDisplayStates[i]=true;
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

        public Server server;

        public Thread serverThread;
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
        }

        public void startServerThread() {
            this.server=new Server(4096,4096);
            this.serverThread = new Thread(this.server);
            this.serverThread.start();
            this.registerKryoClasses(this.server.getKryo());

        }

        public void startDiscoveryClientThread() {
            this.discoveryClientThread = new ClientThread("discoveryClientThread",this.tcpPort,this.udpPort);
            this.registerKryoClasses(this.discoveryClientThread.getClient().getKryo());
        }

        public void connectDiscoveryClient(String ipAdress_) {
            this.discoveryClientThread.setIpAdress(ipAdress_);
            this.discoveryClientThread.start();
        }

        public void startAllClientThreads() {
            this.clientThreads = new ClientThread[this.numberOfPlayers];
            for(int i=0; i<this.numberOfPlayers; i++) {
                if(i!=this.myPlayerNumber) {
                    this.clientThreads[i] = new ClientThread("clientThread "+i,this.tcpPort,this.udpPort);
                    this.registerKryoClasses(this.clientThreads[i].getClient().getKryo());

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
                    this.clientThreads[i].setIpAdress(this.ipAdresses.get(i));
                    this.clientThreads[i].start();
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

        public void setAllClientConnectionStates() {
            this.clientConnectionStates=new int[this.numberOfPlayers];
            for(int i=0;i<this.numberOfPlayers;i++) {
                this.clientConnectionStates[i]=1;
            }
        }

        public boolean checkAllClientConnectionStates(int state) {
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
            myKryo.register(com.badlogic.gdx.math.Vector2.class);
            myKryo.register(com.badlogic.gdx.math.Vector2[].class);
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
            if(!this.discoveryPlayerNames.contains(playerName)){
                this.discoveryPlayerNames.add(playerName);
                return(true);
            }
            return(false);
        }

        public void resetArrayLists() {
            this.discoveryIpAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.ipAdresses = new ArrayList<String>(Arrays.asList(new String[] {}));
            this.playerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryPlayerNames =new ArrayList<String>(Arrays.asList(new String[] {}));
            this.discoveryIsChecked =new ArrayList<Boolean>(Arrays.asList(new Boolean[]{}));
        }

        public class ClientThread extends Thread {
            private Client client;
            private String ipAdress;
            private int tcpPort, udpPort, udpPendingSize;
            private boolean isRunnning;
            private boolean tcpPending;
            private boolean udpPending;
            private ArrayList<Object> tcpPendingObjects;
            private ArrayList<Object> tempTcpPendingObjects;
            private ArrayList<Object> udpPendingObjects;
            private ArrayList<Object> tempUdpPendingObjects;

            private String threadName;

            public ClientThread(String threadName_, int tcpPort_, int udpPort_) {
                this.threadName=threadName_;
                this.client = new Client(4096,4096);
                this.client.start();

                this.tcpPort = tcpPort_;
                this.udpPort = udpPort_;
                this.isRunnning=true;
                this.tcpPending=false;
                this.udpPending=false;
                this.udpPendingSize=2;

                this.tcpPendingObjects = new ArrayList<Object>(Arrays.asList(new Object[] {}));
                this.udpPendingObjects = new ArrayList<Object>(Arrays.asList(new Object[] {}));

            }

            public void run() {
                try {
                    this.client.connect(5000, this.ipAdress,this.tcpPort, this.udpPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (this.isRunnning) {
                    if(this.tcpPending) {
                        this.tempTcpPendingObjects = new ArrayList<Object>(this.tcpPendingObjects);
                        for (int i = 0; i < this.tempTcpPendingObjects.size(); i++) {
                            this.client.sendTCP(this.tempTcpPendingObjects.get(i));
                        }
                        this.tcpPendingObjects = new ArrayList<Object>(Arrays.asList(new Object[]{}));
                        this.tcpPending=false;
                    }

                    if(this.udpPending) {
                        this.tempUdpPendingObjects=new ArrayList<Object>(this.udpPendingObjects);
                        for(int i=0; i<this.tempUdpPendingObjects.size();i++) {
                            this.client.sendUDP(this.tempUdpPendingObjects.get(i));
                        }
                        this.udpPendingObjects = new ArrayList<Object>(Arrays.asList(new Object[] {}));
                        this.udpPending=false;
                    }
                }

            }

            public void sendObject(Object object, String protocol) {
                if(protocol.equals("tcp")) {
                    this.tcpPendingObjects.add(object);
                    this.tcpPending=true;

                } else if(protocol.equals("udp")) {
                    this.udpPendingObjects.add(object);
                    if(this.udpPendingObjects.size() > this.udpPendingSize) {
                        this.udpPendingObjects.remove(0);
                    }
                    this.udpPending=true;
                }
            }

            public void setIpAdress(String ipAdress_) {
                this.ipAdress=ipAdress_;
            }

            public void shutdownClient() {
                this.isRunnning=false;
                this.client.stop();
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
            public int[] ballPlayerFields;
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
            public int[] ballPlayerFields;
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

    class BoundedArrayList<T> extends ArrayList<T> {
        private int maxSize;
        public BoundedArrayList(int size)
        {
            this.maxSize = size;
        }

        public void addLast(T e)
        {
            this.add(e);
            if(this.size() > this.maxSize) {
                this.remove(0);
            }

        }
    }

    GameVariables getGameVariables();
    SettingsVariables getSettingsVariables();
}
