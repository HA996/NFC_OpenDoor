package com.example.nfc_opendoor;

import static com.example.nfc_opendoor.MainActivity.mTB;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBhelper {
//    private static final String ip = "123.25.20.209";
//    private static final String port = "1533";
    private static final String ip = "123.25.20.209";
    private static final String port = "1533";
    private static final String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static final String database = "QLGL";
    private static final String username = "adminAPP";
    private static final String password = "admin@123Abc";
    private static final String url = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databaseName=" + database + ";";
    public static Connection connection = null;

    public static void ConnectDB(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(Classes);
            connection = DriverManager.getConnection(url, username, password);
            mTB.setText(R.string.Wellcome);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            mTB.setText(R.string.ConnectError);
        }
    }
}
