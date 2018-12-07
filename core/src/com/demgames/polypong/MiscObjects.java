package com.demgames.polypong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class MiscObjects {
    private static final String TAG = "MiscObjects";

    private IGlobals globals;

    //global sendclasses
    private IGlobals.SendVariables.TempFrequentObjects tempFrequentObjects = new IGlobals.SendVariables.TempFrequentObjects();

    private IGlobals.SendVariables.SendFrequentBat sendFrequentBat=new IGlobals.SendVariables.SendFrequentBat();
    private IGlobals.SendVariables.SendFrequentInfo sendFrequentInfo =new IGlobals.SendVariables.SendFrequentInfo();
    private IGlobals.SendVariables.SendFieldChange sendFieldChange =new IGlobals.SendVariables.SendFieldChange();
    private IGlobals.SendVariables.SendConnectionState sendConnectionState=new IGlobals.SendVariables.SendConnectionState();

    private int myPlayerNumber;
    private float screenWidth, screenHeight,width, height;
    Vector2 zoomPoint;
    float zoomLevel;

    //class for touch input and gestures

    public Touches touches;

    MiscObjects(IGlobals globals_ , int myPlayerNumber_, float width_, float height_) {
        this.globals = globals_;
        this.myPlayerNumber = myPlayerNumber_;
        this.width = width_;
        this.height = height_;
        this.touches=new Touches(2);

        this.screenWidth = globals.getGameVariables().width;
        this.screenHeight = globals.getGameVariables().height;

        this.zoomLevel = 1f;
        this.zoomPoint = new Vector2(0,-this.height/2);
    }

    /********* OTHER FUNCTIONS *********/
    //adjust camera for zooming


    //transform touch input for variable zoomlevel
    Vector2 transformZoom(Vector2 vec) {
        vec.x*=zoomLevel;
        vec.y = - height + (vec.y + height) * zoomLevel;
        return(vec);
    }

    static float[] vecToFloatArray(Vector2[] vectorArray) {
        float[] floatArray = new float[vectorArray.length*2];
        for(int i=0;i<vectorArray.length;i++) {
            floatArray[2*i]=vectorArray[i].x;
            floatArray[2*i+1]=vectorArray[i].y;
        }
        return(floatArray);
    }

    static Vector2[] transformVectorArray(Vector2[] vectorArray, float scale, float degrees) {
        for(Vector2 vector : vectorArray) {
            vector.scl(scale).rotate(degrees);
        }
        return(vectorArray);
    }
    /********* SEND FUNCTIONS *********/

    void sendFieldChangeFunction(ArrayList<ClassicGameObjects.Ball> sendFieldChangeBallsAL) {
        synchronized (globals.getSettingsVariables().sendThreadLock) {
            sendFieldChange.myPlayerNumber = myPlayerNumber;
            sendFieldChange.numberOfSendBalls = sendFieldChangeBallsAL.size();

            if (sendFieldChange.numberOfSendBalls > 0) {
                sendFieldChange.ballNumbers = new int[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballPlayerFields = new int[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballDisplayStates = new int[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballPositionsX = new float[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballPositionsY = new float[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballVelocitiesX = new float[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballVelocitiesY = new float[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballAngles = new float[sendFieldChangeBallsAL.size()];
                sendFieldChange.ballAngularVelocities = new float[sendFieldChangeBallsAL.size()];


                for (int i = 0; i < sendFieldChange.numberOfSendBalls; i++) {
                    sendFieldChange.ballNumbers[i] = sendFieldChangeBallsAL.get(i).ballNumber;
                    sendFieldChange.ballPlayerFields[i] = sendFieldChangeBallsAL.get(i).tempPlayerField;
                    sendFieldChange.ballDisplayStates[i] = sendFieldChangeBallsAL.get(i).ballDisplayState;
                    sendFieldChange.ballPositionsX[i] = sendFieldChangeBallsAL.get(i).ballBody.getPosition().x;
                    sendFieldChange.ballPositionsY[i] = sendFieldChangeBallsAL.get(i).ballBody.getPosition().y;
                    sendFieldChange.ballVelocitiesX[i] = sendFieldChangeBallsAL.get(i).ballBody.getLinearVelocity().x;
                    sendFieldChange.ballVelocitiesY[i] = sendFieldChangeBallsAL.get(i).ballBody.getLinearVelocity().y;
                    sendFieldChange.ballAngles[i] = sendFieldChangeBallsAL.get(i).ballBody.getAngle();
                    sendFieldChange.ballAngularVelocities[i] = sendFieldChangeBallsAL.get(i).ballBody.getAngularVelocity();
                    //Gdx.app.debug(TAG, "fieldchange of ball "+ sendFieldChange.ballNumbers[i] +" sent");
                }
                globals.getSettingsVariables().sendToAllClients(sendFieldChange, "tcp");
            }
        }
    }

    void sendConnectionStateFunction() {
        sendConnectionState.myPlayerNumber= globals.getSettingsVariables().myPlayerNumber;
        sendConnectionState.connectionState= globals.getSettingsVariables().clientConnectionStates[globals.getSettingsVariables().myPlayerNumber];
        globals.getSettingsVariables().sendToAllClients(sendConnectionState,"tcp");
    }

    void sendFrequentsFunction(ArrayList<ClassicGameObjects.Ball> sendBallsAL, ClassicGameObjects.Bat bat, int[] scores) {
        synchronized (globals.getSettingsVariables().sendThreadLock) {
            sendFrequentBat.myPlayerNumber=myPlayerNumber;
            sendFrequentInfo.myPlayerNumber=myPlayerNumber;

            tempFrequentObjects.sendFrequentBalls = new IGlobals.SendVariables.SendFrequentBall[sendBallsAL.size()];

            for (int i = 0; i < sendBallsAL.size() ; i++) {
                tempFrequentObjects.sendFrequentBalls[i] = new IGlobals.SendVariables.SendFrequentBall();
                tempFrequentObjects.sendFrequentBalls[i].myPlayerNumber = myPlayerNumber;
                tempFrequentObjects.sendFrequentBalls[i].ballNumber = sendBallsAL.get(i).ballNumber;
                tempFrequentObjects.sendFrequentBalls[i].ballPlayerField  = sendBallsAL.get(i).tempPlayerField;
                tempFrequentObjects.sendFrequentBalls[i].ballDisplayState  = sendBallsAL.get(i).ballDisplayState;
                if(sendBallsAL.get(i).ballDisplayState==1) {
                    tempFrequentObjects.sendFrequentBalls[i].ballPositionX = sendBallsAL.get(i).ballBody.getPosition().x;
                    tempFrequentObjects.sendFrequentBalls[i].ballPositionY = sendBallsAL.get(i).ballBody.getPosition().y;
                    tempFrequentObjects.sendFrequentBalls[i].ballVelocityX = sendBallsAL.get(i).ballBody.getLinearVelocity().x;
                    tempFrequentObjects.sendFrequentBalls[i].ballVelocityY = sendBallsAL.get(i).ballBody.getLinearVelocity().y;
                    tempFrequentObjects.sendFrequentBalls[i].ballAngle = sendBallsAL.get(i).ballBody.getAngle();
                    tempFrequentObjects.sendFrequentBalls[i].ballAngularVelocity = sendBallsAL.get(i).ballBody.getAngularVelocity();
                }
            }

            sendFrequentBat.batPositionX=bat.batBody.getPosition().x;
            sendFrequentBat.batPositionY=bat.batBody.getPosition().y;
            sendFrequentBat.batVelocityX=bat.batBody.getLinearVelocity().x;
            sendFrequentBat.batVelocityY=bat.batBody.getLinearVelocity().y;
            sendFrequentBat.batAngle =bat.batBody.getAngle();
            sendFrequentBat.batAngularVelocity =bat.batBody.getAngularVelocity();

            sendFrequentInfo.scores = scores;

            tempFrequentObjects.sendFrequentBat = sendFrequentBat;
            tempFrequentObjects.sendFrequentInfo = sendFrequentInfo;


            globals.getSettingsVariables().sendToAllClients(tempFrequentObjects,"udp");
        }
    }

    class Touches {
        int maxTouchCount;
        Vector2[] touchPos;
        private Vector2[] lastTouchPos;
        private Vector2[] startTouchPos;
        boolean[] isTouched;
        private boolean[] lastIsTouched;

        Touches(int maxTouchCount_) {
            this.maxTouchCount=maxTouchCount_;
            this.touchPos=new Vector2[maxTouchCount];
            this.lastTouchPos=new Vector2[maxTouchCount];
            this.startTouchPos=new Vector2[maxTouchCount];
            this.isTouched=new boolean[maxTouchCount];
            this.lastIsTouched=new boolean[maxTouchCount];

            for (int i=0;i<maxTouchCount;i++) {
                this.touchPos[i]=new Vector2(0,-height*0.9f);
                this.lastTouchPos[i]=touchPos[i];
                this.startTouchPos[i]=touchPos[i];
                this.isTouched[i]=false;
                this.lastIsTouched[i]=false;
            }
        }

        //check for touches
        void checkTouches() {
            for(int i=0;i<this.maxTouchCount;i++) {
                if (Gdx.input.isTouched(i)) {
                    this.isTouched[i] = true;
                    this.touchPos[i]=transformZoom(new Vector2((Gdx.input.getX(i)/screenWidth-0.5f) *width,-Gdx.input.getY(i)/screenHeight*height));
                } else {
                    this.isTouched[i] = false;
                }
                if(!this.lastIsTouched[i] && this.isTouched[i]) {
                    this.startTouchPos[i]=this.touchPos[i];
                }
            }
        }

        private void zoom (float originalDistance, float currentDistance){
            float newZoomLevel=zoomLevel+(originalDistance-currentDistance)/5;
            if(newZoomLevel<=2.0f && newZoomLevel>=1.0f) {
                zoomLevel=newZoomLevel;

            } else if(newZoomLevel>2.0f) {
                zoomLevel=newZoomLevel;//2.0f;
            } else if(newZoomLevel<1.0f) {
                zoomLevel=1.0f;
            }
            zoomPoint.set(0,-height+height/2*zoomLevel);
        }

        //check for zoom gesture
        void checkZoomGesture() {
            if(this.isTouched[0] && this.isTouched[1]) {
                zoom(this.startTouchPos[0].cpy().sub(this.startTouchPos[1]).len(),this.touchPos[0].cpy().sub(this.touchPos[1]).len());
            }

            if(!this.isTouched[0] || !this.isTouched[1]) {
                if(zoomLevel != 2.0 || zoomLevel != 1.0) {
                    //continuously update camera
                    zoomLevel=MathUtils.round(zoomLevel);
                }

            }
        }

        void updateLasts() {
            for(int i=0;i<this.maxTouchCount;i++) {
                this.lastTouchPos[i]=this.touchPos[i];
                this.lastIsTouched[i]=this.isTouched[i];
            }
        }

        //show where screen is touched
        void drawTouchPoints(ShapeRenderer shapeRenderer) {

            for(int i = 0; i < this.maxTouchCount; i++) {
                if (this.isTouched[i]) {
                    shapeRenderer.setColor(0, 1, 0, 0.5f);
                    shapeRenderer.circle(this.touchPos[i].x,this.touchPos[i].y, width/100, 20);
                    if(i>0) {
                        //shapeRenderer.setColor(0, 1, 0, 1);
                        //shapeRenderer.line(this.touchPos[i-1].x,this.touchPos[i-1].y,this.touchPos[i].x,this.touchPos[i].y);
                    }
                }
            }
        }
    }

    //custom arraylist with maximum elements, first one is kicked out if max is reached
    static class BoundedArrayList<T> extends ArrayList<T> {
        private int maxSize;
        public BoundedArrayList(int size)
        {
            this.maxSize = size;
        }

        public void addLast(T e)
        {
            this.add(e);
            if(this.size() > this.maxSize)
                this.remove(0);
        }
    }


}
