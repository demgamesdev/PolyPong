package com.demgames.polypong;

import android.app.Application;
import android.content.Context;

import java.net.InetAddress;
import java.util.List;

import com.demgames.polypong.network.GlobalListener;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;

public class Globals extends Application implements IGlobals{

    //Variablen
    private int gamemode;

    private boolean updateListViewState=false;

    private GlobalListener globalListener;

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
        myKryo.register(float.class);
        myKryo.register(float[].class);
        myKryo.register(Integer.class);
        myKryo.register(Integer[].class);
        myKryo.register(int.class);
        myKryo.register(int[].class);
        myKryo.register(Connection.class);
        myKryo.register(Connection[].class);
        myKryo.register(com.badlogic.gdx.math.Vector2.class);
        myKryo.register(com.badlogic.gdx.math.Vector2[].class);
        myKryo.register(SendVariables.SendBallKinetics.class);
        myKryo.register(SendVariables.SendSettings.class);
        myKryo.register(SendVariables.SendBallScreenChange.class);
        myKryo.register(SendVariables.SendBat.class);
        myKryo.register(SendVariables.SendScore.class);
    }

    public void setGlobalListener(Context context_){
        this.globalListener =new GlobalListener(context_);
    }

    public GlobalListener getGlobalListener() {
        return (this.globalListener);
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

