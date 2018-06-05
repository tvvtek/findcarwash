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


/**
 * if ($_POST['androidKey'] != $androidKey ) exit; // check correctly request from app, secret app key
 $login = $_POST['washRegisterLogin'];
 $password = $_POST['washRegisterPwd'];
 $washName = $_POST['washName'];
 $washAddress = $_POST['washAddress'];
 $washPhone = $_POST['washPhone'];
 $washMinPrice = $_POST['washMinPrice'];
 $washAdditional = $_POST['washAdditional'];
 */

public class WashRegistration extends AppCompatActivity {

    // View
    EditText washRegisterLogin, washRegisterPwd, washRegisterName, washRegisterAddress, washRegisterPhone, washRegisterMinPrice, washRegisterAdditional;
    Button washRegisterBtn;
    ProgressBar washRegisterProgressBar;
    // network
    NetworkHttp networkHttp;
    RegisterWashRequest registerWashRequest;
    HashMap<String, String> myRequest = new HashMap<>();
    private String response = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wash_registration);

        initializeVIew();

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.washRegisterTitle));
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

    /**
     * Send registration button
     * @param view
     */
    public void washRegisterBtn(View view) {
        if (checkCorrectField()){
            myRequest.put("washRegisterLogin", washRegisterLogin.getText().toString().trim());
            myRequest.put("washRegisterPwd", washRegisterPwd.getText().toString().trim());
            myRequest.put("washName", washRegisterName.getText().toString().trim());
            myRequest.put("washAddress", washRegisterAddress.getText().toString().trim());
            myRequest.put("washPhone", washRegisterPhone.getText().toString().trim());
            myRequest.put("washMinPrice", washRegisterMinPrice.getText().toString().trim());
            if (washRegisterAdditional.getText().toString() != null)myRequest.put("washAdditional", washRegisterAdditional.getText().toString().trim());
            myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.getAndroidKey()); // inject android key

            changeStateView(false);
            registerWashRequest = new RegisterWashRequest();
            registerWashRequest.execute();
        }
    }

    private void callFromAsyncTaskEnd(){
        changeStateView(true);
        try {
            response = registerWashRequest.get();
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
            finish();
            Intent intent = new Intent(this, WashRegistrationUploadPhoto.class);
            intent.putExtra("whoRun", "WashRegistration");
            intent.putExtra("myLogin", washRegisterLogin.getText().toString());
            startActivity(intent); // go to the enter activity after success registration
        }
        else sendToast(getResources().getString(R.string.registerUserToastErrorUnknown));
    }


    /**
     * Fast method, ugly. not time for make beautiful logic
     * @return
     */
    private boolean checkCorrectField(){
        boolean result = false;
        if (washRegisterLogin.length() > 2 & washRegisterLogin.length() < 31) // check login
        {
            if (washRegisterPwd.length() > 5 & washRegisterPwd.length() < 31){
                if (washRegisterName.length() > 2 & washRegisterName.length() < 31){
                    if (washRegisterAddress.length() > 5 & washRegisterAddress.length() < 61) {
                        if (washRegisterPhone.length() > 5 & washRegisterPhone.length() < 61) {
                            if (washRegisterMinPrice.length() > 2 & washRegisterMinPrice.length() < 11) {
                                result = true;
                            } else sendToast(getResources().getString(R.string.washRegisterMinPriceToast));
                        }
                        else sendToast(getResources().getString(R.string.washRegisterPhoneToast));
                    }
                    else sendToast(getResources().getString(R.string.washRegisterAddressToast));
                }
                else sendToast(getResources().getString(R.string.washRegisterNameToast));
            }
            else sendToast(getResources().getString(R.string.washRegisterPasswordToast));
        }
        else sendToast(getResources().getString(R.string.washRegisterLoginToast));
        return result;
    }


    private void changeStateView(boolean state){
        if (state){
            washRegisterBtn.setEnabled(true);
            washRegisterProgressBar.setVisibility(View.INVISIBLE);
        }
        else {
            washRegisterBtn.setEnabled(false);
            washRegisterProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void initializeVIew(){
        washRegisterLogin = findViewById(R.id.washRegisterLogin);
        washRegisterPwd = findViewById(R.id.washRegisterPwd);
        washRegisterName = findViewById(R.id.washRegisterName);
        washRegisterAddress = findViewById(R.id.washRegisterAddress);
        washRegisterPhone = findViewById(R.id.washRegisterPhone);
        washRegisterMinPrice = findViewById(R.id.washRegisterMinPrice);
        washRegisterAdditional = findViewById(R.id.washRegisterAdditional);
        washRegisterBtn = findViewById(R.id.washRegisterBtn);
        washRegisterProgressBar = findViewById(R.id.washRegisterProgressBar);
    }

    private void sendToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_SHORT);
        toast.show();
    }


    /**
     * This inner Class for make NetworkHttp worker and update UI
     */
    class RegisterWashRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                networkHttp = new NetworkHttp(MySettings.REGISTER_WASH_SCRIPT);
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