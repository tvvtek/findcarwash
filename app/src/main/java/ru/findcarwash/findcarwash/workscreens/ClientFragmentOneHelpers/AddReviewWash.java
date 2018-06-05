package ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.network.NetworkHttp;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.json.JsonReviewReceive;
import ru.findcarwash.ru.helpers.settings.MySettings;


public class AddReviewWash extends AppCompatActivity {

    private boolean updateOrAddReview = true; // if this false var to review is not exist
    private int rating = 1;
    private String login, id_wash, short_name, review;

    Button star1, star2, star3, star4, star5, saveBtn;
    TextView reviewAddReview, reviewAddReviewText;
    EditText reviewEdit;
    ProgressBar reviewProgressLoad;
    LinearLayout mLayout;
    // other my package
    NetworkHttp networkHttp;
    HashMap<String, String> myRequest = new HashMap<>();
    ReviewNetwork reviewNetwork;

    Gson gsonDecode;

    private String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_add_review_client_wash);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));

        mLayout = findViewById(R.id.reviewAddLayout);

        initializeView();
        Intent intent = getIntent();
        login = intent.getStringExtra(MySettings.LOGIN);
        id_wash = intent.getStringExtra(MySettings.WASH_ID_NAME);
        short_name = intent.getStringExtra(MySettings.WASH_SHORT_NAME_NAME);
        getSupportActionBar().setTitle(short_name);

        checkExistenceReview(); // check review data on server
        changeStateView(false);
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

    private void callFromAsyncTaskEnd(){
        changeStateView(true);
        try {
            response = reviewNetwork.get().toString();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // check receive error first select
        if (!response.equals("error:3") & !response.equals("success:success")){
            gsonDecode = DependenciesFactory.getGsonDecode(); // get singleton obj
            JsonReviewReceive jsonReviewReceive;
            try{
                jsonReviewReceive = gsonDecode.fromJson(response, JsonReviewReceive.class);
                setRatingAndField(jsonReviewReceive);
            } catch (JsonParseException error){
                error.printStackTrace();
            }
        }
        else if (response.equals("success:success")){
            updateOrAddReview = false;
            sendToast(getResources().getString(R.string.reviewSaveSuccess));
        }
        else {
        }
    }

    /**
     * This method call exist review data
     */
    private void setRatingAndField(JsonReviewReceive jsonReviewReceive){
        updateOrAddReview = false; // Review is exist
        this.rating = Integer.parseInt(jsonReviewReceive.rating);
        this.review = jsonReviewReceive.review;
        setRating(rating);
        reviewEdit.setText(review);
        reviewAddReview.setText(getResources().getString(R.string.reviewChangeRating));
        reviewAddReviewText.setText(getResources().getString(R.string.reviewChangeText));
    }

    private void addReview(){
        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.getAndroidKey());
        myRequest.put("event", "add"); // select event for check existence data on serve
        myRequest.put(MySettings.WASH_ID_NAME, id_wash);
        myRequest.put(MySettings.LOGIN, login);
        myRequest.put("review", reviewEdit.getText().toString());
        myRequest.put("rating", Integer.toString(rating));
        reviewNetwork = new ReviewNetwork();
        reviewNetwork.execute();
    }
    private void updateReview(){
        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.getAndroidKey());
        myRequest.put("event", "change"); // select event for check existence data on serve
        myRequest.put(MySettings.WASH_ID_NAME, id_wash);
        myRequest.put(MySettings.LOGIN, login);
        myRequest.put("review", reviewEdit.getText().toString());
        myRequest.put("rating", Integer.toString(rating));
        reviewNetwork = new ReviewNetwork();
        reviewNetwork.execute();
    }
    /**
     * Check existence review data
     */
    private void checkExistenceReview(){
        myRequest.put(MySettings.CLIENT_ANDROID_KEY_NAME, MySettings.getAndroidKey());
        myRequest.put("event", "select"); // select event for check existence data on serve
        myRequest.put(MySettings.WASH_ID_NAME, id_wash);
        myRequest.put(MySettings.LOGIN, login);
        reviewNetwork = new ReviewNetwork();
        reviewNetwork.execute();
    }
    /**
     * This inner Class for make NetworkHttp worker and update UI
     */
    class ReviewNetwork extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                networkHttp = new NetworkHttp(MySettings.REVIEW_SCRIPT);
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

    /**
    * Initialize view
    */
    private void initializeView(){
        star1 = findViewById(R.id.reviewStar1);
        star2 = findViewById(R.id.reviewStar2);
        star3 = findViewById(R.id.reviewStar3);
        star4 = findViewById(R.id.reviewStar4);
        star5 = findViewById(R.id.reviewStar5);
        saveBtn = findViewById(R.id.reviewSaveReviewButton);
        reviewAddReview = findViewById(R.id.reviewAddReview);
        reviewAddReviewText = findViewById(R.id.reviewAddReviewText);
        reviewEdit = findViewById(R.id.reviewReview);
        reviewProgressLoad = findViewById(R.id.reviewProgressLoad);
    }
    private void sendToast(String text) {
        Toast toast = Toast.makeText(this,
                text, Toast.LENGTH_LONG);
        toast.show();
    }
    /**
     * Change view state
     */
    private void changeStateView(boolean state){
        if (!state){
            reviewProgressLoad.setVisibility(View.VISIBLE);
            star1.setVisibility(View.INVISIBLE);
            star2.setVisibility(View.INVISIBLE);
            star3.setVisibility(View.INVISIBLE);
            star4.setVisibility(View.INVISIBLE);
            star5.setVisibility(View.INVISIBLE);
            reviewAddReview.setVisibility(View.INVISIBLE);
            reviewAddReviewText.setVisibility(View.INVISIBLE);
            reviewEdit.setVisibility(View.INVISIBLE);
            saveBtn.setVisibility(View.INVISIBLE);
        }
        else {
            reviewProgressLoad.setVisibility(View.INVISIBLE);
            mLayout.removeView(reviewProgressLoad);
            star1.setVisibility(View.VISIBLE);
            star2.setVisibility(View.VISIBLE);
            star3.setVisibility(View.VISIBLE);
            star4.setVisibility(View.VISIBLE);
            star5.setVisibility(View.VISIBLE);
          //  reviewAddReview.setVisibility(View.VISIBLE);
          //  reviewAddReviewText.setVisibility(View.VISIBLE);
            reviewEdit.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.VISIBLE);
        }
    }
    /**
     * Button click listener
     */
    public void reviewSaveReviewButton(View view) {
        myRequest.clear(); // clear request
        if (updateOrAddReview) addReview();
        else updateReview();
    }
    public void reviewStar1(View view) {
        setRating(1);
    }
    public void reviewStar2(View view) {
        setRating(2);
    }
    public void reviewStar3(View view) {
        setRating(3);
    }
    public void reviewStar4(View view) {
        setRating(4);
    }
    public void reviewStar5(View view) {
        setRating(5);
    }

    private void setRating(int rating){
        if (rating == 1){
            this.rating = 1;
            star1.setBackgroundResource(R.drawable.star_enable);
            star2.setBackgroundResource(R.drawable.star_disable);
            star3.setBackgroundResource(R.drawable.star_disable);
            star4.setBackgroundResource(R.drawable.star_disable);
            star5.setBackgroundResource(R.drawable.star_disable);
        }
        else if (rating == 2){
            this.rating = 2;
            star1.setBackgroundResource(R.drawable.star_enable);
            star2.setBackgroundResource(R.drawable.star_enable);
            star3.setBackgroundResource(R.drawable.star_disable);
            star4.setBackgroundResource(R.drawable.star_disable);
            star5.setBackgroundResource(R.drawable.star_disable);
        }
        else if (rating == 3){
            this.rating = 3;
            star1.setBackgroundResource(R.drawable.star_enable);
            star2.setBackgroundResource(R.drawable.star_enable);
            star3.setBackgroundResource(R.drawable.star_enable);
            star4.setBackgroundResource(R.drawable.star_disable);
            star5.setBackgroundResource(R.drawable.star_disable);
        }
        else if (rating == 4) {
            this.rating = 4;
            star1.setBackgroundResource(R.drawable.star_enable);
            star2.setBackgroundResource(R.drawable.star_enable);
            star3.setBackgroundResource(R.drawable.star_enable);
            star4.setBackgroundResource(R.drawable.star_enable);
            star5.setBackgroundResource(R.drawable.star_disable);
        }
        else if (rating == 5){
            this.rating = 5;
            star1.setBackgroundResource(R.drawable.star_enable);
            star2.setBackgroundResource(R.drawable.star_enable);
            star3.setBackgroundResource(R.drawable.star_enable);
            star4.setBackgroundResource(R.drawable.star_enable);
            star5.setBackgroundResource(R.drawable.star_enable);
        }
    }
}
