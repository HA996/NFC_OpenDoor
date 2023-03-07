package com.example.nfc_opendoor;

import static com.example.nfc_opendoor.MainActivity.*;

import android.graphics.Color;

import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class Button {
    public static void SetButton1(){
        mButtonDoor1.setBackgroundColor(Color.GRAY);
        mButtonDoor1.setEnabled(true);
    }

    public static void SetButton2(){
        mButtonDoor2.setBackgroundColor(Color.GRAY);
        mButtonDoor2.setEnabled(true);
    }

    public static void SetButton3(){
        mButtonDoor3.setBackgroundColor(Color.GRAY);
        mButtonDoor3.setEnabled(true);
    }

    public static void SetButton4(){
        mButtonDoor4.setBackgroundColor(Color.GRAY);
        mButtonDoor4.setEnabled(true);
    }
}
