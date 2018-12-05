package com.demgames.polypong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class MiscObjects {
    private static final String TAG = "MiscObjects";

    private IGlobals globalVariables;

    //global sendclasses

    private IGlobals.SendVariables.SendFrequents sendFrequents=new IGlobals.SendVariables.SendFrequents();
    private IGlobals.SendVariables.SendFieldChange sendFieldChange =new IGlobals.SendVariables.SendFieldChange();
    private IGlobals.SendVariables.SendConnectionState sendConnectionState=new IGlobals.SendVariables.SendConnectionState();

    private int myPlayerNumber;
    private float screenWidth, screenHeight,width, height;
    float zoomLevel;

    //class for touch input and gestures

    public Touches touches;

    MiscObjects(IGlobals globalVariables_ , int myPlayerNumber_, float width_, float height_) {
        this.globalVariables = globalVariables_;
        this.myPlayerNumber = myPlayerNumber_;
        this.width = width_;
        this.height = height_;
        this.touches=new Touches(2);

        this.screenWidth = globalVariables.getGameVariables().width;
        this.screenHeight = globalVariables.getGameVariables().height;

        this.zoomLevel = 1f;
    }

    /********* OTHER FUNCTIONS *********/
    //adjust camera for zooming


    //transform touch input for variable zoomlevel
    Vector2 transformZoom(Vector2 vec) {
        Vector2 camPos = new Vector2(0,-height+height/2*zoomLevel);
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
        if (sendFieldChangeBallsAL.size()>0) {
            sendFieldChange.myPlayerNumber=myPlayerNumber;
            sendFieldChange.balls = new IGlobals.Ball[sendFieldChangeBallsAL.size()];


            for (int i = 0; i < sendFieldChangeBallsAL.size(); i++) {
                sendFieldChange.balls[i]= new IGlobals.Ball();
                sendFieldChange.balls[i].ballNumber = sendFieldChangeBallsAL.get(i).ballNumber;
                sendFieldChange.balls[i].ballPlayerField = sendFieldChangeBallsAL.get(i).tempPlayerField;
                sendFieldChange.balls[i].ballDisplayState = sendFieldChangeBallsAL.get(i).ballDisplayState;
                sendFieldChange.balls[i].ballPositionX = sendFieldChangeBallsAL.get(i).ballBody.getPosition().x;
                sendFieldChange.balls[i].ballPositionY = sendFieldChangeBallsAL.get(i).ballBody.getPosition().y;
                sendFieldChange.balls[i].ballVelocityX = sendFieldChangeBallsAL.get(i).ballBody.getLinearVelocity().x;
                sendFieldChange.balls[i].ballVelocityY = sendFieldChangeBallsAL.get(i).ballBody.getLinearVelocity().y;
                sendFieldChange.balls[i].ballAngle = sendFieldChangeBallsAL.get(i).ballBody.getAngle();
                sendFieldChange.balls[i].ballAngularVelocity = sendFieldChangeBallsAL.get(i).ballBody.getAngularVelocity();
                Gdx.app.debug(TAG, "fieldchange of ball "+ sendFieldChange.balls[i].ballNumber +" sent");
            }
            globalVariables.getSettingsVariables().sendToAllClients(sendFieldChange,"tcp");
        }
    }
    void sendConnectionStateFunction() {
        sendConnectionState.myPlayerNumber=globalVariables.getSettingsVariables().myPlayerNumber;
        sendConnectionState.connectionState=globalVariables.getSettingsVariables().clientConnectionStates[globalVariables.getSettingsVariables().myPlayerNumber];
        globalVariables.getSettingsVariables().sendToAllClients(sendConnectionState,"tcp");
    }

    void sendFrequentsFunction(ArrayList<ClassicGameObjects.Ball> sendBallsAL, ClassicGameObjects.Bat bat, int[] scores) {

        sendFrequents.myPlayerNumber=myPlayerNumber;
        sendFrequents.balls = new IGlobals.Ball[sendBallsAL.size()];
        sendFrequents.bat = new IGlobals.Bat();
        sendFrequents.scores = scores;

        for (int i = 0; i < sendBallsAL.size(); i++) {
            sendFrequents.balls[i] = new IGlobals.Ball();
            sendFrequents.balls[i].ballNumber = sendBallsAL.get(i).ballNumber;
            sendFrequents.balls[i].ballPlayerField = myPlayerNumber;
            sendFrequents.balls[i].ballDisplayState = sendBallsAL.get(i).ballDisplayState;

            if(sendFrequents.balls[i].ballDisplayState ==1) {
                sendFrequents.balls[i].ballPositionX = sendBallsAL.get(i).ballBody.getPosition().x;
                sendFrequents.balls[i].ballPositionY = sendBallsAL.get(i).ballBody.getPosition().y;
                sendFrequents.balls[i].ballVelocityX = sendBallsAL.get(i).ballBody.getLinearVelocity().x;
                sendFrequents.balls[i].ballVelocityY = sendBallsAL.get(i).ballBody.getLinearVelocity().y;
                sendFrequents.balls[i].ballAngle = sendBallsAL.get(i).ballBody.getAngle();
                sendFrequents.balls[i].ballAngularVelocity = sendBallsAL.get(i).ballBody.getAngularVelocity();
                Gdx.app.debug(TAG, "ball "+Integer.toString(sendFrequents.balls[i].ballNumber)+" position "+Float.toString(sendFrequents.balls[i].ballPositionX)+" sent");
                Gdx.app.debug(TAG, "ball "+Integer.toString(sendFrequents.balls[i].ballNumber)+" velocity "+Float.toString(sendFrequents.balls[i].ballVelocityX)+" sent");
            }
            Gdx.app.debug(TAG, "ball "+Integer.toString(sendFrequents.balls[i].ballNumber)+" displaystate "+Integer.toString(sendFrequents.balls[i].ballDisplayState)+" sent");
        }

        sendFrequents.bat.batPositionX=bat.batBody.getPosition().x;
        sendFrequents.bat.batPositionY=bat.batBody.getPosition().y;
        sendFrequents.bat.batVelocityX=bat.batBody.getLinearVelocity().x;
        sendFrequents.bat.batVelocityY=bat.batBody.getLinearVelocity().y;
        sendFrequents.bat.batAngle =bat.batBody.getAngle();
        sendFrequents.bat.batAngularVelocity =bat.batBody.getAngularVelocity();

        globalVariables.getSettingsVariables().sendToAllClients(sendFrequents,"udp");
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
