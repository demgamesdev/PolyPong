package com.demgames.polypong.network.sendclasses;

import android.content.Context;
import android.provider.Settings;

import java.sql.Connection;

import processing.core.PVector;

public class SendSettings {
    //public com.esotericsoftware.kryonet.Connection[] connectionList;
    public PVector[] ballsPositions;
    public PVector[] ballsVelocities;
    public float[] ballsSizes;
    public int gameMode;
    public boolean gravityState;
    public boolean attractionState;
}
