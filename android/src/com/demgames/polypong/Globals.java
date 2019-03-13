package com.demgames.polypong;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.service.autofill.Dataset;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.ServerListener;

import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.datasets.iterator.ExistingDataSetIterator;
import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.Convolution1D;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.Pooling1D;
import org.deeplearning4j.nn.conf.layers.Pooling2D;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.memory.conf.WorkspaceConfiguration;
import org.nd4j.linalg.api.memory.enums.AllocationPolicy;
import org.nd4j.linalg.api.memory.enums.LearningPolicy;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.ExistingMiniBatchDataSetIterator;
import org.nd4j.linalg.dataset.MiniBatchFileDataSetIterator;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
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
        int numberOfBalls, numberOfPlayers;
        public AI.TrainTask trainingTask;
        public TextView infoTextView;
        private Random random = new Random();



        void buildModel(String name) {
            this.nameModel = name;
            String[] split1 = name.split("_");
            this.numberOfPlayers = Integer.parseInt(split1[1]);
            this.numberOfBalls = Integer.parseInt(split1[2]);
            String[] split2 = split1[3].split("-");
            this.n_units = new int[split2.length];
            for(int i =0;i<split2.length;i++) {
                this.n_units[i] = Integer.parseInt(split2[i]);
            }
            System.out.println("nunits " + this.n_units);

            AI.BuildTask buildTask = new AI.BuildTask(this, true, this.infoTextView);
            buildTask.execute();

            this.saveModel();
        }

        void train(DataSet trainingDataSet,Integer n_iterations, boolean showInfo) {
            this.dataSet = trainingDataSet;
            System.out.println("training neural network started");
            System.out.println("set size "+this.dataSet.numExamples());

            this.trainingTask = new AI.TrainTask(this,showInfo,this.infoTextView);
            this.trainingTask.execute(n_iterations,100); //number of epochs, minibatchsize

        }

        void saveModel() {
            try {
                FileOutputStream fos = new FileOutputStream(new File(getFilesDir(),"")+File.separator+"agents"+File.separator+this.nameModel+".model");
                boolean saveUpdater = true;
                ModelSerializer.writeModel(this.model, fos, saveUpdater);
                fos.close();
                System.out.println("model saved");

            } catch (Exception e) {
                Log.e(TAG,"error while saving model" + e.getMessage());
            }
        }

        void loadModel(String name) {
            this.nameModel = name;
            try{
                FileInputStream fis = new FileInputStream(new File(getFilesDir(),"")+File.separator+"agents"+File.separator+this.nameModel+".model");
                this.model = ModelSerializer.restoreMultiLayerNetwork(fis);
                fis.close();
                System.out.println("model loaded");
            } catch (Exception e) {
                Log.e(TAG,"error while loading model" + e.getMessage());
            }

        }

        DataSet sampleDataSet(int numSamples) {
            DataSet tempDatset = new DataSet();
            for(int i=0;i<numSamples;i++) {
                tempDatset.addRow(this.dataSet.get(random.nextInt(this.dataSet.numExamples())),0);
            }
            return(tempDatset);
        }

        void initDataSet(String name) {
            this.nameDataSet = name;
            this.dataSet = new DataSet();
        }

        void createDataSet(String name,List<double[]> inputList, List<double[]> outputList) {
            this.nameDataSet = name;
            INDArray inputs = Nd4j.create(inputList.toArray(new double[inputList.size()][]));
            INDArray outputs= Nd4j.create(outputList.toArray(new double[outputList.size()][]));
            this.dataSet = new DataSet(inputs,outputs);
        }

        void saveData() {
            try {
                FileOutputStream fos = new FileOutputStream(new File(getFilesDir(),"")+File.separator+"data"+File.separator+this.nameDataSet+".ds");
                /*ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(this.dataSet);
                os.close();*/
                this.dataSet.save(fos);
                fos.close();
                System.out.println("dataset saved size "+this.dataSet.numExamples());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DataSet loadData(String name) {
            this.nameDataSet = name;
            DataSet tempDataSet = new DataSet();
            try {
                //System.out.println(Arrays.toString(getApplicationContext().fileList()));
                FileInputStream fis = new FileInputStream(new File(getFilesDir(),"")+File.separator+"data"+File.separator+this.nameDataSet+".ds");
                /*ObjectInputStream is = new ObjectInputStream(fis);
                this.dataSet = (DataSet) is.readObject();
                is.close();*/
                tempDataSet.load(fis);
                fis.close();
                System.out.println("dataset loaded size "+tempDataSet.numExamples());
                this.dataSet = tempDataSet;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return(tempDataSet);
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
                //this.infoTextView.setText("Building model");

            }

            @Override
            //Runs on background thread, this is where we will initiate the Workspace
            protected String doInBackground(Integer... params) {

                NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
                        .seed(1234) //include a random seed for reproducibility
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .updater(new RmsProp(0.01))
                        //.updater(new Adam(0.01))
                        //.updater(new Nesterovs(0.9))
                        .l2(1e-5)
                        .miniBatch(true)
                        .list(); // regularize learning model

                /*builder.layer(new DenseLayer.Builder().nIn(this.ai.n_units[0]).nOut(this.ai.n_units[1]).weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build());*/
                builder.layer(new ConvolutionLayer.Builder().nIn(1).nOut(this.ai.n_units[1])
                        .activation(Activation.RELU)
                        .kernelSize(1,4)
                        .stride(1,4)
                        .build());
                builder.layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(this.ai.numberOfBalls,1)
                        .stride(this.ai.numberOfBalls,1)
                        .build());
                builder.layer(new DenseLayer.Builder().nIn(this.ai.n_units[1]).nOut(this.ai.n_units[2])
                        .activation(Activation.RELU)
                        .build());

                for(int l=2;l<this.ai.n_units.length-2;l++){
                    builder.layer(new DenseLayer.Builder().nOut(this.ai.n_units[l+1])
                            .activation(Activation.RELU)
                            .build());
                }
                builder.layer(new OutputLayer.Builder().nOut(this.ai.n_units[this.ai.n_units.length - 1])
                        .activation(Activation.TANH).lossFunction(LossFunctions.LossFunction.MSE)
                        .build());
                builder.setInputType(InputType.convolutionalFlat(this.ai.numberOfBalls,this.ai.n_units[0],1));
                builder.trainingWorkspaceMode(WorkspaceMode.ENABLED);
                builder.inferenceWorkspaceMode(WorkspaceMode.ENABLED);

                this.ai.model = new MultiLayerNetwork(builder.build());
                this.ai.model.init();
                System.out.println("Model built with "+this.ai.model.getLayers().length +" layers");
                //this.ai.model.setListeners(new ScoreIterationListener(5)); //print the score with every iteration

                return("0");

            }

            @Override
            protected void onPostExecute(String values) {
                super.onPostExecute(values);
                //this.infoTextView.setText("Building model finished");
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
                WorkspaceConfiguration initialConfig = WorkspaceConfiguration.builder()
                        .initialSize(10 * 1024L * 1024L)
                        .policyAllocation(AllocationPolicy.OVERALLOCATE)
                        .policyLearning(LearningPolicy.NONE)
                        .build();

                System.out.println("Training workspace config: " + this.ai.model.getLayerWiseConfigurations().getTrainingWorkspaceMode());
                try(MemoryWorkspace ws = Nd4j.getWorkspaceManager().getAndActivateWorkspace(initialConfig, "TrainingWorkSpace")) {
                    DataSet trainDataSet = this.ai.dataSet;
                    /*FileInputStream fis = new FileInputStream(new File(getFilesDir(),"")+File.separator+"traindataset.ds");
                    trainDataSet.load(fis);
                    fis.close();*/

                    ExistingDataSetIterator testIterator;
                    RegressionEvaluation evaluation = new RegressionEvaluation(trainDataSet.numOutcomes());
                    DataSet miniBatch;
                    double predictionError = 0;
                    String infoText;

                    Nd4j.getMemoryManager().setAutoGcWindow(5000);
                    for (int i = 0; i < params[0]; i++) {
                        miniBatch = trainDataSet.sample(10);
                        this.ai.model.fit(miniBatch);
                        //this.ai.model.fit(miniBatch);
                        if (i % 10 == 0 && this.showInfo) {
                            //evaluation.eval(this.ai.dataSet.getLabels(), this.ai.model.output(this.ai.dataSet.getFeatures(), false));
                            testIterator = new ExistingDataSetIterator(miniBatch);
                            this.ai.model.doEvaluation(testIterator,evaluation);
                            predictionError = evaluation.averagerootMeanSquaredError();
                            infoText = "Epoch " + i + "/" + params[0] + " error " + Math.round(predictionError * 10000.0) / 10000.0;
                            publishProgress(infoText);
                            System.out.println(infoText);
                        }
                        //System.out.println("Epoch " + i + "/" + params[0]);

                        if (this.isCancelled()) {
                            System.out.println("training canceled");
                            break;
                        }
                    }
                }catch(Exception e){e.printStackTrace();}

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

