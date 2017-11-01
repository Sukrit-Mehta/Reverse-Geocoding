package com.example.ishaan.reversegeocoding;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    TextView textTV,textTV0;
    LocationManager locationManager;LocationListener locationListener;

    Double latitude;
    Double longitude;
    String latStr="",longStr="";
    boolean network_enabled=false;
    Button btn;
    ProgressDialog progressDialog;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i("TAG","sabse Upar");
        Log.i("TAG","LEN:" +grantResults);
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTV= (TextView) findViewById(R.id.text);
        textTV0= (TextView) findViewById(R.id.text0);
        btn= (Button) findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLocation();
            }
        });


    }

    private void fetchLocation() {
        requestQueue = Volley.newRequestQueue(this);

        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Fetching your location....");
        progressDialog.show();
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e)
        {

        }
        if( !network_enabled) {
            // notify user
            progressDialog.dismiss();
            final AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuider.setMessage("Location is not on..");

            alertDialogBuider.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getApplicationContext().startActivity(myIntent);
                }
            });
            alertDialogBuider.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MainActivity.this, "Rejected ..!", Toast.LENGTH_SHORT).show();

                }

            });
            alertDialogBuider.show();
        }
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("TAG", "onLocationChanged: "+location.toString());
                latitude=location.getLatitude();
                longitude=location.getLongitude();

                latStr=String.valueOf(latitude);
                longStr=String.valueOf(longitude);
                textTV0.setText(latStr+" , "+longStr);
                progressDialog.dismiss();
                VolleyFunction(latStr,longStr);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    20000, 100, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("TAG", "Ask for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                Log.i("TAG", "Permission given");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        20000, 100, locationListener);
            }
        }

    }

    void VolleyFunction(String lat,String lon)
    {
        Log.d("TAG","latFunc: "+lat);
        Log.d("TAG","longFunc: "+lon);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lon+"&key=AIzaSyDxYAN8Do0UI9ZMdBeDkfsYcQcG5qBfjQY",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            Log.d("response", "onResponse: " + response.toString());
                            textTV.setText(response.toString());
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(MainActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("Yo", "onErrorResponse: "+ error.toString());
                        Toast.makeText(MainActivity.this, "Error Found", Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

}