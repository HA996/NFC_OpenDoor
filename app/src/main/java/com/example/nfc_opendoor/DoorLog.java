package com.example.nfc_opendoor;

import static com.example.nfc_opendoor.DBhelper.connection;
import static com.example.nfc_opendoor.MainActivity.mID;

import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DoorLog {
    public static void WriteLog(String Dactive){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String strDate = sdf.format(c.getTime());
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CardLoginfo (tagID, dooractive, dateuse) VALUES(?,?,?)");
            preparedStatement.setString(1, mID.getText().toString());
            preparedStatement.setString(2, Dactive);
            preparedStatement.setString(3, strDate);

            preparedStatement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
