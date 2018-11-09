package com.demgames.polypong;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.badlogic.gdx.Input.Keys.V;

public class ClassicGame extends ApplicationAdapter{
    private IGlobals globalVariables;

    //thisisnetworkbranch
    public ClassicGame(IGlobals globalVariables_ ) {
        this.globalVariables=globalVariables_;
    }

    private SpriteBatch batch;
    private BitmapFont font;
    private int width, height;
    private ShapeRenderer shapeRenderer;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Matrix4 debugMatrix;
    private OrthographicCamera camera;

    Borders borders;

    private Body leftBorderBody,bottomBorderBody,rightBorderBody,topBorderBody,batBody, otherBatBody;
    private Polygon[] playerScreenShapes;
    private Polygon playerScreen;

    private float batWidth, batHeight, ballRadius;

    private float borderDamping=1f;

    private int numberOfBalls;

    private float zoomLevel=1;

    private long frameNumber=0;
    private int sendFrameSkip=1;
    private long currentMillis=System.currentTimeMillis();

    private Ball [] balls;
    private ArrayList<Integer> sendBallKineticsAL=new ArrayList<Integer>(Arrays.asList(new Integer[]{}));
    private ArrayList<Integer> sendBallScreenChangeAL=new ArrayList<Integer>(Arrays.asList(new Integer[]{}));

    //network
    private IGlobals.SendVariables.SendBallKinetics sendBallKinetics=new IGlobals.SendVariables.SendBallKinetics();
    private IGlobals.SendVariables.SendBallScreenChange sendBallScreenChange=new IGlobals.SendVariables.SendBallScreenChange();
    private IGlobals.SendVariables.SendBat sendBat=new IGlobals.SendVariables.SendBat();
    private IGlobals.SendVariables.SendScore sendScore=new IGlobals.SendVariables.SendScore();

    Touches touches;

    private final float PIXELS_TO_METERS = 100f;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        camera = new OrthographicCamera(width, height);
        //camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.position.set(0, -height/2, 0);
        camera.update();

        debugMatrix=new Matrix4(camera.combined);
        debugMatrix.scale(PIXELS_TO_METERS, PIXELS_TO_METERS,1);
        debugRenderer=new Box2DDebugRenderer();

        if(globalVariables.getGameVariables().gravityState) {
            world=new World(new Vector2(0,-50f),true);
        } else {
            world=new World(new Vector2(0,0f),true);
        }

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);

        touches=new Touches(2);

        numberOfBalls=globalVariables.getGameVariables().numberOfBalls;


        batWidth=width/5;
        batHeight=width/15;

        ballRadius=width/50;

        borders=new Borders();

        balls=new Ball[numberOfBalls];
        for(int i=0;i<numberOfBalls;i++) {
            balls[i]= new Ball(new Vector2(globalVariables.getGameVariables().ballsPositions[i].x*width/PIXELS_TO_METERS,globalVariables.getGameVariables().ballsPositions[i].y*height/PIXELS_TO_METERS),new Vector2(0,0),(1+globalVariables.getGameVariables().ballsSizes[i])*width/50/PIXELS_TO_METERS,i,0);
        }

        BodyDef batBodyDef= new BodyDef();
        batBodyDef.type= BodyDef.BodyType.DynamicBody;
        batBodyDef.position.set(new Vector2(0,-height*0.8f).scl(1/PIXELS_TO_METERS));

        batBody=world.createBody(batBodyDef);
        PolygonShape batShape= new PolygonShape();
        batShape.setAsBox(batWidth/2/PIXELS_TO_METERS,batHeight/2/PIXELS_TO_METERS);
        FixtureDef batFd= new FixtureDef();
        batFd.restitution=0.7f;
        batFd.shape = batShape;
        batShape.dispose();
        batFd.density=1f;
        batFd.friction=0f;
        batBody.createFixture(batFd);


        BodyDef otherBatBodyDef= new BodyDef();
        otherBatBodyDef.type= BodyDef.BodyType.KinematicBody;
        otherBatBodyDef.position.set(new Vector2(0,height*0.8f).scl(1/PIXELS_TO_METERS));
        otherBatBody=world.createBody(batBodyDef);
        otherBatBody.createFixture(batFd);
        globalVariables.getGameVariables().batPosition=new Vector2(otherBatBody.getPosition().x/width*PIXELS_TO_METERS,otherBatBody.getPosition().y/height*PIXELS_TO_METERS);


        BodyDef playerScreenBodyDef = new BodyDef();
        playerScreenBodyDef.type = BodyDef.BodyType.DynamicBody;
        FixtureDef playerScreenFd = new FixtureDef();
        playerScreenFd.isSensor = true;

        playerScreenShapes = new Polygon[2];
        for (int i=0;i<2;i++) {
            playerScreenShapes[i]=new Polygon();
        }
        Vector2[] playerScreenVertices = new Vector2[6];
        float rotatePlayerScreenDegrees = 0;
        switch (globalVariables.getSettingsVariables().myPlayerScreen) {
            case 0: rotatePlayerScreenDegrees = 0;
            case 1: rotatePlayerScreenDegrees = 180;
        }
        playerScreenVertices[0] = new Vector2(-width/2,-height);
        playerScreenVertices[1] = new Vector2(width/2,-height);
        playerScreenVertices[2] = new Vector2(width/2,0);
        playerScreenVertices[3] = new Vector2(width/2,height);
        playerScreenVertices[4] = new Vector2(-width/2,height);
        playerScreenVertices[5] = new Vector2(-width/2,0);

        playerScreen = new Polygon(new float[]{playerScreenVertices[3].x,playerScreenVertices[3].y,playerScreenVertices[4].x,playerScreenVertices[4].y,playerScreenVertices[5].x,
                playerScreenVertices[5].y,playerScreenVertices[2].x,playerScreenVertices[2].y});

        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                // Check to see if the collision is between the second sprite and the bottom of the screen
                // If so apply a random amount of upward force to both objects... just because
                //setupBorderCollision(contact,boxBody);

            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        world.dispose();
        debugRenderer.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touches.checkTouches();
        touches.checkZoomGesture();


        /*Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_BLEND);*/


        Gdx.app.debug("ClassicGame", "pos "+Float.toString(touches.touchPos[1].x)+", "+Float.toString(touches.touchPos[1].y));

        //Gdx.app.debug("ClassicGame", "myplayerscreen " + Integer.toString(globalVariables.getSettingsVariables().myPlayerScreen));

        for (int i = 0; i < numberOfBalls; i++) {
            if(globalVariables.getGameVariables().ballsPlayerScreens[i]==globalVariables.getSettingsVariables().myPlayerScreen) {
                balls[i].checkPlayerScreenContains();
                //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(balls[i].ballNumber)+" computed");
                balls[i].body.setType(BodyDef.BodyType.DynamicBody);
                for (int j = 0; j < touches.maxTouchCount; j++) {
                    if (touches.isTouched[j]){
                        //boxBody.applyForceToCenter(new Vector2(touches.get(i).touchPos.cpy().scl(1/PIXELS_TO_METERS).sub(boxBody.getPosition())),true);
                        if(globalVariables.getGameVariables().attractionState) {
                            balls[i].body.applyForceToCenter(touches.touchPos[j].cpy().scl(1 / PIXELS_TO_METERS).sub(balls[i].body.getPosition()), true);
                        }
                    }
                }

            } else {
                //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(balls[i].ballNumber)+" NOT computed");
                if (frameNumber%sendFrameSkip==0) {
                    balls[i].body.setType(BodyDef.BodyType.KinematicBody);

                    balls[i].body.setTransform(new Vector2(globalVariables.getGameVariables().ballsPositions[i].x*width/PIXELS_TO_METERS,globalVariables.getGameVariables().ballsPositions[i].y*height/PIXELS_TO_METERS),0);
                    balls[i].body.setLinearVelocity(new Vector2(globalVariables.getGameVariables().ballsVelocities[i].x*width/PIXELS_TO_METERS,globalVariables.getGameVariables().ballsVelocities[i].y*height/PIXELS_TO_METERS));

                    frameNumber=0;
                }
                //balls[i].body.applyForceToCenter((-globalVariables.getGameVariables().ballsPositions[i].x*width+balls[i].body.getPosition().x)*100/PIXELS_TO_METERS,(-globalVariables.getGameVariables().ballsPositions[i].y*height+balls[i].body.getPosition().y)*100/PIXELS_TO_METERS,true);
            }
        }
        batBody.setTransform(touches.touchPos[0].cpy().scl(1/PIXELS_TO_METERS),0);
        batBody.setLinearVelocity(touches.touchPos[0].cpy().sub(touches.lastTouchPos[0]));

        otherBatBody.setTransform(new Vector2(globalVariables.getGameVariables().batPosition.x*width/PIXELS_TO_METERS,globalVariables.getGameVariables().batPosition.y*width/PIXELS_TO_METERS),0);
        world.step(1/60f, 6,4);

        sendBallPlayerScreenChange(sendBallScreenChangeAL);
        sendBall(sendBallKineticsAL);
        sendBatFunction(new Vector2(batBody.getPosition().x / width * PIXELS_TO_METERS,batBody.getPosition().y / width * PIXELS_TO_METERS));
        sendBallKineticsAL=new ArrayList<Integer>(Arrays.asList(new Integer[]{}));
        sendBallScreenChangeAL=new ArrayList<Integer>(Arrays.asList(new Integer[]{}));



        //Gdx.app.debug("ClassicGame", "ball 0 vely "+Float.toString(balls[0].body.getLinearVelocity().y/height*PIXELS_TO_METERS) + " ps "+Integer.toString(globalVariables.getGameVariables().ballsPlayerScreens[balls[0].ballNumber]));
        //boxPosition= new Vector2(boxBody.getPosition().x,boxBody.getPosition().y);


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);

        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(-width/2, -height, width, height*2);

        shapeRenderer.setColor(0,0,0,1);
        shapeRenderer.rect(-width/2, -10, width, 20);

        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.rect(batBody.getPosition().x*PIXELS_TO_METERS-batWidth/2, batBody.getPosition().y*PIXELS_TO_METERS-batHeight/2, batWidth, batHeight);
        shapeRenderer.rect(otherBatBody.getPosition().x*PIXELS_TO_METERS-batWidth/2, otherBatBody.getPosition().y*PIXELS_TO_METERS-batHeight/2, batWidth, batHeight);

        touches.drawTouchPoints();

        for(int i = 0; i < numberOfBalls; i++) {

            balls[i].display();
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        //globalVariables.setNumberOfBalls(2);
        /*font.draw(batch,Integer.toString(touchCounter)+" fingers touching, fps: "+Float.toString(Gdx.graphics.getFramesPerSecond()), width /2, height /2);
        font.draw(batch,/*"boxspeed "+Float.toString(boxBody.getLinearVelocity().len())+", touchX "+Float.toString(touches.get(0).touchPos.x)+", touchY "+Float.toString(touches.get(0).touchPos.y), 0, height *0.4f);
        font.draw(batch,"angvel0 "+Float.toString(balls[0].body.getAngularVelocity()),0, height *0.3f);
        font.draw(batch,"zoom "+Float.toString(zoomLevel),0, height *0.2f);*/

        //

        batch.end();

        //debugRenderer.render(world,camera.combined.cpy().scale(PIXELS_TO_METERS,PIXELS_TO_METERS,1));

        touches.updateLasts();
        frameNumber++;
    }

    /********* SEND FUNCTIONS *********/

    void sendBall(ArrayList<Integer> AL) {
        if (AL.size()>0) {
            sendBallKinetics.ballNumbers = AL.toArray(new Integer[0]);
            sendBallKinetics.ballPlayerScreens = new int[AL.size()];
            sendBallKinetics.ballPositions = new Vector2[AL.size()];
            sendBallKinetics.ballVelocities = new Vector2[AL.size()];


            for (int i = 0; i < AL.size(); i++) {
                sendBallKinetics.ballPlayerScreens[i] = globalVariables.getSettingsVariables().myPlayerScreen;
                sendBallKinetics.ballPositions[i] = new Vector2(balls[sendBallKinetics.ballNumbers[i]].body.getPosition().x / width * PIXELS_TO_METERS, balls[sendBallKinetics.ballNumbers[i]].body.getPosition().y / height * PIXELS_TO_METERS);
                sendBallKinetics.ballVelocities[i] = new Vector2(balls[sendBallKinetics.ballNumbers[i]].body.getLinearVelocity().x / width * PIXELS_TO_METERS, balls[sendBallKinetics.ballNumbers[i]].body.getLinearVelocity().y / height * PIXELS_TO_METERS);
            }

            globalVariables.getNetworkVariables().connectionList.get(0).sendUDP(sendBallKinetics);
            //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(theBall.ballNumber)+" sent");
        }
    }

    void sendBallPlayerScreenChange(ArrayList<Integer> AL) {
        if (AL.size()>0) {
            sendBallScreenChange.ballNumbers = AL.toArray(new Integer[0]);
            sendBallScreenChange.ballPlayerScreens = new int[AL.size()];
            sendBallScreenChange.ballPositions = new Vector2[AL.size()];
            sendBallScreenChange.ballVelocities = new Vector2[AL.size()];


            for (int i = 0; i < AL.size(); i++) {
                sendBallScreenChange.ballPlayerScreens[i] = (globalVariables.getSettingsVariables().myPlayerScreen + 1) % 2;
                sendBallScreenChange.ballPositions[i] = new Vector2(balls[sendBallScreenChange.ballNumbers[i]].body.getPosition().x / width * PIXELS_TO_METERS, balls[sendBallScreenChange.ballNumbers[i]].body.getPosition().y / height * PIXELS_TO_METERS);
                sendBallScreenChange.ballVelocities[i] = new Vector2(balls[sendBallScreenChange.ballNumbers[i]].body.getLinearVelocity().x / width * PIXELS_TO_METERS, balls[sendBallScreenChange.ballNumbers[i]].body.getLinearVelocity().y / height * PIXELS_TO_METERS);
            }
            globalVariables.getNetworkVariables().connectionList.get(0).sendTCP(sendBallScreenChange);
        }
    }

    void sendBatFunction(Vector2 position) {
        sendBat.batPosition=position;
        sendBat.batOrientation=0;
        globalVariables.getNetworkVariables().connectionList.get(0).sendUDP(sendBat);
    }



    /********* OTHER FUNCTIONS *********/

    private boolean zoom (float originalDistance, float currentDistance){
        float newZoomLevel=zoomLevel+(originalDistance-currentDistance)/5000;
        if(newZoomLevel<=2.0f && newZoomLevel>=1.0f) {
            zoomLevel=newZoomLevel;

        } else if(newZoomLevel>2.0f) {
            zoomLevel=2.0f;
        } else if(newZoomLevel<1.0f) {
            zoomLevel=1.0f;
        }
        camera.zoom=zoomLevel;
        camera.position.set(0,-height+height/2*zoomLevel,0);
        camera.update();

        return false;
    }

    /********* CLASSES *********/

    //create class Ball
    class Ball {
        Body body;

        float radius,m;

        int[] ballColor =new int[3];
        int ballNumber;
        boolean controlled;
        boolean updateState=true;

        //constructor for Ball
        Ball(Vector2 position_, Vector2 velocity_, float radius_,int ballNumber_,int playerScreen_) {


            radius=radius_;
            m=radius*0.1f*100;
            ballNumber=ballNumber_;

            //physics stuff

            BodyDef bodyDef= new BodyDef();

            if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
                bodyDef.type = BodyDef.BodyType.DynamicBody;
            } else {
                bodyDef.type = BodyDef.BodyType.KinematicBody;
            }
            bodyDef.bullet=true;

            bodyDef.position.set(position_);
            body = world.createBody(bodyDef);

            CircleShape shape = new CircleShape();
            shape.setPosition(new Vector2(0,0));
            shape.setRadius(radius);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape= shape;
            fixtureDef.density=m;

            body.createFixture(fixtureDef);
            shape.dispose();

            body.setLinearVelocity(velocity_);



            /*if(ballNumber==0) {
                ballColor[0]=1;
                ballColor[2]=0;
            }*/


            if (playerScreen_==0) {
                ballColor[0] =0;
                ballColor[1]=0;
                ballColor[2]=1;

            } else {
                ballColor[0] =1;
                ballColor[1]=0;
                ballColor[2]=0;
            }
            //(int)random(255);
        }

        //display ball
        void display() {
            if (globalVariables.getGameVariables().ballsPlayerScreens[ballNumber]==0) {
                ballColor[0] =0;
                ballColor[1]=0;
                ballColor[2]=1;

            } else {
                ballColor[0] =1;
                ballColor[1]=0;
                ballColor[2]=0;
            }

            shapeRenderer.setColor(ballColor[0], ballColor[1], ballColor[2], 0.5f);
            shapeRenderer.circle(this.body.getPosition().x*PIXELS_TO_METERS,this.body.getPosition().y*PIXELS_TO_METERS, radius*PIXELS_TO_METERS, 30);
        }

        void checkPlayerScreenContains() {
            if(playerScreen.contains(this.body.getPosition().cpy().scl(PIXELS_TO_METERS))) {
                sendBallScreenChangeAL.add(this.ballNumber);
            } else {
                sendBallKineticsAL.add(this.ballNumber);
            }
        }

    }

    class Touches {
        private int maxTouchCount;
        private Vector2[] touchPos;
        private Vector2[] lastTouchPos;
        private Vector2[] startTouchPos;
        private boolean[] isTouched;
        private boolean[] lastIsTouched;

        Touches(int maxTouchCount_) {
            maxTouchCount=maxTouchCount_;
            touchPos=new Vector2[maxTouchCount];
            lastTouchPos=new Vector2[maxTouchCount];
            startTouchPos=new Vector2[maxTouchCount];
            isTouched=new boolean[maxTouchCount];
            lastIsTouched=new boolean[maxTouchCount];

            for (int i=0;i<maxTouchCount;i++) {
                touchPos[i]=new Vector2(0,-height/2);
                lastTouchPos[i]=touchPos[i];
                startTouchPos[i]=touchPos[i];
                isTouched[i]=false;
                lastIsTouched[i]=false;
            }
        }

        void checkTouches() {
            for(int i=0;i<this.maxTouchCount;i++) {
                if (Gdx.input.isTouched(i)) {
                    this.isTouched[i] = true;
                    this.touchPos[i]=new Vector2(Gdx.input.getX(i)-width/2,-Gdx.input.getY(i));
                } else {
                    this.isTouched[i] = false;
                }
                if(!this.lastIsTouched[i] && this.isTouched[i]) {
                    this.startTouchPos[i]=this.touchPos[i];
                }
            }
        }

        void checkZoomGesture() {
            if(this.isTouched[0] && this.isTouched[1]) {
                zoom(this.startTouchPos[0].cpy().sub(this.startTouchPos[1]).len(),this.touchPos[0].cpy().sub(this.touchPos[1]).len());
            }

            if(!this.isTouched[0] || !this.isTouched[1]) {
                if(zoomLevel != 2.0 || zoomLevel != 1.0) {
                    zoomLevel=Math.round(zoomLevel);
                    camera.zoom=zoomLevel;
                    camera.position.set(0,-height+height/2*zoomLevel,0);
                    camera.update();
                }

            }
        }

        void updateLasts() {
            for(int i=0;i<this.maxTouchCount;i++) {
                this.lastTouchPos[i]=this.touchPos[i];
                this.lastIsTouched[i]=this.isTouched[i];
            }
        }

        void drawTouchPoints() {
            for(int i = 0; i < this.maxTouchCount; i++) {
                if (this.isTouched[i]) {
                    shapeRenderer.setColor(0, 1, 0, 0.5f);
                    shapeRenderer.circle(this.touchPos[i].x,this.touchPos[i].y, ballRadius, 100);
                    if(i>0) {
                        shapeRenderer.setColor(0, 1, 0, 1);
                        shapeRenderer.line(this.touchPos[i-1].x,this.touchPos[i-1].y,this.touchPos[i].x,this.touchPos[i].y);
                    }
                }
            }
        }
    }

    class Borders{
        Borders() {
            BodyDef borderBodyDef = new BodyDef();
            borderBodyDef.type = BodyDef.BodyType.StaticBody;
            borderBodyDef.position.set(0,0);
            FixtureDef borderFd=new FixtureDef();
            borderFd.restitution = 0.7f;
            //borderFd.density=100000f;

            Vector2 [] borderVertices=new Vector2[4];
            borderVertices[0]=new Vector2(-width/2/PIXELS_TO_METERS,-height/PIXELS_TO_METERS);
            borderVertices[1]=new Vector2(width/2/PIXELS_TO_METERS,-height/PIXELS_TO_METERS);
            borderVertices[2]=new Vector2(width/2/PIXELS_TO_METERS,height/PIXELS_TO_METERS);
            borderVertices[3]= new Vector2(-width/2/PIXELS_TO_METERS,height/PIXELS_TO_METERS);

            final EdgeShape leftBorderShape,bottomBorderShape,rightBorderShape,topBorderShape;
            leftBorderShape=new EdgeShape();
            bottomBorderShape=new EdgeShape();
            rightBorderShape= new EdgeShape();
            topBorderShape=new EdgeShape();

            bottomBorderShape.set(borderVertices[0],borderVertices[1]);
            rightBorderShape.set(borderVertices[1],borderVertices[2]);
            topBorderShape.set(borderVertices[2],borderVertices[3]);
            leftBorderShape.set(borderVertices[3],borderVertices[0]);

            leftBorderBody = world.createBody(borderBodyDef);
            bottomBorderBody= world.createBody(borderBodyDef);
            rightBorderBody= world.createBody(borderBodyDef);
            topBorderBody=world.createBody(borderBodyDef);

            borderFd.shape = leftBorderShape;
            leftBorderBody.createFixture(borderFd);

            borderFd.shape = bottomBorderShape;
            bottomBorderBody.createFixture(borderFd);

            borderFd.shape = rightBorderShape;
            rightBorderBody.createFixture(borderFd);

            borderFd.shape = topBorderShape;
            topBorderBody.createFixture(borderFd);

            leftBorderShape.dispose();
            bottomBorderShape.dispose();
            rightBorderShape.dispose();
            topBorderShape.dispose();
        }
    }

}
