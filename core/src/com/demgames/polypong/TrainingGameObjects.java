package com.demgames.polypong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.demgames.miscclasses.GameObjectClasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrainingGameObjects {
    private static final String TAG = "TrainingGameObjects";

    private int myPlayerNumber;
    private String[] playerNames;
    private int numberOfPlayers,numberOfBalls,updateCounter;

    float width, height,screenWidth, screenHeight, metersToPixels;

    World world;
    GameField gameField;
    Vector2 fixedPoint;
    Ball[] balls;
    Bat[] bats;
    int[] scores;
    double [] agentInput;
    private ConcurrentHashMap<Integer, Integer> ballDisplayStatesMap;
    boolean allBallsDestroyedState,agentmode;

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

    private IGlobals globals;

    TrainingGameObjects (IGlobals globals_,int myPlayerNumber_, int numberOfPlayers_, String[] playerNames_, int numberOfBalls_, GameObjectClasses.Ball[] balls_,
                        float width_, float height_, float screenWidth_, float screenHeight_, MiscObjects miscObjects_, Boolean gravityState_,
                        Boolean attractionState_, boolean agentmode_) {
        this.globals = globals_;
        this.myPlayerNumber = myPlayerNumber_;
        this.numberOfBalls = numberOfBalls_;
        this.numberOfPlayers= numberOfPlayers_;
        this.playerNames = playerNames_;
        this.width = width_;
        this.height = height_;
        this.miscObjects = miscObjects_;
        this.agentmode = agentmode_;

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
            Gdx.app.debug(TAG,"playername " + i + " " + this.playerNames[i]);
            if(this.playerNames[i].equals("memes")) {
                this.texturesMap.put("ball_"+i,new Texture(Gdx.files.internal("balls/ball_memes.png")));
                this.texturesMap.put("playerfield_"+i,new Texture(Gdx.files.internal("field/playerfield_memes.png")));
                this.texturesMap.put("bat_"+i,new Texture(Gdx.files.internal("bats/bat_0.png")));
                Gdx.app.debug(TAG,"memes activated");
            } else if(this.playerNames[i].equals("southpark")) {
                this.texturesMap.put("ball_"+i,new Texture(Gdx.files.internal("balls/ball_southpark.png")));
                this.texturesMap.put("playerfield_"+i,new Texture(Gdx.files.internal("field/playerfield_southpark.png")));
                this.texturesMap.put("bat_"+i,new Texture(Gdx.files.internal("bats/bat_0.png")));
                Gdx.app.debug(TAG,"memes activated");
            }else {
                this.texturesMap.put("ball_"+i,new Texture(Gdx.files.internal("balls/ball_banach.png")));
                this.texturesMap.put("playerfield_"+i,new Texture(Gdx.files.internal("field/playerfield_normal.png")));
                this.texturesMap.put("bat_"+i,new Texture(Gdx.files.internal("bats/bat_0.png")));
            }
        }

        this.texturesMap.put("fieldline",new Texture(Gdx.files.internal("field/fieldline.png")));
        this.texturesMap.put("trigger",new Texture(Gdx.files.internal("field/trigger.png")));
        this.texturesMap.put("trace",new Texture(Gdx.files.internal("effects/orange.png")));

        this.texturesMap.put("goal",new Texture(Gdx.files.internal("field/playerfield_goal.png")));

        this.texturesMap.put("boom",new Texture(Gdx.files.internal("effects/boom.png")));
        this.texturesMap.put("gravityfield",new Texture(Gdx.files.internal("field/gravityfield.png")));

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

        this.fixedPoint = new Vector2(0,-this.height).add(gameField.offset);

        this.balls=new Ball[this.numberOfBalls];
        for(int i=0;i<this.balls.length;i++) {
            this.balls[i]= new Ball(i,balls_[i].ballPlayerField,balls_[i].ballRadius,balls_[i].ballPosition, balls_[i].ballVelocity,balls_[i].ballAngle,balls_[i].ballAngularVelocity,10);
            //Gdx.app.debug("ClassicGame", "setup ball " + Integer.toString(i) + " on field "+ Integer.toString(globalVariables.getGameVariables().ballPlayerFields[i]));
        }

        this.bats= new Bat[this.numberOfPlayers];
        for(int i=0;i<this.numberOfPlayers;i++) {
            this.scores[i] = 0;
            this.bats[i]=new Bat(i);
        }

        this.updateCounter = 0;
        this.agentInput = new double[4*this.numberOfBalls];
    }

    void update() {
        for (int i = 0; i < this.balls.length; i++) {
            if (this.balls[i].ballDisplayState == 1) {
                this.agentInput[0 + 4 * i] = this.balls[i].ballBody.getPosition().x;
                this.agentInput[1 + 4 * i] = this.balls[i].ballBody.getPosition().y;
                this.agentInput[2 + 4 * i] = this.balls[i].ballBody.getLinearVelocity().x;
                this.agentInput[3 + 4 * i] = this.balls[i].ballBody.getLinearVelocity().y;
            }
        }
        if(this.agentmode) {
            float[] prediction = globals.getAgent().predict(this.agentInput);
            this.bats[0].updatePos(prediction[0] * width,prediction[1] * height);
        } else {
            if (miscObjects.touches.isTouched[0] && !miscObjects.touches.isTouched[1]) {
                this.bats[0].updatePos(miscObjects.touches.touchPos[0]);
            }
            if(updateCounter%4==0) {
                this.globals.getAgent().inputs.add(this.agentInput);
                this.globals.getAgent().outputs.add(new double[]{this.bats[0].batBody.getPosition().x/this.width,this.bats[0].batBody.getPosition().y/this.height});
            }
        }
        updateCounter++;
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

        this.world.step(Gdx.graphics.getDeltaTime(), 4,2);

        this.allBallsDestroyedState = this.allBallsDestroyed();
    }

    void displayGame(SpriteBatch spriteBatch) {
        for (Map.Entry<String, Sprite> spriteEntry : gameField.spriteMap.entrySet()) {
            spriteEntry.getValue().draw(spriteBatch);
        }

        for(int i=0;i<this.numberOfPlayers;i++) {
            /*this.gameField.fieldLineSprites[i].draw(spriteBatch);
            this.gameField.spriteMap.get("moveline"+i).draw(spriteBatch);*/
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


        spriteBatch.setProjectionMatrix(originalMatrix);
    }

    void dispose() {
        this.world.dispose();
    }

    /********* CLASSES *********/

    //create class Ball
        class Ball {
            Body ballBody;

            private float ballRadius,frictionFactor,spinFactor,frictionThreshold;

            int ballNumber, playerField, tempPlayerField,tempGoal;

            //arraylist with max length for balltrace
            private MiscObjects.BoundedArrayList<Vector2> ballPositionArrayList;
            private int ballPositionFrameSkip=4;
            private long ballUpdateCounter=0;

            private Vector2 ballForwardPosition,ballUnitVelocity;
            private Vector2 destroyPosition;
            private long destroyTime;

            private Sprite ballSprite;
            private Sprite traceSprite;

            private boolean lostState;

            private int lostCounter;

            int ballDisplayState;

            //constructor for Ball
            Ball(int ballNumber_, int ballPlayerField_, float ballRadius_,Vector2 ballPosition_,Vector2 ballVelocity_, float ballAngle_, float ballAngularVelocity_, int ballPositionArrayListLength_) {


                this.ballRadius =ballRadius_;
                this.ballNumber=ballNumber_;
                this.lostState =false;

                this.ballPositionArrayList = new MiscObjects.BoundedArrayList(ballPositionArrayListLength_);

                //setReceived bodytype
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
                ballFixtureDef.friction = 1000f;
                ballFixtureDef.filter.categoryBits = CATEGORY_BALL;
                ballFixtureDef.filter.maskBits = MASK_BALL;

                this.ballBody.createFixture(ballFixtureDef);
                ballShape.dispose();

                this.ballBody.setTransform(gameField.getNewBallStartPosition(ballPlayerField_),ballAngle_);
                this.ballBody.setLinearVelocity(ballVelocity_);
                this.ballBody.setAngularVelocity(ballAngularVelocity_);

                this.ballUnitVelocity = new Vector2(0,0);
                this.ballForwardPosition = new Vector2(this.ballBody.getPosition());

                this.frictionThreshold = 5f;

                //Gdx.app.debug("ClassicGame checkPlayerField", "ballforwardposition x " + Float.toString(this.ballForwardPosition.x) + " y "+ Float.toString(this.ballForwardPosition.y));
                this.checkPlayerField();
                this.playerField = this.tempPlayerField;

                this.ballSprite = new Sprite(texturesMap.get("ball_"+this.playerField));
                this.ballSprite.setOrigin(this.ballRadius,this.ballRadius);
                this.ballSprite.setSize(2f*this.ballRadius,2f*this.ballRadius);

                this.ballDisplayState = 1;

            }

            void doPhysics() {
                //if ball on my screen apply forces etc. else update position and velocity by the ones stored globally and received from other player
                //Gdx.app.debug("ClassicGame", "ball "+ this. ballNumber+ " globals playerfield "+globalVariables.getGameVariables().ballPlayerFields[this.ballNumber]);
                if(this.ballBody.getLinearVelocity().len()>0) {
                    this.ballUnitVelocity.set(this.ballBody.getLinearVelocity().x/this.ballBody.getLinearVelocity().len(),this.ballBody.getLinearVelocity().y/this.ballBody.getLinearVelocity().len());

                    if(this.ballBody.getLinearVelocity().len()>this.frictionThreshold) {
                        this.frictionFactor = (1-this.frictionThreshold/this.ballBody.getLinearVelocity().len())*1e-5f;
                        this.ballBody.applyForceToCenter(-this.ballUnitVelocity.x*this.frictionFactor,
                                -this.ballUnitVelocity.y*this.frictionFactor,true);
                    }

                    this.spinFactor = 1e-2f*this.ballBody.getMass()*this.ballBody.getAngularVelocity();
                    this.ballBody.applyForceToCenter(-this.ballUnitVelocity.y*this.spinFactor,this.ballUnitVelocity.x*this.spinFactor, true);

                    //Gdx.app.debug("ClassicGame", "ball "+Integer.toString(this.ballNumber)+" computed");
                    if (gameLogicStates.get("attractionState")) {
                        if(miscObjects.touches.isTouched[0] && !miscObjects.touches.isTouched[1]) {
                            Vector2 sub = miscObjects.touches.touchPos[0];
                            sub.sub(this.ballBody.getPosition());
                            this.ballBody.applyForceToCenter(sub.scl(1e-6f), true);
                        }
                        //attraction

                    }
                }


                if (gameLogicStates.get("gravityState")) {
                    //gravity
                    //this.ballBody.applyForceToCenter(new Vector2(this.ballBody.getPosition()).scl(this.ballBody.getMass()*1e-1f/(float)Math.pow(this.ballBody.getPosition().len(),2)), true);//-(this.ballBody.getPosition().y+height/PIXELS_TO_METERS)*1f

                    float grav = (float)Math.exp((this.ballBody.getPosition().y+height)*2f)*1e-8f;
                    //System.out.println("grav "+ grav);
                    this.ballBody.applyForceToCenter(0,-grav, true);//-(this.ballBody.getPosition().y+height/PIXELS_TO_METERS)*1f
                }
                if(this.ballUpdateCounter%this.ballPositionFrameSkip == 0){
                    this.ballPositionArrayList.addLast(this.ballBody.getPosition().cpy());
                }
                this.ballUpdateCounter++;
            }

            void display(SpriteBatch spriteBatch) {
                //color depending on playerfield
                if(this.ballDisplayState == 1) {
                    for (int i=0; i<this.ballPositionArrayList.size();i++) {
                        if(i!=0){
                            this.traceSprite = createLineSprite(this.ballPositionArrayList.get(i-1),this.ballPositionArrayList.get(i),0.01f,texturesMap.get("trace"));
                            this.traceSprite.draw(spriteBatch);
                        }
                        //this.traceSprite.setTexture(texturesMap.get("ball_normal"));
                        //this.traceSprite.setPosition((this.ballPositionArrayList.get(i).x-this.ballRadius/4f), (this.ballPositionArrayList.get(i).y-this.ballRadius/4f));
                        //this.traceSprite.setRotation(this.ballBody.getAngle()/MathUtils.PI*180f+360f/numberOfPlayers*myPlayerNumber);


                        //Gdx.app.debug("ClassicGame", "pos x " +Float.toString(this.ballPositionArrayList.get(i).x)+" y "+ Float.toString(this.ballPositionArrayList.get(i).y));
                    }

                    //this.ballSprite.setTexture(texturesMap.get("ball_normal"));
                    this.ballSprite.setPosition((this.ballBody.getPosition().x-this.ballRadius), (this.ballBody.getPosition().y-this.ballRadius));
                    this.ballSprite.setRotation(this.ballBody.getAngle()/MathUtils.PI*180f+360f/numberOfPlayers*myPlayerNumber);
                    this.ballSprite.draw(spriteBatch);
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
                    Gdx.app.debug(TAG, "ball " + this.ballNumber + " in my field with wrong playernumber");
                    return(true);
                } else if(this.playerField== myPlayerNumber && !gameField.playerFieldPolygons[myPlayerNumber].contains(this.ballBody.getPosition())) {
                    Gdx.app.debug(TAG, "ball " + this.ballNumber + " in other field with my playernumber");
                    return(true);
                }
                return(false);
            }

            void checkPlayerField() {
                this.setBallForwardPosition();
                if (!gameField.gameFieldPolygon.contains(this.ballBody.getPosition())) {
                    if(this.lostCounter>=5) {
                        this.ballDisplayState = 0;
                        Gdx.app.error(TAG, "ball " + this.ballNumber + " lost");

                    }
                    this.lostState = true;
                    Gdx.app.error(TAG, "ball " + this.ballNumber + " outside gamefield " + this.playerField + " for frames " + this.lostCounter + " x " + this.ballBody.getPosition().x + " y " + this.ballBody.getPosition().y);
                    this.tempPlayerField = 999;
                    this.lostCounter++;
                } else {
                    this.lostCounter=0;
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
        private Vector2 newPos,tempPos;

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

            this.batSprite = new Sprite(texturesMap.get("bat_"+this.batPlayerField));
            this.batSprite.setOrigin(this.batWidth/2,this.batHeight/2);
            this.batSprite.setSize(this.batWidth,this.batHeight);
            this.tempPos = new Vector2(this.newPos);
        }

        void updatePos(float x, float y){
            this.tempPos.set(x,y);
        }

        void updatePos(Vector2 vector){
            this.tempPos.set(vector);
        }

        void doPhysics() {
            //similar to ball if my bat then physics else only change position etc.
            float orientation=0;
            if(this.batPlayerField ==myPlayerNumber) {
                //update new position if touch inside my field
                //Gdx.app.debug(TAG,"temppos is "+ this.tempPos.x + " " + this.tempPos.y);
                if (gameField.movementPolygon.contains(this.tempPos)) {
                    this.newPos.set(this.tempPos);
                }
                //force to physically move to touched position
                Vector2 subVector = new Vector2(this.newPos).sub(this.batBody.getPosition());
                Vector2 forceVector = subVector.scl((500f+(float)Math.pow(subVector.len()*10f,3)));
                //torque to setReceived orientation
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
        private Body movementLine;

        private Polygon[] playerFieldPolygons;
        private Polygon[] goalPolygons;
        private Polygon movementPolygon;
        Polygon gameFieldPolygon;

        private Vector2[] playerFieldVertices;
        private Vector2[] gameFieldVertices;
        private Vector2[] movementLineVertices;
        private Vector2[][] ballStartPositions;
        private boolean[][] ballStartPositionStates;

        Vector2 offset;

        private PolygonSprite[] playerFieldSprites;
        private PolygonSprite[] goalSprites;
        private Sprite[] fieldLineSprites;
        private Map<String,PolygonSprite> polygonSpriteMap;
        private PolygonSprite gamefieldSprite;

        private Map<String,Body> bodyMap;
        private Map<String,Sprite> spriteMap;
        private Map<String,Shape> shapeMap;
        private Map<String,Float> floatMap;

        GameField() {
            //TODO generalize to more players
            float borderThickness=width/5;
            float heightPart=0.9f;
            float heightLowPart=0.97f;
            Vector2[] fieldLineVertices = new Vector2[numberOfPlayers];

            this.playerFieldPolygons = new Polygon[numberOfPlayers];
            this.goalPolygons = new Polygon[numberOfPlayers];
            this.gameFieldVertices= new Vector2[7*numberOfPlayers];

            this.ballStartPositions = new Vector2[numberOfPlayers][45];
            this.ballStartPositionStates = new boolean[numberOfPlayers][45];

            this.polygonSpriteMap = new HashMap();
            this.spriteMap = new HashMap();
            this.bodyMap = new HashMap<String, Body>();
            this.shapeMap = new HashMap<String, Shape>();
            this.floatMap = new HashMap<String, Float>();


            //position balls in pyramid startposition
            int startPositionCounter=0;
            int maxRows = 9;
            int switchCounter;
            int flip;
            for (int i=0;i<maxRows;i++) {
                switchCounter=0;
                for (int j=0;j<i+1;j++) {
                    if((i+1)%2==0) {
                        if(j%2==0) {
                            flip = 1;
                            switchCounter++;
                        } else {
                            flip = -1;
                        }
                        this.ballStartPositions[0][startPositionCounter] = new Vector2(width/(float)maxRows*0.8f * flip*(switchCounter-0.5f), height * (-1 + 0.3f + 0.4f/maxRows* i));
                    } else {
                        if(j%2==0) {
                            flip = 1;
                        } else {
                            flip = -1;
                            switchCounter++;
                        }
                        this.ballStartPositions[0][startPositionCounter] = new Vector2(width/(float)maxRows*0.8f * flip*switchCounter, height * (-1 + 0.3f + 0.4f/maxRows * i));
                    }

                    for (int k=0;k<numberOfPlayers;k++) {
                        this.ballStartPositionStates[k][startPositionCounter] = true;
                    }
                    startPositionCounter++;
                }
            }


            for (int i=0;i<numberOfPlayers;i++) {
                this.playerFieldPolygons[i]=new Polygon();
                this.goalPolygons[i] = new Polygon();
            }

            PolygonShape[] borderShapes;

            if (numberOfPlayers==2) {
                this.offset= new Vector2(0,0);
                this.playerFieldVertices = new Vector2[22];
                this.gameFieldVertices= new Vector2[14];

                this.playerFieldVertices[0] = new Vector2(0,0);

                //playerfield
                this.playerFieldVertices[1] = new Vector2(-width/2,0);
                this.playerFieldVertices[2] = new Vector2(-width/2,-height*heightPart);
                this.playerFieldVertices[3] = new Vector2(-width/4,-height*heightLowPart);
                this.playerFieldVertices[4] = new Vector2(-width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[5] = new Vector2(width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[6] = new Vector2(width/4,-height*heightLowPart);
                this.playerFieldVertices[7] = new Vector2(width/2,-height*heightPart);
                this.playerFieldVertices[8] = new Vector2(width/2,0);

                //borders
                this.playerFieldVertices[9] = new Vector2(this.playerFieldVertices[2].x,-this.playerFieldVertices[2].y);
                this.playerFieldVertices[10] = new Vector2(this.playerFieldVertices[3].x,-this.playerFieldVertices[3].y);
                this.playerFieldVertices[11] = new Vector2(this.playerFieldVertices[4].x,-this.playerFieldVertices[4].y);
                this.playerFieldVertices[12] = new Vector2(-width/2-borderThickness,this.playerFieldVertices[11].y);
                this.playerFieldVertices[13] = new Vector2(this.playerFieldVertices[9]).add(-borderThickness,0);
                this.playerFieldVertices[14] = new Vector2(this.playerFieldVertices[2]).add(-borderThickness,0);
                this.playerFieldVertices[15] = new Vector2(-width/2-borderThickness,this.playerFieldVertices[4].y);

                //goalborder
                this.playerFieldVertices[16] = new Vector2(this.playerFieldVertices[4]).sub(0,borderThickness);
                this.playerFieldVertices[17] = new Vector2(this.playerFieldVertices[5]).sub(0,borderThickness);

                //movementline
                this.playerFieldVertices[18] = new Vector2(-width/2,-height * 0.4f);
                this.playerFieldVertices[19] = new Vector2(width/2,-height * 0.4f);

                //field objects
                this.playerFieldVertices[20] = new Vector2(-width/4,-height * 0.3f);
                this.playerFieldVertices[21] = new Vector2(width/4,-height * 0.3f);

                this.movementPolygon = new Polygon(MiscObjects.vecToFloatArray(new Vector2[]{this.playerFieldVertices[18],this.playerFieldVertices[2],this.playerFieldVertices[3],this.playerFieldVertices[6],
                        this.playerFieldVertices[7],this.playerFieldVertices[19]}));
                this.movementLineVertices = new Vector2[]{this.playerFieldVertices[18],this.playerFieldVertices[19]};
                borderShapes= new PolygonShape[3*numberOfPlayers];

            } else {
                this.offset = new Vector2(0,-width/2f*(MathUtils.cosDeg(180f/numberOfPlayers)/MathUtils.sinDeg(180f/numberOfPlayers)));
                this.playerFieldVertices = new Vector2[20];

                //playerfield
                this.playerFieldVertices[0] = new Vector2(0,0);
                this.playerFieldVertices[1] = new Vector2(-width/2,0).add(offset);
                this.playerFieldVertices[2] = new Vector2(-width/2,-height*heightPart).add(offset);
                this.playerFieldVertices[3] = new Vector2(-width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[4] = new Vector2(0,-height*heightLowPart).add(offset);
                this.playerFieldVertices[5] = new Vector2(0,-height*heightLowPart).add(offset);
                this.playerFieldVertices[6] = new Vector2(width/4,-height*heightLowPart).add(offset);
                this.playerFieldVertices[7] = new Vector2(width/2,-height*heightPart).add(offset);
                this.playerFieldVertices[8] = new Vector2(width/2,0).add(offset);

                //borders
                this.playerFieldVertices[10] = new Vector2(this.playerFieldVertices[7]).rotate(-360f/numberOfPlayers);
                this.playerFieldVertices[11] = new Vector2(this.playerFieldVertices[6]).rotate(-360f/numberOfPlayers);
                this.playerFieldVertices[12] = new Vector2(this.playerFieldVertices[5]).rotate(-360f/numberOfPlayers);
                this.playerFieldVertices[9] = new Vector2(this.playerFieldVertices[2]).add(this.playerFieldVertices[10]).sub(this.playerFieldVertices[1]);
                this.playerFieldVertices[13] = new Vector2(this.playerFieldVertices[9]).scl((height+borderThickness)/this.playerFieldVertices[9].len());

                //goalborders
                this.playerFieldVertices[14] = new Vector2(this.playerFieldVertices[4]).sub(0,borderThickness);
                this.playerFieldVertices[15] = new Vector2(this.playerFieldVertices[5]).sub(0,borderThickness);

                //movementline
                this.playerFieldVertices[16] = new Vector2(-width/2,-height * 0.4f);
                this.playerFieldVertices[17] = new Vector2(width/2,-height * 0.4f);

                //field objects
                this.playerFieldVertices[18] = new Vector2(-width/4,-height * 0.3f);
                this.playerFieldVertices[19] = new Vector2(width/4,-height * 0.3f);

                this.movementPolygon = new Polygon(MiscObjects.vecToFloatArray(new Vector2[]{this.playerFieldVertices[16],this.playerFieldVertices[2],this.playerFieldVertices[3],this.playerFieldVertices[6],
                        this.playerFieldVertices[7],this.playerFieldVertices[17]}));
                this.movementLineVertices = new Vector2[]{this.playerFieldVertices[16],this.playerFieldVertices[17]};
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

            this.movementLine = world. createBody(borderBodyDef);
            EdgeShape movementLineShape = new EdgeShape();
            movementLineShape.set(this.movementLineVertices[0],this.movementLineVertices[1]);
            fieldLineFixtureDef.shape = movementLineShape;
            this.movementLine.createFixture(fieldLineFixtureDef);
            movementLineShape.dispose();

            this.floatMap.put("trigger_radius",0.05f);
            this.shapeMap.put("trigger",new CircleShape());
            this.shapeMap.get("trigger").setRadius(this.floatMap.get("trigger_radius"));
            borderFixtureDef.shape = this.shapeMap.get("trigger");
            this.bodyMap.put("trigger",world.createBody(borderBodyDef));
            this.bodyMap.get("trigger").createFixture(borderFixtureDef);
            this.bodyMap.get("trigger").setTransform(0,0,0);
            this.spriteMap.put("trigger",new Sprite(texturesMap.get("trigger")));
            this.spriteMap.get("trigger").setBounds(this.bodyMap.get("trigger").getPosition().x-this.floatMap.get("trigger_radius"),this.bodyMap.get("trigger").getPosition().y-this.floatMap.get("trigger_radius"),
                    2*this.floatMap.get("trigger_radius"),2*this.floatMap.get("trigger_radius"));




            //rotate for perspective of player
            this.playerFieldVertices = MiscObjects.transformVectorArray(this.playerFieldVertices,1,-360f/numberOfPlayers*myPlayerNumber);
            this.ballStartPositions[0] = MiscObjects.transformVectorArray(this.ballStartPositions[0],1,-360f/numberOfPlayers*myPlayerNumber);

            for(int i = 0; i<numberOfPlayers; i++) {
                if(i>0) {
                    this.ballStartPositions[i] = MiscObjects.transformVectorArray(this.ballStartPositions[i-1],1,360f/numberOfPlayers);
                }

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

                    this.spriteMap.put("moveline"+i,createLineSprite(this.playerFieldVertices[16],this.playerFieldVertices[17],0.01f,texturesMap.get("fieldline")));

                }


                this.goalBodies[i] = world.createBody(borderBodyDef);
                borderFixtureDef.shape = goalShapes[i];
                this.goalBodies[i].createFixture(borderFixtureDef);
                goalShapes[i].dispose();

                this.playerFieldVertices = MiscObjects.transformVectorArray(this.playerFieldVertices,1,360f/numberOfPlayers);
                //Gdx.app.debug("ClassicGame", "bordersetup player " + i);
            }
            this.gameFieldPolygon = new Polygon(MiscObjects.vecToFloatArray(this.gameFieldVertices));

            for(int i = 0; i< this.borderBodies.length; i++) {
                this.borderBodies[i] = world.createBody(borderBodyDef);
                borderFixtureDef.shape = borderShapes[i];
                this.borderBodies[i].createFixture(borderFixtureDef);
                borderShapes[i].dispose();
            }

            this.playerFieldSprites = new PolygonSprite[numberOfPlayers];
            this.goalSprites = new PolygonSprite[numberOfPlayers];
            this.fieldLineSprites = new Sprite[numberOfPlayers];
            //playerFieldTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            //texturesMap.get("meme_background").setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            //texturesMap.get("gravityfield").setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.MirroredRepeat);

            //this.polygonSpriteMap.put("gravityfield",createTexturePolygonSprite(this.gameFieldPolygon,texturesMap.get("gravityfield")));
            //this.fieldSprites.get("gravityfield").setBounds(0,0,2*height,2*height);
            /*this.fieldSprites.put("gravityfield",new Sprite(texturesMap.get("gravityfield")));
            this.fieldSprites.get("gravityfield").setSize(width,width);
            this.fieldSprites.get("gravityfield").setPosition(-this.fieldSprites.get("gravityfield").getWidth()/2,-this.fieldSprites.get("gravityfield").getHeight()/2);*/

            for(int i=0; i<numberOfPlayers;i++) {
                float rotateDeg = (360f/ numberOfPlayers * (i - myPlayerNumber));

                this.playerFieldSprites[i]= createTexturePolygonSprite(this.playerFieldPolygons[myPlayerNumber],rotateDeg,texturesMap.get("playerfield_"+i));
                this.goalSprites[i]= createTexturePolygonSprite(this.goalPolygons[myPlayerNumber],rotateDeg,texturesMap.get("goal"));
                this.fieldLineSprites[i] = createLineSprite(fieldLineVertices[i],new Vector2(0,0),0.01f,texturesMap.get("fieldline"));
            }
            //this.playerFieldSprites[0]= createTexturePolygonSprite(this.playerFieldPolygons[0],rotateDeg,texturesMap.get("gravityfield"));

            this.gamefieldSprite = createTexturePolygonSprite(this.gameFieldPolygon,0,texturesMap.get("goal"));
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

        PolygonSprite createTexturePolygonSprite(Polygon polygon,float rotateDeg, Texture texture) {
            Rectangle bounds = polygon.getBoundingRectangle();

            Gdx.app.debug(TAG,"polygon bounds x "+ Float.toString(bounds.x)
                    + " y "+ Float.toString(bounds.y) + " width "+ Float.toString(bounds.width)
                    + " height "+ Float.toString(bounds.height));

            float scale = texture.getWidth()/bounds.width;
            if(texture.getHeight()/bounds.height<scale) {
                scale = texture.getHeight()/bounds.height;
            }
            //float [] vertices = tempPolygon.getVertices();
            float [] vertices = miscObjects.transformFloatVertices(polygon.getVertices(),scale,-bounds.x,-bounds.y);

            PolygonRegion polyRegion = new PolygonRegion(new TextureRegion(texture,Math.round(bounds.width*scale),Math.round(bounds.height*scale)),
                    vertices,new EarClippingTriangulator().computeTriangles(vertices).toArray());
            PolygonSprite polygonSprite = new PolygonSprite(polyRegion);

            polygonSprite.setBounds(bounds.x,bounds.y,bounds.width,bounds.height);

            polygonSprite.setOrigin(-polygonSprite.getX(),-polygonSprite.getY());
            polygonSprite.setRotation(rotateDeg);

            Gdx.app.debug(TAG,"ps bounds x "+ Float.toString(polygonSprite.getBoundingRectangle().x)
            + " y "+ Float.toString(polygonSprite.getBoundingRectangle().y) + " width "+ Float.toString(polygonSprite.getBoundingRectangle().width)
                    + " height "+ Float.toString(polygonSprite.getBoundingRectangle().height));
            //Gdx.app.debug(TAG,"ps or y "+ Float.toString(polygonSprite.getOriginY()));
            return(polygonSprite);
        }

        Vector2 getNewBallStartPosition(int ballPlayerField_){
            for (int i=0;i<10;i++) {
                for (int j=0;j<10;j++) {
                    if(this.ballStartPositionStates[ballPlayerField_][i*10+j]) {
                        this.ballStartPositionStates[ballPlayerField_][i*10+j] = false;
                        return(this.ballStartPositions[ballPlayerField_][i*10 + j]);
                    }
                }
            }
            return(new Vector2(0,0));
        }
    }

    //TODO replace by setReceived
    private Sprite createLineSprite(Vector2 startPosition, Vector2 endPosition,float lineWidth, Texture texture) {
        Vector2 distanceVector = new Vector2(endPosition).sub(startPosition);
        //Gdx.app.debug("ClassicGameObjects","line angle " + distanceVector.angle());
        //Gdx.app.debug("ClassicGameObjects","startpos " + startPosition.x + startPosition.y);
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
