package com.demgames.polypong;

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

public class ClassicGame extends ApplicationAdapter implements InputProcessor{
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

    private Body leftBorderBody,bottomBorderBody,rightBorderBody,topBorderBody,batBody, player0ScreenBody,player1ScreenBody, playerLineBody;
    PolygonShape[] playerScreenShapes;

    private float batWidth, batHeight, ballRadius;

    private float borderDamping=1f;

    private int numberOfBalls;

    private float zoomLevel=1;

    private Ball [] balls;

    //network
    private IGlobals.SendVariables.SendBallKinetics sendBallKinetics=new IGlobals.SendVariables.SendBallKinetics();
    private IGlobals.SendVariables.SendBallScreenChange sendBallScreenChange=new IGlobals.SendVariables.SendBallScreenChange();
    private IGlobals.SendVariables.SendBat sendBat=new IGlobals.SendVariables.SendBat();
    private IGlobals.SendVariables.SendScore sendScore=new IGlobals.SendVariables.SendScore();


    private Map<Integer,TouchInfo> touches = new HashMap<Integer,TouchInfo>();

    private final float PIXELS_TO_METERS = 100f;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        camera = new OrthographicCamera(width, height);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        debugMatrix=new Matrix4(camera.combined);
        debugMatrix.scale(PIXELS_TO_METERS, PIXELS_TO_METERS,1);
        debugRenderer=new Box2DDebugRenderer();

        Gdx.input.setInputProcessor(this);

        if(globalVariables.getGameVariables().gravityState) {
            world=new World(new Vector2(0,-50f),true);
        } else {
            world=new World(new Vector2(0,0f),true);
        }

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);



        for(int i = 0; i < 5; i++){
            touches.put(i, new TouchInfo());
        }

        numberOfBalls=globalVariables.getGameVariables().numberOfBalls;


        batWidth=width/5;
        batHeight=width/15;

        ballRadius=width/50;

        balls=new Ball[numberOfBalls];
        for(int i=0;i<numberOfBalls;i++) {
            balls[i]= new Ball(new Vector2(globalVariables.getGameVariables().ballsPositions[i].x*width/PIXELS_TO_METERS,globalVariables.getGameVariables().ballsPositions[i].y*height/PIXELS_TO_METERS),new Vector2(0,0),(1+globalVariables.getGameVariables().ballsSizes[i])*width/50/PIXELS_TO_METERS,i,0);
        }

        BodyDef batBodyDef= new BodyDef();
        batBodyDef.type= BodyDef.BodyType.KinematicBody;
        batBodyDef.position.set(new Vector2(width/2,height*0.2f).scl(1/PIXELS_TO_METERS));

        batBody=world.createBody(batBodyDef);
        PolygonShape batShape= new PolygonShape();
        batShape.setAsBox(batWidth/2/PIXELS_TO_METERS,batHeight/2/PIXELS_TO_METERS);
        FixtureDef batFd= new FixtureDef();
        batFd.shape = batShape;
        batFd.density=1;
        batFd.friction=0;
        batBody.createFixture(batFd);
        batShape.dispose();

        BodyDef borderBodyDef = new BodyDef();
        borderBodyDef.type = BodyDef.BodyType.StaticBody;
        borderBodyDef.position.set(0,0);
        FixtureDef borderFd=new FixtureDef();

        Vector2 [] borderVertices=new Vector2[4];
        borderVertices[0]=new Vector2(0,height/PIXELS_TO_METERS*2);
        borderVertices[1]=new Vector2(0,0);
        borderVertices[2]=new Vector2(width/PIXELS_TO_METERS,0);
        borderVertices[3]= new Vector2(width/PIXELS_TO_METERS,height/PIXELS_TO_METERS*2);

        final EdgeShape leftBorderShape,bottomBorderShape,rightBorderShape,topBorderShape;
        leftBorderShape=new EdgeShape();
        bottomBorderShape=new EdgeShape();
        rightBorderShape= new EdgeShape();
        topBorderShape=new EdgeShape();

        leftBorderShape.set(borderVertices[0],borderVertices[1]);
        bottomBorderShape.set(borderVertices[1],borderVertices[2]);
        rightBorderShape.set(borderVertices[2],borderVertices[3]);
        topBorderShape.set(borderVertices[3],borderVertices[0]);

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

        BodyDef playerScreenBodyDef = new BodyDef();
        playerScreenBodyDef.type = BodyDef.BodyType.DynamicBody;
        FixtureDef playerScreenFd = new FixtureDef();
        playerScreenFd.isSensor = true;

        PolygonShape playerScreenShape = new PolygonShape();
        Vector2[] playerScreenVertices = new Vector2[6];
        float rotatePlayerScreenDegrees = 0;
        switch (globalVariables.getSettingsVariables().myPlayerScreen) {
            case 0: rotatePlayerScreenDegrees = 0;
            case 1: rotatePlayerScreenDegrees = 180;
        }
        Vector2 midPoint = new Vector2(width/PIXELS_TO_METERS,height/PIXELS_TO_METERS);
        playerScreenVertices[0] = rotateAroundPoint(new Vector2(0,0),midPoint,rotatePlayerScreenDegrees);
        playerScreenVertices[1] = rotateAroundPoint(new Vector2(width/PIXELS_TO_METERS,0),midPoint,rotatePlayerScreenDegrees);
        playerScreenVertices[2] = rotateAroundPoint(new Vector2(width/PIXELS_TO_METERS,height/PIXELS_TO_METERS),midPoint,rotatePlayerScreenDegrees);
        playerScreenVertices[3] = rotateAroundPoint(new Vector2(width/PIXELS_TO_METERS,2*height/PIXELS_TO_METERS),midPoint,rotatePlayerScreenDegrees);
        playerScreenVertices[4] = rotateAroundPoint(new Vector2(0,2*height/PIXELS_TO_METERS),midPoint,rotatePlayerScreenDegrees);
        playerScreenVertices[5] = rotateAroundPoint(new Vector2(0,height/PIXELS_TO_METERS),midPoint,rotatePlayerScreenDegrees);


        Vector2[] player0Screen = new Vector2[]{playerScreenVertices[0],playerScreenVertices[1],playerScreenVertices[2],playerScreenVertices[5]};
        Vector2[] player1Screen = new Vector2[]{playerScreenVertices[3],playerScreenVertices[4],playerScreenVertices[5],playerScreenVertices[2]};
        Gdx.app.debug("setup", "vertex "+Float.toString(player0Screen[0].x));
        playerScreenShape.set(player0Screen);
        //playerScreenShape[1].set(player1Screen);

        player0ScreenBody = world.createBody(playerScreenBodyDef);
        playerScreenFd.shape = playerScreenShape;
        player0ScreenBody.createFixture(playerScreenFd);
       // playerScreenFd.shape = playerScreenShape[1];
        //player1ScreenBody.createFixture(playerScreenFd);
        //playerScreenShape.dispose();


        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                // Check to see if the collision is between the second sprite and the bottom of the screen
                // If so apply a random amount of upward force to both objects... just because
                //setupBorderCollision(contact,boxBody);

                for(int i=0;i<numberOfBalls;i++) {
                    setupBorderCollision(contact,balls[i].body);
                    setupPlayerScreenContact(contact,balls[i]);

                }

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

        /*Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_BLEND);*/
        if(!Gdx.input.isTouched(0) || !Gdx.input.isTouched(1)) {
            if(zoomLevel != 2.0 || zoomLevel != 1.0) {
                zoomLevel=Math.round(zoomLevel);
                camera.zoom=zoomLevel;
                camera.position.set(width/2,height/2*zoomLevel,0);
                camera.update();
            }

        }

        int touchCounter=0;
        //Gdx.app.debug("ClassicGame", "my playerScreen "+Integer.toString(globalVariables.getSettingsVariables().myPlayerScreen));

        //Gdx.app.debug("ClassicGame", "myplayerscreen " + Integer.toString(globalVariables.getSettingsVariables().myPlayerScreen));

        for (int i = 0; i < numberOfBalls; i++) {
            if(globalVariables.getGameVariables().ballsPlayerScreens[i]==globalVariables.getSettingsVariables().myPlayerScreen) {
                //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(balls[i].ballNumber)+" computed");
                balls[i].body.setType(BodyDef.BodyType.DynamicBody);
                for (int j = 0; j < 5; j++) {
                    if (Gdx.input.isTouched(j)) {
                        touchCounter++;

                        //boxBody.applyForceToCenter(new Vector2(touches.get(i).touchPos.cpy().scl(1/PIXELS_TO_METERS).sub(boxBody.getPosition())),true);
                        if(globalVariables.getGameVariables().attractionState) {
                            balls[i].body.applyForceToCenter(touches.get(j).touchPos.cpy().scl(1 / PIXELS_TO_METERS).sub(balls[i].body.getPosition()), true);
                        }
                    }
                }
                sendBall(balls[i]);

            } else {
                //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(balls[i].ballNumber)+" NOT computed");

                balls[i].body.setType(BodyDef.BodyType.KinematicBody);

                balls[i].body.setTransform(new Vector2(globalVariables.getGameVariables().ballsPositions[i].x*width/PIXELS_TO_METERS,globalVariables.getGameVariables().ballsPositions[i].y*height/PIXELS_TO_METERS),0);
                balls[i].body.setLinearVelocity(new Vector2(globalVariables.getGameVariables().ballsVelocities[i].x*width/PIXELS_TO_METERS,globalVariables.getGameVariables().ballsVelocities[i].y*height/PIXELS_TO_METERS));
                //balls[i].body.applyForceToCenter((-globalVariables.getGameVariables().ballsPositions[i].x*width+balls[i].body.getPosition().x)*100/PIXELS_TO_METERS,(-globalVariables.getGameVariables().ballsPositions[i].y*height+balls[i].body.getPosition().y)*100/PIXELS_TO_METERS,true);
            }
        }
        world.step(1/60f, 6,2);

        //Gdx.app.debug("ClassicGame", "ball 0 vely "+Float.toString(balls[0].body.getLinearVelocity().y/height*PIXELS_TO_METERS) + " ps "+Integer.toString(globalVariables.getGameVariables().ballsPlayerScreens[balls[0].ballNumber]));
        //boxPosition= new Vector2(boxBody.getPosition().x,boxBody.getPosition().y);


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);

        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(0, 0, width, height*2);

        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.rect(batBody.getPosition().x*PIXELS_TO_METERS-batWidth/2, batBody.getPosition().y*PIXELS_TO_METERS-batHeight/2, batWidth, batHeight);

        //

        for(int i = 0; i < 5; i++) {
            if (touches.get(i).touched) {
                shapeRenderer.setColor(0, 1, 0, 0.5f);
                shapeRenderer.circle(touches.get(i).touchPos.x,touches.get(i).touchPos.y, ballRadius, 100);
                if(i>0) {
                    shapeRenderer.setColor(0, 1, 0, 1);
                    shapeRenderer.line(touches.get(i-1).touchPos.x,touches.get(i-1).touchPos.y,touches.get(i).touchPos.x,touches.get(i).touchPos.y);
                }
            }
        }

        for(int i = 0; i < numberOfBalls; i++) {
            balls[i].display();
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        //globalVariables.setNumberOfBalls(2);
        font.draw(batch,Integer.toString(touchCounter)+" fingers touching, fps: "+Float.toString(Gdx.graphics.getFramesPerSecond()), width /2, height /2);
        font.draw(batch,/*"boxspeed "+Float.toString(boxBody.getLinearVelocity().len())+*/", touchX "+Float.toString(touches.get(0).touchPos.x)+", touchY "+Float.toString(touches.get(0).touchPos.y), 0, height *0.4f);
        font.draw(batch,"angvel0 "+Float.toString(balls[0].body.getAngularVelocity()),0, height *0.3f);
        font.draw(batch,"zoom "+Float.toString(zoomLevel),0, height *0.2f);

        //

        batch.end();

        debugRenderer.render(world,camera.combined.cpy().scale(PIXELS_TO_METERS,PIXELS_TO_METERS,1));


    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean keyDown(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(pointer < 5){
            touches.get(pointer).touchPos = new Vector2(screenX,height-screenY);
            touches.get(pointer).startTouchPos = new Vector2(screenX,height-screenY);
            touches.get(pointer).lastTouchPos = new Vector2(screenX,height-screenY);
            touches.get(pointer).touched = true;
            if(pointer==0) {
                batBody.setTransform(touches.get(pointer).touchPos.cpy().scl(1/PIXELS_TO_METERS),0);
                batBody.setLinearVelocity(0,0);
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(pointer < 5){
            //touches.get(pointer).touchX = 0;
            //touches.get(pointer).touchY = 0;
            touches.get(pointer).touched = false;
            if(pointer==0) {
                batBody.setTransform(touches.get(pointer).touchPos.cpy().scl(1/PIXELS_TO_METERS),0);
                batBody.setLinearVelocity(0,0);
            }


        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(pointer < 5){
            touches.get(pointer).touchPos = new Vector2(screenX,height-screenY);

            if(pointer==0 || pointer==1) {
                if(touches.get(0).touched && touches.get(1).touched) {
                    zoom(touches.get(0).startTouchPos.cpy().sub(touches.get(1).startTouchPos).len(),touches.get(0).touchPos.cpy().sub(touches.get(1).touchPos).len());
                }
            }
            if(pointer==0) {
                batBody.setTransform(touches.get(pointer).touchPos.cpy().scl(1/PIXELS_TO_METERS),0);
                batBody.setLinearVelocity(touches.get(pointer).touchPos.cpy().sub(touches.get(pointer).lastTouchPos));
                touches.get(pointer).lastTouchPos=new Vector2(touches.get(pointer).touchPos);

            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        // TODO Auto-generated method stub
        return false;
    }





    /********* SEND FUNCTIONS *********/

    void sendBall(Ball theBall) {
        sendBallKinetics.ballNumber=theBall.ballNumber;
        sendBallKinetics.ballPlayerScreen=globalVariables.getSettingsVariables().myPlayerScreen;
        sendBallKinetics.ballPosition=new Vector2(theBall.body.getPosition().x/width*PIXELS_TO_METERS,theBall.body.getPosition().y/height*PIXELS_TO_METERS);
        sendBallKinetics.ballVelocity=new Vector2(theBall.body.getLinearVelocity().x/width*PIXELS_TO_METERS,theBall.body.getLinearVelocity().y/height*PIXELS_TO_METERS);

        if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
            globalVariables.getNetworkVariables().connectionList.get(0).sendUDP(sendBallKinetics);
        } else {
            globalVariables.getNetworkVariables().client.sendUDP(sendBallKinetics);
        }
        //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(theBall.ballNumber)+" sent");

    }

    void sendBallPlayerScreenChange(Ball theBall) {
        sendBallScreenChange.ballNumber=theBall.ballNumber;
        sendBallScreenChange.ballPlayerScreen=globalVariables.getSettingsVariables().myPlayerScreen;
        sendBallScreenChange.ballPosition=new Vector2(theBall.body.getPosition().x/width*PIXELS_TO_METERS,theBall.body.getPosition().y/height*PIXELS_TO_METERS);
        sendBallScreenChange.ballVelocity=new Vector2(theBall.body.getLinearVelocity().x/width*PIXELS_TO_METERS,theBall.body.getLinearVelocity().y/height*PIXELS_TO_METERS);

        if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
            globalVariables.getNetworkVariables().connectionList.get(0).sendTCP(sendBallScreenChange);
        } else {
            globalVariables.getNetworkVariables().client.sendTCP(sendBallScreenChange);
        }
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
        camera.position.set(width/2,height/2*zoomLevel,0);
        camera.update();

        return false;
    }

    void setupBorderCollision(Contact contact, Body body) {
        if((contact.getFixtureA().getBody() == leftBorderBody &&
                contact.getFixtureB().getBody() == body) ||
                (contact.getFixtureB().getBody() == leftBorderBody &&
                        contact.getFixtureA().getBody() == body)) {

            body.setLinearVelocity(-body.getLinearVelocity().x,body.getLinearVelocity().y);
            body.setLinearVelocity(body.getLinearVelocity().scl(borderDamping));

        } else if((contact.getFixtureA().getBody() == bottomBorderBody &&
                contact.getFixtureB().getBody() == body) ||
                (contact.getFixtureB().getBody() == bottomBorderBody &&
                        contact.getFixtureA().getBody() == body)) {

            body.setLinearVelocity(body.getLinearVelocity().x,-body.getLinearVelocity().y);
            body.setLinearVelocity(body.getLinearVelocity().scl(borderDamping));

        } else if((contact.getFixtureA().getBody() == rightBorderBody &&
                contact.getFixtureB().getBody() == body) ||
                (contact.getFixtureB().getBody() == rightBorderBody &&
                        contact.getFixtureA().getBody() == body)) {

            body.setLinearVelocity(-body.getLinearVelocity().x,body.getLinearVelocity().y);
            body.setLinearVelocity(body.getLinearVelocity().scl(borderDamping));

        } else if((contact.getFixtureA().getBody() == topBorderBody &&
                contact.getFixtureB().getBody() == body) ||
                (contact.getFixtureB().getBody() == topBorderBody &&
                        contact.getFixtureA().getBody() == body)) {

            body.setLinearVelocity(body.getLinearVelocity().x,-body.getLinearVelocity().y);
            body.setLinearVelocity(body.getLinearVelocity().scl(borderDamping));
        }
    }

    void setupPlayerScreenContact(Contact contact, Ball theBall) {
        if(((contact.getFixtureA().getBody() == player0ScreenBody &&
                contact.getFixtureB().getBody() == theBall.body) ||
                (contact.getFixtureB().getBody() == player0ScreenBody &&
                        contact.getFixtureA().getBody() == theBall.body) ) &&
                globalVariables.getGameVariables().ballsPlayerScreens[theBall.ballNumber]!=
                        globalVariables.getSettingsVariables().myPlayerScreen) {
            //
            Gdx.app.debug("ClassicGame", "ball "+Integer.toString(theBall.ballNumber)+" entered player screen");

            globalVariables.getGameVariables().ballsPlayerScreens[theBall.ballNumber]=globalVariables.getSettingsVariables().myPlayerScreen;
            sendBallPlayerScreenChange(theBall);
            //theBall.body.setType(BodyDef.BodyType.DynamicBody);
            //globalVariables.getGameVariables().ballsPlayerScreens[theBall.ballNumber]=(globalVariables.getSettingsVariables().myPlayerScreen+1)%2;

        }
    }

    Vector2 rotateAroundPoint(Vector2 vec, Vector2 point, float degrees) {
        vec.sub(point);
        vec.rotate(degrees);
        vec.add(point);
        return(vec);
    }

    /********* CLASSES *********/

    class TouchInfo {

        public Vector2 touchPos= new Vector2(0,0);
        public Vector2 startTouchPos= new Vector2(0,0);
        public Vector2 lastTouchPos= new Vector2(0,0);
        public boolean touched = false;
    }


    //create class Ball
    class Ball {
        Body body;

        int playerScreen;

        float radius,m;

        int[] ballColor =new int[3];
        int ballNumber;
        boolean controlled;
        boolean updateState=true;

        //constructor for Ball
        Ball(Vector2 position_, Vector2 velocity_, float radius_,int ballNumber_,int playerScreen_) {


            radius=radius_;
            m=radius*0.1f;
            ballNumber=ballNumber_;
            playerScreen=playerScreen_;

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


            if (playerScreen==0) {
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
            shapeRenderer.circle(this.body.getPosition().x*PIXELS_TO_METERS,this.body.getPosition().y*PIXELS_TO_METERS, radius*PIXELS_TO_METERS, 100);
        }

    }

}
