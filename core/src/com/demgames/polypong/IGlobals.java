package com.demgames.polypong;

import com.demgames.miscclasses.Agent;
import com.demgames.miscclasses.CommunicationClass;


public interface IGlobals {
    String TAG = "IGlobals";


    CommunicationClass getComm();
    Agent getAgent();

    void finishGDXGameLauncher();
    void showStartDialog(AlertDialogCallback callback);
    void showPauseDialog(AlertDialogCallback callback);
    void showEndDialog(AlertDialogCallback callback);
}
