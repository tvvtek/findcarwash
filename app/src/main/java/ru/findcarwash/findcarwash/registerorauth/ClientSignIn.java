package ru.findcarwash.findcarwash.registerorauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import ru.findcarwash.findcarwash.AppLaunch;
import ru.findcarwash.findcarwash.R;
import ru.findcarwash.network.NetworkHttp;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.json.JsonSignInResponse;
import ru.findcarwash.ru.helpers.settings.MySettings;
import ru.findcarwash.ru.helpers.staticClasses.DeviceHardware;

/**
 * This activity class for sign in client.
 *
 * API
 *
 * SEND POST data to server
 *
 * signInLogin=myLogin
 * clientSignInPwd=myPass
 * deviceModel=myModelPhone
 * androidKey=androidKey
 * tokenFirebase=token
 *
 *
 * RECEIVE JSON obj JsonSignInResponse
 *
 * jsonSignInResponse.error=1 - error login
 * jsonSignInResponse.error=2 - error password
 * jsonSignInResponse.error=3 - unknown error
 * OR
 * jsonSignInResponse.error=0 - no error
 * THEN
 * jsonSignInResponse.signInLogin= my login receive
 * jsonSignInResponse.key= my unique client key length 256(string)
 */

public class ClientSignIn extends AppCompatActivity {
    // const for response and shared pref

    private final String tokenFirebase = "tokenFirebase";
    // inner libs
    Gson gsonDecode;
    // other my package
    NetworkHttp networkHttp;
    // other var
    Context ctx;
    SharedPreferences sPrefGet;
    // Async task and networkHttp
    SignInRequest signInRequest;
    HashMap<String, String> myRequest = new HashMap<>();
    // view elements
    TextView clientRecoverTextBtn;
    Button clientSignInBtn, clientRegisterBtn;
    EditText clientSignInUid, clientSignInPwd;
    ProgressBar clientProgressBarSignIn;
    // var sign in
    private String response = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        setContentView(R.layout.client_sign_in);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setTitle(getResources().getString(R.string.signInUserTitle));

        initializeView(); // init view
        clientProgressBarSignIn.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Press sign in button
    public void clientSingInBtn(View view) {
        boolean startOperation = true;
        if (checkLengthInputField( clientSignInUid.getText().toString()))myRequest.put(MySettings.CLIENT_SIGN_LOGIN_REQUEST, clientSignInUid.getText().toString());
        else {
            sendToast(getResources().getString(R.string.SignInUserToastErrorLoginLength));
            startOperation = false;
        }
        if (checkLengthInputField(clientSignInPwd.getText().toString())) myRequest.put(MySettings.CLIENT_SIGN_PWD_REQUEST, clientSignInPwd.getText().toString());
        else{
            sendToast(getResources().getString(R.string.SignInUserToastErrorPassLength));
            startOperation = false;
        }

        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.CLIENT_KEY); // send mobile phone model
        myRequest.put(MySettings.CLIENT_SIGN_DEVICE_UID_REQUEST, DeviceHardware.getDeviceName()); // send mobile phone model
        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.getAndroidKey()); // inject android key
        myRequest.put(tokenFirebase, getFirebaseToken()); // token Firebase
        if (startOperation) {
            changeStateView(false); // view element switch to disable and go networkHttp request
            signInRequest = new SignInRequest();
            signInRequest.execute();
      //      Log.d(MySettings.LOG_TAG, "Request= " + myRequest);
        }
    }

    public void clientForgotAccess(View view) {

    }

    public void clientRegisterBtn(View view) {
        finish();
        Intent intent = new Intent(ClientSignIn.this, ClientRegister.class);
        startActivity(intent); // go to the work activities after login
    }

    /**
     * This method call from end Async Task end thread
     */
    private void callFromAsyncTaskEnd(){
        changeStateView(true); // change state view elements on Enabled
        try {
            response = signInRequest.get(); // this get response from AsyncTask(server)
        //    Log.d(MySettings.LOG_TAG, "response=" + response);
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
                    // try save local preferences
                    if (writeSharedPreferences(false, jsonSignInResponse.signInLogin, MySettings.CLIENT_EMAIL_PREFERENCE, jsonSignInResponse.key, DeviceHardware.getDeviceName()))
                    {
                        finish();
                        Intent intent = new Intent(ClientSignIn.this, AppLaunch.class);
                        startActivity(intent); // go to the work activities after login
                    }
                    else sendToast(getResources().getString(R.string.signInUserToastErrorWritePreferences));
                }
                else if (jsonSignInResponse.error.equals(MySettings.ERROR_LOGIN)) sendToast(getResources().getString(R.string.signInUserToastErrorLogin));
                else if (jsonSignInResponse.error.equals(MySettings.ERROR_PWD)) sendToast(getResources().getString(R.string.signInUserToastErrorPwd));
                else if (jsonSignInResponse.error.equals(MySettings.ERROR_UNKNOWN)) sendToast(getResources().getString(R.string.signInUserToastErrorUnknown));
                //        Log.d(MySettings.LOG_TAG, "error=" + jsonSignInResponse.error);
                //        Log.d(MySettings.LOG_TAG, "|login=" + jsonSignInResponse.signInLogin);
                //        Log.d(MySettings.LOG_TAG, "|key=" + jsonSignInResponse.key);
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
            editor.putString(MySettings.CLIENT_EMAIL_PREFERENCE, myEmail);
            editor.putString(MySettings.CLIENT_KEY, myKey);
            editor.putString(MySettings.CLIENT_DEVICE_PREFERENCE, myDevice);
            editor.apply();
            return true;
        }
        catch (Exception writeToPreferenceException){
            writeToPreferenceException.printStackTrace();
            return false;
        }
    }

    private void sendToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_SHORT);
        toast.show();
    }


    /**
     * Initialize view method and sync via thread
     */
    private void initializeView(){
        // in section
        clientSignInUid = findViewById(R.id.clientSingInIud);
        clientSignInPwd = findViewById(R.id.clientSingInPwd);
        clientSignInBtn = findViewById(R.id.clientSingInBtn);
        clientProgressBarSignIn = findViewById(R.id.clientSingInProgressBar);
        // recover access
        clientRecoverTextBtn = findViewById(R.id.clientForgotAccess);
        //register
        clientRegisterBtn = findViewById(R.id.clientRegisterBtn);
    }


    /**
     * if state = false to turn all element disabled but progress bar enabled
     * else to turn all element enabled but progress bar disabled
     */
    private void changeStateView(boolean state)
    {
        clientSignInUid.setEnabled(state);
        clientSignInPwd.setEnabled(state);
        clientSignInBtn.setEnabled(state);

        clientRecoverTextBtn.setEnabled(state);
        clientRegisterBtn.setEnabled(state);
        if (!state) clientProgressBarSignIn.setVisibility(View.VISIBLE);
        else clientProgressBarSignIn.setVisibility(View.INVISIBLE);
    }


    /**
     * Check length input fields
     */
    private boolean checkLengthInputField(String field){
        if (field.length() >= MySettings.MIN_LENGTH && MySettings.MAX_LENGTH >= field.length())
            return true;
        else    return false;
    }

    private String getFirebaseToken(){
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        //   Log.d(MySettings.LOG_TAG, "token=" + token);
        return token;
    }


    /**
     * This inner Class for make NetworkHttp worker and update UI
     */
    class SignInRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                networkHttp = new NetworkHttp(MySettings.SIGN_IN_CLIENT_SCRIPT);
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