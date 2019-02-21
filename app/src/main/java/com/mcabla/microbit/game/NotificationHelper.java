package com.mcabla.microbit.game;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;

import androidx.core.content.ContextCompat;
import  com.mcabla.microbit.game.ui.MainActivity;

/**
 * Created by Casper Haems on 29/09/2017.
 * Copyright (c) 2017 Windsurfing Lochristi-Beervelde. All rights reserved.
 */
@TargetApi(Build.VERSION_CODES.O)
public class NotificationHelper extends ContextWrapper {
    private NotificationManager notifManager;
    public static final String CHANNEL_ONE_ID = "com.mcabla.microbit.game.NOTIFCHANNELONE";
    public static final String CHANNEL_ONE_NAME = "Meldingen";


//Create your notification channels//

    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel (CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, notifManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights (true);
            notificationChannel.setLightColor (Color.BLUE);
            notificationChannel.setShowBadge (true);
            notificationChannel.setLockscreenVisibility (Notification.VISIBILITY_PUBLIC);
            getManager ().createNotificationChannel (notificationChannel);
        }
    }

//Create the notification thatâ€™ll be posted to Channel One//

    public Notification.Builder getNotification1(String title, String body, PendingIntent pendingIntent) {

        int color = ContextCompat.getColor (this,R.color.colorAccent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder (getApplicationContext (), CHANNEL_ONE_ID)
                    .setContentTitle (title)
                    .setContentText (body)
                    .setSmallIcon (R.drawable.ic_bluetooth_connected_black_24dp)
                    .setAutoCancel (true)
                    .setColor (color)
                    .setContentIntent (pendingIntent);

        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //noinspection deprecation
            return new Notification.Builder (getApplicationContext ())
                    .setContentTitle (title)
                    .setContentText (body)
                    .setSmallIcon (R.drawable.ic_bluetooth_connected_black_24dp)
                    .setAutoCancel (true)
                    .setColor (color)
                    .setContentIntent (pendingIntent);
        }
        //noinspection deprecation
        return new Notification.Builder (getApplicationContext ())
                .setContentTitle (title)
                .setContentText (body)
                .setSmallIcon (R.drawable.ic_bluetooth_connected_black_24dp)
                .setAutoCancel (true)
                .setContentIntent (pendingIntent);

    }

    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

//Send your notifications to the NotificationManager system service//

    private NotificationManager getManager() {
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notifManager;
    }
}