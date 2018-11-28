package com.demgames.polypong;

import android.app.Application;
import android.content.Context;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.ServerListener;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;

public class Globals extends Application implements IGlobals{
    private static final String TAG = "Globals";

    private ClientListener clientListener;
    private ServerListener serverListener;



    List<InetAddress> hostsList;

    private IGlobals.GameVariables gameVariables=new IGlobals.GameVariables();
    private IGlobals.SettingsVariables settingsVariables =new IGlobals.SettingsVariables();

    public synchronized IGlobals.GameVariables getGameVariables() {
        return(this.gameVariables);
    }
    public synchronized IGlobals.SettingsVariables getSettingsVariables() {
        return(this.settingsVariables);
    }

    public void setListeners(Context context_){
        this.clientListener =new ClientListener(context_);
        this.serverListener =new ServerListener(context_);
    }

    public ClientListener getClientListener() {
        return (this.clientListener);
    }
    public ServerListener getServerListener() {
        return (this.serverListener);
    }

    public void setHostsList(List <InetAddress> newHostsList) {
        this.hostsList=newHostsList;
    }

    public List <InetAddress> getHostsList() {
        return(this.hostsList);
    }


}

