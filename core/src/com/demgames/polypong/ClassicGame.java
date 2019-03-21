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
import java.util.Arrays;
import java.util.List;

public class ClassicGame extends ApplicationAdapter{
    private static final String TAG = "ClassicGame";
    //use of private etc is not consistently done
    private IGlobals globals;
    private String mode;
    private boolean agentmode;
    int screenHeight, screenWidth;

    //setup global variables
    public ClassicGame(IGlobals globals_ ,String mode_, boolean agentmode_, int screenHeight_, int screenWidth_) {
        this.globals =globals_;
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

    private int myPlayerNumber;
    private int numberOfPlayers;
    private boolean allPlayersReady;

    List<String> notReadyPlayerList;

    private MiscObjects miscObjects;

    private ClassicGameObjects gameObjects;

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

        //Gdx.app.debug("ClassicGame", " has focus " + globals.getSettingsVariables().hasFocus);

        this.myPlayerNumber = globals.getSettingsVariables().myPlayerNumber;
        this.numberOfPlayers = globals.getSettingsVariables().numberOfPlayers;
        this.allPlayersReady = false;
        this.notReadyPlayerList = new ArrayList<String>();
        //setup gameobjects
        this.miscObjects = new MiscObjects(globals,this.myPlayerNumber,this.width,this.height);

        this.gameObjects = new ClassicGameObjects(this.globals,this.myPlayerNumber,this.numberOfPlayers,
                globals.getSettingsVariables().playerNames.toArray(new String[0]), globals.getGameVariables().numberOfBalls, globals.getGameVariables().balls,width,height, this.screenWidth, this.screenHeight, miscObjects,
                globals.getGameVariables().gravityState, globals.getGameVariables().attractionState,this.agentmode);

        this.miscObjects.setMaxZoom(this.gameObjects.gameField.gameFieldPolygon.getBoundingRectangle(),this.width);
        //set fov of camera to displayGame
        this.camera = new OrthographicCamera(this.width, this.screenHeight/ this.screenWidth*width);

        //set position to middle of normal screen
        this.camera.position.set(0, -this.height/2+gameObjects.gameField.offset.y, 0);
        this.camera.update();

        //copy camera to debugmatrix for synchronous displaying of elements
        this.debugMatrix=new Matrix4(camera.combined);
        this.debugRenderer=new Box2DDebugRenderer();
        //shaperenderer for rendering shapes duuh
        this.shapeRenderer = new ShapeRenderer();
        //set font and spriteBatch for drawing fonts and textures
        this.spriteBatch = new SpriteBatch();
        this.polygonSpriteBatch = new PolygonSpriteBatch();

        /*for(int i=0; i< globals.getSettingsVariables().numberOfPlayers;i++) {
            globals.getGameVariables().bats[i].batPosition = miscObjects.touches.touchPos[0];
        }*/

        globals.getSettingsVariables().clientConnectionStates[globals.getSettingsVariables().myPlayerNumber] =4;
        IGlobals.SendVariables.SendConnectionState sendConnectionState = new IGlobals.SendVariables.SendConnectionState();
        sendConnectionState.myPlayerNumber = globals.getSettingsVariables().myPlayerNumber;
        sendConnectionState.connectionState = 4;
        globals.getSettingsVariables().sendObjectToAllClients(sendConnectionState, "tcp");

        globals.getGameVariables().gameState =1;

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
        //Gdx.app.debug("ClassicGame", " has focus " + globals.getSettingsVariables().hasFocus);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {

            }
        });

        this.miscObjects.touches.checkTouches(gameObjects.gameField.offset,camera,gameObjects.fixedPoint);
        this.miscObjects.touches.checkZoomGesture();

        this.allPlayersReady = globals.getSettingsVariables().checkAllClientConnectionStates(4);
        //Gdx.app.debug("ClassicGame", " touchPos "+miscObjects.touches.touchPos[0]);

        if(this.allPlayersReady && globals.getGameVariables().gameState == 1) {

            //update from globals and update playerfields

            //dophysics
            gameObjects.updateAndSend(globals);

            if(!gameObjects.allBallsDestroyedState) {
                gameObjects.doPhysics();

            }


        } else if(!gameObjects.allBallsDestroyedState){
            this.notReadyPlayerList = new ArrayList(Arrays.asList(new String[]{}));
            for(int i=0; i<this.numberOfPlayers;i++) {
                if(globals.getSettingsVariables().clientConnectionStates[i]!=4) {
                    this.notReadyPlayerList.add(globals.getSettingsVariables().playerNames.get(i));
                }
            }

            Gdx.app.debug("ClassicGame", " not all players ready");
            for(int i=0; i<this.numberOfPlayers;i++){
                Gdx.app.debug("ClassicGame", " state of player "+i + " : " + globals.getSettingsVariables().clientConnectionStates[i]);
            }
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
        //camera.position.set(0,-height+height/2*miscObjects.zoomLevel+gameObjects.gameField.offset.y,0);
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
        //debugRenderer.render(gameObjects.world,camera.combined);
    }

}
