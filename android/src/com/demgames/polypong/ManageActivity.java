package com.demgames.polypong;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_manage_agents, container, false);
                    ListView agentsListView = (ListView) rootView.findViewById(R.id.agentsListView);

                    List<String> agentsList = new ArrayList<>();
                    File agentsDir = new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + "agents");
                    String[] agentFiles = agentsDir.list();
                    for(int i = 0; i<agentFiles.length;i++) {
                        agentsList.add(agentFiles[i].split("\\.")[0]);
                    }

                    ArrayAdapter<String> agentsAdapter =
                            new ArrayAdapter<>(
                                    getActivity(), // Die aktuelle Umgebung (diese Activity)
                                    R.layout.item_agents, // ID der XML-Layout Datei
                                    R.id.agentsTextView, // ID des TextViews
                                    agentsList); // Beispieldaten in einer ArrayList


                    agentsListView.setAdapter(agentsAdapter);

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

                            makeDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(!agentNameEditText.getText().toString().isEmpty()) {
                                        Intent intent = new Intent(getActivity(),TrainingActivity.class);
                                        intent.putExtra("agentname",agentNameEditText.getText().toString());
                                        //based on item add info to intent
                                        startActivity(intent);
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
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_manage_data, container, false);

                    ListView dataListView = (ListView) rootView.findViewById(R.id.dataListView);

                    FloatingActionButton dataFAB = (FloatingActionButton) rootView.findViewById(R.id.dataFAB);
                    dataFAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
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
