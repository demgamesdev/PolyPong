package com.demgames.miscclasses;

import org.deeplearning4j.datasets.iterator.ExistingDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agent {
    static final String TAG = "Agent";

    public List<double[]> inputs= new ArrayList<double[]>();
    public List<double[]> outputs = new ArrayList<double[]>();

    public String nameModel,nameDataSet;
    private File filesDir;
    private MultiLayerNetwork model;
    public DataSet dataSet;
    int[] n_units;
    int numberOfBalls, numberOfPlayers;
    private Random random = new Random();
    private Thread trainThread;

    public Agent(File filesDir_) {
        this.filesDir = filesDir_;
    }

    public float[] predict(double[] inputs){
        INDArray output = this.model.output(Nd4j.create(new double[][]{inputs}));
        return(new float[]{output.getFloat(0),output.getFloat(1)});
    }


    public void buildModel(String name) {
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

        Thread buildThread = new Thread(new BuildRunnable(this,false));
        buildThread.start();
    }

    public void train(DataSet trainingDataSet,Integer n_iterations, boolean showInfo) {
        this.dataSet = trainingDataSet;
        System.out.println("training neural network started");
        System.out.println("setReceived size "+this.dataSet.numExamples());

        this.trainThread = new Thread(new TrainRunnable(this,n_iterations,false));
        this.trainThread.start();
    }

    public void saveModel() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(this.filesDir,"")+File.separator+"agents"+File.separator+this.nameModel+".model");
            boolean saveUpdater = true;
            ModelSerializer.writeModel(this.model, fos, saveUpdater);
            fos.close();
            System.out.println("model saved");

        } catch (Exception e) {
            System.out.println(TAG + "error while saving model" + e.getMessage());
        }
    }

    public void loadModel(String name) {
        this.nameModel = name;
        try{
            FileInputStream fis = new FileInputStream(new File(this.filesDir,"")+File.separator+"agents"+File.separator+this.nameModel+".model");
            this.model = ModelSerializer.restoreMultiLayerNetwork(fis);
            fis.close();
            System.out.println(TAG + "model loaded");
        } catch (Exception e) {
            System.out.println(TAG + " error while loading model" + e.getMessage());
        }

    }

    public void cancelTraining(){
        if(this.trainThread.isAlive()){
            this.trainThread.interrupt();
        } else {
            System.out.println(TAG + " Trainthread not alive");
        }
    }

    DataSet sampleDataSet(int numSamples) {
        DataSet tempDatset = new DataSet();
        for(int i=0;i<numSamples;i++) {
            tempDatset.addRow(this.dataSet.get(random.nextInt(this.dataSet.numExamples())),0);
        }
        return(tempDatset);
    }

    public void initDataSet(String name) {
        this.nameDataSet = name;
        this.dataSet = new DataSet();
    }

    public void createDataSet(String name,List<double[]> inputList, List<double[]> outputList) {
        this.nameDataSet = name;
        INDArray inputs = Nd4j.create(inputList.toArray(new double[inputList.size()][]));
        INDArray outputs= Nd4j.create(outputList.toArray(new double[outputList.size()][]));
        this.dataSet = new DataSet(inputs,outputs);
    }

    public void saveData() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(this.filesDir,"")+File.separator+"data"+File.separator+this.nameDataSet+".ds");
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

    public DataSet loadData(String name) {
        this.nameDataSet = name;
        DataSet tempDataSet = new DataSet();
        try {
            //System.out.println(Arrays.toString(getApplicationContext().fileList()));
            FileInputStream fis = new FileInputStream(new File(this.filesDir,"")+File.separator+"data"+File.separator+this.nameDataSet+".ds");
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

    private class BuildRunnable implements Runnable{
        private Agent agent;
        boolean showInfo;

        BuildRunnable(Agent agent_, boolean showInfo_) {
            super();
            this.agent = agent_;
            this.showInfo = showInfo_;
        }

        @Override
        public void run() {

            NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
                    .seed(1234) //include a random seed for reproducibility
                    .activation(Activation.RELU)
                    .weightInit(WeightInit.XAVIER)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(new RmsProp(0.01))
                    //.updater(new Adam(0.01))
                    //.updater(new Nesterovs(0.9))
                    //.l2(1e-5)
                    .miniBatch(true)
                    .list(); // regularize learning model

                /*builder.layer(new DenseLayer.Builder().nIn(this.ai.n_units[0]).nOut(this.ai.n_units[1]).weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build());*/
            builder.layer(new ConvolutionLayer.Builder().nIn(1).nOut(this.agent.n_units[1])
                    .activation(Activation.RELU)
                    .kernelSize(1,4)
                    .stride(1,4)
                    .build());
            builder.layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                    .kernelSize(this.agent.numberOfBalls,1)
                    .stride(this.agent.numberOfBalls,1)
                    .build());
            builder.layer(new DenseLayer.Builder().nIn(this.agent.n_units[1]).nOut(this.agent.n_units[2])
                    .activation(Activation.RELU)
                    .build());

            for(int l=2;l<this.agent.n_units.length-2;l++){
                builder.layer(new DenseLayer.Builder().nOut(this.agent.n_units[l+1])
                        .activation(Activation.RELU)
                        .build());
            }
            builder.layer(new OutputLayer.Builder().nOut(this.agent.n_units[this.agent.n_units.length - 1])
                    .activation(Activation.TANH).lossFunction(LossFunctions.LossFunction.MSE)
                    .build());
            builder.setInputType(InputType.convolutionalFlat(this.agent.numberOfBalls,this.agent.n_units[0],1));
            builder.trainingWorkspaceMode(WorkspaceMode.ENABLED);
            builder.inferenceWorkspaceMode(WorkspaceMode.ENABLED);

            this.agent.model = new MultiLayerNetwork(builder.build());
            this.agent.model.init();
            System.out.println("Model built with "+this.agent.model.getLayers().length +" layers");
            this.agent.saveModel();
            //this.ai.model.setListeners(new ScoreIterationListener(5)); //print the score with every iteration

        }

    }

    private class TrainRunnable implements Runnable {
        private Agent agent;
        private int nIterations;
        private boolean showInfo;

        TrainRunnable(Agent agent_,int nIterations_, boolean showInfo_) {
            super();
            this.agent = agent_;
            this.nIterations = nIterations_;
            this.showInfo = showInfo_;
        }

        @Override
        //Runs on background thread, this is where we will initiate the Workspace
        public void run() {
            WorkspaceConfiguration initialConfig = WorkspaceConfiguration.builder()
                    .initialSize(10 * 1024L * 1024L)
                    .policyAllocation(AllocationPolicy.STRICT)
                    .policyLearning(LearningPolicy.NONE)
                    .build();

            System.out.println("Training workspace config: " + this.agent.model.getLayerWiseConfigurations().getTrainingWorkspaceMode());
            try(MemoryWorkspace ws = Nd4j.getWorkspaceManager().getAndActivateWorkspace(initialConfig, "TrainingWorkSpace")) {
                DataSet trainDataSet = this.agent.dataSet;
                    /*FileInputStream fis = new FileInputStream(new File(getFilesDir(),"")+File.separator+"traindataset.ds");
                    trainDataSet.load(fis);
                    fis.close();*/

                ExistingDataSetIterator testIterator;
                RegressionEvaluation evaluation = new RegressionEvaluation(trainDataSet.numOutcomes());
                DataSet miniBatch;
                double predictionError = 0;
                String infoText;

                Nd4j.getMemoryManager().setAutoGcWindow(5000);
                for (int i = 0; i < this.nIterations; i++) {
                    miniBatch = trainDataSet.sample(10);
                    this.agent.model.fit(miniBatch);
                    //this.ai.model.fit(miniBatch);
                    if (i % 10 == 0 && this.showInfo) {
                        //evaluation.eval(this.ai.dataSet.getLabels(), this.ai.model.output(this.ai.dataSet.getFeatures(), false));
                        testIterator = new ExistingDataSetIterator(miniBatch);
                        this.agent.model.doEvaluation(testIterator,evaluation);
                        predictionError = evaluation.averagerootMeanSquaredError();
                        infoText = "Epoch " + i + "/" + nIterations + " error " + Math.round(predictionError * 10000.0) / 10000.0;
                        System.out.println(infoText);
                    }
                    //System.out.println("Epoch " + i + "/" + params[0]);
                }
            }catch(Exception e){e.printStackTrace();}

            System.out.println("training neural network finished");

            this.agent.saveModel();
        }

    }

}
