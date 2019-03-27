package com.demgames.polypong;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.demgames.miscclasses.GameObjectClasses.*;

import java.util.List;

public class CustomAdapters {

    static class ClientPlayerArrayAdapter extends ArrayAdapter<Player> {
        public ClientPlayerArrayAdapter(Context context, int resource, List<Player> players) {
            super(context, resource, players);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Player player = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_choice_multiple, parent, false);
            }


            // Lookup view for data population
            TextView connectionTextView = (TextView) convertView.findViewById(R.id.choiceMultipleTextView);
            // Populate the data into the template view using the data object
            connectionTextView.setText(player.name + " ("+player.ipAdress+")");
            // Return the completed view to render on screen
            return convertView;
        }
    }

    static class ServerArrayAdapter extends ArrayAdapter<Player> {
        public ServerArrayAdapter(Context context, List<Player> players) {
            super(context, 0, players);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Player player = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_choice_multiple, parent, false);
            }


            // Lookup view for data population
            TextView connectionTextView = (TextView) convertView.findViewById(R.id.choiceMultipleTextView);
            // Populate the data into the template view using the data object
            connectionTextView.setText(player.name + " ("+player.ipAdress+")");
            // Return the completed view to render on screen
            return convertView;
        }
    }

    static class PlayerArrayAdapter extends ArrayAdapter<Player> {
        private int layoutRes;
        private int idRes;
        public PlayerArrayAdapter(Context context, int layoutRes_, int idRes_,  List<Player> players) {
            super(context, 0, players);
            this.layoutRes = layoutRes_;
            this.idRes = idRes_;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Player player = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(this.layoutRes, parent, false);
            }


            // Lookup view for data population
            TextView connectionTextView = (TextView) convertView.findViewById(this.idRes);
            // Populate the data into the template view using the data object
            connectionTextView.setText(player.name + " ("+player.ipAdress+")");
            // Return the completed view to render on screen
            return convertView;
        }
    }

}
