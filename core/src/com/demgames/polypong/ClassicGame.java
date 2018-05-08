package com.demgames.polypong;

import java.applet.AppletContext;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class ClassicGame extends ApplicationAdapter implements InputProcessor {
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

    private Body leftBorderBody,bottomBorderBody,rightBorderBody,topBorderBody;//boxBody

    private Vector2 boxPosition;
    private float boxWidth, boxHeight, ballRadius;

    private float borderDamping=1f;

    int numberOfBalls;

    Ball [] balls;

    //network
    IGlobals.SendVariables.SendBallKinetics sendBallKinetics=new IGlobals.SendVariables.SendBallKinetics();
    IGlobals.SendVariables.SendBallScreenChange sendBallScreenChange=new IGlobals.SendVariables.SendBallScreenChange();
    IGlobals.SendVariables.SendBat sendBat=new IGlobals.SendVariables.SendBat();
    IGlobals.SendVariables.SendScore sendScore=new IGlobals.SendVariables.SendScore();


    private Map<Integer,TouchInfo> touches = new HashMap<Integer,TouchInfo>();

    final float PIXELS_TO_METERS = 100f;

    @Override
    public void create() {
        if(globalVariables.getGameVariables().gravityState) {
            world=new World(new Vector2(0,-98f),true);
        } else {
            world=new World(new Vector2(0,0f),true);
        }

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        Gdx.input.setInputProcessor(this);
        for(int i = 0; i < 5; i++){
            touches.put(i, new TouchInfo());
        }

        numberOfBalls=globalVariables.getGameVariables().numberOfBalls;

        boxPosition = new Vector2(width/2,height/2);

        boxWidth=width/10;
        boxHeight=width/10;

        ballRadius=width/50;

        balls=new Ball[numberOfBalls];
        for(int i=0;i<numberOfBalls;i++) {
            balls[i]= new Ball(new Vector2(globalVariables.getGameVariables().ballsPositions[i].x*width,globalVariables.getGameVariables().ballsPositions[i].y*height),new Vector2(0,0),globalVariables.getGameVariables().ballsSizes[i]*width/30,i,0);
        }

        /*BodyDef bodyDef= new BodyDef();
        bodyDef.type= BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(boxPosition.scl(1/PIXELS_TO_METERS));

        boxBody=world.createBody(bodyDef);
        PolygonShape boxShape= new PolygonShape();
        boxShape.setAsBox(boxWidth/2/PIXELS_TO_METERS,boxHeight/2/PIXELS_TO_METERS);
        FixtureDef boxFd= new FixtureDef();
        boxFd.shape = boxShape;
        boxFd.density=1;
        boxFd.friction=0;
        boxBody.createFixture(boxFd);
        boxShape.dispose();*/

        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.StaticBody;
        bodyDef2.position.set(0,0);
        FixtureDef borderFd=new FixtureDef();

        Vector2 [] borderVertices=new Vector2[4];
        borderVertices[0]=new Vector2(0,height/PIXELS_TO_METERS);
        borderVertices[1]=new Vector2(0,0);
        borderVertices[2]=new Vector2(width/PIXELS_TO_METERS,0);
        borderVertices[3]= new Vector2(width/PIXELS_TO_METERS,height/PIXELS_TO_METERS);

        final EdgeShape leftBorderShape,bottomBorderShape,rightBorderShape,topBorderShape;
        leftBorderShape=new EdgeShape();
        bottomBorderShape=new EdgeShape();
        rightBorderShape= new EdgeShape();
        topBorderShape=new EdgeShape();

        leftBorderShape.set(borderVertices[0],borderVertices[1]);
        bottomBorderShape.set(borderVertices[1],borderVertices[2]);
        rightBorderShape.set(borderVertices[2],borderVertices[3]);
        topBorderShape.set(borderVertices[3],borderVertices[0]);

        leftBorderBody = world.createBody(bodyDef2);
        bottomBorderBody= world.createBody(bodyDef2);
        rightBorderBody= world.createBody(bodyDef2);
        topBorderBody=world.createBody(bodyDef2);

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

        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                // Check to see if the collision is between the second sprite and the bottom of the screen
                // If so apply a random amount of upward force to both objects... just because
                //setupBorderCollsision(contact,boxBody);

                for(int i=0;i<numberOfBalls;i++) {
                    setupBorderCollsision(contact,balls[i].body);
                }

                //ballphysics
                /*for(int i=0;i<numberOfBalls;i++) {
                    setupBorderCollsision(contact,balls[i].body);
                    for(int j=i+1;j<numberOfBalls;j++) {
                        if(i!=j) {
                            if ((contact.getFixtureA().getBody() == balls[i].body &&
                                    contact.getFixtureB().getBody() == balls[j].body) ||
                                    (contact.getFixtureB().getBody() == balls[i].body &&
                                            contact.getFixtureA().getBody() == balls[j].body)) {

                                Vector2 relP = balls[i].body.getLinearVelocity().scl(2 * balls[i].body.getMass() / (balls[i].body.getMass() + balls[j].body.getMass())).add(
                                        balls[j].body.getLinearVelocity().scl(2 * balls[j].body.getMass() / (balls[j].body.getMass() + balls[j].body.getMass())));

                                balls[i].body.setLinearVelocity(relP.sub(balls[i].body.getLinearVelocity()));
                                balls[j].body.setLinearVelocity(relP.sub(balls[j].body.getLinearVelocity()));
                            }
                        }

                    }
                }*/

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
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        /*Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_BLEND);*/

        int touchCounter=0;
        if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
            if(globalVariables.getGameVariables().attractionState) {
                for (int i = 0; i < 5; i++) {
                    if (touches.get(i).touched) {
                        touchCounter++;

                        //boxBody.applyForceToCenter(new Vector2(touches.get(i).touchPos.cpy().scl(1/PIXELS_TO_METERS).sub(boxBody.getPosition())),true);
                        for (int j = 0; j < numberOfBalls; j++) {
                            balls[j].body.applyForceToCenter(touches.get(i).touchPos.cpy().scl(1 / PIXELS_TO_METERS).sub(balls[j].body.getPosition()), true);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < numberOfBalls; i++) {
                balls[i].body.setTransform(new Vector2(globalVariables.getGameVariables().ballsPositions[i].x*width,globalVariables.getGameVariables().ballsPositions[i].y*height),0);
            }
        }

        world.step(1/60f, 6,2);

        if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
            for (int i = 0; i < numberOfBalls; i++) {
                sendBall(balls[i]);
            }
        }


        //boxPosition= new Vector2(boxBody.getPosition().x,boxBody.getPosition().y);


        batch.begin();

        //globalVariables.setNumberOfBalls(2);
        font.draw(batch,Integer.toString(touchCounter)+" fingers touching, fps: "+Float.toString(Gdx.graphics.getFramesPerSecond()), width /2, height /2);
        font.draw(batch,/*"boxspeed "+Float.toString(boxBody.getLinearVelocity().len())+*/", touchX "+Float.toString(touches.get(0).touchPos.x)+", touchY "+Float.toString(touches.get(0).touchPos.y), 0, height *0.4f);
        font.draw(batch,"angvel0 "+Float.toString(balls[0].body.getAngularVelocity()),0, height *0.3f);

        //

        batch.end();

        shapeRenderer.begin(ShapeType.Filled);

        /*shapeRenderer.setColor(0, 1, 1, 0.5f);
        shapeRenderer.rect((boxPosition.x*PIXELS_TO_METERS-boxWidth/2), (boxPosition.y*PIXELS_TO_METERS-boxHeight/2), boxWidth, boxHeight);
        */

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
            touches.get(pointer).touched = true;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(pointer < 5){
            //touches.get(pointer).touchX = 0;
            //touches.get(pointer).touchY = 0;
            touches.get(pointer).touched = false;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(pointer < 5){
            touches.get(pointer).touchPos = new Vector2(screenX,height-screenY);
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
        sendBallKinetics.ballNumber=theBall.ballnumber;
        sendBallKinetics.ballPosition=new Vector2(theBall.body.getPosition().x/width,theBall.body.getPosition().y/height);
        sendBallKinetics.ballVelocity=new Vector2(theBall.body.getLinearVelocity().x/width,theBall.body.getLinearVelocity().y/height);

        if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
            globalVariables.getNetworkVariables().connectionList.get(0).sendUDP(sendBallKinetics);
        }
    }


    /********* OTHER FUNCTIONS *********/

    void setupBorderCollsision(Contact contact, Body body) {
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

    /********* CLASSES *********/

    class TouchInfo {
        public Vector2 touchPos= new Vector2(0,0);
        public boolean touched = false;
    }


    //create class Ball
    class Ball {
        Body body;

        int playerScreen;

        float radius,m;

        int[] ballColor =new int[3];
        int ballnumber;
        boolean controlled;
        boolean updateState=true;

        //constructor for Ball
        Ball(Vector2 position_, Vector2 velocity_, float radius_,int ballNumber_,int playerScreen_) {


            radius=radius_;
            m=radius*0.1f;
            ballnumber=ballNumber_;
            playerScreen=playerScreen_;

            //physics stuff

            BodyDef bodyDef= new BodyDef();

            if(globalVariables.getSettingsVariables().myPlayerScreen==0) {
                bodyDef.type = BodyDef.BodyType.DynamicBody;
            } else {
                bodyDef.type = BodyDef.BodyType.KinematicBody;
            }

            bodyDef.position.set(position_.scl(1/PIXELS_TO_METERS));
            body = world.createBody(bodyDef);

            CircleShape shape = new CircleShape();
            shape.setPosition(position_.scl(1/PIXELS_TO_METERS));
            shape.setRadius(radius/PIXELS_TO_METERS);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape= shape;
            fixtureDef.density=m;

            body.createFixture(fixtureDef);
            shape.dispose();

            body.setLinearVelocity(velocity_.scl(1/PIXELS_TO_METERS));

            ballColor[0] =0;
            ballColor[1]=0;
            ballColor[2]=1;

            if(ballnumber==0) {
                ballColor[0]=1;
                ballColor[2]=0;
            }


            if (playerScreen!=0) {
                ballColor[0] =1;
                ballColor[2]=0;
            }
            //(int)random(255);
        }

        //display ball
        void display() {
            shapeRenderer.setColor(ballColor[0], ballColor[1], ballColor[2], 0.5f);
            shapeRenderer.circle(this.body.getPosition().x*PIXELS_TO_METERS,this.body.getPosition().y*PIXELS_TO_METERS, radius, 100);
        }

    }

}
