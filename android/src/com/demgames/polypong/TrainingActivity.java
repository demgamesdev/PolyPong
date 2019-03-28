package com.demgames.polypong;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demgames.miscclasses.GameObjectClasses;
import com.google.common.util.concurrent.AtomicDouble;

import org.nd4j.linalg.dataset.DataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TrainingActivity extends AppCompatActivity {

    private static final String TAG = "TrainingActivity";
    private TextView infoTextView;

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

        this.infoTextView = (TextView) findViewById(R.id.trainingTextView);
        final TextView agentNameTextView = (TextView) findViewById(R.id.agentNameTextView);
        final CheckBox resumeCheckBox = (CheckBox) findViewById(R.id.resumeCheckBox);
        final ListView trainingDataListView = (ListView) findViewById(R.id.trainingDataListView);

        String agentName = getIntent().getStringExtra("agentname");
        String[] agentNameSplit1 = agentName.split("_");
        String[] agentNameSplit2 = agentNameSplit1[1].split("-");
        int ballNumber = Integer.parseInt(agentNameSplit1[2]);

        agentNameTextView.setText("Training of " + agentNameSplit1[0]);

        List<String> dataList = new ArrayList<>();
        List<String> dataNameList = new ArrayList<>();
        File dataDir = new File(getApplication().getFilesDir().getAbsolutePath() + File.separator + "data");
        String[] dataFiles = dataDir.list();
        for(int i = 0; i<dataFiles.length;i++) {
            String[] tempSplit1 = dataFiles[i].split("\\.");
            String[] tempSplit2 = tempSplit1[0].split("_");//name_balls_players.ds

            if(tempSplit2[2].equals(Integer.toString(ballNumber))) {
                dataList.add(tempSplit1[0]);
                dataNameList.add(tempSplit2[0] + " (" + tempSplit2[1] + ";" + tempSplit2[2] + ")");
            }
        }

        ArrayAdapter<String> dataAdapter =
                new ArrayAdapter<>(getApplication(), R.layout.item_choice_multiple,R.id.choiceMultipleTextView,dataNameList); // Beispieldaten in einer ArrayList

        trainingDataListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        trainingDataListView.setAdapter(dataAdapter);



        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globals.setupAgent(getApplicationContext());

                List<DataSet> checkedDataSetList = new ArrayList<>();
                SparseBooleanArray checkedItemPositions = trainingDataListView.getCheckedItemPositions();

                for(int i=0;i<dataList.size();i++) {
                    if(checkedItemPositions.get(i)){
                        DataSet tempDataSet = globals.getAgent().loadDataSet(dataList.get(i));
                        checkedDataSetList.add(tempDataSet);
                        System.out.println("dataset " + i + " size " + tempDataSet.numExamples());
                    }
                }

                if(checkedDataSetList.size()>0) {
                    DataSet combinedDataSet = DataSet.merge(checkedDataSetList);
                    System.out.println("combined dataset size " + combinedDataSet.numExamples());

                    int nIterations = 5000;

                    globals.getAgent().train(combinedDataSet, agentName,nIterations, resumeCheckBox.isChecked(),true);
                    TrainInfoTask trainingInfoTask = new TrainInfoTask();
                    trainingInfoTask.execute(nIterations);
                    //model.save();
                } else {
                    Toast.makeText(getApplication(), "Select at least one dataset",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globals.setupAgent(getApplicationContext());
                globals.getAgent().loadModel(agentName);
                //model.test(trainingSet.dataSet);

                globals.getComm().initGame(0,ballNumber,2,"normal",true,false,true);
                globals.getComm().resetPlayerMap();
                globals.getComm().playerMap.put(0,new GameObjectClasses.Player("Dummy","0.0.0.0"));
                globals.getComm().playerMap.put(1,new GameObjectClasses.Player("Dummy","0.0.0.0"));


                Intent startGDXGameLauncher = new Intent(getApplicationContext(), GDXGameLauncher.class);

                startGDXGameLauncher.putExtra("myplayername",getIntent().getStringExtra("myplayername"));
                startGDXGameLauncher.putExtra("myplayernumber",0);
                startGDXGameLauncher.putExtra("numberofplayers",globals.getComm().playerMap.size());
                startGDXGameLauncher.putExtra("numberofballs",ballNumber);
                startGDXGameLauncher.putExtra("gravitystate",true);
                startGDXGameLauncher.putExtra("attractionstate",false);
                startGDXGameLauncher.putExtra("gamemode","testing");
                startGDXGameLauncher.putExtra("mode","normal");
                startGDXGameLauncher.putExtra("agentmode",true);
                startActivity(startGDXGameLauncher);

            }
        });
    }

    private class TrainInfoTask extends AsyncTask<Integer, Double, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            Globals globals = (Globals) getApplicationContext();
            AtomicDouble[] trainingData =globals.getAgent().getTrainingData();
            while(globals.getAgent().isTrainingRunning()) {
                trainingData = globals.getAgent().getTrainingData();
                publishProgress((double)params[0],trainingData[0].get(),trainingData[1].get());
                try{
                    Thread.sleep(100);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            publishProgress((double)params[0],trainingData[0].get(),trainingData[1].get());
            System.out.println(TAG + " TrainInfoTask ended");
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            infoTextView.setText("Episode "+values[1].intValue() + "/"+values[0].intValue() + " error "+String.format("%.4f",values[2]));
        }
    }





    @Override
    protected void onDestroy() {
        Globals globals = (Globals) getApplicationContext();

        try{
            globals.getAgent().interruptTraining();
            Toast.makeText(getApplication(), "Training canceled",
                    Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
