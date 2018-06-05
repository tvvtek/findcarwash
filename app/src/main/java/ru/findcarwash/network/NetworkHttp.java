package ru.findcarwash.network;


import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class NetworkHttp {
    public NetworkHttp(String scriptName){
        this.scriptName = scriptName;
    }

    private volatile int state = 0;
    private String responce = "null";
    MySettings mySettings;
    private String scriptName = null;
    private HashMap<String, String> request = null;
    private String serverAddress = mySettings.getUrl();

    Thread thread_connect = new Thread(new Runnable() {
        public void run()
        {
            if (scriptName != null) {
                try {
                    OkHttpClient okHttpClient = DependenciesFactory.getOkHttpClient(); // get singltone okhttp obj
                    FormBody.Builder formBuilder = new FormBody.Builder().add("", ""); // this empty key+value
                    Set<Map.Entry<String, String>> requestSet = request.entrySet(); // prepare map for parsing request
                    for (Map.Entry<String, String> line : requestSet) {
                        formBuilder.add(line.getKey(), line.getValue()); // make body from hashmap to form for request
                    }
                    RequestBody formBody = formBuilder.build();
                    Request request = new Request.Builder()
                            .url(serverAddress + scriptName)
                            .post(formBody)
                            .build();

                    Response response = okHttpClient.newCall(request).execute(); // this stop code till data received from server
                    if (!response.isSuccessful()) {
                        throw new IOException("Error connect code= " + response);
                    } else {
                        responce = response.body().string();
                        state = 1; // success
                    }
                } catch (Exception error_connect) {
                    state = -1;
                    Log.d(mySettings.getLogTag(), error_connect.toString());
                }
            }
        }
    });

    public int getStateRequest(){
        return state;
    }
    public void sendRequest(HashMap<String, String> request) {
        this.request = null;
        this.request = request;
        if (this.request != null)thread_connect.start();
    }
    public String getResponse() {
        return responce;
    }
}
