package com.example.nfc_opendoor;

import static android.content.ContentValues.TAG;

import static com.example.nfc_opendoor.DBhelper.connection;
import static com.example.nfc_opendoor.MainActivity.*;


import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class Card {
    private static final int fieldLength = 0xFF; // 128 bytes
    static byte[] _CL_SelApp2 = new byte[]{0x00, (byte) 0xA4, 0x04, 0x00, 0x0A, 0x66, 0x69, 0x6C, 0x65, 0x50, 0x6B, 0x67, 0x41, 0x70, 0x70};
    static byte[] _CL_SelEF2 = new byte[]{0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x00, 0x02};
    static byte[] _CL_ReadBinary = new byte[]{0x00, (byte) 0xB0, 0x01, (byte) 0xFF, (byte) (fieldLength - 5)};
    private static int count = 0;
    static byte[] tagid;
    public static final String CHARS = "0123456789ABCDEF";
    static Handler mhandler = new Handler();


    public static String toHexString(byte[] data){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < data.length; i++){
            sb.append(CHARS.charAt((data[i] >> 4) & 0x0f)).append(CHARS.charAt(data[i] & 0x0f));
        }

        return sb.toString();
    }

    public static void ReadCard(Intent passedIntent){
        Tag tag = passedIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] tmpTextField;
        if (tag != null) {
            count++;
            if (count == 999) {
                count = 1;
            }
            IsoDep nfc = IsoDep.get(tag);
            try {
                nfc.connect();
                try {
                    tmpTextField = readFromCard((byte) 0x00, (byte) 0x00, nfc);
                    if (tmpTextField == null) {
                        mTB.append("Card is not Valid" + "\n");
                        return;
                    }
                    String txtFullHexString = ToHexString(tmpTextField);
                    String txtFullString = revertToString(txtFullHexString);
                    String[] txtTextFields = txtFullString.split("[;]");
                    if (isValidIndex(txtTextFields, 1) || isValidIndex(txtTextFields, 2)) {
                        return;
                    }
                    mName.setText(txtTextFields[0]);
                    nfc.close();
                    Statement statement = null;
                    tagid = tag.getId();
//                    String taghexstring = ToHexString(tagid);
//                    String tagstring = revertToString(taghexstring);
                    String tagstring = Card.toHexString(tagid);
                    mID.setText(tagstring);
//                    mDVname.setText(tagstring);
                    try {
                        statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT tag FROM CardInfo WHERE tag='" + tagstring + "';");
                        resultSet.next();
                        if(resultSet.getString(1).equals(tagstring)){
                            ResultSet rs = statement.executeQuery("SELECT status FROM CardInfo WHERE tag='" + tagstring + "';");
                            rs.next();
                            if(rs.getString(1).equals("1")){
                                mTB.setText(R.string.Wellcome);
                                mButtonDoor1.setVisibility(View.VISIBLE);
                                mButtonDoor2.setVisibility(View.VISIBLE);
                                mButtonDoor3.setVisibility(View.VISIBLE);
                                mButtonDoor4.setVisibility(View.VISIBLE);
                                mhandler.removeCallbacks(Card::TimeOut);
                                mhandler.postDelayed(Card::TimeOut, 40000);
                            }else if(rs.getString(1).equals("2")){
                                mTB.setText(R.string.NotActiveCard);
                                mButtonDoor1.setVisibility(View.GONE);
                                mButtonDoor2.setVisibility(View.GONE);
                                mButtonDoor3.setVisibility(View.GONE);
                                mButtonDoor4.setVisibility(View.GONE);
                            }else if(rs.getString(1).equals("3")){
                                mTB.setText(R.string.LockedCard);
                                mButtonDoor1.setVisibility(View.GONE);
                                mButtonDoor2.setVisibility(View.GONE);
                                mButtonDoor3.setVisibility(View.GONE);
                                mButtonDoor4.setVisibility(View.GONE);
                            }else if(rs.getString(1).equals("")){
                                mTB.setText(R.string.CardDelete);
                                mButtonDoor1.setVisibility(View.GONE);
                                mButtonDoor2.setVisibility(View.GONE);
                                mButtonDoor3.setVisibility(View.GONE);
                                mButtonDoor4.setVisibility(View.GONE);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            tagid = tag.getId();
        }
    }

    private static byte[] readFromCard(byte offset1, byte offset2, IsoDep nfc) {
        byte[] resp;
        byte[] tempStr = new byte[fieldLength - 5];


        try {
            resp = nfc.transceive(_CL_SelApp2);
            //mMessage.setText("Response: " + gen.ToHexString(resp));
            String mRespData = ToHexString(resp);
            Log.v(TAG, "CL_SelApp2 Resp: "+ mRespData);
            int SW1_90 = 0x90;
            int SW2_00 = 0x00;
            if ((resp[2] == (byte) SW1_90) && (resp[3] == SW2_00)) {
                resp = nfc.transceive(_CL_SelEF2);
                //mMessage.setText("Response: " + gen.ToHexString(resp));
                mRespData = ToHexString(resp);
                //Log.v(TAG, "CL_SelEF2 Resp: "+ mRespData);
                if ((resp[0] == (byte) SW1_90) && (resp[1] == SW2_00)) {
                    //_CL_ReadBinary[2] = offset1;
                    //_CL_ReadBinary[3] = offset2;
                    Log.v(TAG, "CL_ReadBinary CMD: "+ ToHexString(_CL_ReadBinary));
                    resp = nfc.transceive(_CL_ReadBinary);
                    mRespData = ToHexString(resp);
                    Log.v(TAG, "CL_ReadBinary: "+ mRespData);
                    if ((resp[fieldLength - 5] == (byte) SW1_90) && (resp[fieldLength - 4] == SW2_00)) {
                        System.arraycopy(resp, 0, tempStr, 0, fieldLength - 5);
                        return tempStr;
                    } else {
                        Log.v(TAG, "CL_ReadBinary Fails ");
                    }
                } else {
                    Log.v(TAG, "CL_SelEF2 Fails ");
                }
            } else {
                Log.v(TAG, "CL_SelApp2 Fails ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String ToHexString(byte[] data) {
        String var2 = "";

        for(int var3 = 0; var3 < data.length; ++var3) {
            var2 = var2 + "" + String.format("%02X", data[var3]);
        }

        return var2;
    }
    private static String revertToString(String hex){
        if(hex.length()%2!=0){
            System.err.println("Invlid hex string.");
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hex.length(); i = i + 2) {
            // Step-1 Split the hex string into two character group
            String s = hex.substring(i, i + 2);
            // Step-2 Convert the each character group into integer using valueOf method
            int n = Integer.valueOf(s, 16);
            // Step-3 Cast the integer value to char
            builder.append((char)n);
        }
        return builder.toString();
    }

    public static boolean isValidIndex(String[] arr, int index) {
        try {
            String i = arr[index];
        } catch (IndexOutOfBoundsException e) {
            return true;
        }
        return false;
    }

    public static void TimeOut() {
        mTB.setText(R.string.Wellcome);
        mButtonDoor1.setVisibility(View.GONE);
        mButtonDoor2.setVisibility(View.GONE);
        mButtonDoor3.setVisibility(View.GONE);
        mButtonDoor4.setVisibility(View.GONE);
        mName.setText("");
    }
}
