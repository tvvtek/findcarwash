package ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.network.NetworkHttp;
import ru.findcarwash.ru.helpers.json.JsonReviewItemsReceive;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class WashListReviewEvaluations extends AppCompatActivity {
    // AvtionBar name wash
    private String short_name = "";
    private String id;
    // list
    WashListReviewEvaluationAdapter washListReviewEvaluationAdapter;
    ArrayList<WashListReviewEvaluationItem> washListReviewEvaluationItem = new ArrayList<>();
    // view
    TextView evaluationEmptyText;
    ListView listViewReview;
    LinearLayout mLayout;
    ProgressBar progressBar;
    // Async task
    GetReviewsFromServer getReviewsFromServer;
    // Network and request
    NetworkHttp networkHttp;
    HashMap<String, String> myRequest = new HashMap<>();

    private String response = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_evaluations_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setElevation(0);

        mLayout = findViewById(R.id.washEvaluationsList); // get layout for add or remove view from code
        Intent intent = getIntent();
        short_name = intent.getStringExtra(MySettings.WASH_SHORT_NAME_NAME);
        id = intent.getStringExtra(MySettings.WASH_ID_NAME);

        getSupportActionBar().setTitle(short_name);
        initializeView();
        changeStateView(false);
        // make and start network request
        myRequest.clear();
        myRequest.put("id", id);
        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.ANDROID_KEY);

        getReviewsFromServer = new GetReviewsFromServer();
        getReviewsFromServer.execute();
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
    private void setAdapter(){
        washListReviewEvaluationAdapter = new WashListReviewEvaluationAdapter(getApplicationContext(), washListReviewEvaluationItem);
        listViewReview.setAdapter(washListReviewEvaluationAdapter);
    }

    private void callFromAsyncTaskEnd(){
        try {
            response = getReviewsFromServer.get();
           // Log.d(MySettings.LOG_TAG, "responce=" + response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // check response valid
        if(!response.equals("error:3") && response.equals("null")){
      //
           // Log.d(MySettings.LOG_TAG, "washListReviewEvaluationItem=");
        }
        makeListReview(response);
        changeStateView(true);
        setAdapter();
    }

    /**
     * Parse json response from server
     */
    private ArrayList<JsonReviewItemsReceive> makeListReview(String response) {
        ArrayList<JsonReviewItemsReceive> jsonReviewItemsReceives = new ArrayList<>();
        try {
            JSONArray mJsonArray = new JSONArray(response);
            JSONObject mJsonObject;
            for (int i = 0; i < mJsonArray.length(); i++) {
                mJsonObject = mJsonArray.getJSONObject(i);
                // for all data
                jsonReviewItemsReceives.add(new JsonReviewItemsReceive(
                        mJsonObject.get("login").toString(),
                        mJsonObject.get("review").toString(),
                        mJsonObject.get("rating").toString()));
                // for  catalogList
                washListReviewEvaluationItem.add(new WashListReviewEvaluationItem(
                        mJsonObject.get("login").toString(), mJsonObject.get("review").toString(),  mJsonObject.get("rating").toString()
                ));
                mLayout.removeView(progressBar);
                mLayout.removeView(evaluationEmptyText);
                if (mJsonArray.length() == 0) evaluationEmptyText.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonReviewItemsReceives;
    }

    /**
     * Init view
     */
    private void initializeView(){
        evaluationEmptyText = findViewById(R.id.evaluationEmptyText);
        progressBar = findViewById(R.id.evaluationsProgressBar);
        listViewReview = findViewById(R.id.evaluationsList);
    }
    /**
     *  Change state View
     */
    private void changeStateView(boolean state){
        if (!state){
            progressBar.setVisibility(View.VISIBLE);
            listViewReview.setVisibility(View.INVISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            listViewReview.setVisibility(View.VISIBLE);
        }
    }
    /**
     * This inner Class for make NetworkHttp worker and update UI
     */
    class GetReviewsFromServer extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                networkHttp = new NetworkHttp(MySettings.REVIEW_LIST_SCRIPT);
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