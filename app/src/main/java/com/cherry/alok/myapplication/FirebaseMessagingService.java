package com.cherry.alok.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by alok on 26/7/16.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
         showNotification(remoteMessage.getData());
    }
//remoteMessage.getData().get("title") , remoteMessage.getData().get("message")
    private void showNotification(Map<String,String> mapofdata)
    {
        Intent i = new Intent(this,OrderActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String title = mapofdata.get("title");
        String message = mapofdata.get("message");

        PendingIntent pendingIntent =   PendingIntent.getActivity(this, 0 , i , PendingIntent.FLAG_UPDATE_CURRENT);
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setAutoCancel(true)
                .setContentTitle(title).setContentText(message)
                .setSmallIcon(R.drawable.carediticon)
        .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0 , builder.build());
    }
}
