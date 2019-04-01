package com.demgames.polypong;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

import com.demgames.miscclasses.Agent;
import com.demgames.miscclasses.CommunicationClass;
import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.ServerListener;

public class Globals extends Application implements IGlobals{

    private static final String TAG = "Globals";


    private ClientListener clientListener;
    private ServerListener serverListener;

    List<InetAddress> hostsList;

    private CommunicationClass comm = new CommunicationClass();
    private Agent agent;

    private GDXGameLauncher gdxGameLauncher; // This is the main android activity

    public void setGameLauncher(GDXGameLauncher gdxGameLauncher_){
        this.gdxGameLauncher = gdxGameLauncher_;
    }



    @Override
    public synchronized CommunicationClass getComm() {return (this.comm);}
    @Override
    public synchronized Agent getAgent() {return (this.agent);}

    public void setupAgent(Context context) {
        this.agent = new Agent(context.getFilesDir());
    }

    public void showAlertDialog(final AlertDialogCallback callback){

        gdxGameLauncher.runOnUiThread(new Runnable(){

            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(gdxGameLauncher);
                builder.setTitle("Test");
                builder.setMessage("Testing");
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        callback.positiveButtonPressed();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        callback.negativeButtonPressed();

                    }

                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

}

