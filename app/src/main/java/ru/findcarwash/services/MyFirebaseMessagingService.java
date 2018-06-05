package ru.findcarwash.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.findcarwash.findcarwash.AppLaunch;
import ru.findcarwash.findcarwash.R;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String event, senderLogin, senderName, receiverLogin, senderId;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
      //  Log.d(MySettings.LOG_TAG, "onMessageReceived");
     //   Log.d(MySettings.LOG_TAG, "myLigon=" + remoteMessage.getData().get("myLogin"));
      //  myLogin = remoteMessage.getData().get("myLogin");
        event = remoteMessage.getData().get("event");
        receiverLogin = remoteMessage.getData().get("receiverLogin");
        senderLogin = remoteMessage.getData().get("senderLogin");
        senderName = remoteMessage.getData().get("senderName");
        senderId = remoteMessage.getData().get("senderId");

        sendNotification();
    }

    private void sendNotification() {
        Intent intent = new Intent(this, AppLaunch.class);

        intent.putExtra(MySettings.NOTIFICATION_EVENT, event);
        intent.putExtra("receiverLogin", receiverLogin);
        intent.putExtra("senderLogin", senderLogin);
        intent.putExtra("senderName", senderName);
        intent.putExtra("senderId", senderId);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, MySettings.NOTIFICATION_CHAT_CHANNEL_NAME)
                        .setSmallIcon(R.drawable.default_wash)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(getResources().getString(R.string.chatNotificationInfoMessage) + " " + senderName)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(MySettings.NOTIFICATION_CHAT_CHANNEL,
                    getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}