package com.is1423.socialmedia.notifications;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import java.util.Objects;

public class Notification extends ContextWrapper {
    private static final String ID = "some_id";
    private static final String NAME = "FirebaseAPP";

    private NotificationManager notificationManager;


    public Notification(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createChanel();

        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChanel() {
        NotificationChannel notificationChannel = new NotificationChannel(ID,NAME,NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if (Objects.isNull(notificationManager)){
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getNotification(String title,
                                                            String body,
                                                            PendingIntent pendingIntent,
                                                            Uri soundUri,
                                                            String icon){
        return new android.app.Notification.Builder(getApplicationContext(), ID)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(body)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));
    }
}
