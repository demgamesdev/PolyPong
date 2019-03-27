package com.demgames.miscclasses;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Listener;
import com.demgames.miscclasses.GameObjectClasses.*;
import com.demgames.miscclasses.NetworkThreads.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class CommunicationClass {

    public String gameMode;
    private int tcpPort,udpPort;
    public int myPlayerNumber, numberOfPlayers,setupConnectionState, gameState;
    public boolean gameHasFocus, gravityState, attractionState;
    public float friction;

    public static final Object receiveThreadLock = new Object();
    public static final Object sendThreadLock = new Object();
    public static final Object connectionThreadLock = new Object();

    private ServerThread serverThread;
    private ClientThread discoveryClientThread;
    private Map<Integer,ClientThread> clientThreadsMap;

    public int[] playerScores;
    public Ball[] balls;
    public Bat[] bats;

    public List<Boolean> discoveryIsChecked;
    public List<Player> discoveryPlayers;
    public List<String> discoveryHostList;

    public Map<Integer,Player> playerMap;

    public ConcurrentHashMap<Integer,Integer> clientConnectionStatesMap;

    public CommunicationClass() {
        this.tcpPort=12000;
        this.udpPort=12001;
    }

    public void initGame(int myPlayerNumber_,int numberOfBalls_, int numberOfPlayers_,String gameMode_, boolean gravityState_, boolean attractionState_, boolean setupState) {
        this.myPlayerNumber = myPlayerNumber_;
        this.numberOfPlayers = numberOfPlayers_;
        this.gameMode = gameMode_;
        this.gravityState = gravityState_;
        this.attractionState = attractionState_;
        this.initBalls(numberOfBalls_,numberOfPlayers_,setupState);
        this.initBats(numberOfPlayers_);
        this.playerScores = new int[numberOfPlayers_];

    }

    public void initBalls(int numberOfBalls_, int numberOfPlayers_, boolean setupState) {
        Random rand = new Random();
        this.balls = new Ball[numberOfBalls_];

        for (int i = 0; i < numberOfBalls_; i++) {
            this.balls[i] = new Ball();
            if (setupState) {
                this.balls[i].ballNumber = i;
                this.balls[i].ballPlayerField = i%numberOfPlayers_;
                this.balls[i].balltempPlayerField = this.balls[i].ballPlayerField;
                this.balls[i].ballDisplayState = 1;
                //this.balls[i].ballRadius = (rand.nextFloat()*0.2f+0.8f)*0.05f * this.factor;
                this.balls[i].ballRadius = 0.04f;
                this.balls[i].ballPosition = new Vector2(0,0);
                this.balls[i].ballVelocity = new Vector2(0,0);
                this.balls[i].ballAngle = 0f;
                this.balls[i].ballAngularVelocity = 0f;
                this.balls[i].ballUpdateState = false;

            }
            //this.balls[i].ballDisplayState=1;
        }
    }

    public void initBats(int numberOfPlayers_) {
        this.bats = new Bat[numberOfPlayers_];

        for (int i = 0; i < numberOfPlayers_; i++) {
            this.bats[i]= new Bat();
            this.bats[i].batPlayerField = i;
            this.bats[i].batPosition = new Vector2(0,0);
            this.bats[i].batVelocity = new Vector2(0,0);
            this.bats[i].batAngle = 0;
            this.bats[i].batAngularVelocity = 0;
            this.bats[i].batUpdateState = false;
        }
    }

    public void startServerThread(Listener listener) {
        this.serverThread = new ServerThread("serverThread",this.tcpPort,this.udpPort);
        this.serverThread.registerKryo();
        this.serverThread.start();
        this.serverThread.bind();
        this.serverThread.getServer().addListener(listener);

    }

    public void shutdownServer() {
        this.serverThread.shutdownServer();
    }

    public void startDiscoveryClientThread(Listener listener) {
        this.discoveryClientThread = new ClientThread("discoveryClientThread",this.tcpPort,this.udpPort);
        this.discoveryClientThread.registerKryo();
        this.discoveryClientThread.start();
        this.discoveryClientThread.getClient().addListener(listener);
    }

    public List<InetAddress> discoverHosts() {
        return(this.discoveryClientThread.getClient().discoverHosts(this.udpPort,500));
    }

    public void discoveryRequest(String myPlayerName, String ipAdress, String myIpAdress) {
        this.discoveryClientThread.addDiscoveryRequest(myPlayerName,ipAdress,myIpAdress);
    }

    public void connectDiscoveryClient(String ipAdress, String myIpAdress) {
        this.discoveryClientThread.connect(ipAdress,myIpAdress);
    }

    public void sendDiscoveryClientObject(Object object, String protocol) {
        this.discoveryClientThread.addObjectToProtocolSendList(object,protocol);
    }


    public void startAllClientThreads(int myPlayerNumber,int numberOfPlayers, Listener listener) {
        this.clientThreadsMap = new HashMap<Integer, ClientThread>();
        this.clientConnectionStatesMap = new ConcurrentHashMap<Integer, Integer>();
        for(int i=0; i<numberOfPlayers; i++) {
            if(i!=myPlayerNumber) {
                this.clientThreadsMap.put(i,new ClientThread("clientThread "+i,this.tcpPort,this.udpPort));
                this.clientThreadsMap.get(i).registerKryo();
                this.clientThreadsMap.get(i).start();
                this.clientThreadsMap.get(i).getClient().addListener(listener);
                this.clientConnectionStatesMap.put(i,1); //initialize in state 1 - searching, before settings received
            }
        }
    }

    public void connectAllClients(Map<Integer,Player> playerMap_, String myIpAdress) {
        for(int i : clientThreadsMap.keySet()) {
            this.clientThreadsMap.get(i).connect(playerMap_.get(i).ipAdress,myIpAdress);
                //this.clientThreads[i].start();

        }
    }

    public void sendObjectToAllClients(Object object, String protocol) {
        for(int i : clientThreadsMap.keySet()) {
            this.clientThreadsMap.get(i).addObjectToProtocolSendList(object, protocol);
        }
    }

    public void sendObjectToClient(Object object, String protocol,int clientIndex) {
        this.clientThreadsMap.get(clientIndex).addObjectToProtocolSendList(object, protocol);
    }



    public void sendGameInfoToAllClients(int myPlayerNumber, Bat bat, ConcurrentHashMap<Integer,Ball> ballsMap, int[] scores, boolean sendTcp) {
        for(int i : clientThreadsMap.keySet()) {
            this.clientThreadsMap.get(i).addGameInfo(myPlayerNumber,bat,ballsMap, scores,sendTcp);
        }
    }

    public void shutdownAllClients() {
        if(this.clientThreadsMap!=null) {
            for (int i : clientThreadsMap.keySet()) {
                this.clientThreadsMap.get(i).shutdownClient();
            }
        }
    }

    public void shutdownDiscoveryClient() {
        this.discoveryClientThread.shutdownClient();
    }

    public boolean checkAllClientConnectionStates( int state) {
        if(this.clientThreadsMap==null){
            return(false);
        }
        for(int i : clientThreadsMap.keySet()) {
            if(!(this.clientConnectionStatesMap.get(i).equals(state))) {
                com.esotericsoftware.minlog.Log.debug("client"+i+" not in state "+state);
                return(false);
            }
        }
        com.esotericsoftware.minlog.Log.debug("all clients in state "+state);
        return(true);
    }

    public void setSetupConnectionState(int setupConnectionState_){
        this.setupConnectionState = setupConnectionState_;
    }

    public void setPlayerMap(Map<Integer,Player> playerMap_){
        this.playerMap = playerMap_;
    }

    public void setGameState(int gameState_){
        this.gameState = gameState_;
    }

    public boolean addDiscoveryHost(String ipAdress){
        for(int i = 0; i<this.discoveryHostList.size(); i++) {
            if ((this.discoveryHostList.get(i).equals(ipAdress))) {
                return (false);
            }
        }
        this.discoveryHostList.add(ipAdress);
        return true;
    }

    public boolean addDiscoveryPlayer(String playerName, String ipAdress){
        for(int i =0; i<this.discoveryPlayers.size();i++) {
            if ((this.discoveryPlayers.get(i).ipAdress.equals(ipAdress))) {
                return (false);
            }
        }
        this.discoveryPlayers.add(new Player(playerName, ipAdress));
        return true;
    }

    public void resetLists() {
        this.discoveryHostList = new ArrayList<String>();
        this.discoveryPlayers = new ArrayList<Player>();
        this.discoveryIsChecked =new ArrayList<Boolean>(Arrays.asList(new Boolean[]{}));
    }

    public void resetPlayerMap() {
        this.playerMap = new HashMap<Integer, Player>();
    }

}
