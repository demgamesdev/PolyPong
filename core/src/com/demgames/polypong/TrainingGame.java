package com.demgames.polypong;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;

public class TrainingGame extends ApplicationAdapter{
    private static final String TAG = "TrainingGame";
    //use of private etc is not consistently done
    private IGlobals globals;
    private String mode;
    private boolean gravityState,attractionState,agentmode;
    private int myPlayerNumber,numberOfPlayers,numberOfBalls,screenHeight, screenWidth;

    private String[] playerNames;

    //setup global variables
    public TrainingGame(IGlobals globals_ , int myPlayerNumber_, int numberOfPlayers_, int numberOfBalls_, boolean gravityState_,
                       boolean attractionState_,String mode_, boolean agentmode_, int screenHeight_, int screenWidth_) {
        this.globals =globals_;
        this.myPlayerNumber = myPlayerNumber_;
        this.numberOfPlayers = numberOfPlayers_;
        this.numberOfBalls = numberOfBalls_;
        this.gravityState = gravityState_;
        this.attractionState = attractionState_;
        this.mode =mode_;
        this.agentmode =agentmode_;
        this.screenHeight = screenHeight_;
        this.screenWidth = screenWidth_;
    }

    //declare renderer and world related stuff
    private SpriteBatch spriteBatch;
    private PolygonSpriteBatch polygonSpriteBatch;
    private float width, height;
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer debugRenderer;
    private Matrix4 debugMatrix;
    private OrthographicCamera camera;

    private boolean allPlayersReady;

    List<String> notReadyPlayerList;

    private MiscObjects miscObjects;

    private TrainingGameObjects gameObjects;

    //stuff for potential use
    private long frameNumber=0;
    private int sendFrameSkip=1;
    private long currentMillis=System.currentTimeMillis();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        //get width and height of displayGame
        this.width = 1f;
        this.height = 2f;

        //Gdx.app.debug("ClassicGame", " has focus " + globals.getSettingsVariables().gameHasFocus);

        this.allPlayersReady = false;
        this.notReadyPlayerList = new ArrayList<String>();
        this.playerNames = new String[this.numberOfPlayers];
        for(int i=0;i<this.numberOfPlayers;i++) {
            this.playerNames[i] = globals.getComm().playerMap.get(i).name;
            Gdx.app.debug(TAG,"playername " + i + " " + this.playerNames[i]);
        }
        //setup gameobjects
        this.miscObjects = new MiscObjects(globals,this.myPlayerNumber,this.width,this.height,this.screenHeight,this.screenWidth);

        this.gameObjects = new TrainingGameObjects(this.globals,this.myPlayerNumber,this.numberOfPlayers,
                this.playerNames, this.numberOfBalls, globals.getComm().balls,width,height, this.screenWidth, this.screenHeight, miscObjects,
                this.gravityState, this.attractionState,this.agentmode);

        this.miscObjects.setMaxZoom(this.gameObjects.gameField.gameFieldPolygon.getBoundingRectangle(),this.width);
        //setReceived fov of camera to displayGame
        this.camera = new OrthographicCamera(this.width, (float)this.screenHeight/ this.screenWidth*width);

        //setReceived position to middle of normal screen
        this.camera.position.set(0, -this.height/2+gameObjects.gameField.offset.y, 0);
        this.camera.update();

        //copy camera to debugmatrix for synchronous displaying of elements
        this.debugMatrix=new Matrix4(camera.combined);
        this.debugRenderer=new Box2DDebugRenderer();
        //shaperenderer for rendering shapes duuh
        this.shapeRenderer = new ShapeRenderer();
        //setReceived font and spriteBatch for drawing fonts and textures
        this.spriteBatch = new SpriteBatch();
        this.polygonSpriteBatch = new PolygonSpriteBatch();

        this.globals.getAgent().inputs.clear();
        this.globals.getAgent().outputs.clear();

    }

    //executed when closed i think
    @Override
    public void dispose() {


        this.spriteBatch.dispose();
        this.polygonSpriteBatch.dispose();
        this.debugRenderer.dispose();
        this.gameObjects.dispose();
    }

    @Override
    public void render() {
        //Gdx.app.debug("ClassicGame", " has focus " + globals.getSettingsVariables().gameHasFocus);

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {

            }
        });
        /*this.gameInput = new double[]{gameObjects.bats[this.myPlayerNumber].batBody.getPosition().x,gameObjects.bats[this.myPlayerNumber].batBody.getPosition().y,
                gameObjects.bats[this.myPlayerNumber].batBody.getLinearVelocity().x,gameObjects.bats[this.myPlayerNumber].batBody.getLinearVelocity().y,gameObjects.balls[0].ballBody.getPosition().x,gameObjects.balls[0].ballBody.getPosition().y,
                gameObjects.balls[0].ballBody.getLinearVelocity().x,gameObjects.balls[0].ballBody.getLinearVelocity().y};*/

        double [] gameInput = new double[4*gameObjects.balls.length];
        for(int i=0;i<gameObjects.balls.length;i++) {
            gameInput[0+4*i] = gameObjects.balls[i].ballBody.getPosition().x;
            gameInput[1+4*i] = gameObjects.balls[i].ballBody.getPosition().y;
            gameInput[2+4*i] = gameObjects.balls[i].ballBody.getLinearVelocity().x;
            gameInput[3+4*i] = gameObjects.balls[i].ballBody.getLinearVelocity().y;
        }


        if(this.agentmode) {
            INDArray output = globals.getNeuralNetwork().model.output(Nd4j.create(new double[][]{gameInput}));
            System.out.println("output " + output + output.getFloat(0));
            globals.getComm().bats[this.myPlayerNumber].batPosition.set(output.getFloat(0)*width,output.getFloat(1)*height);
        } else {
            if (frameNumber%4 == 0) {

                this.globals.getAgent().inputs.add(gameInput);
                this.globals.getAgent().outputs.add(new double[]{gameObjects.bats[0].batBody.getPosition().x/width,gameObjects.bats[0].batBody.getPosition().y/height});
            }
        }
        this.miscObjects.touches.checkTouches(gameObjects.gameField.offset,camera,gameObjects.fixedPoint);
        this.miscObjects.touches.checkZoomGesture();

        if(!gameObjects.allBallsDestroyedState) {
            gameObjects.doPhysics();

        }
        this.drawScreen();

        miscObjects.touches.updateLasts();
        frameNumber++;
        //Gdx.app.debug("ClassicGame", " here 2");

    }

    void drawScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        //Gdx.gl.glDisable(GL20.GL_BLEND);
        camera.zoom= miscObjects.zoomLevel;
        //camera.position.setReceived(0,-height+height/2*miscObjects.zoomLevel+gameObjects.gameField.offset.y,0);
        camera.position.set(0,gameObjects.fixedPoint.y+miscObjects.zoomLevel*camera.viewportHeight/2,0);
        //Gdx.app.debug(TAG,Float.toString(gameObjects.fixedPoint.y));
        //Gdx.app.debug(TAG,Float.toString(camera.viewportHeight));
        //-height+height/2*miscObjects.zoomLevel
        camera.update();


        shapeRenderer.setProjectionMatrix(camera.combined);
        polygonSpriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);

        polygonSpriteBatch.begin();
        gameObjects.gameField.display(polygonSpriteBatch);
        polygonSpriteBatch.end();

        /*shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        miscObjects.touches.drawTouchPoints(shapeRenderer);
        shapeRenderer.end();*/

        spriteBatch.begin();
        gameObjects.displayGame(spriteBatch);
        gameObjects.displayUI(spriteBatch,this.notReadyPlayerList,this.allPlayersReady);
        //show fp
        //
        spriteBatch.end();

        //uncomment for box2d bodies to be shown
        debugRenderer.render(gameObjects.world,camera.combined);
    }

}


