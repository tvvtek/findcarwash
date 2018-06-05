package ru.findcarwash.findcarwash.workscreens.ClientWorkScreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers.AboutWash;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers.CatalogItem;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers.CatalogItemAdapter;
import ru.findcarwash.network.NetworkHttp;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.json.JsonCatalogReceive;
import ru.findcarwash.ru.helpers.settings.MySettings;
import ru.findcarwash.ru.helpers.staticClasses.DeviceHardware;

public class FragmentOneCatalog extends Fragment {

    private String login, model;
    private String response = "null";

    ArrayList<CatalogItem> washListOriginal = new ArrayList<>();
    ArrayList<CatalogItem> washListRender = new ArrayList<>();
    CatalogItemAdapter catalogAdapter;
    // view
    SearchView searchWash;
    ProgressBar progressLoadCatalog;
    Button catalogRepeatLoadButton;
    ListView catalogListView;
    LinearLayout mainLayout;
    // network workers
    NetworkHttp networkHttp;
    HashMap<String, String> myRequest = new HashMap<>();
    GetCatalogFromServer getCatalog;
    // pref
    SharedPreferences sPrefGet;

    Context ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        washListRender.clear();
        washListOriginal.clear();
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ctx = getContext();
        /**
         * View section
         */
        View view = inflater.inflate(R.layout.client_fragment_one_catalog, container, false);
        mainLayout = view.findViewById(R.id.fragment_one_catalog_base_layout); // get layout for add or remove view from code
        initializeListCatalog(view); // initialize all view
        changeStateView(false); // set invisible list for wait downloading data
        /**
         * Make and send request for get Catalog form server
         */
        sPrefGet = DependenciesFactory.getPreferencesGet();
        login = sPrefGet.getString(MySettings.CLIENT_LOGIN_PREFERENCE, "null");
        model = DeviceHardware.getDeviceName();
        myRequest.put(MySettings.CLIENT_SIGN_LOGIN_REQUEST, login);
        myRequest.put(MySettings.CLIENT_SIGN_DEVICE_UID_REQUEST, model);
        getCatalog = new GetCatalogFromServer();
        getCatalog.execute();

        /**
         * Click on item section
         */
        catalogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goAboutWashActivity(washListRender.get(position).id, washListRender.get(position).shortName,
                                    washListRender.get(position).description, washListRender.get(position).rating,
                                    washListRender.get(position).minPrice, washListOriginal.get(position).washAddress,
                                    washListRender.get(position).phone, washListRender.get(position).image,
                                    washListRender.get(position).latitude, washListRender.get(position).longitude,
                                    washListRender.get(position).loginWash);
               /* Log.d(MySettings.LOG_TAG, "id=" + washListRender.get(position).id);
                Log.d(MySettings.LOG_TAG, "shortName=" + washListRender.get(position).shortName);
                Log.d(MySettings.LOG_TAG, "description=" + washListRender.get(position).description);
                Log.d(MySettings.LOG_TAG, "rating=" + washListRender.get(position).rating);
                Log.d(MySettings.LOG_TAG, "minPrice=" + washListRender.get(position).minPrice);
                Log.d(MySettings.LOG_TAG, "washAddress=" + washListRender.get(position).washAddress);
                Log.d(MySettings.LOG_TAG, "phone=" + washListRender.get(position).phone);
                Log.d(MySettings.LOG_TAG, "image=" + washListRender.get(position).image);
                Log.d(MySettings.LOG_TAG, "latitude=" + washListRender.get(position).latitude);
                Log.d(MySettings.LOG_TAG, "longitude=" + washListRender.get(position).longitude); */
            }
        });

        /**
         * Repeat load button
         */
        catalogRepeatLoadButton.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            changeStateView(false);
            getCatalog = null;
            getCatalog = new GetCatalogFromServer();
            getCatalog.execute();
        }});

        /**
         *  Search section
         */
        searchWash.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText.toString());
                return false;
            }
        });


        return view;
    }
    /**
     * Live search on list
     */
    private void searchList(String strSearch){
        washListRender.clear();
        if (strSearch.length() > 0) {
            for (int i = 0; i < washListOriginal.size(); i++) {
                // search item on shortName or washAddress
                if (
                        washListOriginal.get(i).shortName.toUpperCase().contains(strSearch.toUpperCase()) ||
                        washListOriginal.get(i).washAddress.toUpperCase().contains(strSearch.toUpperCase()))
                {
                    // get filtered SearchList
                   washListRender.add(new CatalogItem(
                           washListOriginal.get(i).id,
                           washListOriginal.get(i).shortName,
                            washListOriginal.get(i).description,
                            washListOriginal.get(i).rating,
                            washListOriginal.get(i).minPrice,
                            washListOriginal.get(i).washAddress,
                            washListOriginal.get(i).phone,
                           washListOriginal.get(i).image,
                           washListOriginal.get(i).latitude,
                           washListOriginal.get(i).longitude,
                           washListOriginal.get(i).loginWash));
                }
            }
        }
        else {
            // return to original WashList
            for (int i = 0; i < washListOriginal.size(); i++) {
                washListRender.add(new CatalogItem(
                        washListOriginal.get(i).id,
                        washListOriginal.get(i).shortName,
                        washListOriginal.get(i).description,
                        washListOriginal.get(i).rating,
                        washListOriginal.get(i).minPrice,
                        washListOriginal.get(i).washAddress,
                        washListOriginal.get(i).phone,
                        washListOriginal.get(i).image,
                        washListOriginal.get(i).latitude,
                        washListOriginal.get(i).longitude,
                        washListOriginal.get(i).loginWash));
            }
        }
        try {
            catalogAdapter.notifyDataSetChanged();
        }
        catch (Exception notifyError){
            notifyError.printStackTrace();
        }
    }

    /**
     * Init List
     */
    private void initializeListCatalog(View view){
        searchWash = view.findViewById(R.id.searchWash);
        progressLoadCatalog = view.findViewById(R.id.catalogProgressLoad);
        catalogListView = view.findViewById(R.id.catalogList);
        catalogRepeatLoadButton = view.findViewById(R.id.catalogRepeatLoadButton);
    }

    private void changeStateView(boolean state){
        if (!state){
            progressLoadCatalog.setVisibility(View.VISIBLE);
            catalogListView.setVisibility(View.INVISIBLE);
            catalogRepeatLoadButton.setVisibility(View.INVISIBLE);
        }
        else {
            mainLayout.removeView(progressLoadCatalog);
            searchWash.setVisibility(View.VISIBLE);
            catalogListView.setVisibility(View.VISIBLE);
        }
    }

    private void makeListView(){
        /**
         * Generate test data
         */
        //fillData(); // generate test data
        /**
         * Generate valid data
         */
        makeObjFromJson(response); // washListOriginal is complete
        /**
         * Make list adapter from test data
         */
        washListRender = new ArrayList<>(washListOriginal);
        if (getActivity() != null) {
            catalogAdapter = new CatalogItemAdapter(getContext(), washListRender);
            catalogListView.setAdapter(catalogAdapter);
        }
    }

    private void sendToast(String text) {
        Toast toast = Toast.makeText(getContext(),
                text, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Go About Wash activity for receive full info about wash
     */
    private void goAboutWashActivity(String id, String short_name, String description,
                                     String rating, String minPrice, String washAddress,
                                     String phone, String image, String latitude, String longitude, String loginWash){
        Intent intent = new Intent(getContext(), AboutWash.class);
        intent.putExtra(MySettings.LOGIN, login);
        intent.putExtra(MySettings.WASH_ID_NAME, id);
        intent.putExtra(MySettings.WASH_SHORT_NAME_NAME, short_name);
        intent.putExtra(MySettings.WASH_DESCRIPTION_NAME, description);
        intent.putExtra(MySettings.WASH_RATING_NAME, rating);
        intent.putExtra(MySettings.WASH_MIN_PRICE_NAME, minPrice);
        intent.putExtra(MySettings.WASH_ADDRESS_NAME, washAddress);
        intent.putExtra(MySettings.WASH_PHONE_NAME, phone);
        intent.putExtra(MySettings.WASH_IMAGE_NAME, image);
        // GPS locate
        intent.putExtra(MySettings.WASH_LATITUDE, latitude);
        intent.putExtra(MySettings.WASH_LONGITUDE, longitude);

        intent.putExtra(MySettings.WASH_LOGIN, loginWash);

        startActivity(intent);
    }

    /**
     * This method call from asyncTask thread end
     */
    private void callFromAsyncTaskEnd(){
        try {
            response = getCatalog.get(); // receive data from server
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (!response.equals("null")){
            makeListView(); // make catalog
            changeStateView(true); // change state to progress bar invisible and catalogList visible
            mainLayout.removeView(catalogRepeatLoadButton);
            mainLayout.removeView(progressLoadCatalog);
        }
        else {
            sendToast(getResources().getString(R.string.signInUserToastErrorInet));
            catalogRepeatLoadButton.setVisibility(View.VISIBLE);
            searchWash.setVisibility(View.INVISIBLE);
            progressLoadCatalog.setVisibility(View.INVISIBLE);
        }
//        Log.d(MySettings.LOG_TAG, "TUS=" +  makeObjFromJson(response).get(1).short_name);
    }

    /**
     * Parse json response from server
     */
    private ArrayList<JsonCatalogReceive> makeObjFromJson(String response) {
        ArrayList<JsonCatalogReceive> jsonCatalog = new ArrayList<>();
        try {
            JSONArray mJsonArray = new JSONArray(response);
            JSONObject mJsonObject;
            for (int i = 0; i < mJsonArray.length(); i++) {
                mJsonObject = mJsonArray.getJSONObject(i);
                // for all data
                jsonCatalog.add(new JsonCatalogReceive(
                        mJsonObject.get("id").toString(),
                        mJsonObject.get("short_name").toString(),
                        mJsonObject.get("phone").toString(),
                        mJsonObject.get("description").toString(),
                        mJsonObject.get("rating").toString(),
                        mJsonObject.get("minPrice").toString(),
                        mJsonObject.get("washAddress").toString(),
                        mJsonObject.get("image").toString(),

                        mJsonObject.get("latitude").toString(),
                        mJsonObject.get("longitude").toString(),
                        mJsonObject.get("loginWash").toString()));
                // for  catalogList
                washListOriginal.add(new CatalogItem(
                        mJsonObject.get("id").toString(),
                        mJsonObject.get("short_name").toString(),
                        mJsonObject.get("description").toString(),
                        mJsonObject.get("rating").toString(),
                        mJsonObject.get("minPrice").toString(),
                        mJsonObject.get("washAddress").toString(),
                        mJsonObject.get("phone").toString(),
                        mJsonObject.get("image").toString(),
                        mJsonObject.get("latitude").toString(),
                        mJsonObject.get("longitude").toString(),
                        mJsonObject.get("loginWash").toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonCatalog;
    }

    /**
     * This inner Class for make NetworkHttp worker and update UI
     */
    class GetCatalogFromServer extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                networkHttp = new NetworkHttp(MySettings.CATALOG_SCRIPT);
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