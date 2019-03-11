package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nd4j.linalg.dataset.DataSet;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


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

        final TextView infoTextView = (TextView) findViewById(R.id.trainingTextView);
        final TextView agentNameTextView = (TextView) findViewById(R.id.agentNameTextView);
        final CheckBox resumeCheckBox = (CheckBox) findViewById(R.id.resumeCheckBox);
        final ListView trainingDataListView = (ListView) findViewById(R.id.trainingDataListView);
        globals.getAI().infoTextView = infoTextView;

        String agentName = getIntent().getStringExtra("agentname");
        String[] agentNameSplit1 = agentName.split("_");
        String[] agentNameSplit2 = agentNameSplit1[1].split("-");
        int ballNumber = Integer.parseInt(agentNameSplit2[0])/4;

        agentNameTextView.setText("Training of " + agentNameSplit1[0]);

        List<String> dataList = new ArrayList<>();
        List<String> dataNameList = new ArrayList<>();
        File dataDir = new File(getApplication().getFilesDir().getAbsolutePath() + File.separator + "data");
        String[] dataFiles = dataDir.list();
        for(int i = 0; i<dataFiles.length;i++) {
            String[] tempSplit1 = dataFiles[i].split("\\.");
            String[] tempSplit2 = tempSplit1[0].split("_");//name_balls_players.ds

            if(tempSplit2[1].equals(Integer.toString(ballNumber))) {
                dataList.add(tempSplit1[0]);
                dataNameList.add(tempSplit2[0] + " (" + tempSplit2[1] + ";" + tempSplit2[2] + ")");
            }
        }

        ArrayAdapter<String> dataAdapter =
                new ArrayAdapter<>(
                        getApplication(), // Die aktuelle Umgebung (diese Activity)
                        android.R.layout.simple_list_item_multiple_choice,// , // ID der XML-Layout Datei
                        dataNameList); // Beispieldaten in einer ArrayList

        trainingDataListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        trainingDataListView.setAdapter(dataAdapter);

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<DataSet> checkedDataSetList = new ArrayList<>();
                SparseBooleanArray checkedItemPositions = trainingDataListView.getCheckedItemPositions();
                for(int i=0;i<dataList.size();i++) {
                    if(checkedItemPositions.get(i)){
                        checkedDataSetList.add(globals.getAI().loadData(dataList.get(i)));
                        System.out.println("dataset " + i + " size " + checkedDataSetList.get(i).numExamples());
                    }
                }

                DataSet combinedDataSet = DataSet.merge(checkedDataSetList);
                System.out.println("combined dataset size " + combinedDataSet.numExamples());



                if(resumeCheckBox.isChecked()) {
                    globals.getAI().loadModel(agentName);
                } else {
                    globals.getAI().buildModel(agentName);
                }

                globals.getAI().train(combinedDataSet,10000,true);
                //model.save();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globals.getAI().loadModel(agentName);
                globals.getGameVariables().model = globals.getAI().model;
                //model.test(trainingSet.dataSet);

                globals.getGameVariables().myPlayerNumber = 0;
                globals.getSettingsVariables().gameMode = "testing";
                globals.getSettingsVariables().playerNames.add("test1");
                globals.getSettingsVariables().playerNames.add("test2");

                globals.getGameVariables().numberOfBalls = ballNumber;
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
        Globals globals = (Globals) getApplicationContext();

        try{
            globals.getAI().trainingTask.cancel(true);
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
