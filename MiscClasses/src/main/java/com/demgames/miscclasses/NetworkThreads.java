package com.demgames.miscclasses;

import com.demgames.miscclasses.GameObjectClasses.*;
import com.demgames.miscclasses.SendClasses.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkThreads {

    public static class ServerThread extends Thread {
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

        public void registerKryo() {
            registerKryoClasses(this.server.getKryo());
        }
    }

    public static class ClientThread extends Thread {
        private Client client;
        private String connectionIpAdress;
        private int tcpPort, udpPort;
        private boolean isRunnning;

        private long connectionReferenceTime,discoveryReferenceTime, sendGameInfoReferenceTime;
        private int connectionTimer,discoveryTimer, sendGameInfoTimer;

        private List<Object> tcpPendingObjects;
        private List<Object> udpPendingObjects;
        private List<String> discoveryList;

        private AtomicBoolean tcpPending,udpPending,connectionPending,discoveryPending,gameInfoPending,sendGameInfoTcp;

        private SendGameInfo sendGameInfo;
        private SendDiscoveryRequest sendDiscoveryRequest;

        private Multimap<String,Object> sendMultiMap;
        private Map<String,Object> sendInfoMap;

        private final Object connectionThreadLock = new Object();
        private final Object sendProtocolThreadLock = new Object();
        private final Object sendGameInfoThreadLock = new Object();
        private final Object discoveryThreadLock = new Object();

        private String threadName;

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

            this.connectionPending=new AtomicBoolean();
            this.tcpPending = new AtomicBoolean();
            this.udpPending = new AtomicBoolean();
            this.discoveryPending=new AtomicBoolean();
            this.gameInfoPending= new AtomicBoolean();

            this.sendGameInfoTcp = new AtomicBoolean();
            this.sendGameInfo = new SendGameInfo();
            this.sendDiscoveryRequest = new SendDiscoveryRequest();
            this.sendGameInfo.gameInfoMap = new ConcurrentHashMap<String, Object>();

            this.sendMultiMap = Multimaps.synchronizedMultimap(HashMultimap.<String, Object>create());
            this.sendInfoMap = new ConcurrentHashMap<String, Object>();

        }

        public void run() {
            while (this.isRunnning) {
                if(System.currentTimeMillis() - this.connectionReferenceTime > this.connectionTimer) {
                    if (this.connectionPending.get()) {
                        synchronized (this.connectionThreadLock) {
                            try {
                                this.client.connect(5000, this.connectionIpAdress, this.tcpPort, this.udpPort);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            this.connectionPending.set(false);
                        }
                        this.connectionReferenceTime=System.currentTimeMillis();
                    }
                }
                try {
                    if(System.currentTimeMillis() - this.discoveryReferenceTime > this.discoveryTimer) {
                        if (this.discoveryPending.get()) {
                            synchronized (this.discoveryThreadLock) {
                                for (String ipAdress : this.discoveryList) {
                                    try {
                                        this.client.connect(5000, ipAdress, this.tcpPort, this.udpPort);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    this.client.sendUDP(this.sendDiscoveryRequest);
                                }
                                this.discoveryPending.set(false);
                                this.discoveryList.clear();
                            }
                            this.discoveryReferenceTime = System.currentTimeMillis();
                        }
                    }


                    if (this.tcpPending.get()) {
                        synchronized (this.sendProtocolThreadLock) {
                            for(Object object : this.tcpPendingObjects){
                                this.client.sendTCP(object);
                            }
                            this.tcpPendingObjects.clear();
                            this.tcpPending.set(false);
                        }
                    }

                    if (this.udpPending.get()) {
                        synchronized (this.sendProtocolThreadLock) {
                            for(Object object : this.udpPendingObjects){
                                this.client.sendUDP(object);
                            }
                            this.udpPendingObjects.clear();
                            this.udpPending.set(false);
                        }
                    }

                    if(System.currentTimeMillis() - this.sendGameInfoReferenceTime > this.sendGameInfoTimer) {
                        if (this.gameInfoPending.get()) {
                            synchronized (this.sendGameInfoThreadLock) {
                                if(this.sendInfoMap.get("myplayernumber")!=null)this.sendGameInfo.gameInfoMap.put("myplayernumber", this.sendInfoMap.get("myplayernumber"));
                                if(this.sendInfoMap.get("bat")!=null)this.sendGameInfo.gameInfoMap.put("bat", this.sendInfoMap.get("bat"));
                                if(this.sendInfoMap.get("balls")!=null)this.sendGameInfo.gameInfoMap.put("balls", this.sendInfoMap.get("balls"));
                                if(this.sendInfoMap.get("scores")!=null)this.sendGameInfo.gameInfoMap.put("scores", this.sendInfoMap.get("scores"));

                                if (this.sendGameInfoTcp.get()) {
                                    this.client.sendTCP(this.sendGameInfo);
                                    this.sendGameInfoTcp.set(false);
                                } else {
                                    this.client.sendUDP(this.sendGameInfo);
                                }

                                this.sendInfoMap.clear();
                                this.sendGameInfo.gameInfoMap.clear();
                                this.gameInfoPending.set(false);
                                //Thread.sleep(this.sendGameInfoTimer);
                            }

                            this.sendGameInfoReferenceTime = System.currentTimeMillis();
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }
            this.client.stop();

        }

        void connect(String ipAdress, String myIpAdress) {
            if(!ipAdress.equals("127.0.0.1") && !ipAdress.equals(myIpAdress)) {
                synchronized (this.connectionThreadLock) {
                    this.connectionIpAdress = ipAdress;
                    this.connectionPending.set(true);
                }
            }
        }

        public void addObjectToProtocolSendList(Object object, String protocol) {
            try {
                if (protocol.equals("tcp")) {
                    synchronized (this.sendProtocolThreadLock) {
                        this.tcpPendingObjects.add(object);
                        this.tcpPending.set(true);
                    }

                } else if (protocol.equals("udp")) {
                    synchronized (this.sendProtocolThreadLock) {
                        this.udpPendingObjects.add(object);
                        this.udpPending.set(true);
                    }

                }
            }catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        public void addGameInfo(int myPlayerNumber, Bat bat, ConcurrentHashMap<Integer,Ball> ballsMap, int[] scores, boolean sendTcp){
            synchronized (this.sendGameInfoThreadLock) {
                this.sendInfoMap.put("myplayernumber", myPlayerNumber);
                this.sendInfoMap.put("bat", new Bat(bat));
                this.sendInfoMap.put("balls", ballsMap);
                this.sendInfoMap.put("scores", scores);
                this.sendGameInfoTcp.set(sendTcp);
                this.gameInfoPending.set(true);
            }
        }

        public void addDiscoveryRequest(String myPlayerName, String ipAdress_, String myIpAdress) {
            this.sendDiscoveryRequest.myPlayerName = myPlayerName;
            if(!ipAdress_.equals("127.0.0.1") && !ipAdress_.equals(myIpAdress)) {
                synchronized (this.discoveryThreadLock){
                    if(!this.discoveryList.contains(ipAdress_)){
                        this.discoveryList.add(ipAdress_);
                    }
                    this.discoveryPending.set(true);
                }
            }
        }

        public void shutdownClient() {
            this.isRunnning=false;
        }

        public Client getClient() {
            return(this.client);
        }

        public void registerKryo() {
            registerKryoClasses(this.client.getKryo());
        }
    }

    public static void registerKryoClasses(Kryo kryo) {
        kryo.register(float.class);
        kryo.register(float[].class);
        kryo.register(int.class);
        kryo.register(int[].class);
        kryo.register(String.class);
        kryo.register(String[].class);
        kryo.register(boolean.class);
        kryo.register(boolean[].class);
        kryo.register(Connection.class);
        kryo.register(Connection[].class);
        kryo.register(Object.class);
        kryo.register(Object[].class);
        kryo.register(com.badlogic.gdx.math.Vector2.class);
        kryo.register(com.badlogic.gdx.math.Vector2[].class);
        kryo.register(ConcurrentHashMap.class);
        kryo.register(HashMap.class);
        kryo.register(SendSettings.class);
        kryo.register(SendConnectionState.class);
        kryo.register(SendConnectionRequest.class);
        kryo.register(SendDiscoveryRequest.class);
        kryo.register(SendDiscoveryResponse.class);
        kryo.register(SendGameInfo.class);
        kryo.register(Ball.class);
        kryo.register(Ball[].class);
        kryo.register(Bat.class);
        kryo.register(Bat[].class);
        kryo.register(Player.class);

    }


}
