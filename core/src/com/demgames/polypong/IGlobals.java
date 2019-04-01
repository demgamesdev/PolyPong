package com.demgames.polypong;

import com.demgames.miscclasses.Agent;
import com.demgames.miscclasses.CommunicationClass;


public interface IGlobals {
    String TAG = "IGlobals";


    CommunicationClass getComm();
    Agent getAgent();

    void showAlertDialog(AlertDialogCallback callback);
}
