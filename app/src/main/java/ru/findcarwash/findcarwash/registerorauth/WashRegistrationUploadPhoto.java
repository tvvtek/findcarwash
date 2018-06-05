package ru.findcarwash.findcarwash.registerorauth;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.WashWorkScreens.WashChatList;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.settings.MySettings;


public class WashRegistrationUploadPhoto extends AppCompatActivity {

    private String whoRun;
    private String myLogin;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    // View
    ImageView photo;
    Button washUploadBtn, washSkipBtn;
    // IMAGE
    File directory;
    Uri makePhotoUri;
    private final int SELECT_PICTURE = 1;
    private final int REQUEST_CODE_PHOTO = 2;
    private volatile String selectedImagePath;
    // Task for download
    UploadImageToServer uploadImageToServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wash_registration_upload_photo);
        createDirectory();
        verifyStoragePermissions(this);
        initializeVIew();
        changeStateView(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        getSupportActionBar().setTitle(getResources().getString(R.string.washRegisterPhotoTitle));
        whoRun = getIntent().getStringExtra("whoRun"); // WashRegistration or WashChatList for correctly go back
        myLogin = getIntent().getStringExtra("myLogin");
    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void changeStateView(boolean state){
        if (state){
            washUploadBtn.setVisibility(View.VISIBLE);
            washUploadBtn.setEnabled(true);
        }
        else {
            washUploadBtn.setVisibility(View.INVISIBLE);
            washUploadBtn.setEnabled(false);
        }
    }

    private void initializeVIew(){
        photo = findViewById(R.id.washRegisterImageView);
        washUploadBtn = findViewById(R.id.washUploadBtn);
        washSkipBtn = findViewById(R.id.washSkipBtn);
    }

    private void sendToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Upload button
     * @param view
     */
    public void washUploadBtn(View view) {
        uploadImageToServer = new UploadImageToServer();
        uploadImageToServer.execute();
    }

    /**
     * Skip button
     * @param view
     */
    public void washSkipBtn(View view) {
        goNextActivity();
    }

    /**
     * Go to camera
     * @param view
     */
    public void washRegisterMakePhotoBtn(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }


    /**
     * Go to gallery
     * @param view
     */
    public void washRegisterSelectImageBtn(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    /**
     * Start after start Intent Camera or Gallery
     * @param requestCode event code
     * @param resultCode success or NO success
     * @param data data from intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE){
            if (resultCode == RESULT_OK)
             {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                photo.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                changeStateView(true);
            }
        }
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                selectedImagePath = getPath(makePhotoUri);
                photo.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                changeStateView(true);
            }
        }
    }

    /**
     * Generate local URI before write photo from camera
     * @return URI
     */
    private Uri generateFileUri() {
        File file = null;
        file = new File(directory.getPath() + "/" + "photo_"
                + System.currentTimeMillis() + ".jpg");
        makePhotoUri = Uri.fromFile(file);
        return makePhotoUri;
    }

    /**
     * Create local subdir on storage
     */
    private void createDirectory() {
        directory = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "FindCarWash");
        if (!directory.exists())
            directory.mkdirs();
    }

    /**
     * Get local path ro Photo
     * @param uri Uri to photo
     * @return path
     */
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }


    /**
     * Method for change activity
     */
    private void goNextActivity(){
        if (whoRun != null && whoRun.equals("WashRegistration")){
            finish();
            Intent washWorkScreen = new Intent(WashRegistrationUploadPhoto.this, WashSingIn.class);
            startActivity(washWorkScreen);
        }
        else if (whoRun != null && whoRun.equals("WashChatList")) {
            finish();
            Intent washWorkScreen = new Intent(WashRegistrationUploadPhoto.this, WashChatList.class);
            startActivity(washWorkScreen);
        }
        else {
            sendToast(getResources().getString(R.string.washRegisterLoadImageToastErrorActivity));
        }
    }


    /**
     * Class Async Task for download photo to server
     */
    private class UploadImageToServer extends AsyncTask<String, Integer, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(WashRegistrationUploadPhoto.this);
            progressDialog.setMessage(getResources().getString(R.string.washRegisterLoadImageDialog));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... str) {
            String res = null;
            try {
                File sourceFile = new File(selectedImagePath);
                final MediaType MEDIA_TYPE = MediaType.parse("image/*");
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("nameImage", myLogin + "_NEW.jpeg") // name
                        .addFormDataPart("image", "tmp.jpeg", RequestBody.create(MEDIA_TYPE, sourceFile)) // body photo
                        .build();
                Log.d(MySettings.LOG_TAG, "myLogin=" + myLogin);

                Request request = new Request.Builder()
                        .url(MySettings.URL + MySettings.REGISTER_WASH_UPLOAD_IMAGE_SCRIPT)
                        .post(requestBody)
                        .build();

                OkHttpClient client = DependenciesFactory.getOkHttpClient();
                Response response = client.newCall(request).execute();
                res = response.body().string();
                return res;

            } catch (UnknownHostException | UnsupportedEncodingException e) {
              //  Log.e(MySettings.LOG_TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
               // Log.e(MySettings.LOG_TAG, "Other Error: " + e);
            }
            return res;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (progressDialog != null)
                progressDialog.dismiss(); // cancel dialog
            if (response != null && response.equals("1") ) {
                try {
                    goNextActivity();
                } catch (Exception e) {
                    sendToast(getResources().getString(R.string.washRegisterLoadImageToastError));
                    e.printStackTrace();
                }
            }
            else {
            }
        }
    }
}

