package com.is1423.socialmedia.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.is1423.socialmedia.MessageActivity;
import com.is1423.socialmedia.common.Constant;

import java.util.Objects;

public class FirebaseService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed: " + task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        updateToken(token);
                    }
                });

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.COMMON_KEY.SHARED_PREFERENCES_SP_USER_NAME, MODE_PRIVATE);
        String savedCurrentUser = sharedPreferences.getString(Constant.COMMON_KEY.SHARED_PREFERENCES_CURRENT_USERID_KEY, "None");

        String sent = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.SENT);
        String user = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.USER);
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (Objects.nonNull(fUser) && sent.equals(fUser.getUid())) {
            if (!savedCurrentUser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendNotification(remoteMessage);
                } else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.USER);
        String icon = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.ICON);
        String title = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.TITLE);
        String body = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.BODY);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.COMMON_KEY.PARTNER_UID_KEY, user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i > 0) {
            j = i;
        }
        notificationManager.notify(j, builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.USER);
        String icon = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.ICON);
        String title = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.TITLE);
        String body = remoteMessage.getData().get(Constant.REMOTE_MESSAGE.BODY);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.COMMON_KEY.PARTNER_UID_KEY, user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification1 = new Notification(this);
        android.app.Notification.Builder builder = notification1.getNotification(title, body, pendingIntent, defaultSoundUri, icon);

        int j = 0;
        if (i > 0) {
            j = i;
        }
        notification1.getManager().notify(j, builder.build());
    }

    private void updateToken(String s) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.TABLE.TOKEN);
        Token token = new Token(s);
        ref.child(user.getUid()).setValue(token);
    }


}
