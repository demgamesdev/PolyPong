package com.demgames.polypong;

import android.app.Application;
import android.util.Log;
import android.content.Context;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import processing.core.PVector;

import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.GameListener;
import com.demgames.polypong.network.ServerListener;
import com.demgames.polypong.network.sendclasses.SendBallKinetics;
import com.demgames.polypong.network.sendclasses.SendBallScreenChange;
import com.demgames.polypong.network.sendclasses.SendBat;
import com.demgames.polypong.network.sendclasses.SendScore;
import com.demgames.polypong.network.sendclasses.SendSettings;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;

public class Globals extends Application {

    //Variablen
    private String myIpAdress;
    private String remoteIpAdress;
    private int numberOfBalls;
    private int gamemode;
    private int myPort=12000;
    private float friction=(float)0.0;
    private int myPlayerScreen;

    private Server server = new Server();
    private Client client = new Client();

    private ClientListener clientListener;
    private ServerListener serverListener;
    private GameListener gameListener;

    private List<Connection> connectionList = new ArrayList<Connection>(Arrays.asList(new Connection[]{}));
    List<InetAddress> hostsList;

    private String ServerList[] =new String[]{};

    private boolean connectState=false;
    private boolean readyState=false;
    private boolean gameLaunched=false;
    private boolean attraction=false;
    private boolean gravity=false;
    private boolean updateListViewState=false;

    private static final String TAG = "Globals";


    //arrayadapter for updating list of listview
    //private ArrayAdapter<String> arrayAdapter;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private List<String> ipAdressList = new ArrayList<String>(Arrays.asList(new String[] {}));

    private List<String> playerNamesList=new ArrayList<String>(Arrays.asList(new String[] {}));

    private PVector[] ballsPositions;
    private PVector[] ballsVelocities;
    private int[] ballsPlayerScreens;
    private float[] ballsSizes;

    private PVector batPosition;
    private float batOrientation;

    private int myScore;
    private int otherScore;

    public class GameVariables {
        PVector[] ballsPositions;
        PVector[] ballsVelocities;
        int[] ballsPlayerScreens;
        float[] ballsSizes;

        PVector batPosition;
        float batOrientation;

        protected int myScore;
        int otherScore;
    }

    private GameVariables gameVariables=new GameVariables();

    public GameVariables getGameVariables() {
        return(this.gameVariables);
    }

    //---------------NETWORKING-------------------

    public void setMyIpAdress(String myIpAdress_) {
        this.myIpAdress=myIpAdress_;
    }

    public String getMyIpAdress() {
        return(this.myIpAdress);
    }

    //----------------------------------

    public void setRemoteIpAdress(String remoteIpAdress_) {
        this.remoteIpAdress=remoteIpAdress_;
    }

    public String getRemoteIpAdress() {
        return(this.remoteIpAdress);
    }

    //----------------------------------

    public void setMyPort(int myPort_) {
        this.myPort=myPort_;
    }

    public int getMyPort() {
        return(this.myPort);
    }

    //----------------------------------

    public void setConnectState(boolean connectState_) {
        this.connectState=connectState_;
    }

    public boolean getConnectState() {
        return(this.connectState);
    }


    //----------------------------------


    public void setReadyStateState(boolean readyState_) {
        this.readyState=readyState_;
    }

    public boolean getReadyState() {
        return(this.readyState);
    }


    //----------------------------------

    public void setUpdateListViewState(boolean newState) {
        this.updateListViewState=newState;
    }

    public boolean getUpdateListViewState() {
        return(this.updateListViewState);
    }

    //----------------------------------

    public void setIpAdressList(String[] IpAdresses_) {
        this.ipAdressList=new ArrayList<String>(Arrays.asList(IpAdresses_));
    }

    public List<String> getIpAdressList() {
        return(this.ipAdressList);
    }


    //----------------------------------

    public Server getServer() {
        return(this.server);
    }

    public Client getClient() {
        return(this.client);
    }

    public void registerKryoClasses(Kryo myKryo) {
        myKryo.register(SendBallKinetics.class);
        myKryo.register(processing.core.PVector.class);
        myKryo.register(processing.core.PVector[].class);
        myKryo.register(float.class);
        myKryo.register(float[].class);
        myKryo.register(SendSettings.class);
        myKryo.register(Connection.class);
        myKryo.register(Connection[].class);
        myKryo.register(SendBallScreenChange.class);
        myKryo.register(SendBat.class);
        myKryo.register(SendScore.class);
    }

    public void setClientListener(Context context_){
        this.clientListener=new ClientListener(context_);
    }

    public ClientListener getClientListener() {
        return (this.clientListener);
    }

    public void setServerListener(Context context_){
        this.serverListener=new ServerListener(context_);
    }

    public ServerListener getServerListener() {
        return (this.serverListener);
    }

    public void setGameListener(Context context_){
        this.gameListener=new GameListener(context_);
    }

    public GameListener getGameListener() {
        return (this.gameListener);
    }


    //Todo make private or so
    public void addToConnectionList(Connection newConnection){
        if(!this.connectionList.contains(newConnection)){
            this.connectionList.add(newConnection);
        }
    }

    public Connection [] getConnectionList() {
        return(this.connectionList.toArray(new Connection[0]));
    }

    public void setConnectionList(Connection[] newConnectionList) {
        this.connectionList=new ArrayList<Connection>(Arrays.asList(newConnectionList));
    }

    public void setHostsList(List <InetAddress> newHostsList) {
        this.hostsList=newHostsList;
    }

    public List <InetAddress> getHostsList() {
        return(this.hostsList);
    }


    //----------------------------------

    public void setGameLaunched(boolean gameLaunched_) {
        this.gameLaunched=gameLaunched_;
    }

    public boolean getGameLaunched() {
        return(this.gameLaunched);
    }




    //---------------LIST MANIPULATION-------------------

    public boolean addIpTolist(String IpAdress){
        if(!this.ipAdressList.contains(IpAdress)){
            this.ipAdressList.add(IpAdress);
            Log.d("addiptolist",IpAdress +" added");
            return(true);
        }
        return(false);
    }

    //----------------------------------

    public boolean addPlayerNameTolist(String newPlayerName){
        if(!this.playerNamesList.contains(newPlayerName)){
            this.playerNamesList.add(newPlayerName);
            return(true);
        }
        return(false);
    }


    //----------------GAMEVARIABLES------------------

    public void setNumberOfBalls(int numberOfBalls_) {
        this.numberOfBalls=numberOfBalls_;
    }

    public int getNumberOfBalls() {
        return(this.numberOfBalls);
    }


    //----------------------------------

    public void setMyPlayerScreen(int newPlayerScreen_) {
        this.myPlayerScreen=newPlayerScreen_;
    }

    public int getMyPlayerScreen() {
        return(this.myPlayerScreen);
    }

    //----------------------------------

    public void setPlayerNamesList(String[] playerNames_) {
        this.playerNamesList=new ArrayList<String>(Arrays.asList(playerNames_));
    }

    public List<String> getPlayerNamesList() {
        return(this.playerNamesList);
    }

    //----------------------------------

    public void setAttractionState(Boolean attraction_) {
        this.attraction=attraction_;
    }

    public Boolean getAttractionState() {
        return(this.attraction);
    }

    //----------------------------------

    public void setGravityState(Boolean gravity_) {
        this.gravity=gravity_;
    }

    public Boolean getGravityState() {
        return(this.gravity);
    }

    public void setFriction(Float friction_) {
        this.friction=friction_;
    }

    public Float getFriction() {
        return(this.friction);
    }


    //----------------------------------

    public void setGameMode(int gamemode_) {
        this.gamemode=gamemode_;
    }

    public int getGameMode() {
        return(this.gamemode);
    }

    //----------------BALLSFUNCTIONS------------------

    public void setBalls(boolean randomPosition) {
        Random rand=new Random();
        this.ballsPositions=new PVector[this.numberOfBalls];
        this.ballsVelocities=new PVector[this.numberOfBalls];
        this.ballsPlayerScreens=new int[this.numberOfBalls];
        this.ballsSizes=new float[this.numberOfBalls];
        if(randomPosition) {
            for (int i = 0; i < this.numberOfBalls; i++) {
                this.ballsPositions[i] = new PVector(rand.nextFloat(),rand.nextFloat());
                this.ballsVelocities[i] = new PVector(0,0);
                this.ballsPlayerScreens[i]=0;
                this.ballsSizes[i] = rand.nextFloat();
            }
        }
    }

    public void setBallPosition(int ballNumber_, PVector ballPosition_) {
        this.ballsPositions[ballNumber_]=ballPosition_;
    }

    public void setBallVelocity(int ballNumber_, PVector ballVelocity_) {
        this.ballsVelocities[ballNumber_]=ballVelocity_;
    }

    public void setBallSize(int ballNumber_, float ballSize_) {
        this.ballsSizes[ballNumber_]=ballSize_;
    }


    public PVector[] getBallsPositions() {
        return(this.ballsPositions);
    }

    public PVector[] getBallsVelocities() {
        return(this.ballsVelocities);
    }

    public float[] getBallsSizes() {
        return(this.ballsSizes);
    }

    public void setBallPlayerScreen(int ballNumber_,int playerScreen_) {
        this.ballsPlayerScreens[ballNumber_]=playerScreen_;
    }

    public int[] getBallsPlayerScreens() {
        return(this.ballsPlayerScreens);
    }

    public void setBatPosition(PVector batPosition_) {
        this.batPosition=batPosition_;
    }

    public PVector getBatPosition() {
        return(this.batPosition);
    }

    public void setBatOrientation(float batOrientation_) {
        this.batOrientation=batOrientation_;
    }

    public float getBatOrientation() {
        return (this.batOrientation);
    }

    public void setMyScore(int myScore_) {
        this.myScore=myScore_;
    }

    public int getMyScore() {
        return (this.myScore);
    }

    public void setOtherScore(int otherScore_) {
        this.otherScore=otherScore_;
    }

    public int getOtherScore() {
        return (this.otherScore);
    }

}

