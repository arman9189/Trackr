package com.example.root.trackr;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TrackFriend extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location globalLocation;
    private User currentUser;
    Bundle extras;
    private SharedPreferences sharedPreferencesProfileInformation;
    TimerTask doAsynchronousTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        extras = getIntent().getExtras();
        Log.d("---------------------", String.valueOf(extras.getInt("id")));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_friend);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initialize();
        callAsynchronousTask();
    }


    private void initialize(){
        sharedPreferencesProfileInformation = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        doAsynchronousTask.cancel();
        finish();
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
//                            getLocation();


                            {
                                Gson gson = new Gson();
                                String json = sharedPreferencesProfileInformation.getString("currentUser", "");
                                currentUser = gson.fromJson(json, User.class);



                                User requestUser = new User("0", "0", extras.getInt("id"), "0", "0", currentUser.getAuthToken());
                                String postUser = gson.toJson(requestUser, User.class);

                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                        Request.Method.POST,
                                        AppConfig.MAP_URL_GET,
                                        postUser,
                                        new Response.Listener<JSONObject>() {
                                            Location location;
                                            @Override
                                            public void onResponse(JSONObject jsonObject) {
                                                try {
                                                    Log.d("TAG", "onResponse: ----------------------");
                                                    Log.d("RESPONSE", jsonObject.toString());
                                                    Double latitude,longitude;
                                                    latitude = Double.valueOf(jsonObject.getString("latitude"));
                                                    longitude = Double.valueOf(jsonObject.getString("longitude"));
                                                    Log.d("latitude--", jsonObject.getString("latitude"));
                                                    try {
                                                        //                                  globalLocation.setLatitude(location.getLatitude())
                                                        //                                  globalLocation.setLongitude(location.getLongitude());
                                                        mMap.clear();
                                                        LatLng latLng = new LatLng(latitude,longitude);
                                                        mMap.addMarker(new MarkerOptions().position(latLng).title("Your friend is here"));
                                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                                                        //                                 {
                                                        //                                    Toast.makeText(TrackFriend.this, "User has gone offline...", Toast.LENGTH_SHORT).show();
                                                        //                                    goneOffline();
                                                        //                                }

                                                    } catch (NullPointerException ne) {
                                                        ne.printStackTrace();
                                                        Toast.makeText(TrackFriend.this, "unable to fetch location from database", Toast.LENGTH_SHORT).show();
                                                    }

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(TrackFriend.this, "Volley Error", Toast.LENGTH_SHORT).show();
                                                error.printStackTrace();
                                            }
                                        }
                                ){
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        super.getHeaders();
                                        HashMap<String, String> headers = new HashMap<String, String>();
                                        headers.put("Content-Type", "application/json");
                                        headers.put("charset", "utf-8");
                                        return headers;
                                    }
                                };

                                int socketTimeout = 10000;//10 seconds - change to what you want
                                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                jsonObjectRequest.setRetryPolicy(policy);
                                MySingleton.getInstance(TrackFriend.this).addToRequestQueue(jsonObjectRequest);
                            }



                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000);
    }



//    private void getLocation(){
//        //TODO volley code
//        Gson gson = new Gson();
//        String json = sharedPreferencesProfileInformation.getString("currentUser", "");
//        currentUser = gson.fromJson(json, User.class);
//
//
//
//        User requestUser = new User("0", "0", extras.getInt("id"), "0", "0", currentUser.getAuthToken());
//        String postUser = gson.toJson(requestUser, User.class);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//                Request.Method.POST,
//                AppConfig.MAP_URL_GET,
//                postUser,
//                new Response.Listener<JSONObject>() {
//                    Location location;
//                    @Override
//                    public void onResponse(JSONObject jsonObject) {
//                        try {
//                            Log.d("TAG", "onResponse: ----------------------");
//                            Log.d("RESPONSE", jsonObject.toString());
//                            location.setLatitude(Double.parseDouble(jsonObject.getString("latitude")));
//                            location.setLongitude(Double.parseDouble(jsonObject.getString("longitude")));
//                            Log.d("onLatitude: ", String.valueOf(location.getLatitude()));
//                            Log.d("onLongitude: ", String.valueOf(location.getLongitude()));
//                            Toast.makeText(TrackFriend.this,
//                                             String.valueOf(location.getLongitude())
//                                            +" "
//                                            +String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT).show();
////                            Gson gson = new Gson();
////                            User responseUser = gson.fromJson(jsonObject.toString(), User.class);
//                            try {
//                                    globalLocation = location;
//                                    Toast.makeText(TrackFriend.this, String.valueOf(location.getLatitude())+" "+String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
////                                  globalLocation.setLatitude(location.getLatitude())
////                                  globalLocation.setLongitude(location.getLongitude());
//                                    mMap.clear();
//                                    LatLng latLng = new LatLng(globalLocation.getLatitude(),globalLocation.getLongitude());
//                                    mMap.addMarker(new MarkerOptions().position(latLng).title("Your friend is here"));
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//
////                                 {
////                                    Toast.makeText(TrackFriend.this, "User has gone offline...", Toast.LENGTH_SHORT).show();
////                                    goneOffline();
////                                }
//
//                            } catch (NullPointerException ne) {
//                                ne.printStackTrace();
//                            Toast.makeText(TrackFriend.this, "unable to fetch location from database", Toast.LENGTH_SHORT).show();
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(TrackFriend.this, "Volley Error", Toast.LENGTH_SHORT).show();
//                        error.printStackTrace();
//                    }
//                }
//        ){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                super.getHeaders();
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/json");
//                headers.put("charset", "utf-8");
//                return headers;
//            }
//        };
//
//        int socketTimeout = 10000;//10 seconds - change to what you want
//        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        jsonObjectRequest.setRetryPolicy(policy);
//        MySingleton.getInstance(TrackFriend.this).addToRequestQueue(jsonObjectRequest);
//    }



    private void goneOffline(){
        //TODO finish activity
    }

}