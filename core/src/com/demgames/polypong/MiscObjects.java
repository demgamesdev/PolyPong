package com.demgames.polypong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import javax.xml.bind.util.ValidationEventCollector;

public class MiscObjects {
    private static final String TAG = "MiscObjects";

    private IGlobals globals;

    //global sendclasses

    private int myPlayerNumber;
    private float screenWidth, screenHeight,width, height;
    Vector2 zoomPoint;
    float zoomLevel;
    float maxZoomLevel;

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
    void setMaxZoom(Rectangle rectangle, float width) {
        Gdx.app.debug(TAG,"camera height " +Float.toString(width*this.globals.getGameVariables().height/this.globals.getGameVariables().width));
        Gdx.app.debug(TAG,"game rect height " +Float.toString(rectangle.height));
        if(rectangle.height/(width*this.globals.getGameVariables().height/this.globals.getGameVariables().width)>rectangle.width/width) {
            this.maxZoomLevel = rectangle.height/(width*this.globals.getGameVariables().height/this.globals.getGameVariables().width);
        } else {
            this.maxZoomLevel = rectangle.width/width;
        }
    }

    //transform touch input for variable zoomlevel
    Vector2 transformZoom(float touchX,float touchY, Camera camera, Vector2 fixedPoint) {
        return(new Vector2(touchX*zoomLevel, zoomLevel*(touchY + camera.viewportHeight/2)+fixedPoint.y));
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
        Vector2[] newVectorArray = new Vector2[vectorArray.length];
        Gdx.app.debug(TAG,Float.toString(vectorArray[0].x));
        for(int i=0;i<vectorArray.length;i++) {
            newVectorArray[i] = new Vector2(vectorArray[i]).scl(scale).rotate(degrees);
        }
        return(newVectorArray);
    }

    static float[] transformFloatVertices(float[] floatArray, float scale, float translateX, float translateY) {
        float[] tempfloat= new float[floatArray.length];


        for(int i =0; i<floatArray.length/2;i++) {
            tempfloat[2*i] = floatArray[2*i];
            tempfloat[2*i+1] = floatArray[2*i+1];

            tempfloat[2*i]+=translateX;
            tempfloat[2*i+1]+=translateY;

            tempfloat[2*i]*=scale;
            tempfloat[2*i+1]*=scale;
        }
        return(tempfloat);
    }

    static float[] getMinXY(float[] floatArray){
        float[] tempFloat = new float[]{floatArray[0],floatArray[1]};
        for(int i =1; i<floatArray.length/2;i++) {
            if(floatArray[2*i]<tempFloat[0]) {
                tempFloat[0]= floatArray[2*i];
            }

            if(floatArray[2*i+1]<tempFloat[1]) {
                tempFloat[1]= floatArray[2*i+1];
            }
        }
        return(tempFloat);
    }

    static float[] getMaxXY(float[] floatArray){
        float[] tempFloat = new float[]{floatArray[0],floatArray[1]};
        for(int i =1; i<floatArray.length/2;i++) {
            if(floatArray[2*i]>tempFloat[0]) {
                tempFloat[0]= floatArray[2*i];
            }

            if(floatArray[2*i+1]>tempFloat[1]) {
                tempFloat[1]= floatArray[2*i+1];
            }
        }
        return(tempFloat);
    }

    //touchclass

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
        void checkTouches(boolean updateBat,Vector2 offset, Camera camera, Vector2 fixedPoint) {
            for(int i=0;i<this.maxTouchCount;i++) {
                if (Gdx.input.isTouched(i)) {
                    this.isTouched[i] = true;
                    this.touchPos[i]=transformZoom((Gdx.input.getX(i)/screenWidth-0.5f) *camera.viewportWidth,(-Gdx.input.getY(i)/screenHeight+0.5f) *camera.viewportHeight,camera, fixedPoint);
                } else {
                    this.isTouched[i] = false;
                }
                if(!this.lastIsTouched[i] && this.isTouched[i]) {
                    this.startTouchPos[i]=this.touchPos[i];
                }
            }

            if(updateBat && this.isTouched[0] && !this.isTouched[1]) {
                globals.getGameVariables().bats[globals.getGameVariables().myPlayerNumber].batPosition.set(this.touchPos[0]);
            }
        }

        private void zoom (float originalDistance, float currentDistance){
            float newZoomLevel=zoomLevel+(originalDistance-currentDistance)/5;
            if(newZoomLevel<=maxZoomLevel && newZoomLevel>=1.0f) {
                zoomLevel=newZoomLevel;

            } else if(newZoomLevel>maxZoomLevel) {
                zoomLevel=maxZoomLevel;//2.0f;
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
