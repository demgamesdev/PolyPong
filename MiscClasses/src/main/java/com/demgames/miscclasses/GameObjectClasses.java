package com.demgames.miscclasses;

import com.badlogic.gdx.math.Vector2;

public class GameObjectClasses {
    public static class Ball{

        public int ballNumber;
        public int ballPlayerField;
        public int balltempPlayerField;

        public float ballRadius;
        public int ballDisplayState;
        public Vector2 ballPosition;
        public Vector2 ballVelocity;
        public float ballAngle;
        public float ballAngularVelocity;
        public boolean ballUpdateState;

        public Ball(){}
        public Ball(Ball ball_){
            this.ballNumber = ball_.ballNumber;
            this.ballPlayerField = ball_.ballPlayerField;
            this.balltempPlayerField = ball_.balltempPlayerField;
            this.ballRadius = ball_.ballRadius;
            this.ballPosition = ball_.ballPosition;
            this.ballVelocity = ball_.ballVelocity;
            this.ballAngle = ball_.ballAngle;
            this.ballAngularVelocity = ball_.ballAngularVelocity;
            this.ballUpdateState = ball_.ballUpdateState;

        }

        public Ball(int ballNumber_,int ballPlayerField_,int balltempPlayerField_,int ballDisplayState_,float ballRadius_, Vector2 ballPosition_, Vector2 ballVelocity_,
             float ballAngle_, float ballAngularVelocity_, boolean ballUpdateState_){
            this.ballNumber = ballNumber_;
            this.ballPlayerField = ballPlayerField_;
            this.balltempPlayerField = balltempPlayerField_;
            this.ballDisplayState = ballDisplayState_;
            this.ballRadius = ballRadius_;
            this.ballPosition = ballPosition_;
            this.ballVelocity = ballVelocity_;
            this.ballAngle = ballAngle_;
            this.ballAngularVelocity = ballAngularVelocity_;
            this.ballUpdateState = ballUpdateState_;
        }

        public void setReceived(Ball ball_, float rotateRad) {
            this.ballNumber = ball_.ballNumber;
            this.ballPlayerField = ball_.balltempPlayerField;
            this.balltempPlayerField = ball_.balltempPlayerField;
            this.ballDisplayState = ball_.ballDisplayState;
            this.ballRadius = ball_.ballRadius;
            this.ballPosition = ball_.ballPosition.rotateRad(rotateRad);
            this.ballVelocity = ball_.ballVelocity.rotateRad(rotateRad);
            this.ballAngle = ball_.ballAngle+rotateRad;
            this.ballAngularVelocity = ball_.ballAngularVelocity;
            this.ballUpdateState = ball_.ballUpdateState;
        }


    }

    public static class Bat{
        public int batPlayerField;
        public Vector2 batPosition;
        public Vector2 batVelocity;
        public float batAngle;
        public float batAngularVelocity;
        public boolean batUpdateState;

        public Bat() {};
        public Bat(Bat bat_){
            this.batPlayerField = bat_.batPlayerField;
            this.batPosition = bat_.batPosition;
            this.batVelocity = bat_.batVelocity;
            this.batAngle = bat_.batAngle;
            this.batAngularVelocity = bat_.batAngularVelocity;
            this.batUpdateState = bat_.batUpdateState;
        }

        public Bat(int batPlayerField_, Vector2 batPosition_, Vector2 batVelocity_, float batAngle_, float batAngularVelocity_, boolean batUpdateState_){
            this.batPlayerField = batPlayerField_;
            this.batPosition = batPosition_;
            this.batVelocity = batVelocity_;
            this.batAngle = batAngle_;
            this.batAngularVelocity = batAngularVelocity_;
            this.batUpdateState = batUpdateState_;
        }

        public void setReceived(Bat bat_, float rotateRad) {
            this.batPlayerField = bat_.batPlayerField;
            this.batPosition = bat_.batPosition.rotateRad(rotateRad);
            this.batVelocity = bat_.batVelocity.rotateRad(rotateRad);
            this.batAngle = bat_.batAngle+rotateRad;
            this.batAngularVelocity = bat_.batAngularVelocity;
            this.batUpdateState = bat_.batUpdateState;
        }
    }

    public static class Player {
        public String name;
        public String ipAdress;

        public Player(){};
        public Player(String name_, String ipAdress_) {
            this.name = name_;
            this.ipAdress = ipAdress_;
        }

        public Player(Player player_){
            this.name = player_.name;
            this.ipAdress = player_.ipAdress;
        }

    }
}