package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.input.WeightedSum;
import org.neuroph.core.learning.IterativeLearning;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.learning.ResilientPropagation;
import org.neuroph.core.transfer.Gaussian;
import org.neuroph.core.transfer.Linear;
import org.neuroph.core.transfer.Log;
import org.neuroph.core.transfer.Ramp;
import org.neuroph.core.transfer.Sgn;
import org.neuroph.core.transfer.Sigmoid;
import org.neuroph.core.transfer.Sin;
import org.neuroph.core.transfer.Step;
import org.neuroph.core.transfer.Tanh;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.core.transfer.Trapezoid;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.comp.neuron.BiasNeuron;
import org.neuroph.nnet.comp.neuron.InputNeuron;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.DynamicBackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.LayerFactory;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.random.GaussianRandomizer;
import org.neuroph.util.random.RangeRandomizer;

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




        // create training set (logical XOR function)



        // create multi layer perceptron

        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "training";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = 1;
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
                NN nn = new NN("test");


                DS trainingSet = new DS("test");
                /*List<double[]> inputs = new ArrayList<double[]>(){};
                List<double[]> outputs = new ArrayList<double[]>(){};
                inputs.add(new double[]{0, 0});
                inputs.add(new double[]{1, 0});
                inputs.add(new double[]{0, 1});
                inputs.add(new double[]{1, 1});
                outputs.add(new double[]{0});
                outputs.add(new double[]{1});
                outputs.add(new double[]{1});
                outputs.add(new double[]{0});

                trainingSet.create(inputs,outputs);
                trainingSet.save();*/
                /*trainingSet.add(new double[]{0,0,0,0},new double[]{0,0});
                trainingSet.add(new double[]{-5,5,0.5,0.5},new double[]{1,6});
                trainingSet.add(new double[]{0,0,0.6,0.6},new double[]{0,1});
                trainingSet.add(new double[]{0.7,-1,0.7,1},new double[]{-2,-1});*/

                trainingSet.load();

                if(resumeCheckBox.isChecked()) {
                    nn.load();
                } else {
                    nn.build(new int[]{trainingSet.dataSet.getInputSize(),100,trainingSet.dataSet.getOutputSize()});
                }

                nn.train(trainingSet);
                //nn.save();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NN nn = new NN("test");
                nn.load();
                //nn.test(trainingSet.dataSet);
                globals.getGameVariables().nn = nn.neuralNetwork;

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "testing";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = 1;
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




    class NN {
        private String name;
        private NeuralNetwork neuralNetwork;
        private int maxEpochs =10000;

        NN(String name_) {
            this.name = name_;
        }

        void build(int[] n_units) {

            this.neuralNetwork = new NeuralNetwork();
            NeuronProperties neuronProperties = new NeuronProperties();
            neuronProperties.setProperty("transferFunction", TransferFunctionType.RAMP);
            neuronProperties.setProperty("inputFunction", WeightedSum.class);

            Layer layer = LayerFactory.createLayer(n_units[0], new NeuronProperties(InputNeuron.class, Linear.class));
            layer.addNeuron(new BiasNeuron());
            this.neuralNetwork.addLayer(layer);
            Layer prevLayer = layer;
            for(int l=1;l<n_units.length-1;l++) {
                layer = LayerFactory.createLayer(n_units[l],neuronProperties);
                layer.addNeuron(new BiasNeuron());
                this.neuralNetwork.addLayer(layer);
                ConnectionFactory.fullConnect(prevLayer,layer);
                prevLayer = layer;
            }
            neuronProperties.setProperty("transferFunction", TransferFunctionType.LINEAR);
            layer = LayerFactory.createLayer(n_units[n_units.length-1],neuronProperties);
            this.neuralNetwork.addLayer(layer);
            ConnectionFactory.fullConnect(prevLayer,layer);
            NeuralNetworkFactory.setDefaultIO(this.neuralNetwork);

            //this.neuralNetwork.randomizeWeights(new RangeRandomizer(-0.7, 0.7));
            this.neuralNetwork.randomizeWeights(new GaussianRandomizer(0,0.01));

            //this.neuralNetwork = new MultiLayerPerceptron(n_units);


            //this.neuralNetwork.randomizeWeights();
        }

        void train(DS trainingSet) {
            final Handler handler = new Handler();
            class UpdateRunnable implements Runnable {
                String text;
                TextView textView;
                UpdateRunnable(String text_,TextView textView_){
                    this.text = text_;
                    this.textView = textView_;
                }
                public void run(){
                    this.textView.setText(this.text);
                }
            }

            class TrainingThread extends Thread {
                int maxEpochs;
                DS trainingSet;
                NN nn;
                TextView textView;
                String text;
                TrainingThread(int maxEpochs_, DS trainingSet_, NN nn_){
                    super();
                    this.maxEpochs = maxEpochs_;
                    this.trainingSet = trainingSet_;
                    this.nn = nn_;
                }
                public void run() {

                    //BackPropagation trainingRule = new BackPropagation();



                    MBP trainingRule = new MBP();
                    //RPROP trainingRule = new RPROP();
                    //trainingRule.setBatchMode(true);
                    trainingRule.setLearningRate(0.01);


                    trainingRule.setMomentum(0.9);
                    trainingRule.setNeuralNetwork(this.nn.neuralNetwork);
                    trainingRule.onStart();
                    //trainingRule.setMaxIterations(10);


                    //this.nn.neuralNetwork.setLearningRule(trainingRule);

                    //this.nn.neuralNetwork.learn(trainingSet.dataSet);

                    for(int i=0;i<this.maxEpochs;i++) {
                        trainingRule.doOneLearningIteration(this.trainingSet.sampleMinibatch(100));
                        //trainingRule.doLearningEpoch(this.trainingSet.dataSet);
                        if(i%10 == 0) {
                            System.out.println("Epoch " + i + "/"+this.maxEpochs +" error " + Math.round(trainingRule.getTotalNetworkError()*10000.0)/10000.0);
                            handler.post(new UpdateRunnable("Epoch " + i + "/"+this.maxEpochs +" error " + Math.round(trainingRule.getTotalNetworkError()*10000.0)/10000.0,trainingTextView));
                            //trainingTextView.setText("Epoch " + i + "/" + this.maxEpochs + " error " + trainingRule.getTotalNetworkError());
                            /*handler.post(new Runnable(, trainingTextView) {
                                @Override
                                public void run() {
                                    trainingTextView.setText("Epoch " + i + "/" + this.maxEpochs + " error " + trainingRule.getTotalNetworkError());
                                }
                            });*/


                        }

                        if(this.isInterrupted()){
                            System.out.println("Training interrupted");
                            break;
                        }
                    }

                    //this.neuralNetwork.learn(trainingSet);
                    this.nn.save();
                    System.out.println("training neural network finished");

                }

            }

            System.out.println("training neural network started");
            System.out.println("set size "+trainingSet.dataSet.size());

            TrainingActivity.this.trainingThread = new TrainingThread(this.maxEpochs,trainingSet,this);
            try{
                trainingThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*for(DataSetRow dataRow : trainingSet.getRows()) {
                System.out.print("Input: " + Arrays.toString(dataRow.getInput()));
                System.out.println(" Output: " + Arrays.toString(dataRow.getDesiredOutput()));

            }*/

            this.test(trainingSet.sampleMinibatch(10));
        }

        void test(DataSet testSet) {
            System.out.println("testing neural network");

            for(DataSetRow dataRow : testSet.getRows()) {

                this.neuralNetwork.setInput(dataRow.getInput());
                this.neuralNetwork.calculate();
                double[] networkOutput = this.neuralNetwork.getOutput();
                System.out.print("Target: " + Arrays.toString(dataRow.getDesiredOutput()));
                System.out.println(" Output: " + Arrays.toString(networkOutput));

            }
        }

        void save() {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(this.name+".nn", MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(this.neuralNetwork);
                os.close();
                fos.close();
                System.out.println("neural network saved");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void load() {
            try {
                FileInputStream fis = getApplicationContext().openFileInput(this.name+".nn");
                ObjectInputStream is = new ObjectInputStream(fis);
                this.neuralNetwork = (NeuralNetwork) is.readObject();
                is.close();
                fis.close();
                System.out.println("neural network loaded");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        class MBP extends DynamicBackPropagation{
            @Override
            public void onStart() {
                super.onStart();
                // create MomentumTrainingData objects that will be used during the training to store previous weight value
                for (Layer layer : this.neuralNetwork.getLayers()) {
                    for (Neuron neuron : layer.getNeurons()) {
                        for (Connection connection : neuron.getInputConnections()) {
                            connection.getWeight().setTrainingData(new MomentumTrainingData());
                        }
                    } // for
                } // for
            }
        }

        class RPROP extends ResilientPropagation{
            @Override
            public void onStart() {
                super.onStart(); // init all stuff from superclasses

                // create ResilientWeightTrainingtData objects that will hold additional data (resilient specific) during the training
                for (Layer layer : this.neuralNetwork.getLayers()) {
                    for (Neuron neuron : layer.getNeurons()) {
                        for (Connection connection : neuron.getInputConnections()) {
                            connection.getWeight().setTrainingData(new ResilientWeightTrainingtData());
                        }
                    }
                }
            }
        }
    }

    public class DS{
        private String name;
        private DataSet dataSet;
        private Random random = new Random();

        DS(String name_){
            this.name = name_;
        }

        void build(int inputSize,int outputSize) {
            this.dataSet =  new DataSet(inputSize,outputSize);
        }


        void create(List<double[]> inputs, List<double[]> outputs) {
            for(int i=0;i<inputs.size();i++) {
                this.dataSet.addRow(new DataSetRow(inputs.get(i),outputs.get(i)));
            }
        }

        void add(double[] input, double[] output){
            this.dataSet.addRow(new DataSetRow(input,output));
        }

        DataSet sampleMinibatch(int size) {
            DataSet tempDataSet = new DataSet(this.dataSet.getInputSize(),this.dataSet.getOutputSize());
            for(int i=0;i<size;i++) {
                tempDataSet.addRow(this.dataSet.getRowAt(this.random.nextInt(this.dataSet.size())));
            }
            return(tempDataSet);
        }

        void save() {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(this.name+".ds", Context.MODE_PRIVATE);
                //FileOutputStream fos = new FileOutputStream(new File(getFilesDir(),"")+File.separator+this.name+".ds");
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(this.dataSet);
                os.close();
                fos.close();
                System.out.println("dataset saved size "+this.dataSet.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void load() {
            try {
                System.out.println(Arrays.toString(getApplicationContext().fileList()));
                FileInputStream fis = getApplicationContext().openFileInput(this.name+".ds");
                //FileInputStream fis = new FileInputStream(new File(getFilesDir(),"")+File.separator+this.name+".ds");
                ObjectInputStream is = new ObjectInputStream(fis);
                this.dataSet = (DataSet) is.readObject();
                is.close();
                fis.close();
                System.out.println("dataset loaded size "+this.dataSet.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
