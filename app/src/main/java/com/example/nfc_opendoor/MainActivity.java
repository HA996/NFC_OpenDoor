package com.example.nfc_opendoor;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static com.example.nfc_opendoor.CheckPhoneInfo.CheckPhone;
import static com.example.nfc_opendoor.DBhelper.ConnectDB;
import static com.example.nfc_opendoor.Door.ActiveDoor;
import static com.example.nfc_opendoor.Door.ActiveRDoor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminInfo;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.content.Intent;

import static com.example.nfc_opendoor.Button.*;
import static com.example.nfc_opendoor.DoorLog.*;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
public class MainActivity extends AppCompatActivity{
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    public static TextView mTB;
    public static Button mButtonDoor1;
    public static Button mButtonDoor2;
    public static Button mButtonDoor3;
    public static Button mButtonDoor4;
    public static Button mButtonExit;
    public static TextView mID;
    public static TextView mName;
    public static TextView mDVname;
    private IntentFilter[] mlntentFilters;
    private String[][] mNFCTechLists;
    private static final String TAG = "BCardReader";
    public static ClientChannel channel;
    private ClientSession session = null;
    static Handler mhandler = new Handler();

    Thread gpio = new Thread(() -> {
        try {

            SshClient client = SshClient.setUpDefaultClient();
            client.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
            client.start();
            session = client.connect("bme", "123.25.20.209", 2222).verify(10000).getSession();
            session.addPasswordIdentity("123456");
            session.auth().verify(50000);
            System.out.println("Connection establihed");

            // Create a channel to communicate
            channel = session.createChannel(Channel.CHANNEL_SHELL);
            System.out.println("Starting shell");

            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOut(responseStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    String deviceName = Build.ID;

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, mlntentFilters, mNFCTechLists);
        }
    }

    @Override
    protected void onPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTB = findViewById(R.id.tvTB);
        mID = findViewById(R.id.tvID);
        mDVname = findViewById(R.id.tvDVname);
        mName = findViewById(R.id.tvName);
        mButtonDoor1 = findViewById(R.id.btnDoor1);
        mButtonDoor2 = findViewById(R.id.btnDoor2);
        mButtonDoor3 = findViewById(R.id.btnDoor3);
        mButtonDoor4 = findViewById(R.id.btnDoor4);
        mButtonExit = findViewById(R.id.btnExit);

        String key = "user.home";
        Context Syscontext;
        Syscontext = getApplicationContext();
        String val = Syscontext.getApplicationInfo().dataDir;
        System.setProperty(key, val);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        ConnectDB();

        gpio.start();

        mDVname.setText(deviceName);

        CheckPhone();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!nfcAdapter.isEnabled()) {
            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setTitle("Connection Error");
            ad.setMessage("Please turn on NFC in Settings.");
            ad.setPositiveButton("OK", (dialog, whichButton) -> startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS)));
            ad.create();
            ad.show();
        }

        Intent targetIntent = new Intent(this, MainActivity.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pendingIntent = PendingIntent.getActivity(this, 0, targetIntent, FLAG_MUTABLE);

        IntentFilter filter_1 = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter filter_2 = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter filter_3 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        try{
            filter_1.addDataType("*/*");
            filter_2.addDataType("*/*");
            filter_3.addDataType("*/*");
        }
        catch(Exception e)
        {
            Log.e("TagDispatch", e.toString());
        }

        mlntentFilters = new IntentFilter [] {filter_1, filter_2, filter_3};

        mNFCTechLists = new String[][] { new String[]{ NfcA.class.getName()},
                new String[]{NfcB.class.getName()}};

        mButtonDoor1.setOnClickListener(v -> {
            Door.commandD = "raspi-gpio set 4 dl\n";
            ActiveDoor();
            mTB.setText(R.string.ActiveDoor);
            if (mTB.getText() == getString(R.string.ActiveDoor)) {
                Door.commandD = "raspi-gpio set 4 dh\n";
                mhandler.removeCallbacks(Door::ActiveDoor);
                mhandler.postDelayed(Door::ActiveDoor, 300);
                WriteLog("Mở Cổng");
            }
            mButtonDoor1.setBackgroundColor(Color.RED);
            mButtonDoor1.setEnabled(false);
            mhandler.removeCallbacks(com.example.nfc_opendoor.Button::SetButton1);
            mhandler.postDelayed(com.example.nfc_opendoor.Button::SetButton1, 5000);
        });

        mButtonDoor2.setOnClickListener(v -> {
            Door.commandD = "raspi-gpio set 4 dl\n";
            ActiveDoor();
            mTB.setText(R.string.ActiveDoor2);
            if (mTB.getText() == getString(R.string.ActiveDoor2)) {
                Door.commandD = "raspi-gpio set 4 dh\n";
                mhandler.removeCallbacks(Door::ActiveDoor);
                mhandler.postDelayed(Door::ActiveDoor, 300);
                WriteLog("Đóng Cổng");
            }
            mButtonDoor2.setBackgroundColor(Color.RED);
            mButtonDoor2.setEnabled(false);
            mhandler.removeCallbacks(com.example.nfc_opendoor.Button::SetButton2);
            mhandler.postDelayed(com.example.nfc_opendoor.Button::SetButton2, 5000);
        });

        mButtonDoor3.setOnClickListener(v -> {
            Door.commandD = "raspi-gpio set 15 dh\n";
            ActiveRDoor();
            mTB.setText(R.string.ActiveRDoor1);
            if (mTB.getText() == getString(R.string.ActiveRDoor1)) {
                Door.commandD = "raspi-gpio set 15 dl\n";
                mhandler.removeCallbacks(Door::ActiveRDoor);
                mhandler.postDelayed(Door::ActiveRDoor, 500);
                WriteLog("Mở cửa quấn");
            }
            mButtonDoor3.setBackgroundColor(Color.RED);
            mButtonDoor3.setEnabled(false);
            mhandler.removeCallbacks(com.example.nfc_opendoor.Button::SetButton3);
            mhandler.postDelayed(com.example.nfc_opendoor.Button::SetButton3, 5000);
        });

        mButtonDoor4.setOnClickListener(v -> {
            Door.commandD = "raspi-gpio set 14 dl\n";
            ActiveDoor();
            mTB.setText(R.string.ActiveRDoor1);
            if (mTB.getText() == getString(R.string.ActiveRDoor1)) {
                Door.commandD = "raspi-gpio set 14 dh\n";
                mhandler.removeCallbacks(Door::ActiveRDoor);
                mhandler.postDelayed(Door::ActiveRDoor, 1000);
                WriteLog("Đóng cửa quấn");
            }
            mButtonDoor4.setBackgroundColor(Color.RED);
            mButtonDoor4.setEnabled(false);
            mhandler.removeCallbacks(com.example.nfc_opendoor.Button::SetButton4);
            mhandler.postDelayed(com.example.nfc_opendoor.Button::SetButton4, 5000);
        });

        mButtonExit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }

    @Override
    public void onNewIntent(Intent passedIntent) {
        super.onNewIntent(passedIntent);
        Card.ReadCard(passedIntent);
    }
}