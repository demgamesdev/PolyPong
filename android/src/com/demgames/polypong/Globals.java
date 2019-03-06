package com.demgames.polypong;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.ServerListener;

import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;

public class Globals extends Application implements IGlobals{
    private static final String TAG = "Globals";

    private ClientListener clientListener;
    private ServerListener serverListener;

    List<InetAddress> hostsList;

    private IGlobals.GameVariables gameVariables=new IGlobals.GameVariables();
    private IGlobals.SettingsVariables settingsVariables =new IGlobals.SettingsVariables();
    public AI ai = new AI();

    public synchronized IGlobals.GameVariables getGameVariables() {
        return(this.gameVariables);
    }
    public synchronized IGlobals.SettingsVariables getSettingsVariables() {
        return(this.settingsVariables);
    }
    public synchronized AI getAI(){
        return(this.ai);
    }

    public void setListeners(Context context_){
        this.clientListener =new ClientListener(context_);
        this.serverListener =new ServerListener(context_);
    }

    public ClientListener getClientListener() {
        return (this.clientListener);
    }
    public ServerListener getServerListener() {
        return (this.serverListener);
    }

    public void setHostsList(List <InetAddress> newHostsList) {
        this.hostsList=newHostsList;
    }

    public List <InetAddress> getHostsList() {
        return(this.hostsList);
    }

    class AI {

        public String nameModel;
        public String nameDataSet;
        public MultiLayerNetwork model;
        public DataSet dataSet;
        int[] n_units;
        public AI.TrainTask trainingTask;
        public TextView infoTextView;



        void buildModel(String name, int[] n_units_) {
            this.nameModel = name;
            this.n_units = n_units_;

            AI.BuildTask buildTask = new AI.BuildTask(this, true, this.infoTextView);
            buildTask.execute();
        }

        void train(Integer n_iterations, boolean showInfo) {

            System.out.println("training neural network started");
            System.out.println("set size "+this.dataSet.numExamples());

            this.trainingTask = new AI.TrainTask(this,showInfo,this.infoTextView);
            this.trainingTask.execute(n_iterations);

        }

        void saveModel() {
            try {
                OutputStream outputStream = new FileOutputStream(new File(getFilesDir(),"")+File.separator+this.nameModel+".model");
                boolean saveUpdater = true;
                ModelSerializer.writeModel(this.model, outputStream, saveUpdater);
                System.out.println("model saved");

            } catch (Exception e) {
                Log.e(TAG,"error while saving model" + e.getMessage());
            }
        }

        void loadModel(String name) {
            this.nameModel = name;
            try{
                FileInputStream fis = new FileInputStream(new File(getFilesDir(),"")+File.separator+this.nameModel+".model");
                this.model = ModelSerializer.restoreMultiLayerNetwork(fis);
                System.out.println("model loaded");
            } catch (Exception e) {
                Log.e(TAG,"error while loading model" + e.getMessage());
            }

        }

        void createDataSet(String name,List<double[]> inputList, List<double[]> outputList) {
            this.nameDataSet = name;
            INDArray inputs = Nd4j.create(inputList.toArray(new double[inputList.size()][]));
            INDArray outputs= Nd4j.create(outputList.toArray(new double[outputList.size()][]));
            this.dataSet = new DataSet(inputs,outputs);
        }

        void saveData() {
            try {
                FileOutputStream fos = new FileOutputStream(new File(getFilesDir(),"")+File.separator+this.nameDataSet+".ds");
                this.dataSet.save(fos);
                fos.close();
                System.out.println("dataset saved size "+this.dataSet.numExamples());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void loadData(String name) {
            this.nameDataSet = name;
            this.dataSet = new DataSet();
            try {
                //System.out.println(Arrays.toString(getApplicationContext().fileList()));
                FileInputStream fis = new FileInputStream(new File(getFilesDir(),"")+File.separator+this.nameDataSet+".ds");
                this.dataSet.load(fis);
                fis.close();
                System.out.println("dataset loaded size "+this.dataSet.numExamples());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class BuildTask extends AsyncTask<Integer, String, String> {
            private AI ai;
            TextView infoTextView;
            boolean showInfo;

            BuildTask(AI ai_, boolean showInfo_, TextView infoTextView_) {
                super();
                this.ai = ai_;
                this.showInfo = showInfo_;
                this.infoTextView = infoTextView_;

            }

            // Runs in UI before background thread is called
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                this.infoTextView.setText("Building model");

            }

            @Override
            //Runs on background thread, this is where we will initiate the Workspace
            protected String doInBackground(Integer... params) {

                MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                        .seed(1234) //include a random seed for reproducibility
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .updater(new RmsProp(0.01))
                        .l2(1e-4) // regularize learning model
                        .list()

                        .layer( new DenseLayer.Builder().nIn(this.ai.n_units[0]).nOut(this.ai.n_units[1]).weightInit(WeightInit.XAVIER)
                                .activation(Activation.RELU)
                                .build())
                        .layer(new DenseLayer.Builder().nIn(this.ai.n_units[1]).nOut(this.ai.n_units[2]).weightInit(WeightInit.XAVIER)
                                .activation(Activation.RELU)
                                .build())
                        .layer( new OutputLayer.Builder().nIn(this.ai.n_units[2]).nOut(this.ai.n_units[this.ai.n_units.length - 1]).weightInit(WeightInit.XAVIER)
                                .activation(Activation.TANH).lossFunction(LossFunctions.LossFunction.SQUARED_LOSS)
                                .build())
                        .build();

                this.ai.model = new MultiLayerNetwork(conf);
                this.ai.model.init();
                //this.ai.model.setListeners(new ScoreIterationListener(5)); //print the score with every iteration

                return("0");

            }

            @Override
            protected void onPostExecute(String values) {
                super.onPostExecute(values);
                this.infoTextView.setText("Building model finished");
                //Handle results and update UI here.
            }

        }

        public class TrainTask extends AsyncTask<Integer, String, INDArray> {
            private AI ai;
            private boolean showInfo;
            private TextView infoTextView;
            TrainTask(AI ai_, boolean showInfo_, TextView infoTextView_) {
                super();
                this.ai = ai_;
                this.showInfo = showInfo_;
                this.infoTextView = infoTextView_;

            }

            // Runs in UI before background thread is called
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            //Runs on background thread, this is where we will initiate the Workspace
            protected INDArray doInBackground(Integer... params) {
                List<Pair<INDArray, INDArray>> dataPairs = new ArrayList<>();
                for (DataSet dataRow : this.ai.dataSet) {
                    dataPairs.add(new Pair<INDArray, INDArray>(dataRow.getFeatures(), dataRow.getLabels()));
                }
                DataSetIterator dataSetIterator = new INDArrayDataSetIterator(dataPairs, 100);
                DataSet miniBatch;
                RegressionEvaluation evaluation = new RegressionEvaluation(this.ai.dataSet.numOutcomes());
                double predictionError;
                String infoText;
                for (int i = 0; i < params[0]; i++) {
                    //miniBatch = dataSetIterator.next();
                    this.ai.model.fit(dataSet);
                    if(i% 10 ==0 && this.showInfo) {
                        evaluation.eval(this.ai.dataSet.getLabels(), this.ai.model.output(this.ai.dataSet.getFeatures(), false));
                        predictionError = evaluation.averageMeanSquaredError();
                        infoText = "Epoch " + i + "/"+params[0] +" error " + Math.round(predictionError*10000.0)/10000.0;
                        publishProgress(infoText);
                        System.out.println(infoText);
                    }

                    if(this.isCancelled()){
                        System.out.println("training canceled");
                        break;
                    }
                }

                System.out.println("training neural network finished");

                this.ai.saveModel();
                return(this.ai.model.output(this.ai.dataSet.getFeatures()));
            }

            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                if(this.showInfo) {
                    this.infoTextView.setText(values[0]);
                }

            }

            protected void onPostExecute(INDArray result) {
                super.onPostExecute(result);
                //Handle results and update UI here.
            }

        }
    }

}

