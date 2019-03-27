package com.demgames.miscclasses;

import com.demgames.miscclasses.GameObjectClasses.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SendClasses {

    static public class SendSettings {
        public int numberOfPlayers;
        public int yourPlayerNumber;
        public Map<Integer,Player> playerMap;

        public Ball[] balls;

        public String gameMode;
        public boolean gravityState;
        public boolean attractionState;

        public SendSettings(){}
        public SendSettings(SendSettings sendSettings_){
            this.numberOfPlayers = sendSettings_.numberOfPlayers;
            this.yourPlayerNumber = sendSettings_.yourPlayerNumber;
            this.playerMap = sendSettings_.playerMap;
            this.balls = sendSettings_.balls;
            this.gameMode = sendSettings_.gameMode;
            this.gravityState = sendSettings_.gravityState;
            this.attractionState = sendSettings_.attractionState;
        }
        public SendSettings(SendSettings sendSettings_,int overrideYourPlayerNumber){
            this.numberOfPlayers = sendSettings_.numberOfPlayers;
            this.yourPlayerNumber = overrideYourPlayerNumber;
            this.playerMap = sendSettings_.playerMap;
            this.balls = sendSettings_.balls;
            this.gameMode = sendSettings_.gameMode;
            this.gravityState = sendSettings_.gravityState;
            this.attractionState = sendSettings_.attractionState;
        }
    }

    static public class SendConnectionState {
        public int myPlayerNumber;
        public int connectionState;

       public SendConnectionState(int myPlayerNumber_, int connectionState_){
            this.myPlayerNumber = myPlayerNumber_;
            this.connectionState = connectionState_;
        }
        public SendConnectionState() {}
    }

    static public class SendDiscoveryRequest {
        public String myPlayerName;
        public SendDiscoveryRequest() {}
        public SendDiscoveryRequest(String myPlayerName_){
            this.myPlayerName = myPlayerName_;
        }

    }

    static public class SendDiscoveryResponse {
        public String myPlayerName;
        public SendDiscoveryResponse() {}
        public SendDiscoveryResponse(String myPlayerName_){
            this.myPlayerName = myPlayerName_;
        }
    }

    static public class SendConnectionRequest {
        public String myPlayerName;
        public SendConnectionRequest() {}
        public SendConnectionRequest(String myPlayerName_){
            this.myPlayerName = myPlayerName_;
        }
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
