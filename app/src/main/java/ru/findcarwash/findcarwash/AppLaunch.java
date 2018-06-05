package ru.findcarwash.findcarwash;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import ru.findcarwash.findcarwash.registerorauth.ClientSignIn;
import ru.findcarwash.findcarwash.registerorauth.WashSingIn;
import ru.findcarwash.findcarwash.workscreens.ChatClient.ClientChat;
import ru.findcarwash.findcarwash.workscreens.ChatWash.WashChat;
import ru.findcarwash.findcarwash.workscreens.ClientWorkScreen.ClientWorkScreen;
import ru.findcarwash.findcarwash.workscreens.WashWorkScreens.WashChatList;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.settings.MySettings;

/**
 * Во всей местах предполагается что
 * receiver это клиент(получатель, локальный клиент или админ)
 * sender это отправитель(удаленный клиент или админ)
 */

public class AppLaunch extends AppCompatActivity {
    SharedPreferences preferences;

    private String event, receiverLogin, senderLogin, senderName, senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Context ctx = getApplicationContext();
        /**
         * Check local key file on shared preferences. If he is and correct to start work screen
         * else to startBranch register or sign in
         */
        setContentView(R.layout.app_launch);
        initializeObject(ctx); // initialize all object and inject to static class

        /**
         * Registration Firebase service and initialize Notification manager
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(MySettings.NOTIFICATION_CHAT_CHANNEL,
                    MySettings.NOTIFICATION_CHAT_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW));
        }

        preferences = DependenciesFactory.getPreferencesGet(); // receive singltone preferences
     //   Log.d(MySettings.LOG_TAG, "KEY=" + preferences.getString(MySettings.CLIENT_KEY, ""));
        if (getIntent().getExtras() != null && getIntent().getStringExtra(MySettings.NOTIFICATION_EVENT) != null) {
            //   Log.d(MySettings.LOG_TAG, "getExtras " + getIntent().getExtras());
            event = getIntent().getStringExtra(MySettings.NOTIFICATION_EVENT);
            receiverLogin = getIntent().getStringExtra("receiverLogin"); // мой логин
            senderLogin = getIntent().getStringExtra("senderLogin"); // логин отправителя
            senderName = getIntent().getStringExtra("senderName"); // имя отправителя
            senderId = getIntent().getStringExtra("senderId"); // ИД отправителя
           // Log.d(MySettings.LOG_TAG, "event=" + event);
            if (event != null && event.equals(MySettings.NOTIFICATION_EVENT_GO_CHAT)) {
                // check admin OR client mode app
                if (preferences.getBoolean(MySettings.IS_WASH, false) == false){
                    finish();
                    goChatAfterReceiveFirebaseNotificationClient(); // go Client chat
                }
                else if (preferences.getBoolean(MySettings.IS_WASH, false) == true){
                    finish();
                    goChatAfterReceiveFirebaseNotificationAdmin(); // go Admin chat
                }
            }
        }
       // Log.d(MySettings.LOG_TAG, "event=" + event);
        else if (preferences.getString(MySettings.CLIENT_KEY, "").length() == MySettings.lengthKey &
                 preferences.getBoolean(MySettings.IS_WASH, false) == false) {
            finish();
            Intent intent = new Intent(AppLaunch.this, ClientWorkScreen.class);
            startActivity(intent); // go to the work activities after check key length
        } else if (preferences.getBoolean(MySettings.IS_WASH, false) == true){
            finish();
            Intent intent = new Intent(AppLaunch.this, WashChatList.class);
            startActivity(intent);
        }
    }

    public void StartScreenClient(View view) {
        Intent intent = new Intent(AppLaunch.this, ClientSignIn.class);
        startActivity(intent);
    }
    public void StartScreenWash(View view) {
        Intent intent = new Intent(AppLaunch.this, WashSingIn.class);
        startActivity(intent);
    }

    private void initializeObject(Context ctx){
        SharedPreferences preferences = getPreferences(ctx.MODE_PRIVATE);
        DependenciesFactory.preferencesSet(preferences);
    }

    private void goChatAfterReceiveFirebaseNotificationClient(){
        finish();
        Intent intent = new Intent(this, ClientChat.class);
        intent.putExtra("receiverLogin", receiverLogin);
        intent.putExtra("senderName", senderName);
        intent.putExtra("senderId", senderId);
        intent.putExtra("senderLogin", senderLogin);
        startActivity(intent);
    }

    private void goChatAfterReceiveFirebaseNotificationAdmin(){
        finish();
        Intent intent = new Intent(this, WashChat.class);
        intent.putExtra("receiverLogin", receiverLogin); // мой логин
        intent.putExtra("senderName", senderName); // имя отправителя
        intent.putExtra("senderId", senderId);// ИД отправителя
        intent.putExtra("senderLogin", senderLogin); // логин отправителя
        startActivity(intent);
    }
}