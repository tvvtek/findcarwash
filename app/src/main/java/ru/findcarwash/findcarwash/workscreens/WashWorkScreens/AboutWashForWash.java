package ru.findcarwash.findcarwash.workscreens.WashWorkScreens;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.ChatClient.ClientChat;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers.AddReviewWash;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers.Map;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers.WashListReviewEvaluations;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class AboutWashForWash extends AppCompatActivity {


    private String login, id, short_name, description, rating, minPrice, washAddress, phone, image, latitude, longitude, loginWash;

    private int screenWidth;

    ImageView aboutWashPhoto, aboutWashRating;
    TextView aboutWashDescription, aboutWashAddress, aboutWashPhone;

    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_description_for_wash);
     /*   getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setElevation(0);

        ctx = getApplicationContext();
        // get data CarWash
       /* login = intent.getStringExtra(MySettings.LOGIN);
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

        initializeView(); // initialize View*/
    }
}