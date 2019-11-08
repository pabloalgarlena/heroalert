package com.example.heroalert;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, FetchDataService.class));


        int PERMISSION_ALL = 1;

        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.WAKE_LOCK
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        ArrayList<Action> data = new ArrayList<>();
        final ArrayList<double[]> coords = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String json = prefs.getString("Json", "");
        JSONObject jsonOb;
        try {
            jsonOb = new JSONObject(json);
            JSONArray actions =  jsonOb.getJSONArray("actions");
            for(int i = 0; i < actions.length(); i++) {
                JSONArray action = actions.getJSONArray(i);
                double easting = action.getDouble(1);
                double northing = action.getDouble(2);
                String utmString = String.format("30 N %s %s", easting, northing);
                UTM2Deg deg = new UTM2Deg(utmString);
                double destinationLatitude = deg.latitude;
                double destinationLongitude = deg.longitude;
                double[] coord = {destinationLatitude, destinationLongitude};
                coords.add(coord);

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder. getFromLocation(destinationLatitude, destinationLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    Action actionObj = new Action(action.getString(3), address);
                    data.add(actionObj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        class ActionAdapter extends ArrayAdapter<Action> {

            ArrayList<Action> dat;

            public ActionAdapter(Context context, ArrayList<Action> dat) {
                super(context, R.layout.listitem_address, dat);
                this.dat = dat;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View item = inflater.inflate(R.layout.listitem_address, null);

                TextView title = item.findViewById(R.id.title);
                title.setText(dat.get(position).getActionTitle());

                TextView address = item.findViewById(R.id.address);
                address.setText(dat.get(position).getAddress());

                return(item);
            }
        }

        ActionAdapter adapter = new ActionAdapter(this, data);
        ListView optionList = findViewById(R.id.LstOpciones);
        optionList.setAdapter(adapter);

        optionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                double destinationLatitude = coords.get(position)[0];
                double destinationLongitude = coords.get(position)[1];
                String uri = "http://maps.google.com/maps?q=loc:" + destinationLatitude + "," + destinationLongitude;
                Log.d("Maps uri", uri);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });



    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
