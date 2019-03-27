package com.demgames.polypong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demgames.miscclasses.GameObjectClasses;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ManageActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        List<String> agentsList = new ArrayList<>();
        List<String> agentsNameList = new ArrayList<>();
        List<String> dataList = new ArrayList<>();
        List<String> dataNameList = new ArrayList<>();
        ArrayAdapter<String> agentsAdapter;
        ArrayAdapter<String> dataAdapter;

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {

            }
        }

        void updateAgentListView() {
            agentsList.clear();
            agentsNameList.clear();
            File agentsDir = new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + "agents");
            String[] agentFiles = agentsDir.list();
            for(int i = 0; i<agentFiles.length;i++) {
                String[] tempSplit1 = agentFiles[i].split("\\.");
                agentsList.add(tempSplit1[0]);
                String[] tempSplit2 = tempSplit1[0].split("_");

                agentsNameList.add(tempSplit2[0]+" (" + tempSplit2[3]+")");
            }
            agentsAdapter.notifyDataSetChanged();
        }

        void updateDataListView() {
            dataList.clear();
            dataNameList.clear();
            File dataDir = new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + "data");
            String[] dataFiles = dataDir.list();
            for(int i = 0; i<dataFiles.length;i++) {
                String[] tempSplit1 = dataFiles[i].split("\\.");
                dataList.add(tempSplit1[0]);
                String[] tempSplit2 = tempSplit1[0].split("_");//name_balls_players.ds

                dataNameList.add(tempSplit2[0]+" (" + tempSplit2[1]+";" + tempSplit2[2]+")");
            }
            dataAdapter.notifyDataSetChanged();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            final Globals globals = (Globals) getActivity().getApplicationContext();


            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_manage_agents, container, false);
                    ListView agentsListView = (ListView) rootView.findViewById(R.id.agentsListView);

                    agentsAdapter =
                            new ArrayAdapter<>(
                                    getActivity(), // Die aktuelle Umgebung (diese Activity)
                                    R.layout.item_textview, // ID der XML-Layout Datei
                                    R.id.listViewtextView, // ID des TextViews
                                    agentsNameList); // Beispieldaten in einer ArrayList


                    agentsListView.setAdapter(agentsAdapter);

                    updateAgentListView();

                    agentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            Intent intent = new Intent(getActivity(),TrainingActivity.class);
                            intent.putExtra("agentname",agentsList.get(i));
                            //based on item add info to intent
                            startActivity(intent);
                        }
                    });

                    FloatingActionButton agentsFAB = (FloatingActionButton) rootView.findViewById(R.id.agentsFAB);
                    agentsFAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Log.d("ManageActivity","agentsFAB pressed");
                            AlertDialog.Builder makeDialog = new AlertDialog.Builder(getActivity());
                            View mView = getLayoutInflater().inflate(R.layout.dialog_create_agent,null);
                            EditText agentNameEditText = (EditText) mView.findViewById(R.id.agentNameEditText);
                            TextView ballsTextView = (TextView) mView.findViewById(R.id.ballsTextView);
                            SeekBar ballsSeekBar = (SeekBar) mView.findViewById(R.id.ballsSeekBar);
                            TextView layersTextView = (TextView) mView.findViewById(R.id.layersTextView);
                            SeekBar layersSeekBar = (SeekBar) mView.findViewById(R.id.layersSeekBar);
                            TextView unitsTextView = (TextView) mView.findViewById(R.id.unitsTextView);
                            SeekBar unitsSeekBar = (SeekBar) mView.findViewById(R.id.unitsSeekBar);

                            ballsTextView.setText("Balls setReceived to "+ ballsSeekBar.getProgress());
                            layersTextView.setText("Layers setReceived to "+ layersSeekBar.getProgress());
                            unitsTextView.setText("Units of first layer setReceived to "+ unitsSeekBar.getProgress());

                            ballsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    ballsTextView.setText("Balls setReceived to " + progress);
                                }

                                public void onStartTrackingTouch(SeekBar seekBar) {
                                    // TODO Auto-generated method stub
                                }

                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });
                            layersSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    layersTextView.setText("Layers setReceived to " + progress);
                                }

                                public void onStartTrackingTouch(SeekBar seekBar) {
                                    // TODO Auto-generated method stub
                                }

                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });
                            unitsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    unitsTextView.setText("Units of first layer setReceived to "+ progress);
                                }

                                public void onStartTrackingTouch(SeekBar seekBar) {
                                    // TODO Auto-generated method stub
                                }

                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });



                            makeDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(!agentNameEditText.getText().toString().isEmpty()) {

                                        int numberOfPlayers = 2;
                                        int numberOfBalls = ballsSeekBar.getProgress();
                                        int[] n_units = new int[layersSeekBar.getProgress()+2];
                                        n_units[0] = 4; //inputs dimension
                                        n_units[1] = unitsSeekBar.getProgress();
                                        n_units[n_units.length-1] = 2;
                                        String tempUnits = Integer.toString(n_units[0]) +"-" + Integer.toString(n_units[1]);
                                        for(int l=2;l<n_units.length-1;l++) {
                                            n_units[l] = 32; //hidden units
                                            tempUnits+="-" + Integer.toString(n_units[l]);
                                        }
                                        tempUnits+="-" + Integer.toString(n_units[n_units.length-1]);

                                        String agentName = agentNameEditText.getText().toString()+ "_"+ Integer.toString(numberOfPlayers) + "_" + Integer.toString(numberOfBalls) + "_" + tempUnits;

                                        globals.getNeuralNetwork().buildModel(agentName);

                                        Intent startTraining = new Intent(getActivity(),TrainingActivity.class);
                                        startTraining.putExtra("agentname",agentName);
                                        startTraining.putExtra("myplayername",getActivity().getIntent().getStringExtra("myplayernumber"));
                                        //based on item add info to intent
                                        startActivity(startTraining);
                                        updateAgentListView();
                                    }
                                }
                            });

                            makeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    updateAgentListView();
                                }
                            });

                            makeDialog.setView(mView);
                            AlertDialog ad = makeDialog.create();
                            ad.show();

                        }
                    });

                    break;


                case 2:
                    rootView = inflater.inflate(R.layout.fragment_manage_data, container, false);

                    ListView dataListView = (ListView) rootView.findViewById(R.id.dataListView);

                    dataAdapter =
                            new ArrayAdapter<>(
                                    getActivity(), // Die aktuelle Umgebung (diese Activity)
                                    R.layout.item_textview, // ID der XML-Layout Datei
                                    R.id.listViewtextView, // ID des TextViews
                                    dataNameList); // Beispieldaten in einer ArrayList


                    dataListView.setAdapter(dataAdapter);
                    updateDataListView();

                    FloatingActionButton dataFab = (FloatingActionButton) rootView.findViewById(R.id.dataFAB);
                    dataFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d("ManageActivity","agentsFAB pressed");
                            AlertDialog.Builder makeDialog = new AlertDialog.Builder(getActivity());
                            View mView = getLayoutInflater().inflate(R.layout.dialog_create_data,null);
                            EditText dataNameEditText = (EditText) mView.findViewById(R.id.dataNameEditText);
                            TextView ballsTextView = (TextView) mView.findViewById(R.id.ballsTextView);
                            SeekBar ballsSeekBar = (SeekBar) mView.findViewById(R.id.ballsSeekBar);

                            ballsTextView.setText("Balls setReceived to "+ ballsSeekBar.getProgress());

                            ballsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    ballsTextView.setText("Balls setReceived to " + progress);
                                }

                                public void onStartTrackingTouch(SeekBar seekBar) {
                                    // TODO Auto-generated method stub
                                }

                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });


                            makeDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(!dataNameEditText.getText().toString().isEmpty()) {

                                        int numberOfPlayers = 2;
                                        String dataName = dataNameEditText.getText().toString()+ "_" + Integer.toString(numberOfPlayers) + "_" + ballsSeekBar.getProgress() ;





                                        globals.getAgent().inputs.clear();
                                        globals.getAgent().outputs.clear();
                                        globals.setupNeuralNetwork(getContext());
                                        globals.getNeuralNetwork().initDataSet(dataName);
                                        globals.getNeuralNetwork().saveData();

                                        globals.getComm().initGame(0,ballsSeekBar.getProgress(),2,"normal",true,false,true);
                                        globals.getComm().resetPlayerMap();
                                        globals.getComm().playerMap.put(0,new GameObjectClasses.Player("Dummy","0.0.0.0"));
                                        globals.getComm().playerMap.put(1,new GameObjectClasses.Player("Dummy","0.0.0.0"));

                                        Intent startGDXGameLauncher = new Intent(getActivity(), GDXGameLauncher.class);

                                        startGDXGameLauncher.putExtra("dataname",dataName);
                                        startGDXGameLauncher.putExtra("myplayername",getActivity().getIntent().getStringExtra("myplayername"));
                                        startGDXGameLauncher.putExtra("myplayernumber",0);
                                        startGDXGameLauncher.putExtra("numberofplayers",globals.getComm().playerMap.size());
                                        startGDXGameLauncher.putExtra("numberofballs",ballsSeekBar.getProgress());
                                        startGDXGameLauncher.putExtra("gravitystate",true);
                                        startGDXGameLauncher.putExtra("attractionstate",false);
                                        startGDXGameLauncher.putExtra("gamemode","training");
                                        startGDXGameLauncher.putExtra("mode","normal");
                                        startGDXGameLauncher.putExtra("agentmode",false);
                                        startActivity(startGDXGameLauncher);
                                        updateDataListView();
                                    }
                                }
                            });

                            makeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            makeDialog.setView(mView);
                            AlertDialog ad = makeDialog.create();
                            ad.show();

                        }
                    });

                    break;
            }


            return rootView;

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return ("Agents");
                case 1:
                    return ("Data");
            }
            return null;
        }
    }
}
