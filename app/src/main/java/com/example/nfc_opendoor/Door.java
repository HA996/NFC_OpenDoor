package com.example.nfc_opendoor;

import static com.example.nfc_opendoor.MainActivity.channel;

import org.apache.sshd.client.channel.ClientChannelEvent;

import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class Door {
    public static String commandD;
    public static void ActiveDoor() {
        try {
            // Open channel
            channel.open().verify(500, TimeUnit.MILLISECONDS);
            try (OutputStream pipedIn = channel.getInvertedIn()) {
                pipedIn.write(commandD.getBytes());
                pipedIn.flush();
            }

            // Close channel
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                    TimeUnit.SECONDS.toSeconds(1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void ActiveRDoor() {
        try {
            // Open channel
            channel.open().verify(10, TimeUnit.SECONDS);
            try (OutputStream pipedIn = channel.getInvertedIn()) {
                pipedIn.write(commandD.getBytes());
                pipedIn.flush();
            }

            // Close channel
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                    TimeUnit.SECONDS.toMillis(5));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
