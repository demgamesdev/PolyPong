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
import java.util.List;

public class TrainingGame extends ApplicationAdapter{
    private static final String TAG = "ClassicGame";
    //use of private etc is not consistently done
    private IGlobals globals;

    //setup global variables
    public TrainingGame(IGlobals globals_ ) {
        this.globals =globals_;
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

    private TrainingGameObjects gameObjects;

    //stuff for potential use
    private long frameNumber=0;
    private int sendFrameSkip=1;
    private long currentMillis=System.currentTimeMillis();

    double[] gameInput;


    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        //get width and height of displayGame
        this.width = 1f;
        this.height = 2f;

        //Gdx.app.debug("ClassicGame", " has focus " + globals.getSettingsVariables().hasFocus);

        this.myPlayerNumber = globals.getGameVariables().myPlayerNumber;
        this.numberOfPlayers = globals.getGameVariables().numberOfPlayers;
        this.allPlayersReady = false;
        this.notReadyPlayerList = new ArrayList<String>();
        //setup gameobjects
        this.miscObjects = new MiscObjects(globals,this.myPlayerNumber,this.width,this.height);

        this.gameObjects = new TrainingGameObjects(globals,this.myPlayerNumber,this.numberOfPlayers,
                globals.getSettingsVariables().playerNames.toArray(new String[0]), globals.getGameVariables().numberOfBalls, globals.getGameVariables().balls,width,height, globals.getGameVariables().width, globals.getGameVariables().height, miscObjects,
                globals.getGameVariables().gravityState, globals.getGameVariables().attractionState);

        this.miscObjects.setMaxZoom(this.gameObjects.gameField.gameFieldPolygon.getBoundingRectangle(),this.width);
        //set fov of camera to displayGame
        this.camera = new OrthographicCamera(this.width, globals.getGameVariables().height/ globals.getGameVariables().width*width);

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

        this.globals.getGameVariables().inputs = new ArrayList<double[]>();
        this.globals.getGameVariables().outputs = new ArrayList<double[]>();

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
        /*this.gameInput = new double[]{gameObjects.bats[this.myPlayerNumber].batBody.getPosition().x,gameObjects.bats[this.myPlayerNumber].batBody.getPosition().y,
                gameObjects.bats[this.myPlayerNumber].batBody.getLinearVelocity().x,gameObjects.bats[this.myPlayerNumber].batBody.getLinearVelocity().y,gameObjects.balls[0].ballBody.getPosition().x,gameObjects.balls[0].ballBody.getPosition().y,
                gameObjects.balls[0].ballBody.getLinearVelocity().x,gameObjects.balls[0].ballBody.getLinearVelocity().y};*/
        this.gameInput = new double[4*gameObjects.balls.length];
        for(int i=0;i<gameObjects.balls.length;i++) {
            this.gameInput[4*i+0] = gameObjects.balls[i].ballBody.getPosition().x;
            this.gameInput[4*i+1] = gameObjects.balls[i].ballBody.getPosition().y;
            this.gameInput[4*i+2] = gameObjects.balls[i].ballBody.getLinearVelocity().x;
            this.gameInput[4*i+3] = gameObjects.balls[i].ballBody.getLinearVelocity().y;
        }

        if(globals.getGameVariables().aiState) {
            globals.getGameVariables().nn.setInput(gameInput);
            globals.getGameVariables().nn.calculate();
            globals.getGameVariables().bats[this.myPlayerNumber].batPosition.set((float) globals.getGameVariables().nn.getOutput()[0],
                    (float) globals.getGameVariables().nn.getOutput()[1]);
        } else {
            if (frameNumber%2 == 0) {
                this.globals.getGameVariables().inputs.add(gameInput);
                this.globals.getGameVariables().outputs.add(new double[]{gameObjects.bats[0].batBody.getPosition().x,gameObjects.bats[0].batBody.getPosition().y});
            }
        }
        this.miscObjects.touches.checkTouches(!globals.getGameVariables().aiState,gameObjects.gameField.offset,camera,gameObjects.fixedPoint);
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
        debugRenderer.render(gameObjects.world,camera.combined);
    }

}


