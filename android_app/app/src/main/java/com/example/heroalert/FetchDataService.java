package com.example.heroalert;

import android.app.IntentService;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import androidx.annotation.NonNull;

public class FetchDataService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    public FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private final LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            Location location = locationList.get(0);
            for (int i=0; i<locationList.size(); i++){
                location = locationList.get(i);
                Log.d("AppLocationService", "Latitude  - " + location.getLatitude() + ", longitude  - " + location.getLongitude());
            }
            editor.putString("Latitude", String.valueOf(location.getLatitude()));
            editor.putString("Longitude", String.valueOf(location.getLongitude()));
            editor.apply();
        }
    };


    public void updateLocation(final double latitude, final double longitude) {
        Log.d("Service", "Getting actions around location degrees: " + latitude + "/" + longitude);
        Deg2UTM utm = new Deg2UTM(latitude, longitude);
        Log.d("Service", "Getting actions around location utm: " + utm.Easting + "/" + utm.Northing);
        String sex = getDefaults("sex", getApplicationContext());
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = String.format("http://linux-516376.hi.inet:8080/herometer/?easting=%s&northing=%s&genre=%s", utm.Easting, utm.Northing, sex);
        Log.d("Service", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Service", response);
                        editor.putString("Json", response);
                        editor.apply();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Service", "Error Response: " + error);
                    }
                }
        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "Ejecutandose");
        return START_STICKY; //or return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent restartService = new Intent("RestartService");
        sendBroadcast(restartService);
    }

    @Override
    public void onCreate() {
        prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        editor = prefs.edit();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.d("Location real", String.valueOf(location));
                    editor.putString("Latitude", String.valueOf(location.getLatitude()));
                    editor.putString("Longitude", String.valueOf(location.getLongitude()));
                    editor.apply();
                }
            }
        });
        fusedLocationClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Location", e.toString());
            }
        });


        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
//                Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                fusedLocationClient.getLastLocation();
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                double latitude = Double.valueOf(prefs.getString("Latitude", ""));
                double longitude = Double.valueOf(prefs.getString("Longitude", ""));

                updateLocation(latitude, longitude);
                Intent intent = new Intent(context, HeroAlert.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                ComponentName thisWidget = new ComponentName(context, HeroAlert.class);
                int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(thisWidget);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                sendBroadcast(intent);

                handler.postDelayed(runnable, 5000);
            }
        };


        handler.postDelayed(runnable, 10000);
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "none");
    }

}

