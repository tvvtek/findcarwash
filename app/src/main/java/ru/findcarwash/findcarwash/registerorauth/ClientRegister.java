package ru.findcarwash.findcarwash.registerorauth;

import android.content.Intent;
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
import android.widget.Toast;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.network.NetworkHttp;
import ru.findcarwash.ru.helpers.settings.MySettings;
import ru.findcarwash.ru.helpers.staticClasses.DeviceHardware;

public class ClientRegister extends AppCompatActivity {

    /**
     * API
     *
     * send to server POST data
     *
     * clientRegisterInLogin=login
     * clientRegisterInPwd=password
     * clientRegisterEmail=empty OR clientRegisterEmail=email
     * clientDeviceModel=modelPhone
     * androidKey=key(secret android key)
     *
     * receive data from server on String
     *
     * error:1 - login exist
     * error:3 - unknown error
     * success:success - registration completed
     */
    // const for network work
    private final String CLIENT_REGISTER_LOGIN = "clientRegisterInLogin";
    private final String CLIENT_REGISTER_PWD = "clientRegisterInPwd";
    private final String CLIENT_REGISTER_DEVICE_UID = "clientDeviceModel";
    private final String CLIENT_REGISTER_EMAIL = "clientRegisterEmail";
    private String clientRegisterEmail = "empty";
    //View elements
    EditText clientLoginRegister, clientPwdRegister, clientEmailRegister;
    Button goRegisterBtn;
    ProgressBar progressRegister;
    // Network
    NetworkHttp networkHttp;
    RegisterRequest registerRequest;
    HashMap<String, String> myRequest = new HashMap<>();
    private String response = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_registration);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setTitle(getResources().getString(R.string.registerUserHeadInfo));

        initializeView(); // initialize View
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

    /**
     * Go register button press
     */
    public void GoClientRegister(View view) {
        boolean startOperation = true;
        if (!clientEmailRegister.getText().toString().isEmpty()) clientRegisterEmail = clientEmailRegister.getText().toString();

        if (checkLengthInputField(clientLoginRegister.getText().toString())) myRequest.put(CLIENT_REGISTER_LOGIN, clientLoginRegister.getText().toString());
        else {
            sendToast(getResources().getString(R.string.registerUserToastErrorLoginLength));
            startOperation = false;
        }

        if (checkLengthInputField(clientPwdRegister.getText().toString())) myRequest.put(CLIENT_REGISTER_PWD, clientPwdRegister.getText().toString());
        else {
            sendToast(getResources().getString(R.string.registerUserToastErrorPassLength));
            startOperation = false;
        }
        myRequest.put(CLIENT_REGISTER_EMAIL, clientRegisterEmail);
        myRequest.put(CLIENT_REGISTER_DEVICE_UID, DeviceHardware.getDeviceName());
        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.getAndroidKey()); // inject android key
        if (startOperation) {
            changeStateView(false);
            registerRequest = new RegisterRequest();
            registerRequest.execute(); // start request
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

    private void sendToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * This method called after and network request
     */
    private void callFromAsyncTaskEnd(){
        changeStateView(true); // change state view elements on Enabled
        try {
            response = registerRequest.get();
            Log.d(MySettings.LOG_TAG, "response=" + response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (response.equals("null")) sendToast(getResources().getString(R.string.signInUserToastErrorInet)); // send error message Check internet
        else if (response.equals("error:" + MySettings.ERROR_LOGIN)) sendToast(getResources().getString(R.string.registerUserToastErrorLogin)); // error login number 1
        else if (response.equals("error:" + MySettings.ERROR_UNKNOWN)) sendToast(getResources().getString(R.string.registerUserToastErrorUnknown)); //error unknown number 3
        else if (response.equals("success:" + MySettings.SUCCESS)){
            // registration success, go to enter activity
            sendToast(getResources().getString(R.string.registerUserToastSuccess));
            Intent intent = new Intent(ClientRegister.this, ClientSignIn.class);
            startActivity(intent); // go to the enter activity after success registration
            finish();
        }
        else sendToast(getResources().getString(R.string.registerUserToastErrorUnknown));
    }

    /**
     * if state = false to turn all element disabled but progress bar enabled
     * else to turn all element enabled but progress bar disabled
     */
    private void changeStateView(boolean state) {
        goRegisterBtn.setEnabled(state);
        clientLoginRegister.setEnabled(state);
        clientPwdRegister.setEnabled(state);
        clientEmailRegister.setEnabled(state);
       // goRegisterBtn.setActivated(false);
        if (!state) progressRegister.setVisibility(View.VISIBLE);
        else progressRegister.setVisibility(View.INVISIBLE);
    }

    /**
     * This method used for init view element
     */
    private void initializeView() {
        clientLoginRegister = findViewById(R.id.clientRegisterLogin);
        clientPwdRegister = findViewById(R.id.clientRegisterPwd);
        clientEmailRegister = findViewById(R.id.clientRegisterEmail);
        goRegisterBtn = findViewById(R.id.goRegisterBtn);
        progressRegister = findViewById(R.id.clientRegisterProgress);
        progressRegister.setVisibility(View.INVISIBLE);
    }


    /**
     * This inner Class for make NetworkHttp worker and update UI
     */
    class RegisterRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                networkHttp = new NetworkHttp(MySettings.REGISTER_CLIENT_SCRIPT);
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
            callFromAsyncTaskEnd(); // call method after end async task
        }
    }
}
