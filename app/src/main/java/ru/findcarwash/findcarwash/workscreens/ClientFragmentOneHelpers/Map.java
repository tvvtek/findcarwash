package ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private String short_name, latitude, longitude;
    private double lat = -33.852;
    private double lng = 151.211;

    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_map_for_wash);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));

        Intent intent = getIntent();
        short_name = intent.getStringExtra(MySettings.WASH_SHORT_NAME_NAME);
        latitude = intent.getStringExtra(MySettings.WASH_LATITUDE);
        longitude = intent.getStringExtra(MySettings.WASH_LONGITUDE);
        getSupportActionBar().setTitle(short_name);

        try {
            this.lat = Double.parseDouble(latitude);
            this.lng = Double.parseDouble(longitude);
        }
        catch(Exception errorConvertTypes){
            errorConvertTypes.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng wash = new LatLng(lat, lng);

        googleMap.addMarker(new MarkerOptions().position(wash)
                .title(short_name));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(wash));
        googleMap.setMinZoomPreference(15.0f);
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
}
