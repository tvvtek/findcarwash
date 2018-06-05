package ru.findcarwash.ru.helpers.factory;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class DependenciesFactory {
    private static OkHttpClient okHttpClient;
    private static Gson gsonEncode;
    private static Gson gsonDecode;
    private static SharedPreferences preferencesGet;

    private static Realm realm;
    private static RealmConfiguration realmConfig;


    public DependenciesFactory() {
    }

    // return singltone networkStartApp object
    public static OkHttpClient getOkHttpClient(){
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(MySettings.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(MySettings.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(MySettings.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient;
    }
    // return singltone gsone obj encode

    public static Gson getGsonEncode(){
        if (gsonEncode == null){
            gsonEncode = new Gson();
        }
        return gsonEncode;
    }
    // return singltone gsone obj encode
    public static Gson getGsonDecode(){
        if (gsonDecode == null){
            GsonBuilder builder = new GsonBuilder();
            gsonDecode = builder.create();
        }
        return gsonDecode;
    }

    // make shared preferences on singl obj
    public static void preferencesSet(SharedPreferences prefGet) {
        if (preferencesGet == null) preferencesGet = prefGet;
    }
    // return shared preferences
    public static SharedPreferences getPreferencesGet(){
        return preferencesGet;
    }

    // Singleton Realm instance
    public static Realm getRealm(){
        if (realm == null){
            realm = Realm.getDefaultInstance();
            realmConfig = new RealmConfiguration.Builder().name("historyChat.realm").build();
            Realm.setDefaultConfiguration(realmConfig);
        }
        return realm;
    }
}