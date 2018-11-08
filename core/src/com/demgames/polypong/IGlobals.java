package com.demgames.polypong;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;

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

        public Vector2 batPosition;
        public float batOrientation;

        public float friction;

        public boolean gravityState;
        public boolean attractionState;

        public int myScore;
        public int otherScore;

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
            if (randomPosition) {
                for (int i = 0; i < this.numberOfBalls; i++) {
                    this.ballsPositions[i] = new Vector2(rand.nextFloat(), rand.nextFloat());
                    this.ballsVelocities[i] = new Vector2(0, 0);
                    this.ballsPlayerScreens[i] = 0;
                    this.ballsSizes[i] = rand.nextFloat();
                }
            }
        }
    }

    class SettingsVariables {
        String mode;
        public int myPlayerScreen;
        public int gameMode;

        public boolean connectState;
        public boolean readyState;
        public boolean gameLaunched;
        public boolean updateListViewState;

        public List<String> playerNamesList;

        SettingsVariables() {
            connectState=false;
            readyState=false;
            gameLaunched=false;
            updateListViewState=false;

            playerNamesList=new ArrayList<String>(Arrays.asList(new String[] {}));
        }

        boolean addPlayerNameTolist(String newPlayerName){
            if(!this.playerNamesList.contains(newPlayerName)){
                this.playerNamesList.add(newPlayerName);
                return(true);
            }
            return(false);
        }


    }

    class NetworkVariables {
        String myIpAdress;
        String remoteIpAdress;
        int tcpPort,udpPort;
        List<String> ipAdressList;
        List<Connection> connectionList;

        Server server;
        Client client;

        NetworkVariables() {

            tcpPort=12000;
            udpPort=12001;
            udpPort=12001;

            server=new Server(4096,4096);
            //server=new Server();
            client=new Client(4096,4096);
            //client=new Client();

            ipAdressList= new ArrayList<String>(Arrays.asList(new String[] {}));
            connectionList = new ArrayList<Connection>(Arrays.asList(new Connection[]{}));
        }

        Connection [] getConnectionArray() {
            return(this.connectionList.toArray(new Connection[0]));
        }

        public void addToConnectionList(Connection newConnection){
            if(!this.connectionList.contains(newConnection)){
                this.connectionList.add(newConnection);
            }
        }

        public boolean addIpTolist(String IpAdress){
            if(!this.ipAdressList.contains(IpAdress)){
                this.ipAdressList.add(IpAdress);
                //Log.d("addiptolist",IpAdress +" added");
                return(true);
            }
            return(false);
        }



    }

    class SendVariables {
        static public class SendSettings {
            public Vector2[] ballsPositions;
            public Vector2[] ballsVelocities;
            public float[] ballsSizes;
            public int gameMode;
            public boolean gravityState;
            public boolean attractionState;
        }

        static public class SendBallKinetics {
            public Integer[] ballNumbers;
            public int[] ballPlayerScreens;
            public Vector2[] ballPositions;
            public Vector2[] ballVelocities;
        }

        static public class SendBat {
            public Vector2 batPosition;
            public float batOrientation;
        }

        static public class SendBallScreenChange {
            public Integer[] ballNumbers;
            public int[] ballPlayerScreens;
            public Vector2[] ballPositions;
            public Vector2[] ballVelocities;
        }

        static public class SendScore {
            public int myScore;
            public int otherScore;
        }
    }

    class testClass {
        String test;
    }

    GameVariables getGameVariables();
    SettingsVariables getSettingsVariables();
    NetworkVariables getNetworkVariables();
}
