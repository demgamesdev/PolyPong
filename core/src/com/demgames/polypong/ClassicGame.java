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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassicGame extends ApplicationAdapter{
    private static final String TAG = "ClassicGame";
    //use of private etc is not consistently done
    private IGlobals globalVariables;

    //setup global variables
    public ClassicGame(IGlobals globalVariables_ ) {
        this.globalVariables=globalVariables_;
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

        //Gdx.app.debug("ClassicGame", " has focus " + globalVariables.getSettingsVariables().hasFocus);

        this.myPlayerNumber = globalVariables.getSettingsVariables().myPlayerNumber;
        this.numberOfPlayers = globalVariables.getSettingsVariables().numberOfPlayers;
        this.allPlayersReady = false;
        this.notReadyPlayerList = new ArrayList<String>();
        //setup gameobjects
        this.miscObjects = new MiscObjects(globalVariables,this.myPlayerNumber,this.width,this.height);

        this.gameObjects = new ClassicGameObjects(this.myPlayerNumber,this.numberOfPlayers,
                globalVariables.getSettingsVariables().playerNames.toArray(new String[0]),globalVariables.getGameVariables().numberOfBalls,globalVariables.getGameVariables().balls,width,height,globalVariables.getGameVariables().width,globalVariables.getGameVariables().height, miscObjects,
                globalVariables.getGameVariables().gravityState,globalVariables.getGameVariables().attractionState);

        //set fov of camera to displayGame
        this.camera = new OrthographicCamera(this.width, globalVariables.getGameVariables().height/globalVariables.getGameVariables().width*width);

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

        /*for(int i=0; i< globalVariables.getSettingsVariables().numberOfPlayers;i++) {
            globalVariables.getGameVariables().bats[i].batPosition = miscObjects.touches.touchPos[0];
        }*/

        globalVariables.getSettingsVariables().clientConnectionStates[globalVariables.getSettingsVariables().myPlayerNumber] =4;
        IGlobals.SendVariables.SendConnectionState sendConnectionState = new IGlobals.SendVariables.SendConnectionState();
        sendConnectionState.myPlayerNumber = globalVariables.getSettingsVariables().myPlayerNumber;
        sendConnectionState.connectionState = 4;
        globalVariables.getSettingsVariables().sendObjectToAllClients(sendConnectionState, "tcp");

        globalVariables.getGameVariables().gameState =1;

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
        //Gdx.app.debug("ClassicGame", " has focus " + globalVariables.getSettingsVariables().hasFocus);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {

            }
        });
        this.miscObjects.touches.checkTouches(gameObjects.gameField.offset,camera,gameObjects.fixedPoint);
        this.miscObjects.touches.checkZoomGesture();

        this.allPlayersReady = globalVariables.getSettingsVariables().checkAllClientConnectionStates(4);
        //Gdx.app.debug("ClassicGame", " touchPos "+miscObjects.touches.touchPos[0]);

        if(this.allPlayersReady && globalVariables.getGameVariables().gameState == 1) {

            //update from globals and update playerfields

            //dophysics
            gameObjects.updateAndSend(globalVariables);

            if(!gameObjects.allBallsDestroyedState) {
                gameObjects.doPhysics();

            }


        } else if(!gameObjects.allBallsDestroyedState){
            this.notReadyPlayerList = new ArrayList(Arrays.asList(new String[]{}));
            for(int i=0; i<this.numberOfPlayers;i++) {
                if(globalVariables.getSettingsVariables().clientConnectionStates[i]!=4) {
                    this.notReadyPlayerList.add(globalVariables.getSettingsVariables().playerNames.get(i));
                }
            }

            Gdx.app.debug("ClassicGame", " not all players ready");
            for(int i=0; i<this.numberOfPlayers;i++){
                Gdx.app.debug("ClassicGame", " state of player "+i + " : " + globalVariables.getSettingsVariables().clientConnectionStates[i]);
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
