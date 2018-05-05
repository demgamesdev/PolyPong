package com.demgames.polypong;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.demgames.polypong.network.sendclasses.SendBallKinetics;
import com.demgames.polypong.network.sendclasses.SendBallScreenChange;
import com.demgames.polypong.network.sendclasses.SendBat;
import com.demgames.polypong.network.sendclasses.SendScore;


import processing.core.*;

public class Sketch extends PApplet {

    Activity myActivity;
    Context myContext;
    SensorManager sensorManager;
    Sensor sensor;
    AccelerometerListener accelerometerListener;
    PVector accelerometerVector=new PVector(0,0,0);
    Globals globalVariables;
    //Globals globalVariables = (Globals) getApplicationContext;

    //listening port of application
    String myipadress;
    String mode;
    float zoom=1;
    float maxZoom=(float)0.5;
    int myplayerscreen;
    Float frict;

    /********* VARIABLES *********/

    //declare game variables
    float batfrict=(float)0.05;
    float gravityMag=(float)0.6;
    float attractionMag=200;
    float inelast=(float)0.6;
    float ballspring=3;
    float batspring=6;
    float mousevelocity=1;
    float pinchZero;
    float zoomZero;

    boolean gravityState;
    boolean attractionState;
    boolean scoreState=false;

    int framecounter=0;
    int gameScreen=1;
    int myScore=0;
    int otherScore=0;

    int numberofballs;

    //define pvector of last touch event

    PVector newMousePos;
    PVector lastMousePos;
    PVector zoompoint;
    Bat mybat;
    Bat otherbat;
    //declare array of balls and buttons
    Ball[] balls;
    //test

    SendBallKinetics sendBallKinetics=new SendBallKinetics();
    SendBallScreenChange sendBallScreenChange=new SendBallScreenChange();
    SendBat sendBat=new SendBat();
    SendScore sendScore=new SendScore();

    //constructor which is called in GameLaunchActivity

    Sketch(String mode_) {
        mode=mode_;


        if(mode.equals("host")) {
            myplayerscreen=0;
        } else if(mode.equals("client")) {
            myplayerscreen=1;
        }

    }


    //set size of canvas
    public void settings() {
        fullScreen();
        smooth();
        //frameRate(60);

    }

    public void setup() {

        frameRate(60);
        myActivity=this.getActivity();
        myContext = myActivity.getApplicationContext();

        globalVariables=(Globals)myContext;

        sensorManager = (SensorManager)myContext.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerListener = new AccelerometerListener();
        sensorManager.registerListener(accelerometerListener, sensor, SensorManager.SENSOR_DELAY_GAME);

        myipadress=globalVariables.getMyIpAdress();

        numberofballs=globalVariables.getNumberOfBalls();
        //println(remoteipadress);
        frict=globalVariables.getFriction();

        gravityState=globalVariables.getGravityState();
        attractionState=globalVariables.getAttractionState();

        //set mode of rect drawings
        rectMode(CENTER);

        newMousePos=new PVector(width/2,height*(float)0.9);
        lastMousePos=new PVector(newMousePos.x,newMousePos.y);


        //initialize bat and balls objects
        mybat=new Bat(width/2,height,width/4,height/25,true,myplayerscreen);
        otherbat=new Bat(width/2,-height,width/4,height/25,false,(myplayerscreen+1)%2);

        globalVariables.setBatPosition(otherbat.position);

        //initialize zoompoint
        zoompoint=new PVector(width/2,height);

        if(mode.equals("host")) {
            //globalVariables.getServer().addListener(globalVariables.getGameListener());
            createBallsHost();
        } else if(mode.equals("client")) {
            //globalVariables.getClient().addListener(globalVariables.getGameListener());
            createBallsClient();
        }

        //println("myplayerscreen = "+str(myplayerscreen));
    }

    //loop for drawing
    public void draw() {
        background(0);

        //show different screens
        switch(gameScreen) {
            case 0:
                break;
            case 1:
                //testScreen();
                showGameScreen();
                break;
            case 2:
                //showGameOverScreen();
                break;
        }



        framecounter++;
    }

    /********* SCREENCONTENT *********/


    void showGameScreen() {
        //println("mybat x ",mybat.position.x,"mybat y ",mybat.position.y);
        //display and check buttons for pressing
        background(0);

        checkZoom("out");
        fill(255);
        rect(width/2,0,width,2*height);
        float linethickness=height/50;
        fill(0);
        rect(width/2,-linethickness/2,width,linethickness,linethickness/2);

        textFont(createFont("SansSerif", (float) (height *0.03), true));
        textAlign(CENTER);
        fill(0);
        text(str(parseInt(frameRate)),width*(float)0.8,height*(float)0.1);
        text(str(myScore)+":"+str(otherScore),width*(float)0.5,height*(float)0.1);
        //checkZoom("in");

        //checkZoom("out");


        //move and display bat
        mybat.move();
        sendBat(mybat);
        mybat.display();
        otherbat.writeSketch();
        //println("otherbatposition ",globalVariables.getBatPosition());
        otherbat.display();

        //println("mybat x ",mybat.position.x,"mybat y ",mybat.position.y);
        //println("otherbat x ",otherbat.position.x,"otherbat y ",otherbat.position.y);

        //first check all balls for collisions and then update all balls
        for (Ball ball : balls) {
            if(ball.playerScreen==myplayerscreen) {
                /*println("begin:"+str(ball.position.x)+", "+str(ball.position.y)
                        +str(ball.velocity.x)+", "+str(ball.velocity.y));*/
                if(ball.updateState) {
                    ball.checkBallCollision();
                    ball.checkExternalForce();
               /* println("end:"+str(ball.position.x)+", "+str(ball.position.y)
                        +str(ball.velocity.x)+", "+str(ball.velocity.y));*/
                    ball.checkBoundaryCollision();
                    ball.checkBatCollision(mybat);
                }
            }
        }
        for (Ball ball : balls) {
            if(ball.playerScreen==myplayerscreen) {
                /*println("update ball "+str(ball.ballnumber)+": "+str(ball.position.x)+", "+str(ball.position.y)+"; "
                        +str(ball.velocity.x)+", "+str(ball.velocity.y));*/
                if(ball.updateState) {
                    //println("ball "+str(ball.ballnumber)+" on this screen");
                    ball.update();
                    ball.writeGlobal();
                    ball.checkScore();
                    sendBall(ball);
                    ball.checkPlayerScreenChange();
                }

            } else {
                ball.writeSketch();
                //println("ball ",ball.ballnumber," position ",globalVariables.getBallsPositions()[ball.ballnumber]);
            }

            ball.display();
            //println("playerscreen ",globalVariables.getBallsPlayerScreens()[ball.ballnumber]);
            ball.playerScreen=globalVariables.getBallsPlayerScreens()[ball.ballnumber];
        }


        //update lastMousePos
        lastMousePos=new PVector(newMousePos.x,newMousePos.y);
    }

    void showGameOverScreen() {
        for (int j=0;j<balls.length;j++) {
            balls[j].display();
        }
        fill(0);
        line(width/2,height,balls[0].position.x,balls[0].position.y);
    }

    /********* EVENTHANDLER *********/

    class AccelerometerListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            accelerometerVector.x=event.values[0];
            accelerometerVector.y=event.values[1];
            accelerometerVector.z=event.values[2];
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }



    /********* SENDING FUNCTIONS *********/

    //send ball data to remotelocation
    void sendBall(Ball theBall) {
        sendBallKinetics.ballNumber=theBall.ballnumber;
        sendBallKinetics.ballPosition=new PVector(theBall.position.x/width,theBall.position.y/height);
        sendBallKinetics.ballVelocity=new PVector(theBall.velocity.x/width,theBall.velocity.y/height);

        if(mode.equals("host")) {
            globalVariables.getConnectionList()[0].sendUDP(sendBallKinetics);
        } else if(mode.equals("client")) {
            globalVariables.getClient().sendUDP(sendBallKinetics);
        }
    }

    void sendPlayerScreenChange(Ball theBall) {
        sendBallScreenChange.ballNumber=theBall.ballnumber;

        sendBallScreenChange.ballPosition=new PVector(theBall.position.x/width,theBall.position.y/height);
        sendBallScreenChange.ballVelocity=new PVector(theBall.velocity.x/width,theBall.velocity.y/height);

        if(mode.equals("host")) {
            globalVariables.getConnectionList()[0].sendTCP(sendBallScreenChange);
        } else if(mode.equals("client")) {
            globalVariables.getClient().sendTCP(sendBallScreenChange);
        }

        globalVariables.setBallPlayerScreen(theBall.ballnumber,(myplayerscreen+1)%2);
        theBall.playerScreen=(myplayerscreen+1)%2;
    }



    void sendBat(Bat theBat) {
        sendBat.batPosition=new PVector(theBat.position.x/width,theBat.position.y/height);

        sendBat.batOrientation=theBat.orientation;

        if(mode.equals("host")) {
            globalVariables.getConnectionList()[0].sendUDP(sendBat);
        } else if(mode.equals("client")) {
            globalVariables.getClient().sendUDP(sendBat);
        }

        //println("bat sent");

    }

    void sendScore() {
        sendScore.myScore=myScore;
        sendScore.otherScore=otherScore;

        if(mode.equals("host")) {
            globalVariables.getConnectionList()[0].sendTCP(sendScore);
        } else if(mode.equals("client")) {
            globalVariables.getClient().sendTCP(sendScore);
        }
    }

    /********* OTHER FUNCTIONS *********/

    void createBallsHost() {
        Globals globalVariables = (Globals) myContext;
        balls=new Ball[numberofballs];

        //balls[0]=new Ball(bat.origin.x,bat.origin.y,0,0,width/20,false,0,0);
        for(int i=0;i<balls.length;i++) {

            balls[i] = new Ball(globalVariables.getBallsPositions()[i].x*width, globalVariables.getBallsPositions()[i].y*height, globalVariables.getBallsVelocities()[i].x*width,globalVariables.getBallsVelocities()[i].y*height, (1+globalVariables.getBallsSizes()[i])*width/40, false, i, 0);
        }
    }

    void createBallsClient() {
        Globals globalVariables = (Globals) myContext;
        balls=new Ball[numberofballs];

        for(int i=0;i<balls.length;i++) {
            //println("createballsclient ",globalVariables.getBallsPositions()[i].x);
            balls[i] = new Ball(width*(1-globalVariables.getBallsPositions()[i].x), -globalVariables.getBallsPositions()[i].y*height, -globalVariables.getBallsVelocities()[i].x*width,-globalVariables.getBallsVelocities()[i].y*height, (1+globalVariables.getBallsSizes()[i])*width/40, false, i, 0);
        }
    }

    void checkZoom(String zoommode) {
        translate(zoompoint.x,zoompoint.y);
        switch(zoommode) {
            case "out":
                scale(zoom);
                break;
            case "in":
                scale(1/zoom);
                break;
            default:
                scale(1);
        }
        translate(-zoompoint.x,-zoompoint.y);

    }

    void rotatePlayerScreen(int playerScreen,String rotateMode) {
        float rotationangle=0;
        if(playerScreen==(myplayerscreen+1)%2) {
            rotationangle=PI;
        }

        translate(width/2,0);
        switch(rotateMode) {
            case "cw":
                rotate(rotationangle);
                break;
            case "acw":
                rotate(-rotationangle);
                break;

        }
        translate(-width/2,0);
    }

    /********* CLASSES *********/


    //create class Ball
    class Ball {
        PVector position;
        PVector velocity;

        int playerScreen;

        float radius,m;

        int[] ballcolor=new int[3];
        int ballnumber;
        boolean controlled;
        boolean updateState=true;

        //constructor for Ball
        Ball(float x, float y, float xvel, float yvel, float r_,boolean controlled_,int ballnumber_,int playerScreen_) {
            position = new PVector(x,y);
            velocity = new PVector(xvel,yvel);
            radius=r_;
            m=(float)(radius*0.1);
            controlled=controlled_;
            ballnumber=ballnumber_;
            playerScreen=playerScreen_;

            ballcolor[0] =0;
            ballcolor[1]=0;
            ballcolor[2]=255;

            if (playerScreen!=0) {
                ballcolor[0] =255;
                ballcolor[2]=0;
            }
            //(int)random(255);
        }

        //update position of ball with velocity
        void update() {

            position.add(PVector.mult(velocity,60/frameRate));
        }

        void writeGlobal() {
            globalVariables.setBallPosition(this.ballnumber,this.position);
            globalVariables.setBallVelocity(this.ballnumber,this.velocity);
        }

        void writeSketch() {
            position.x=(1 - globalVariables.getBallsPositions()[this.ballnumber].x)*width;
            position.y=- globalVariables.getBallsPositions()[this.ballnumber].y*height;
            velocity.x=-globalVariables.getBallsVelocities()[this.ballnumber].x*width;
            velocity.y=-globalVariables.getBallsVelocities()[this.ballnumber].y*height;
            playerScreen=globalVariables.getBallsPlayerScreens()[ballnumber];
        }

        //display ball
        void display() {

            fill(ballcolor[0],ballcolor[1],ballcolor[2]);
            ellipse(position.x,position.y,radius*2,radius*2);
        }

        //check for collision with border of screen
        void checkBoundaryCollision() {
            if (position.x+radius+velocity.x>width) {
                position.x=width-radius;
                velocity.x*=-inelast;
            } else if (position.x-radius+velocity.x<0) {
                position.x=radius;
                velocity.x*=-inelast;
            } if (position.y+radius+velocity.y>height && !scoreState) {
                position.y=height-radius;
                velocity.y*=-inelast;
            } /*else if (position.y-radius+velocity.y<0) {
                position.y=radius;
                velocity.y*=-inelast;
            }*/
            else if (position.y-radius+velocity.y<-height) {
                position.y=-height+radius;
                velocity.y*=-inelast;
            }
        }


        void checkScore() {
            if (position.y-radius>height && scoreState) {
                otherScore++;
                updateState=false;
                position.y=2*height;
                sendScore();
            }

        }


        //check for collision with other balls and push away like a spring
        void checkBallCollision() {
            PVector relPos;
            float dist;
            float mindist;
            for (int i=0;i<balls.length;i++) {
                if (i != ballnumber) {
                    relPos = PVector.sub(balls[i].position, position);
                    dist = relPos.mag();

                    mindist = radius + balls[i].radius;

                    if (dist < mindist) {
                        PVector equil = PVector.add(position, PVector.mult(relPos, mindist / dist));
                        velocity.add(PVector.mult(PVector.sub(balls[i].position, equil), ballspring/m));
                        velocity.mult(inelast);
                    }
                }
            }
        }


        //check for collision with Bat thebat and again behave like a spring
        void checkBatCollision(Bat thebat) {
            PVector relPosition=PVector.sub(position,thebat.position);

            float positionparallel=PVector.dot(thebat.orparallel,relPosition);
            float positionnormal=PVector.dot(thebat.ornormal,relPosition);


            //print("projections:", positionparallel,positionnormal);
            float ballvelocityparallel=PVector.dot(thebat.orparallel,velocity);
            float ballvelocitynormal=PVector.dot(thebat.ornormal,velocity);
            float batvelocityparallel=PVector.dot(thebat.orparallel,thebat.velocity);
            float batvelocitynormal=PVector.dot(thebat.ornormal,thebat.velocity);

            float factor=(float)1;
            float part=(float)0.9;

            //println("pos: ",positionparallel,positionnormal,", vel: ",velocityparallel,velocitynormal);
            //println(positionnormal-(thebat.hei/2+radius)*part);
            //println(positionnormal+velocitynormal-(thebat.hei/2+radius)*part);
            //print("------");

            if(abs(positionparallel)<=(thebat.wid/2)) {
                if(positionnormal>0) {
                    if(positionnormal+ballvelocitynormal-batvelocitynormal<thebat.hei/2+radius) {
                        velocity=PVector.add(PVector.mult(thebat.orparallel,ballvelocityparallel),PVector.mult(thebat.ornormal,abs(ballvelocitynormal)+abs(batvelocitynormal)));
                        velocity.mult(inelast/factor);
                        //println("up in");
                    } else {
                        //println("up");
                    }
                } else if(positionnormal<0) {
                    if(positionnormal+ballvelocitynormal-batvelocitynormal>-(thebat.hei/2+radius)) {
                        velocity=PVector.add(PVector.mult(thebat.orparallel,ballvelocityparallel),PVector.mult(thebat.ornormal,-abs(ballvelocitynormal)-abs(batvelocitynormal)));
                        velocity.mult(inelast/factor);
                        //println("down in");
                    } else {
                        //println("down");
                    }

                }

            }else if (abs(positionnormal)<=(thebat.hei/2)){
                if(positionparallel>0) {
                    if(positionparallel+ballvelocityparallel-batvelocityparallel<thebat.wid/2+radius) {
                        velocity=PVector.add(PVector.mult(thebat.orparallel,abs(ballvelocityparallel)+abs(batvelocityparallel)),PVector.mult(thebat.ornormal,ballvelocitynormal));
                        velocity.mult(inelast/factor);
                        //println("right in");
                    } else {
                        //println("right");
                    }
                } else if(positionparallel<0) {
                    if(positionparallel+ballvelocityparallel-batvelocityparallel>-(thebat.wid/2+radius)) {
                        velocity=PVector.add(PVector.mult(thebat.orparallel,-abs(ballvelocityparallel)-abs(batvelocityparallel)),PVector.mult(thebat.ornormal,ballvelocitynormal));
                        velocity.mult(inelast/factor);
                        //println("left in");
                    } else {
                        //println("left");
                    }

                }

            }else if(positionparallel>0 && positionnormal>0) {
                PVector vertex=PVector.add(thebat.position,PVector.add(PVector.mult(thebat.orparallel,thebat.wid/2),PVector.mult(thebat.ornormal,thebat.hei/2)));
                PVector relVertexPos=PVector.sub(position,vertex);
                float vertexDistance=relVertexPos.mag();
                relVertexPos.mult(1/vertexDistance);
                float ballVertexVelocity=PVector.dot(relVertexPos,velocity);
                float batVertexVelocity=PVector.dot(relVertexPos,thebat.velocity);

                //print(vertexDistance-radius);
                if(vertexDistance+ballVertexVelocity-batVertexVelocity<radius) {
                    velocity=PVector.mult(relVertexPos,abs(ballVertexVelocity)+abs(batVertexVelocity));
                    velocity.mult(inelast/factor);
                    //println("up right in");
                } else {
                    //println("up right");
                }

            } else if(positionparallel<0 && positionnormal>0) {
                PVector vertex=PVector.add(thebat.position,PVector.add(PVector.mult(thebat.orparallel,-thebat.wid/2),PVector.mult(thebat.ornormal,thebat.hei/2)));
                PVector relVertexPos=PVector.sub(position,vertex);
                float vertexDistance=relVertexPos.mag();
                relVertexPos.mult(1/vertexDistance);
                float ballVertexVelocity=PVector.dot(relVertexPos,velocity);
                float batVertexVelocity=PVector.dot(relVertexPos,thebat.velocity);

                //print(vertexDistance-radius);
                if(vertexDistance+ballVertexVelocity-batVertexVelocity<radius) {
                    velocity=PVector.mult(relVertexPos,abs(ballVertexVelocity)+abs(batVertexVelocity));
                    velocity.mult(inelast/factor);
                    //println("up left in");
                } else {
                    //println("up left");
                }

            } else if(positionparallel>0 && positionnormal<0) {
                PVector vertex=PVector.add(thebat.position,PVector.add(PVector.mult(thebat.orparallel,thebat.wid/2),PVector.mult(thebat.ornormal,-thebat.hei/2)));
                PVector relVertexPos=PVector.sub(position,vertex);
                float vertexDistance=relVertexPos.mag();
                relVertexPos.mult(1/vertexDistance);
                float ballVertexVelocity=PVector.dot(relVertexPos,velocity);
                float batVertexVelocity=PVector.dot(relVertexPos,thebat.velocity);

                //print(vertexDistance-radius);
                if(vertexDistance+ballVertexVelocity-batVertexVelocity<radius) {
                    velocity=PVector.mult(relVertexPos,abs(ballVertexVelocity)+abs(batVertexVelocity));
                    velocity.mult(inelast/factor);
                    //println("down right in");
                } else {
                    //println("down right");
                }

            } else if(positionparallel<0 && positionnormal<0) {
                PVector vertex=PVector.add(thebat.position,PVector.add(PVector.mult(thebat.orparallel,-thebat.wid/2),PVector.mult(thebat.ornormal,-thebat.hei/2)));
                PVector relVertexPos=PVector.sub(position,vertex);
                float vertexDistance=relVertexPos.mag();
                relVertexPos.mult(1/vertexDistance);
                float ballVertexVelocity=PVector.dot(relVertexPos,velocity);
                float batVertexVelocity=PVector.dot(relVertexPos,thebat.velocity);

                //print(vertexDistance-radius);
                if(vertexDistance+ballVertexVelocity-batVertexVelocity<radius) {
                    velocity=PVector.mult(relVertexPos,abs(ballVertexVelocity)+abs(batVertexVelocity));
                    velocity.mult(inelast/factor);
                    //println("down left in");
                } else {
                    //println("down left");
                }
            }


        }

        //add external force such as gravity, mouse attraction and friction
        void checkExternalForce() {
            if (gravityState) {
                velocity.add(new PVector(0, gravityMag));

            }
            if(attractionState) {
                if (mousePressed) {
                    //println("pressed");
                    PVector relPos = PVector.sub(newMousePos, position);
                    float dist = relPos.mag();
                    velocity.add(PVector.mult(relPos, attractionMag / pow(dist, 2)));
                }
            }
            velocity.add(PVector.mult(velocity,-frict/m));
        }

        void checkPlayerScreenChange() {
            if(position.y+velocity.y<0) {
                sendPlayerScreenChange(this);
            }
        }
    }


    //create class bat
    class Bat {
        PVector origin,position,lastposition,velocity,orparallel,ornormal;

        float orientation,moveradius,wid,hei;
        float accelerometerSensitivity=(float)5.0;
        int[] batcolor=new int[3];
        boolean controlled;

        int playerScreen;

        //cunstructor for bat
        Bat(float x, float y, float wid_,float hei_, boolean controlled_,int playerScreen_) {
            rotatePlayerScreen(playerScreen,"acw");
            origin= new PVector(x,y);
            position = new PVector(newMousePos.x,newMousePos.y);
            velocity = new PVector(0,0);

            orientation=getAccelerometerAngle(accelerometerVector.x,accelerometerSensitivity);
            setOrvectors();

            moveradius=width*(float)0.7;
            wid=wid_;
            hei=hei_;

            playerScreen=playerScreen_;

            batcolor[0] =0;
            batcolor[1]=0;
            batcolor[2]=255;

            if (playerScreen!=0) {
                batcolor[0] =255;
                batcolor[2]=0;
            }

            controlled=controlled_;
            rotatePlayerScreen(playerScreen,"cw");

        }


        //move bat to new position
        void move() {
            origin= new PVector(width/2,height);
            if(controlled) {

                if(abs(newMousePos.x-origin.x)<=zoom*width/2 && newMousePos.y>=(1-zoom)*height) {
                    position = new PVector((newMousePos.x - (1 - zoom) * zoompoint.x) / zoom, (newMousePos.y - (1 - zoom) * zoompoint.y) / zoom);
                }

                velocity = PVector.sub(newMousePos, lastMousePos);
                velocity.mult(mousevelocity);

                orientation=getAccelerometerAngle(accelerometerVector.x,accelerometerSensitivity);
                setOrvectors();
            }
        }

        void writeSketch() {
            position.x=width*globalVariables.getBatPosition().x;
            position.y=height*globalVariables.getBatPosition().y;
            orientation=globalVariables.getBatOrientation();
        }

        //display bat also rotating
        void display() {
            //pushMatrix();
            rotatePlayerScreen(playerScreen,"acw");

            /*fill(batcolor[0],batcolor[1],batcolor[2],50);
            ellipse(width/2,height,2*moveradius,2*moveradius);*/

            /*rectMode(CORNERS);
            fill(0);
            rect(width, height - moveradius, width + moveradius, height + moveradius);
            rect(-moveradius, height - moveradius, 0, height + moveradius);
            rect(0, height, width, height + moveradius);*/

            rectMode(CENTER);

            fill(batcolor[0],batcolor[1],batcolor[2]);
            translate(position.x,position.y);
            rotate(orientation-PI/2);
            rect(0,0,wid,hei);
            rotate(-(orientation-PI/2));
            translate(-(position.x),-(position.y));

            /*fill(255);
            ellipse(position.x+wid*ornormal.x,position.y+wid*ornormal.y,50,50);
            fill(255,0,0);
            ellipse(position.x+wid*orparallel.x,position.y+wid*orparallel.y,50,50);*/
            rotatePlayerScreen(playerScreen,"cw");
            //popMatrix();

        }

        //get angle by position relative to origin
        float getangle(float x, float y) {
            float angle=-atan((-y + origin.y) / (x - origin.x))+PI;
            if (x>=origin.x) {
                angle=-atan((-y + origin.y) / (x - origin.x));
            }

            return(angle);
        }

        float getAccelerometerAngle(float x, float y) {
            float angle=-atan((-y) / (x))+PI;
            if (x>=0) {
                angle=-atan((-y) / (x));
            }

            return(angle);
        }

        //set parallel and perpendicular unit vectors for colllision management
        void setOrvectors() {
            ornormal=PVector.add(PVector.mult(new PVector(1,0),cos(orientation)),
                    PVector.mult(new PVector(0,1),sin(orientation)));
            orparallel=new PVector(-ornormal.y,ornormal.x);

        }

    }

    public void touchStarted() {
        println(touches.length);

        if(touches.length==1) {
            newMousePos=new PVector(touches[0].x,touches[0].y);
            lastMousePos=new PVector(newMousePos.x,newMousePos.y);
            println(touches[0].x,touches[0].y);
        } else if(touches.length==2) {
            float xPinch=touches[0].x-touches[1].x;
            float yPinch=touches[0].y-touches[1].y;
            pinchZero=sqrt(xPinch*xPinch+yPinch*yPinch);
            zoomZero=zoom;

            /*println(touches[0].x,touches[0].y);
            println(touches[1].x,touches[1].y);
            println();*/
        }
    }

    public void touchMoved() {
        if(touches.length==1) {
            newMousePos = new PVector(touches[0].x, touches[0].y);
            println(touches[0].x, touches[0].y);
        } else if(touches.length==2) {
            float xPinch=touches[0].x-touches[1].x;
            float yPinch=touches[0].y-touches[1].y;
            float deltaPinch=sqrt(xPinch*xPinch+yPinch*yPinch)/pinchZero-1;
            if(zoomZero+deltaPinch<(float)1.5 && zoomZero+deltaPinch >(float)0.45)
                zoom=zoomZero+deltaPinch;
            //println(zoom);
        }
    }

    public void touchEnded() {
        if(zoom>(float)(1+maxZoom)/2) {
            zoom=1;
        } else {
            zoom=maxZoom;
        }
    }

}