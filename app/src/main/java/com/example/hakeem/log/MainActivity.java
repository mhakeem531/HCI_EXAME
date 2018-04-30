package com.example.hakeem.log;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.hakeem.log.app.AppConfig;
import com.example.hakeem.log.app.AppController;
import com.example.hakeem.log.helper.RequestHandler;
import com.example.hakeem.log.helper.SQLiteHandler;
import com.example.hakeem.log.helper.SessionManager;
import com.example.hakeem.log.helper.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    public static final String UPLOAD_KEY = "image";
    public static final String UPLOAD_EMAIL_KEY = "mail";
    private ProgressDialog pDialog;


    private SQLiteHandler db;
    private SessionManager session;
    private Bitmap newProfileImge;
    public static final int GET_FROM_GALLERY = 3;
    private User ourUser;

    private ImageView myProfile;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView txtName = findViewById(R.id.name);
        TextView txtEmail = findViewById(R.id.email);
        Button btnLogout = findViewById(R.id.btnLogout);
        myProfile = findViewById(R.id.show_profile);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
        ourUser = db.getUserDetails2();
        // Displaying the user details on the screen
        txtName.setText(ourUser.getName());
        txtEmail.setText(ourUser.getEmail());


//        byte[] outImage = ourUser.getProfileImage();
//        ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
//        Bitmap theImage = BitmapFactory.decodeStream(imageStream);
//        myProfile.setImageBitmap(theImage);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfileImage();
            }
        });
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, GET_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                newProfileImge = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                myProfile.setImageBitmap(newProfileImge);

                //convert bitmap to byt
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                newProfileImge.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                byte imageInByte[] = stream.toByteArray();

                Log.e("mail is ", ourUser.getEmail());

                if (db.updateUserProfileImage(ourUser.getEmail(), imageInByte) > 0) {
                    Toast.makeText(getApplicationContext(), "profile updated!", Toast.LENGTH_LONG).show();
                    // TODO upload image to server
                    //  uploadImage();


                    uploadProfileImage(ourUser.getEmail(), newProfileImge);

                } else {
                    Toast.makeText(getApplicationContext(), "didn't updated!", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //  Now we have the Image which is to bet uploaded in bitmap.
//    We will convert this bitmap to base64 string
//    So we will create a method to convert this bitmap to base64 string

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    private void uploadImage() {

        class UploadImage extends AsyncTask<Bitmap, Void, String> {

            private RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("after is ", s);
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String uploadImage = getStringImage(bitmap);

                HashMap<String, String> data = new HashMap<>();
                data.put(UPLOAD_KEY, uploadImage);
                data.put(UPLOAD_EMAIL_KEY, ourUser.getEmail());

                String result = rh.sendPostRequest(AppConfig.URL_UPLOAD_PROFILE_PHOTO, data);
                return result;
            }
        }
        UploadImage ui = new UploadImage();
        ui.execute(newProfileImge);

    }


    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void uploadProfileImage(final String email, final Bitmap image) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("uploading ...");
        showDialog();

        //  final String uploadImage = getStringImage(image);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPLOAD_PROFILE_PHOTO, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();


                if (Objects.equals(response, "Image Uploaded Successfully")) {
                    Toast.makeText(getApplicationContext(),
                            "changed Successfully " + response, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "can't upload it " + response, Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage() + "hakeem", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                String uploadImage = getStringImage(image);
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("mail", email);
                params.put("image", uploadImage);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    public void invokeProfileImageFromServer(final String userEmail) {

        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("fetch image ...");
        showDialog();

        //  final String uploadImage = getStringImage(image);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_INVOKE_PROFILE_PHOTO, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "invoked: " + response);
                hideDialog();
//                StringBuilder x = new StringBuilder();
//                for(int i = 0 ; i < 5; i++){
//                    x.append(response.charAt(i));
//                }

                byte[] encodeByte = Base64.decode(response, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

               myProfile.setImageBitmap(bitmap);







            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "invockation  Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage() + "hakeem", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("mail", userEmail);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);


    }

    public void come(View view) {
        invokeProfileImageFromServer(ourUser.getEmail());


    }
}
