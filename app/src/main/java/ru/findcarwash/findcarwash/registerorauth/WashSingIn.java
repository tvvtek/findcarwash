package ru.findcarwash.findcarwash.registerorauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.WashWorkScreens.WashChatList;
import ru.findcarwash.network.NetworkHttp;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.json.JsonSignInResponse;
import ru.findcarwash.ru.helpers.settings.MySettings;
import ru.findcarwash.ru.helpers.staticClasses.DeviceHardware;

/**
 * This activity class for sign in wash.
 */

public class WashSingIn extends AppCompatActivity {

    // const for response and shared pref
    private final String CLIENT_DEVICE_PREFERENCE = "myDevice";
    private final String WASH_EMAIL_PREFERENCE = "myEmail";
    private final String tokenFirebase = "tokenFirebase";

    // const for network work
    private final String WASH_SIGN_LOGIN_REQUEST = "washSignInLogin";
    private final String WASH_SIGN_PWD_REQUEST = "washSignInPwd";
    // inner libs
    Gson gsonDecode;
    // other my package
    NetworkHttp networkHttp;
    // other var
    Context ctx;
    SharedPreferences sPrefGet;
    // Async task and networkHttp
    WashSingIn.WashSignInRequest washSignInRequest;
    HashMap<String, String> myRequest = new HashMap<>();
    // view elements
    TextView washRecoverTextBtn;
    Button washSignInBtn, washRegisterBtn;
    EditText washSignInUid, washSignInPwd;
    ProgressBar washProgressBarSignIn;
    // var sign in
    private String response = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wash_sign_in);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setTitle(getResources().getString(R.string.signInUserTitle));

        initializeView();
        changeStateView(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Press sign in button
    public void washSingInBtn(View view) {
        boolean startOperation = true;
        if (checkLengthInputField( washSignInUid.getText().toString()))myRequest.put(WASH_SIGN_LOGIN_REQUEST, washSignInUid.getText().toString());
        else {
            sendToast(getResources().getString(R.string.SignInUserToastErrorLoginLength));
            startOperation = false;
        }
        if (checkLengthInputField(washSignInPwd.getText().toString())) myRequest.put(WASH_SIGN_PWD_REQUEST, washSignInPwd.getText().toString());
        else{
            sendToast(getResources().getString(R.string.SignInUserToastErrorPassLength));
            startOperation = false;
        }
        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.getAndroidKey());  // "androidKey" secret android key device
        myRequest.put(MySettings.CLIENT_SIGN_DEVICE_UID_REQUEST, DeviceHardware.getDeviceName()); // send mobile phone model
        myRequest.put(tokenFirebase, getFirebaseToken()); // token Firebase
        if (startOperation) {
            changeStateView(false); // view element switch to disable and go networkHttp request
            washSignInRequest = new WashSingIn.WashSignInRequest();
            washSignInRequest.execute();
          //  Log.d(MySettings.LOG_TAG, "Request=" + myRequest);
        }
    }

    /**
     * if state = false to turn all element disabled but progress bar enabled
     * else to turn all element enabled but progress bar disabled
     */
    private void changeStateView(boolean state)
    {
        washSignInUid.setEnabled(state);
        washSignInPwd.setEnabled(state);
        washSignInBtn.setEnabled(state);

        washRecoverTextBtn.setEnabled(state);
        washRegisterBtn.setEnabled(state);
        if (!state) washProgressBarSignIn.setVisibility(View.VISIBLE);
        else washProgressBarSignIn.setVisibility(View.INVISIBLE);
    }

    /**
     * This method call from end Async Task end thread
     */
    private void callFromAsyncTaskEnd(){
        changeStateView(true); // change state view elements on Enabled
        try {
            response = washSignInRequest.get(); // this get response from AsyncTask(server)
         //   Log.d(MySettings.LOG_TAG, "response=" + response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (response.equals("null")) sendToast(getResources().getString(R.string.signInUserToastErrorInet)); // send error message
        else {
            gsonDecode = DependenciesFactory.getGsonDecode();
            JsonSignInResponse jsonSignInResponse;
            try{
                jsonSignInResponse = gsonDecode.fromJson(response, JsonSignInResponse.class); // parse data from response to object
                // check response on error code
                if(jsonSignInResponse.error.equals(MySettings.ERROR_NO))
                {
                    if (writeSharedPreferences(true, jsonSignInResponse.signInLogin, WASH_EMAIL_PREFERENCE, jsonSignInResponse.key, DeviceHardware.getDeviceName()))
                    {
                        // AUTH OK
                        Intent intent = new Intent(this, WashChatList.class);
                        startActivity(intent); // go to the work activities after login
                        finish();
                    }
                    else sendToast(getResources().getString(R.string.signInUserToastErrorWritePreferences));
                }
                else if (jsonSignInResponse.error.equals(MySettings.ERROR_LOGIN)) sendToast(getResources().getString(R.string.signInUserToastErrorLogin));
                else if (jsonSignInResponse.error.equals(MySettings.ERROR_PWD)) sendToast(getResources().getString(R.string.signInUserToastErrorPwd));
                else if (jsonSignInResponse.error.equals(MySettings.ERROR_UNKNOWN)) sendToast(getResources().getString(R.string.signInUserToastErrorUnknown));
            }
            catch (JsonParseException parseError){
                parseError.printStackTrace();
                sendToast(getResources().getString(R.string.signInUserToastErrorUnknown));
            }
        }
    }

    private boolean writeSharedPreferences(boolean wash, String myLogin, String myEmail, String myKey, String myDevice){
        try {
            sPrefGet = DependenciesFactory.getPreferencesGet();
            SharedPreferences.Editor editor = sPrefGet.edit();
            editor.putBoolean(MySettings.IS_WASH, wash);
            editor.putString(MySettings.CLIENT_LOGIN_PREFERENCE, myLogin);
            editor.putString(MySettings.CLIENT_KEY, myKey);
            editor.putString(CLIENT_DEVICE_PREFERENCE, myDevice);
            editor.apply();
            return true;
        }
        catch (Exception writeToPreferenceException){
            writeToPreferenceException.printStackTrace();
            return false;
        }
    }

    /**
     * Check length input fields
     */
    private boolean checkLengthInputField(String field){
        if (field.length() >= MySettings.MIN_LENGTH && MySettings.MAX_LENGTH >= field.length())
            return true;
        else    return false;
    }

    /**
     * Initialize view method and sync via thread
     */
    private void initializeView(){
        // in section
        washSignInUid = findViewById(R.id.washSingInIud);
        washSignInPwd = findViewById(R.id.washSingInPwd);
        washSignInBtn = findViewById(R.id.washSingInBtn);
        washProgressBarSignIn = findViewById(R.id.washSingInProgressBar);
        // recover access
        washRecoverTextBtn = findViewById(R.id.washForgotAccess);
        //register
        washRegisterBtn = findViewById(R.id.washRegisterBtn);
    }

    private void sendToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private String getFirebaseToken(){
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        //   Log.d(MySettings.LOG_TAG, "token=" + token);
        return token;
    }

    public void washRegisterBtn(View view) {
        Intent washRegistration = new Intent(this, WashRegistration.class);
        startActivity(washRegistration);
    }

    public void washForgotAccess(View view) {
        // somecode
    }

    /**
     * This inner Class for make NetworkHttp worker and update UI
     */
    class WashSignInRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                networkHttp = new NetworkHttp(MySettings.SIGN_IN_WASH_SCRIPT);
                networkHttp.sendRequest(myRequest);
                while (networkHttp.getStateRequest() == 0){} // wait change state request from network obj
            } catch (Exception e) {
                e.printStackTrace();
            }
            return networkHttp.getResponse().toString();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            callFromAsyncTaskEnd(); // call method after end asynk task
        }
    }
}