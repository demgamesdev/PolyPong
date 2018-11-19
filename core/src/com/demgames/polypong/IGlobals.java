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

import sun.rmi.runtime.Log;

public interface IGlobals {

    class GameVariables {

        public int numberOfBalls;

        public Vector2[] ballsPositions;
        public Vector2[] ballsVelocities;
        public int[] ballsPlayerScreens;
        public float[] ballsSizes;
        public boolean[] ballDisplayStates;

        //TODO generalize to more players
        public Vector2[] batPositions = new Vector2[2];
        //public Vector2[] batVelocities = new Vector2[2];
        public float[] batOrientations = new float[2];

        public float friction;

        public boolean gravityState;
        public boolean attractionState;

        public int[] playerScores;

        GameVariables() {
            numberOfBalls=1;
            friction=0.1f;

            gravityState=false;
            attractionState=true;

            //TODO for more players
            playerScores=new int[2];
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
                    this.ballsPositions[i] = new Vector2(rand.nextFloat()*2f-1f, rand.nextFloat()*2f-1f);
                    this.ballsVelocities[i] = new Vector2(0, 0);
                    this.ballsPlayerScreens[i] = 0;
                    this.ballsSizes[i] = rand.nextFloat();
                    this.ballDisplayStates[i]=true;
                }
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

        public Server server;
        public Client[] clients;
        public Client discoveryClient;

        public Thread serverThread;
        public Thread[] clientThreads;
        public Thread discoveryClientThread;

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
            this.discoveryClient =new Client(4096,4096);
            this.discoveryClientThread = new Thread(this.discoveryClient);
            this.discoveryClientThread.start();
            this.registerKryoClasses(this.discoveryClient.getKryo());
        }

        public void connectClients() {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(i!=this.myPlayerNumber) {
                    try {
                        this.clients[i].connect(5000, this.ipAdresses.get(i),
                                this.tcpPort, this.udpPort);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void sendToClients(Object object, String protocol) {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {

                    if(protocol.equals("tcp")) {
                        this.clients[i].sendTCP(object);

                    } else if(protocol.equals("udp")) {
                        this.clients[i].sendUDP(object);

                    }
                }
            }
        }

        public void setClientListeners(Listener listener) {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clients[i].addListener(listener);
                }
            }
        }

        public void stopClients() {
            for(int i=0;i<this.numberOfPlayers;i++) {

                if(i!=this.myPlayerNumber) {
                    this.clients[i].stop();
                }
            }
        }

        public void startGameThreads() {
            this.clients = new Client[this.numberOfPlayers];
            this.clientThreads = new Thread[this.numberOfPlayers];
            for(int i=0; i<this.numberOfPlayers; i++) {
                if(i!=this.myPlayerNumber) {
                    this.clients[i] = new Client(4096,4096);
                    this.clientThreads[i] = new Thread(this.clients[i]);
                    this.clientThreads[i].start();
                    this.registerKryoClasses(this.clients[i].getKryo());
                }
            }
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

        public void setClientConnectionStates() {
            this.clientConnectionStates=new int[this.numberOfPlayers];
            for(int i=0;i<this.numberOfPlayers;i++) {
                this.clientConnectionStates[i]=1;
            }
        }

        public boolean checkClientConnectionState(int state) {
            for(int i=0;i<this.numberOfPlayers;i++) {
                if(!(this.clientConnectionStates[i]==state)) {
                    com.esotericsoftware.minlog.Log.debug("client"+i+" not in state "+state);
                    return(false);
                }
            }
            com.esotericsoftware.minlog.Log.debug("all clients in state "+state);
            return(true);
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
            public Integer[] ballNumbers;
            public int[] ballPlayerFields;
            public Vector2[] ballPositions;
            public Vector2[] ballVelocities;
        }

        static public class SendBat {
            public  int batPlayerField;
            public Vector2 batPosition;
            //public Vector2 batVelocity;
            public float batOrientation;
        }

        static public class SendBallScreenChange {
            public Integer[] ballNumbers;
            public int[] ballPlayerFields;
            public Vector2[] ballPositions;
            public Vector2[] ballVelocities;
        }

        static public class SendBallGoal {
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
