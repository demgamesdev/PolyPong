package com.demgames.polypong;

import android.app.Application;
import android.util.Log;
import android.content.Context;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.GameListener;
import com.demgames.polypong.network.ServerListener;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;

public class Globals extends Application implements IGlobals{

    //Variablen
    private int gamemode;

    private boolean updateListViewState=false;

    private ClientListener clientListener;
    private ServerListener serverListener;
    private GameListener gameListener;

    private static final String TAG = "Globals";

    List<InetAddress> hostsList;

    private GameVariables gameVariables=new GameVariables();
    private SettingsVariables settingsVariables=new SettingsVariables();
    private NetworkVariables networkVariables=new NetworkVariables();

    public GameVariables getGameVariables() {
        return(this.gameVariables);
    }
    public SettingsVariables getSettingsVariables() {
        return(this.settingsVariables);
    }
    public NetworkVariables getNetworkVariables() {
        return(this.networkVariables);
    }

    public void registerKryoClasses(Kryo myKryo) {
        myKryo.register(SendVariables.SendBallKinetics.class);
        myKryo.register(com.badlogic.gdx.math.Vector2.class);
        myKryo.register(com.badlogic.gdx.math.Vector2[].class);
        myKryo.register(float.class);
        myKryo.register(float[].class);
        myKryo.register(SendVariables.SendSettings.class);
        myKryo.register(Connection.class);
        myKryo.register(Connection[].class);
        myKryo.register(SendVariables.SendBallScreenChange.class);
        myKryo.register(SendVariables.SendBat.class);
        myKryo.register(SendVariables.SendScore.class);
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

    public void setUpdateListViewState(boolean newState) {
        this.updateListViewState=newState;
    }

    public boolean getUpdateListViewState() {
        return(this.updateListViewState);
    }

    public void setHostsList(List <InetAddress> newHostsList) {
        this.hostsList=newHostsList;
    }

    public List <InetAddress> getHostsList() {
        return(this.hostsList);
    }


}

