package com.demgames.polypong;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.TransferFunctionType;

public class TrainingActivity extends AppCompatActivity {

    private static final String TAG = "trainingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_training);

        final Globals globals = (Globals) getApplicationContext();

        final Button trainButton = (Button) findViewById(R.id.trainButton);
        final Button testButton = (Button) findViewById(R.id.testButton);




        // create training set (logical XOR function)
        final DS trainingSet = new DS("xor",2, 1);
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
        trainingSet.load();

        // create multi layer perceptron
        final NN nn = new NN("test",new int[]{2,10,1});

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nn.train(trainingSet.dataSet);
                nn.save();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nn.load();
                nn.test(trainingSet.dataSet);

            }
        });
    }



    class NN {
        private String name;
        private int [] n_units;
        private NeuralNetwork neuralNetwork;

        NN(String name_,int[] n_units_) {
            this.name = name_;
            this.n_units = n_units_;

            this.neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.TANH,this.n_units);

        }

        void train(DataSet trainingSet) {
            System.out.println("training neural network started");
            this.neuralNetwork.learn(trainingSet);
            System.out.println("training neural network finished");
        }

        void test(DataSet testSet) {
            System.out.println("testing neural network");
            for(DataSetRow dataRow : testSet.getRows()) {

                this.neuralNetwork.setInput(dataRow.getInput());
                this.neuralNetwork.calculate();
                double[ ] networkOutput = this.neuralNetwork.getOutput();
                System.out.print("Input: " + Arrays.toString(dataRow.getInput()) );
                System.out.println(" Output: " + Arrays.toString(networkOutput) );

            }
        }

        void save() {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(this.name+".nn", Context.MODE_PRIVATE);
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

    }

    public class DS{
        private String name;
        private DataSet dataSet;

        DS(String name_, int inputSize, int outputSize){
            this.dataSet = new DataSet(inputSize,outputSize);
            this.name = name_;
        }


        void create(List<double[]> inputs, List<double[]> outputs) {
            for(int i=0;i<inputs.size();i++) {
                this.dataSet.addRow(new DataSetRow(inputs.get(i),outputs.get(i)));
            }
        }

        void add(double[] input, double[] output){
            this.dataSet.addRow(new DataSetRow(input,output));
        }

        void save() {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(this.name+".ds", Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(this.dataSet);
                os.close();
                fos.close();
                System.out.println("dataset saved");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void load() {
            try {
                FileInputStream fis = getApplicationContext().openFileInput(this.name+".ds");
                ObjectInputStream is = new ObjectInputStream(fis);
                this.dataSet = (DataSet) is.readObject();
                is.close();
                fis.close();
                System.out.println("dataset loaded");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
