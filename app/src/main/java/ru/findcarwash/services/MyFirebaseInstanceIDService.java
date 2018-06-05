package ru.findcarwash.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private String token;

    @Override
    public void onTokenRefresh() {
        /**
         * Generate token and call sendRegistrationToServer method
         */
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
      //  Log.d(MySettings.LOG_TAG, "Refreshed token: " + refreshedToken);
        this.token = refreshedToken;
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
    }
}
