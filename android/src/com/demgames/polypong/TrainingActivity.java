package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


import org.deeplearning4j.datasets.iterator.DoublesDataSetIterator;
import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;


public class TrainingActivity extends AppCompatActivity {

    private static final String TAG = "trainingActivity";
    private TextView trainingTextView;
    Thread trainingThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_training);

        final Globals globals = (Globals) getApplicationContext();

        final Button genButton = (Button) findViewById(R.id.genButton);
        final Button trainButton = (Button) findViewById(R.id.trainButton);
        final Button testButton = (Button) findViewById(R.id.testButton);

        this.trainingTextView = (TextView) findViewById(R.id.trainingTextView);
        final CheckBox resumeCheckBox = (CheckBox) findViewById(R.id.resumeCheckBox);


        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "training";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = 2;
                globals.getGameVariables().numberOfPlayers = 2;
                globals.getGameVariables().setBalls(true);
                globals.getGameVariables().setBats();

                globals.getGameVariables().aiState = false;
                globals.getGameVariables().gravityState = true;

                globals.getGameVariables().inputs.clear();
                globals.getGameVariables().outputs.clear();
                Intent startGame = new Intent(getApplicationContext(), GDXGameLauncher.class);
                startActivity(startGame);
            }
        });

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globals.getAI().loadData("test");

                if(resumeCheckBox.isChecked()) {
                    globals.getAI().loadModel("test");
                } else {
                    globals.getAI().buildModel("test",new int[]{globals.getAI().dataSet.numInputs(),20,20,globals.getAI().dataSet.numOutcomes()});
                }

                globals.getAI().train(1000);
                //model.save();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globals.getAI().loadModel("test");
                globals.getGameVariables().model = globals.getAI().model;
                //model.test(trainingSet.dataSet);

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "testing";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = 2;
                globals.getGameVariables().numberOfPlayers = 2;
                globals.getGameVariables().setBalls(true);
                globals.getGameVariables().setBats();

                globals.getGameVariables().aiState = true;
                globals.getGameVariables().gravityState = true;
                Intent startGame = new Intent(getApplicationContext(), GDXGameLauncher.class);
                startActivity(startGame);

            }
        });
    }






    @Override
    protected void onDestroy() {

        try{
            this.trainingThread.interrupt();
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
