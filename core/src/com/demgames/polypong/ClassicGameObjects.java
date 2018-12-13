package com.demgames.polypong;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassicGameObjects {
    private static final String TAG = "ClassicGameObjects";

    private int myPlayerNumber;
    private String[] playerNames;
    private int numberOfPlayers;
    private int numberOfBalls;

    float width, height,screenWidth, screenHeight, metersToPixels;

    World world;
    GameField gameField;
    Ball[] balls;
    Bat[] bats;
    int[] scores;
    private ConcurrentHashMap<Integer, Integer> ballDisplayStatesMap;
    boolean allBallsDestroyedState;

    private MiscObjects miscObjects;

    private Map<String, Texture> texturesMap;
    private Map<String, Sprite> spritesMap;
    private Map<String, Boolean> gameLogicStates;
    private Map<Integer, BitmapFont> fontsMap;
    private GlyphLayout glyphLayout;

    private Matrix4 originalMatrix;

    //define category and mask bits to decide which bodies collide with what
    final short CATEGORY_BORDER = 0x0001;
    final short CATEGORY_BALL = 0x0002;
    final short CATEGORY_BAT = 0x0004;
    final short CATEGORY_FIELDLINE = 0x0008;
    final short MASK_BORDER= CATEGORY_BALL | CATEGORY_BAT;
    final short MASK_BALL = CATEGORY_BORDER | CATEGORY_BALL | CATEGORY_BAT;
    final short MASK_BAT = CATEGORY_BORDER | CATEGORY_BALL | CATEGORY_BAT | CATEGORY_FIELDLINE;
    final short MASK_FIELDLINE  = CATEGORY_BAT;

    ClassicGameObjects (int myPlayerNumber_, int numberOfPlayers_, String[] playerNames_, int numberOfBalls_, IGlobals.Ball[] balls_, float width_, float height_, float screenWidth_, float screenHeight_, MiscObjects miscObjects_, Boolean gravityState_, Boolean attractionState_) {
        this.myPlayerNumber = myPlayerNumber_;
        this.numberOfBalls = numberOfBalls_;
        this.numberOfPlayers= numberOfPlayers_;
        this.playerNames = playerNames_;
        this.width = width_;
        this.height = height_;
        this.miscObjects = miscObjects_;

        this.metersToPixels = 1f/1080f;
        this.allBallsDestroyedState = false;
        this.ballDisplayStatesMap = new ConcurrentHashMap<Integer, Integer>();
        this.fontsMap = new HashMap<Integer, BitmapFont>();

        this.texturesMap = new HashMap<String, Texture>();
        this.spritesMap = new HashMap<String, Sprite>();
        this.gameLogicStates = new HashMap<String, Boolean>();

        this.gameLogicStates.put("gravityState",gravityState_);
        this.gameLogicStates.put("attractionState",attractionState_);


        this.world=new World(new Vector2(0f,0f),true);
        this.scores = new int[numberOfPlayers];

        //load textures to map
        for(int i=0;i<numberOfPlayers;i++) {
            if(i<2) {
                this.texturesMap.put("ball_"+i,new Texture(Gdx.files.internal("balls/ball_"+i+".png")));
            }else {
                this.texturesMap.put("ball_"+i,new Texture(Gdx.files.internal("balls/ball_2.png")));
            }
        }
        this.texturesMap.put("ball_off",new Texture(Gdx.files.internal("balls/ball_off.png")));
        this.texturesMap.put("bat_0",new Texture(Gdx.files.internal("bats/bat_0.png")));

        this.texturesMap.put("meme_background",new Texture(Gdx.files.internal("field/meme_background.jpg")));
        this.texturesMap.put("normal_background",new Texture(Gdx.files.internal("field/playerfield_background.png")));
        this.texturesMap.put("fieldline",new Texture(Gdx.files.internal("field/fieldline.png")));

        this.texturesMap.put("goal",new Texture(Gdx.files.internal("field/playerfield_goal.png")));

        this.texturesMap.put("boom",new Texture(Gdx.files.internal("effects/boom.png")));
        this.texturesMap.put("gravityfield",new Texture(Gdx.files.internal("field/test_region.png")));

        this.spritesMap.put("boom",new Sprite(texturesMap.get("boom")));

        //font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Quicksand-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        this.fontsMap.put(parameter.size, generator.generateFont(parameter));
        parameter.size = 48;
        this.fontsMap.put(parameter.size, generator.generateFont(parameter));
        parameter.size = 60;
        this.fontsMap.put(parameter.size, generator.generateFont(parameter));
        generator.dispose();

        glyphLayout = new GlyphLayout();

        //create game objects
        this.gameField = new GameField();
        this.balls=new Ball[this.numberOfBalls];
        for(int i=0;i<this.balls.length;i++) {
            this.balls[i]= new Ball(i,balls_[i].ballRadius,balls_[i].ballPosition.scl(this.width,this.height), balls_[i].ballVelocity,balls_[i].ballAngle,balls_[i].ballAngularVelocity,10);
            //Gdx.app.debug("ClassicGame", "setup ball " + Integer.toString(i) + " on field "+ Integer.toString(globalVariables.getGameVariables().ballPlayerFields[i]));
        }

        this.bats= new Bat[this.numberOfPlayers];
        for(int i=0;i<this.numberOfPlayers;i++) {
            this.scores[i] = 0;
            this.bats[i]=new Bat(i);
        }
    }


    void updateAndSend(IGlobals globals) {
        synchronized (globals.getSettingsVariables().receiveThreadLock) {
            for (int i = 0; i < this.balls.length; i++) {
                if (this.balls[i].ballDisplayState == 1) {
                    if (globals.getGameVariables().ballUpdateStates[i]) {
                        this.balls[i].ballDisplayState = globals.getGameVariables().balls[i].ballDisplayState;
                        if (this.balls[i].ballDisplayState == 1 && this.balls[i].tempPlayerField != myPlayerNumber) {
                            this.balls[i].playerField = globals.getGameVariables().ballPlayerFields[i];
                            this.balls[i].tempPlayerField = this.balls[i].playerField;
                            this.balls[i].ballBody.setTransform(globals.getGameVariables().balls[i].ballPosition, globals.getGameVariables().balls[i].ballAngle);
                            this.balls[i].ballBody.setLinearVelocity(globals.getGameVariables().balls[i].ballVelocity);
                            this.balls[i].ballBody.setAngularVelocity(globals.getGameVariables().balls[i].ballAngularVelocity);
                        }
                        globals.getGameVariables().ballUpdateStates[i] = false;
                    }
                    Gdx.app.debug(TAG, "ball " + this.balls[i].ballNumber + " displayState " + globals.getGameVariables().balls[i].ballDisplayState + " playerfield " + globals.getGameVariables().ballPlayerFields[i] + " globals ");
                    Gdx.app.debug(TAG, "ball " + this.balls[i].ballNumber + " displayState " + this.balls[i].ballDisplayState + " playerfield " + this.balls[i].playerField + " tempplayerfield " + this.balls[i].tempPlayerField);
                }
            }

            //Gdx.app.debug("ClassicGame", "setup ball " + Integer.toString(i) + " on field "+ Integer.toString(globalVariables.getGameVariables().ballPlayerFields[i]));

            for (int i = 0; i < this.numberOfPlayers; i++) {
                if (i != myPlayerNumber) {
                    if (globals.getGameVariables().batUpdateStates[i]) {
                        this.bats[i].batBody.setTransform(globals.getGameVariables().bats[i].batPosition, globals.getGameVariables().bats[i].batAngle);
                        this.bats[i].batBody.setLinearVelocity(globals.getGameVariables().bats[i].batVelocity);
                        this.bats[i].batBody.setAngularVelocity(globals.getGameVariables().bats[i].batAngularVelocity);
                        globals.getGameVariables().batUpdateStates[i] = false;
                    }
                    this.scores[i] = globals.getGameVariables().playerScores[i];
                }
            }
        }

        for(int i=0;i<this.balls.length;i++) {
            if (this.balls[i].ballDisplayState == 1) {
                this.balls[i].checkWrongPlayerField();
            }
            if (this.balls[i].playerField == this.myPlayerNumber) {
                if (this.balls[i].ballDisplayState == 1) {
                    this.balls[i].checkPlayerField();
                    this.balls[i].checkGoal();
                    if (this.balls[i].tempGoal == 1) {
                        scores[myPlayerNumber] -= 1;
                    }
                    if (this.balls[i].tempPlayerField == this.myPlayerNumber) {
                        Gdx.app.debug(TAG, "ball " + Integer.toString(this.balls[i].ballNumber) + " on field "+ Integer.toString(this.balls[i].tempPlayerField) + " sendfrequentball");
                        globals.getSettingsVariables().sendFrequentBallToAllClient(this.balls[i]);
                    } else {
                        if (this.balls[i].tempPlayerField != 999) {
                            Gdx.app.debug(TAG, "ball " + Integer.toString(this.balls[i].ballNumber) + " on field "+ Integer.toString(this.balls[i].tempPlayerField) + " sendfieldchangeball");
                            globals.getSettingsVariables().sendFieldChangeBallToAllClients(this.balls[i]);
                        }
                    }
                }else {
                    this.ballDisplayStatesMap.put(i,this.balls[i].ballDisplayState);
                }
            }
        }
        globals.getSettingsVariables().sendFrequentInfoToAllClients(this.bats[myPlayerNumber],this.ballDisplayStatesMap,scores);
        this.ballDisplayStatesMap.clear();
    }

    void doPhysics() {
        //Gdx.app.debug("ClassicGameObjects", " touchPos "+miscObjects.touches.touchPos[0]);
        for(int i=0;i<this.balls.length;i++) {
            if(this.balls[i].ballDisplayState ==1) {
                this.balls[i].doPhysics();
            } else {
                this.balls[i].destroyBall();
            }
            //Gdx.app.debug("ClassicGame", "setup ball " + Integer.toString(i) + " on field "+ Integer.toString(globalVariables.getGameVariables().ballPlayerFields[i]));
        }

        for(int i=0;i<this.bats.length;i++) {
            this.bats[i].doPhysics();
        }

        this.world.step(Gdx.graphics.getDeltaTime(), 8, 3);

        this.allBallsDestroyedState = this.allBallsDestroyed();
    }

    void displayGame(SpriteBatch spriteBatch) {
        for(int i=0;i<this.numberOfPlayers;i++) {
            this.gameField.fieldLineSprites[i].draw(spriteBatch);
            this.gameField.spriteMap.get("moveline"+i).draw(spriteBatch);
            this.bats[i].display(spriteBatch);
        }

        for(int i=0;i<this.balls.length;i++) {
            this.balls[i].display(spriteBatch);
            //Gdx.app.debug("ClassicGame", "setup ball " + Integer.toString(i) + " on field "+ Integer.toString(globalVariables.getGameVariables().ballPlayerFields[i]));
        }
    }

    void displayUI(SpriteBatch spriteBatch, List<String> notReadyPlayerList, boolean allPlayersReady) {
        originalMatrix = spriteBatch.getProjectionMatrix().cpy();
        spriteBatch.setProjectionMatrix(originalMatrix.cpy().scale(metersToPixels, metersToPixels,1));
        this.drawText(spriteBatch,this.fontsMap.get(32),"fps: "+Float.toString(Gdx.graphics.getFramesPerSecond()),(width/2.5f+this.gameField.offset.x)/metersToPixels,(-height/20f+this.gameField.offset.y)/metersToPixels,true,true);
        String tempScore = "";
        for(int i=0; i<this.numberOfPlayers;i++) {
            if(i!=0) tempScore+=":";
            tempScore += this.scores[i];
        }
        this.drawText(spriteBatch,this.fontsMap.get(60),tempScore,0,(this.gameField.offset.y-height/7f)/metersToPixels,true,true);

        if(!allPlayersReady) {
            String message = "Waiting for:\n";
            for(String name : notReadyPlayerList) {
                message+=name + "\n";
            }

            this.drawText(spriteBatch,this.fontsMap.get(48),message, 0, (this.gameField.offset.y-height/2f)/metersToPixels,true,false);
        }

        if(allBallsDestroyedState) {
            this.drawText(spriteBatch,this.fontsMap.get(48),"Spiel beendet. "+this.playerNames[this.getMaxScoreIndex()] + " hat gewonnen!", 0, (this.gameField.offset.y-height/3f)/metersToPixels,true,false);
        }

        spriteBatch.setProjectionMatrix(originalMatrix);
    }

    void dispose() {
        this.world.dispose();
    }

    /********* CLASSES *********/

    //create class Ball
    class Ball {
        Body ballBody;

        private float ballRadius;

        int ballNumber, playerField, tempPlayerField,tempGoal;

        //arraylist with max length for balltrace
        private MiscObjects.BoundedArrayList<Vector2> ballPositionArrayList;
        private int ballPositionFrameSkip=4;
        private long ballUpdateCounter=0;

        private Vector2 ballForwardPosition;
        private Vector2 destroyPosition;
        private long destroyTime;

        private Sprite ballSprite;
        private Sprite traceSprite;

        private boolean lostState;

        int ballDisplayState;

        //constructor for Ball
        Ball(int ballNumber_, float ballRadius_,Vector2 ballPosition_,Vector2 ballVelocity_, float ballAngle_, float ballAngularVelocity_, int ballPositionArrayListLength_) {


            this.ballRadius =ballRadius_;
            this.ballNumber=ballNumber_;
            this.lostState =false;

            this.ballPositionArrayList = new MiscObjects.BoundedArrayList(ballPositionArrayListLength_);

            //set bodytype
            BodyDef ballBodyDef= new BodyDef();
            ballBodyDef.type = BodyDef.BodyType.DynamicBody;
            ballBodyDef.bullet=true;
            ballBodyDef.position.set(ballPosition_);
            this.ballBody = world.createBody(ballBodyDef);

            CircleShape ballShape = new CircleShape();
            ballShape.setPosition(new Vector2(0,0));
            ballShape.setRadius(this.ballRadius);

            FixtureDef ballFixtureDef = new FixtureDef();
            ballFixtureDef.shape= ballShape;
            ballFixtureDef.density=1e-5f;
            ballFixtureDef.friction = 100f;
            ballFixtureDef.filter.categoryBits = CATEGORY_BALL;
            ballFixtureDef.filter.maskBits = MASK_BALL;

            this.ballBody.createFixture(ballFixtureDef);
            ballShape.dispose();

            this.ballBody.setTransform(ballPosition_,ballAngle_);
            this.ballBody.setLinearVelocity(ballVelocity_);
            this.ballBody.setAngularVelocity(ballAngularVelocity_);

            this.ballForwardPosition = new Vector2(this.ballBody.getPosition());

            //Gdx.app.debug("ClassicGame checkPlayerField", "ballforwardposition x " + Float.toString(this.ballForwardPosition.x) + " y "+ Float.toString(this.ballForwardPosition.y));
            this.checkPlayerField();
            this.playerField = this.tempPlayerField;

            this.ballSprite = new Sprite(texturesMap.get("ball_"+this.playerField));
            this.ballSprite.setOrigin(this.ballRadius,this.ballRadius);
            this.ballSprite.setSize(2f*this.ballRadius,2f*this.ballRadius);

            this.traceSprite = new Sprite(texturesMap.get("ball_"+this.playerField));
            this.traceSprite.setOrigin(this.ballRadius/4f,this.ballRadius/4f);
            this.traceSprite.setSize(2f*this.ballRadius/4f,2f*this.ballRadius/4f);

            this.ballDisplayState = 1;

        }

        void doPhysics() {
        //if ball on my screen apply forces etc. else update position and velocity by the ones stored globally and received from other player
            //Gdx.app.debug("ClassicGame", "ball "+ this. ballNumber+ " globals playerfield "+globalVariables.getGameVariables().ballPlayerFields[this.ballNumber]);
            if (this.playerField == myPlayerNumber) {

                //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(this.ballNumber)+" computed");
                for (int j = 0; j < miscObjects.touches.maxTouchCount; j++) {
                    if (miscObjects.touches.isTouched[j]) {
                        if (gameLogicStates.get("attractionState")) {
                            //attraction
                            //this.ballBody.applyForceToCenter((new Vector2(miscObjects.touches.touchPos[j]).sub(balls[this.ballNumber].ballBody.getPosition()).scl(1e-4f)), true);
                            if(this.ballBody.getLinearVelocity().len()>0) {
                                this.ballBody.applyForceToCenter(new Vector2(-this.ballBody.getLinearVelocity().y,this.ballBody.getLinearVelocity().x).scl(1e-2f*this.ballBody.getMass()*this.ballBody.getAngularVelocity()/this.ballBody.getLinearVelocity().len()), true);
                            }

                        }
                    }
                }
                if (gameLogicStates.get("gravityState")) {
                    //gravity
                    this.ballBody.applyForceToCenter(new Vector2(this.ballBody.getPosition()).scl(this.ballBody.getMass()*1e-2f/(float)Math.pow(this.ballBody.getPosition().len(),2)), true);//-(this.ballBody.getPosition().y+height/PIXELS_TO_METERS)*1f
                }
            }
            if(this.ballUpdateCounter%this.ballPositionFrameSkip == 0){
                this.ballPositionArrayList.addLast(this.ballBody.getPosition().cpy());
            }
            this.ballUpdateCounter++;
        }

        void display(SpriteBatch spriteBatch) {
            //color depending on playerfield
            if(this.ballDisplayState == 1) {
                if(false) {//this.lostState
                    this.ballSprite.setTexture(texturesMap.get("ball_off"));//
                } else {
                    this.ballSprite.setTexture(texturesMap.get("ball_"+this.playerField));//
                }
                this.ballSprite.setPosition((this.ballBody.getPosition().x-this.ballRadius), (this.ballBody.getPosition().y-this.ballRadius));
                this.ballSprite.setRotation(this.ballBody.getAngle()/MathUtils.PI*180f);
                this.ballSprite.draw(spriteBatch);
                for (int i=0; i<this.ballPositionArrayList.size();i++) {
                    if(false) {//this.lostState
                        this.traceSprite.setTexture(texturesMap.get("ball_off"));//
                    } else {
                        this.traceSprite.setTexture(texturesMap.get("ball_"+this.playerField));//
                    }
                    this.traceSprite.setPosition((this.ballPositionArrayList.get(i).x-this.ballRadius/4f), (this.ballPositionArrayList.get(i).y-this.ballRadius/4f));
                    this.traceSprite.setRotation(this.ballBody.getAngle()/MathUtils.PI*180f);
                    this.traceSprite.draw(spriteBatch);

                    //Gdx.app.debug("ClassicGame", "pos x " +Float.toString(this.ballPositionArrayList.get(i).x)+" y "+ Float.toString(this.ballPositionArrayList.get(i).y));
                }
                //spriteBatch.setColor(1,1,1,1);
                //font.draw(spriteBatch,Integer.toString(this.ballNumber), this.ballBody.getPosition().x * PIXELS_TO_METERS, this.ballBody.getPosition().y * PIXELS_TO_METERS);
            } else {
                if(System.currentTimeMillis() - this.destroyTime <100) {
                    spritesMap.get("boom").setSize(2f*this.ballRadius,2f*this.ballRadius);
                    spritesMap.get("boom").setPosition(this.destroyPosition.x - spritesMap.get("boom").getWidth() / 2, this.destroyPosition.y - spritesMap.get("boom").getHeight() / 2);
                    spritesMap.get("boom").draw(spriteBatch);
                }
            }
        }

        boolean checkWrongPlayerField() {
            if(this.playerField!= myPlayerNumber && gameField.playerFieldPolygons[myPlayerNumber].contains(this.ballBody.getPosition())) {
                Gdx.app.error(TAG, "ball " + this.ballNumber + " in my field with wrong playernumber");
                return(true);
            } else if(this.playerField== myPlayerNumber && !gameField.playerFieldPolygons[myPlayerNumber].contains(this.ballBody.getPosition())) {
                Gdx.app.error(TAG, "ball " + this.ballNumber + " in other field with my playernumber");
                return(true);
            }
            return(false);
        }

        void checkPlayerField() {
            this.setBallForwardPosition();
            if (!gameField.gameFieldPolygon.contains(this.ballBody.getPosition())) {
                this.lostState = true;
                Gdx.app.error("ClassicGame", "ball " + this.ballNumber + " outside gamefield " + this.playerField + " x " + this.ballBody.getPosition().x + " y " + this.ballBody.getPosition().y);
                this.tempPlayerField = 999;
            } else {
                for (int i = 0; i < numberOfPlayers; i++) {
                    if (gameField.playerFieldPolygons[i].contains(this.ballForwardPosition)) {
                        if (gameField.playerFieldPolygons[i].contains(this.ballBody.getPosition())) {
                            Gdx.app.debug("ClassicGame", "ball on playerField " + i);
                            this.tempPlayerField = i;
                            break;

                        }
                    }
                }
            }
        }

        void checkGoal() {
            if (gameField.goalPolygons[myPlayerNumber].contains(this.ballBody.getPosition())) {
                Gdx.app.debug("ClassicGame", "ball " + Integer.toString(this.ballNumber) + " in my goal");
                this.ballDisplayState = 0;
                this.tempGoal =1;
            } else {
                this.tempGoal=0;
            }
        }

        void destroyBall() {
            if(this.ballBody!=null) {
                this.destroyPosition = new Vector2(this.ballBody.getPosition());
                this.destroyTime = System.currentTimeMillis();
                world.destroyBody(this.ballBody);
                this.ballBody.setUserData(null);
                this.ballBody = null;
            }
        }

        void setBallForwardPosition() {
            if(this.ballBody.getLinearVelocity().len()>0) {
                this.ballForwardPosition.set(this.ballBody.getPosition().x + this.ballBody.getLinearVelocity().x/this.ballBody.getLinearVelocity().len()*this.ballRadius,
                        this.ballBody.getPosition().y+ this.ballBody.getLinearVelocity().y/this.ballBody.getLinearVelocity().len()*this.ballRadius);
            }
            this.ballForwardPosition.set(this.ballBody.getPosition());
        }

    }

    class Bat {
        Body batBody;
        private int batPlayerField;
        private float batWidth = width/4;
        private float batHeight = height/40;
        private Vector2 newPos;

        private Sprite batSprite;
        Bat(int batPlayerField_) {
            this.batPlayerField =batPlayerField_;
            BodyDef bodyDef= new BodyDef();
            bodyDef.type= BodyDef.BodyType.DynamicBody;

            this.batBody=world.createBody(bodyDef);
            PolygonShape batShape= new PolygonShape();
            batShape.setAsBox(this.batWidth/2,this.batHeight/2);
            FixtureDef batFixtureDef= new FixtureDef();
            //batFixtureDef.restitution=0f;
            batFixtureDef.shape = batShape;
            batShape.dispose();
            batFixtureDef.density=10f;
            batFixtureDef.friction=0.1f;
            batFixtureDef.restitution=0.5f;
            batFixtureDef.filter.categoryBits = CATEGORY_BAT;
            batFixtureDef.filter.maskBits = MASK_BAT;
            this.batBody.createFixture(batFixtureDef);
            this.batBody.setLinearDamping(200f);
            this.batBody.setAngularDamping(2000f);
            this.newPos = new Vector2(miscObjects.touches.touchPos[0]).rotate(360f/numberOfPlayers*(this.batPlayerField-myPlayerNumber));
            this.batBody.setTransform(this.newPos,2f*MathUtils.PI/numberOfBalls*((this.batPlayerField-myPlayerNumber)));

            this.batSprite = new Sprite(texturesMap.get("bat_0"));
            this.batSprite.setOrigin(this.batWidth/2,this.batHeight/2);
            this.batSprite.setSize(this.batWidth,this.batHeight);
        }

        void doPhysics() {
            //similar to ball if my bat then physics else only change position etc.
            float orientation=0;
            if(this.batPlayerField ==myPlayerNumber) {
                //update new position if touch inside my field
                if(gameField.playerFieldPolygons[myPlayerNumber].contains(miscObjects.touches.touchPos[0])) {
                    if(miscObjects.touches.isTouched[0] && !miscObjects.touches.isTouched[1]) {
                        this.newPos.set(miscObjects.touches.touchPos[0]);
                    }
                }
                //force to physically move to touched position
                Vector2 subVector = new Vector2(this.newPos).sub(this.batBody.getPosition());
                Vector2 forceVector = subVector.scl((500f+(float)Math.pow(subVector.len()*10f,3)));
                //torque to set orientation
                float torque = -(this.batBody.getAngle()-orientation)*(10f+Math.abs((this.batBody.getAngle()-orientation)));
                //forceVector.scl(1/forceVector.len());
                this.batBody.applyForceToCenter(forceVector,true);
                this.batBody.applyTorque(torque,true);
            }

        }

        void display(SpriteBatch spriteBatch) {
            this.batSprite.setTexture(texturesMap.get("bat_0"));
            this.batSprite.setRotation(this.batBody.getAngle()* (180f/MathUtils.PI));
            this.batSprite.setPosition(this.batBody.getPosition().x-this.batWidth/2,this.batBody.getPosition().y-this.batHeight/2);
            this.batSprite.draw(spriteBatch);
        }

    }

    class GameField{
        //private Body fieldLineBody;
        private Body[] borderBodies;
        private Body[] goalBodies;
        private Polygon[] playerFieldPolygons;
        private Polygon[] goalPolygons;
        private Polygon gameFieldPolygon;

        private Vector2[] playerFieldVertices;
        private Vector2[] gameFieldVertices;
        Vector2 offset;

        private PolygonSprite[] playerFieldSprites;
        private PolygonSprite[] goalSprites;
        private Sprite[] fieldLineSprites;
        private Map<String,PolygonSprite> polygonSpriteMap;
        private Map<String,Sprite> spriteMap;
        private PolygonSprite gamefieldSprite;
        GameField() {
            //TODO generalize to more players
            float borderThickness=width/5;
            float heightPart=0.9f;
            float heightLowPart=0.97f;
            Vector2[] fieldLineVertices = new Vector2[numberOfPlayers];

            this.playerFieldPolygons = new Polygon[numberOfPlayers];
            this.goalPolygons = new Polygon[numberOfPlayers];
            this.gameFieldVertices= new Vector2[7*numberOfPlayers];

            this.polygonSpriteMap = new HashMap();
            this.spriteMap = new HashMap();

            //EdgeShape fieldLineShape = new EdgeShape();


            for (int i=0;i<playerFieldPolygons.length;i++) {
                this.playerFieldPolygons[i]=new Polygon();
                this.goalPolygons[i] = new Polygon();
            }

            PolygonShape[] borderShapes;

            if (numberOfPlayers==2) {
                this.offset = new Vector2(0,-width/3f);
                this.playerFieldVertices = new Vector2[20];
                this.gameFieldVertices= new Vector2[14];

                this.playerFieldVertices[0] = new Vector2(0,0);
                this.playerFieldVertices[1] = new Vector2(-width/2,0);
                this.playerFieldVertices[2] = new Vector2(-width/2,-height*heightPart).add(offset);
                this.playerFieldVertices[3] = new Vector2(-width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[4] = new Vector2(-width/4,-height-borderThickness).add(offset);
                this.playerFieldVertices[5] = new Vector2(width/4,-height-borderThickness).add(offset);
                this.playerFieldVertices[6] = new Vector2(width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[7] = new Vector2(width/2,-height*heightPart).add(offset);
                this.playerFieldVertices[8] = new Vector2(width/2,0);

                this.playerFieldVertices[9] = new Vector2(this.playerFieldVertices[2].x,-this.playerFieldVertices[2].y);
                this.playerFieldVertices[10] = new Vector2(this.playerFieldVertices[3].x,-this.playerFieldVertices[3].y);
                this.playerFieldVertices[11] = new Vector2(this.playerFieldVertices[4].x,-this.playerFieldVertices[4].y);
                this.playerFieldVertices[12] = new Vector2(-width/2-borderThickness,this.playerFieldVertices[11].y);
                this.playerFieldVertices[13] = new Vector2(this.playerFieldVertices[9]).add(-borderThickness,0);
                this.playerFieldVertices[14] = new Vector2(this.playerFieldVertices[2]).add(-borderThickness,0);
                this.playerFieldVertices[15] = new Vector2(-width/2-borderThickness,this.playerFieldVertices[4].y);

                this.playerFieldVertices[16] = new Vector2(this.playerFieldVertices[4]).sub(0,borderThickness);
                this.playerFieldVertices[17] = new Vector2(this.playerFieldVertices[5]).sub(0,borderThickness);

                this.playerFieldVertices[18] = new Vector2(this.playerFieldVertices[1]).sub(offset);
                this.playerFieldVertices[19] = new Vector2(this.playerFieldVertices[8]).sub(offset);

                borderShapes= new PolygonShape[3*numberOfPlayers];

            } else {
                this.offset = new Vector2(0,-width/2f*(MathUtils.cosDeg(180f/numberOfPlayers)/MathUtils.sinDeg(180f/numberOfPlayers)));
                this.playerFieldVertices = new Vector2[16];

                this.playerFieldVertices[0] = new Vector2(0,0);
                this.playerFieldVertices[1] = new Vector2(-width/2,0).add(offset);
                this.playerFieldVertices[2] = new Vector2(-width/2,-height*heightPart).add(offset);
                this.playerFieldVertices[3] = new Vector2(-width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[4] = new Vector2(-width/4,-height-borderThickness).add(offset);
                this.playerFieldVertices[5] = new Vector2(width/4,-height-borderThickness).add(offset);
                this.playerFieldVertices[6] = new Vector2(width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[7] = new Vector2(width/2,-height*heightPart).add(offset);
                this.playerFieldVertices[8] = new Vector2(width/2,0).add(offset);

                this.playerFieldVertices[10] = new Vector2(this.playerFieldVertices[7]).rotate(-360f/numberOfPlayers);
                this.playerFieldVertices[11] = new Vector2(this.playerFieldVertices[6]).rotate(-360f/numberOfPlayers);
                this.playerFieldVertices[12] = new Vector2(this.playerFieldVertices[5]).rotate(-360f/numberOfPlayers);

                this.playerFieldVertices[9] = new Vector2(this.playerFieldVertices[2]).add(this.playerFieldVertices[10]).sub(this.playerFieldVertices[1]);

                this.playerFieldVertices[13] = new Vector2(this.playerFieldVertices[9]).scl((height+borderThickness)/this.playerFieldVertices[9].len());

                this.playerFieldVertices[14] = new Vector2(this.playerFieldVertices[4]).sub(0,borderThickness);
                this.playerFieldVertices[15] = new Vector2(this.playerFieldVertices[5]).sub(0,borderThickness);

                borderShapes = new PolygonShape[5*numberOfPlayers];

            }

            for (int i=0;i<borderShapes.length;i++) {
                borderShapes[i] = new PolygonShape();
            }
            this.borderBodies = new Body[borderShapes.length];

            PolygonShape[] goalShapes = new PolygonShape[numberOfPlayers];
            this.goalBodies = new Body[numberOfPlayers];
            for (int i=0;i<goalShapes.length;i++) {
                goalShapes[i] = new PolygonShape();
            }

            //rotate for perspective of player
            this.playerFieldVertices = MiscObjects.transformVectorArray(playerFieldVertices,1,-360f/numberOfPlayers*myPlayerNumber);

            for(int i = 0; i<numberOfPlayers; i++) {
                if (numberOfPlayers==2) {
                    this.playerFieldPolygons[i] = new Polygon(MiscObjects.vecToFloatArray(new Vector2[]{this.playerFieldVertices[1],this.playerFieldVertices[2],this.playerFieldVertices[3],
                            this.playerFieldVertices[4],this.playerFieldVertices[5],this.playerFieldVertices[6],this.playerFieldVertices[7],this.playerFieldVertices[8]}));
                } else {
                    this.playerFieldPolygons[i] = new Polygon(MiscObjects.vecToFloatArray(new Vector2[]{this.playerFieldVertices[0],this.playerFieldVertices[1],this.playerFieldVertices[2],this.playerFieldVertices[3],
                            this.playerFieldVertices[4],this.playerFieldVertices[5],this.playerFieldVertices[6],this.playerFieldVertices[7],this.playerFieldVertices[8]}));
                }
                this.playerFieldPolygons[i] = new Polygon(MiscObjects.vecToFloatArray(new Vector2[]{this.playerFieldVertices[0],this.playerFieldVertices[1],this.playerFieldVertices[2],this.playerFieldVertices[3],
                        this.playerFieldVertices[6],this.playerFieldVertices[7],this.playerFieldVertices[8]}));
                this.goalPolygons[i] = new Polygon(MiscObjects.vecToFloatArray(new Vector2[]{this.playerFieldVertices[3],this.playerFieldVertices[4],this.playerFieldVertices[5],
                        this.playerFieldVertices[6]}));
                fieldLineVertices[i]= new Vector2(playerFieldVertices[1]);
                //TODO generalize for more players
                for(int j=1;j<8;j++) {
                    this.gameFieldVertices[j-1+i*7] = new Vector2(this.playerFieldVertices[j]);
                }

                if (numberOfPlayers==2) {
                    borderShapes[0 + i * 3].set(new Vector2[]{this.playerFieldVertices[9], this.playerFieldVertices[10], this.playerFieldVertices[11], this.playerFieldVertices[12],this.playerFieldVertices[13]});
                    borderShapes[1 + i * 3].set(new Vector2[]{this.playerFieldVertices[2], this.playerFieldVertices[1], this.playerFieldVertices[9], this.playerFieldVertices[13], this.playerFieldVertices[14]});
                    borderShapes[2 + i * 3].set(new Vector2[]{this.playerFieldVertices[4], this.playerFieldVertices[3], this.playerFieldVertices[2], this.playerFieldVertices[14], this.playerFieldVertices[15]});

                    goalShapes[i].set(new Vector2[]{this.playerFieldVertices[4],
                            this.playerFieldVertices[5],this.playerFieldVertices[16],
                            this.playerFieldVertices[17]});

                    this.spriteMap.put("moveline"+i,createLineSprite(this.playerFieldVertices[18],this.playerFieldVertices[19],0.01f,texturesMap.get("fieldline")));
                } else {
                    borderShapes[0 + i * 5].set(new Vector2[]{this.playerFieldVertices[2], this.playerFieldVertices[1], this.playerFieldVertices[10], this.playerFieldVertices[9]});
                    borderShapes[1 + i * 5].set(new Vector2[]{this.playerFieldVertices[10], this.playerFieldVertices[11], this.playerFieldVertices[12]});
                    borderShapes[2 + i * 5].set(new Vector2[]{this.playerFieldVertices[4], this.playerFieldVertices[3],this.playerFieldVertices[2]});
                    borderShapes[3 + i * 5].set(new Vector2[]{this.playerFieldVertices[4], this.playerFieldVertices[2],this.playerFieldVertices[9]});
                    borderShapes[4 + i * 5].set(new Vector2[]{this.playerFieldVertices[10], this.playerFieldVertices[12],this.playerFieldVertices[9]});

                    goalShapes[i].set(new Vector2[]{this.playerFieldVertices[4],
                            this.playerFieldVertices[5],this.playerFieldVertices[14],
                            this.playerFieldVertices[15]});

                    this.spriteMap.put("moveline"+i,createLineSprite(this.playerFieldVertices[1],this.playerFieldVertices[8],0.01f,texturesMap.get("fieldline")));
                }


                this.playerFieldVertices = MiscObjects.transformVectorArray(this.playerFieldVertices,1,360f/numberOfPlayers);
                //Gdx.app.debug("ClassicGame", "bordersetup player " + i);
            }
            this.gameFieldPolygon = new Polygon(MiscObjects.vecToFloatArray(this.gameFieldVertices));

            BodyDef borderBodyDef= new BodyDef();
            borderBodyDef.type = BodyDef.BodyType.StaticBody;
            borderBodyDef.position.set(0,0);
            FixtureDef borderFixtureDef=new FixtureDef();
            borderFixtureDef.restitution = 0.7f;
            borderFixtureDef.friction = 0f;
            borderFixtureDef.filter.categoryBits=CATEGORY_BORDER;
            borderFixtureDef.filter.maskBits=MASK_BORDER;

            FixtureDef fieldLineFixtureDef=new FixtureDef();
            fieldLineFixtureDef.filter.categoryBits=CATEGORY_FIELDLINE;
            fieldLineFixtureDef.filter.maskBits=MASK_FIELDLINE;

            for(int i = 0; i< this.borderBodies.length; i++) {
                this.borderBodies[i] = world.createBody(borderBodyDef);
                borderFixtureDef.shape = borderShapes[i];
                this.borderBodies[i].createFixture(borderFixtureDef);
                borderShapes[i].dispose();
            }

            for(int i= 0;i<this.goalBodies.length;i++) {
                this.goalBodies[i] = world.createBody(borderBodyDef);
                borderFixtureDef.shape = goalShapes[i];
                this.goalBodies[i].createFixture(borderFixtureDef);
                goalShapes[i].dispose();
            }

            this.playerFieldSprites = new PolygonSprite[numberOfPlayers];
            this.goalSprites = new PolygonSprite[numberOfPlayers];
            this.fieldLineSprites = new Sprite[numberOfPlayers];
            //playerFieldTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            texturesMap.get("meme_background").setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.MirroredRepeat);
            //texturesMap.get("gravityfield").setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.MirroredRepeat);

            this.polygonSpriteMap.put("gravityfield",createTexturePolygonSprite(this.gameFieldPolygon.getVertices(),texturesMap.get("gravityfield")));
            //this.fieldSprites.get("gravityfield").setBounds(0,0,2*height,2*height);
            /*this.fieldSprites.put("gravityfield",new Sprite(texturesMap.get("gravityfield")));
            this.fieldSprites.get("gravityfield").setSize(width,width);
            this.fieldSprites.get("gravityfield").setPosition(-this.fieldSprites.get("gravityfield").getWidth()/2,-this.fieldSprites.get("gravityfield").getHeight()/2);*/

            for(int i=0; i<numberOfPlayers;i++) {
                this.playerFieldSprites[i]= createTexturePolygonSprite(this.playerFieldPolygons[i].getVertices(),texturesMap.get("normal_background"));
                this.goalSprites[i]= createTexturePolygonSprite(this.goalPolygons[i].getVertices(),texturesMap.get("goal"));
                this.fieldLineSprites[i] = createLineSprite(fieldLineVertices[i],new Vector2(0,0),0.01f,texturesMap.get("fieldline"));
            }
            this.gamefieldSprite = createTexturePolygonSprite(this.gameFieldPolygon.getVertices(),texturesMap.get("goal"));
        }

        void display(PolygonSpriteBatch polygonSpriteBatch) {
            //background

            //field background
            for(int i=0; i<numberOfPlayers;i++) {
                this.playerFieldSprites[i].draw(polygonSpriteBatch);
                this.goalSprites[i].draw(polygonSpriteBatch);
            }
            //this.fieldSprites.get("gravityfield").draw(polygonSpriteBatch);
            if(gameLogicStates.get("gravityState")) {

            }

        }

        PolygonSprite createTexturePolygonSprite(float [] vertices, Texture texture) {
            //vertices = miscObjects.transformFloatVertices(vertices,texture.getWidth()/width,texture.getWidth()/2,texture.getHeight()/2);
            PolygonRegion polyRegion = new PolygonRegion(new TextureRegion(texture),
                    vertices,new EarClippingTriangulator().computeTriangles(vertices).toArray());
            PolygonSprite polygonSprite = new PolygonSprite(polyRegion);
            /*polygonSprite.translate(-texture.getWidth()/2,-texture.getHeight()/2);
            Gdx.app.debug(TAG,"polygonsprite width " + polygonSprite.getWidth() + " pos x " + polygonSprite.getX());*/
            //polygonSprite.setPosition(-polygonSprite.getWidth()/2,-polygonSprite.getHeight()/2);
            //polygonSprite.setScale(width/texture.getWidth());
            return(polygonSprite);
        }
    }

    private Sprite createLineSprite(Vector2 startPosition, Vector2 endPosition,float lineWidth, Texture texture) {
        Vector2 distanceVector = new Vector2(endPosition).sub(startPosition);
        Gdx.app.debug("ClassicGameObjects","line angle " + distanceVector.angle());
        Gdx.app.debug("ClassicGameObjects","startpos " + startPosition.x + startPosition.y);
        Sprite lineSprite = new Sprite(texture);
        lineSprite.setSize(distanceVector.len(),lineWidth);
        lineSprite.setOrigin(0,lineWidth/2f);
        lineSprite.setRotation(distanceVector.angle());
        lineSprite.setPosition(startPosition.x,startPosition.y);
        return(lineSprite);
    }
    void drawText(SpriteBatch spriteBatch, BitmapFont bitmapFont, String text, float posX, float posY, boolean centerX, boolean centerY) {
        this.glyphLayout.setText(bitmapFont,text);
        if(centerX) posX-=this.glyphLayout.width/2;
        if(centerY) posY+=this.glyphLayout.height/2;
        bitmapFont.draw(spriteBatch,text, posX,posY);

    }

    boolean allBallsDestroyed() {
        for(int i=0;i<this.balls.length;i++) {
            if(this.balls[i].ballDisplayState==1) {
                return(false);
            }
        }
        return(true);
    }

    int getMaxScoreIndex() {
        int tempIndex=0;
        int tempScore = this.scores[0];
        for(int i=0;i<this.scores.length;i++) {
            if( this.scores[i]> tempScore) {
                tempIndex = i;
            }
        }
        return(tempIndex);
    }

}
