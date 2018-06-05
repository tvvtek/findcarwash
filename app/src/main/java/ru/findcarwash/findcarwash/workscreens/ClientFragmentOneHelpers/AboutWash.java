package ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.ChatClient.ClientChat;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class AboutWash extends AppCompatActivity {

    private String login, id, short_name, description, rating, minPrice, washAddress, phone, image, latitude, longitude, loginWash;

    private int screenWidth;

    ImageView aboutWashPhoto, aboutWashRating;
    Button aboutWashGoMap, aboutWashGoChat, aboutWashGoGoReview;
    TextView aboutWashDescription, aboutWashAddress, aboutWashPhone;

    Context ctx;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_description_for_wash);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setElevation(0);

        ctx = getApplicationContext();
        // get data CarWash
        Intent intent = getIntent();
        login = intent.getStringExtra(MySettings.LOGIN);
        id = intent.getStringExtra(MySettings.WASH_ID_NAME);
        short_name = intent.getStringExtra(MySettings.WASH_SHORT_NAME_NAME);
        description = intent.getStringExtra(MySettings.WASH_DESCRIPTION_NAME);
        rating = intent.getStringExtra(MySettings.WASH_RATING_NAME);
        minPrice = intent.getStringExtra(MySettings.WASH_MIN_PRICE_NAME);
        washAddress = intent.getStringExtra(MySettings.WASH_ADDRESS_NAME);
        phone = intent.getStringExtra(MySettings.WASH_PHONE_NAME);
        image = intent.getStringExtra(MySettings.WASH_IMAGE_NAME);
        // GPS locate wash
        latitude = intent.getStringExtra(MySettings.WASH_LATITUDE);
        longitude = intent.getStringExtra(MySettings.WASH_LONGITUDE);
        // Login Wash
        loginWash = intent.getStringExtra(MySettings.WASH_LOGIN);
        getSupportActionBar().setTitle(short_name);// set wash name to Action Bar

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth  = displaymetrics.widthPixels;

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


    private void initializeView(){
        aboutWashPhoto = findViewById(R.id.aboutWashPhoto);
        aboutWashRating = findViewById(R.id.aboutWashRating);

        aboutWashGoMap = findViewById(R.id.aboutWashGoMap);
        aboutWashGoChat = findViewById(R.id.aboutWashGoChat);
        aboutWashGoGoReview = findViewById(R.id.aboutWashGoWatchReview);

        aboutWashDescription = findViewById(R.id.aboutWashDescription);
        aboutWashAddress = findViewById(R.id.aboutWashAddress);
        aboutWashPhone = findViewById(R.id.aboutWashPhone);
        // download wash image full size
        Picasso.with(ctx)
                .load(MySettings.URL + MySettings.CATALOG_WASH_IMAGES + image)
                .resize(screenWidth, 0) // resizes the image to these dimensions (in pixel)
                .centerInside()
                .placeholder(R.drawable.progress_animation)
                .error( R.drawable.default_wash)
                .resize(screenWidth, screenWidth) // resizes the image to these dimensions (in pixel)
                .into(aboutWashPhoto);
        setRatingImage(rating); // set rating star
        aboutWashDescription.setText(description);
        aboutWashAddress.setText(getResources().getString(R.string.aboutWashWashAddress) + " " + washAddress);
        aboutWashPhone.setText(getResources().getString(R.string.aboutWashWashPhone) + " " + phone);
    }

    /**
     *  Set rating image stars
     */
    private void setRatingImage(String rating){
        try {
            int rating_start = Integer.parseInt(rating);
            if (rating_start == 0) aboutWashRating.setImageResource(R.drawable.rating_star_null);
            if (rating_start == 1) aboutWashRating.setImageResource(R.drawable.rating_star_one);
            if (rating_start == 2) aboutWashRating.setImageResource(R.drawable.rating_star_two);
            if (rating_start == 3) aboutWashRating.setImageResource(R.drawable.rating_star_three);
            if (rating_start == 4) aboutWashRating.setImageResource(R.drawable.rating_star_four);
            if (rating_start == 5) aboutWashRating.setImageResource(R.drawable.rating_star_five);
        }
        catch (IncompatibleClassChangeError error){
            error.printStackTrace();
            aboutWashRating.setImageResource(R.drawable.rating_star_five);
        }
    }

    public void aboutWashGoChat(View view) {
        Intent intent = new Intent(this, ClientChat.class);
        intent.putExtra("receiverLogin", login);
        intent.putExtra("senderName", short_name);
        intent.putExtra(MySettings.WASH_ID_NAME, id);
        intent.putExtra("senderLogin", loginWash);
        startActivity(intent);
        finish();
    }

    public void aboutWashGoMap(View view) {
            Intent intent = new Intent(this, Map.class);
            intent.putExtra(MySettings.WASH_SHORT_NAME_NAME, short_name);
            intent.putExtra(MySettings.WASH_LATITUDE, latitude);
            intent.putExtra(MySettings.WASH_LONGITUDE, longitude);
            startActivity(intent);
    }

    public void aboutWashGoWatchReview(View view) {
        Intent intent = new Intent(this, WashListReviewEvaluations.class);
        intent.putExtra(MySettings.WASH_ID_NAME, id);
        intent.putExtra(MySettings.WASH_SHORT_NAME_NAME, short_name);
        startActivity(intent);
    }

    public void aboutWashGoWatchAddReview(View view) {
        Intent intent = new Intent(this, AddReviewWash.class);
        intent.putExtra(MySettings.LOGIN, login);
        intent.putExtra(MySettings.WASH_ID_NAME, id);
        intent.putExtra(MySettings.WASH_SHORT_NAME_NAME, short_name);
        startActivity(intent);
    }

    public void aboutWashGoPhoneBtn(View view) {
        if (phone.length() > 1){
            intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        }
    }
}