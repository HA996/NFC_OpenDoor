package com.example.nfc_opendoor;

import static com.example.nfc_opendoor.DBhelper.connection;
import static com.example.nfc_opendoor.MainActivity.mButtonDoor1;
import static com.example.nfc_opendoor.MainActivity.mButtonDoor2;
import static com.example.nfc_opendoor.MainActivity.mButtonDoor3;
import static com.example.nfc_opendoor.MainActivity.mButtonDoor4;
import static com.example.nfc_opendoor.MainActivity.mDVname;
import static com.example.nfc_opendoor.MainActivity.mName;
import static com.example.nfc_opendoor.MainActivity.mTB;

import android.view.View;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckPhoneInfo {
    public static void CheckPhone(){
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT phoneID FROM PhoneInfo WHERE phoneID='" + mDVname.getText().toString() + "';");
            resultSet.next();
            if(resultSet.getString(1).equals(mDVname.getText().toString())){
                mTB.setText(R.string.Wellcome);
                mButtonDoor1.setVisibility(View.VISIBLE);
                mButtonDoor2.setVisibility(View.VISIBLE);
                mButtonDoor3.setVisibility(View.VISIBLE);
                mButtonDoor4.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
