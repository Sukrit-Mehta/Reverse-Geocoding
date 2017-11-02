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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    TextView textTV,textTV0;
    LocationManager locationManager;LocationListener locationListener;

    Double latitude;
    Double longitude;
    String latStr="",longStr="";
    boolean network_enabled=false;
    Button btn, btn2;
    ProgressDialog progressDialog;
    Boolean locationFound = false;

    String locality = "", city, state, country, postalCode;

    public static final Integer PLACE_AUTOCOMPLETE_REQUEST_CODE = 1001;

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
        btn2 = (Button) findViewById(R.id.button2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationFound = false;
                fetchLocation();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findPlace();
            }
        });
    }

    private void fetchLocation() {

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

                if (!locationFound) {
                    Log.d("TAG", "onLocationChanged: " + location.toString());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    latStr = String.valueOf(latitude);
                    longStr = String.valueOf(longitude);
                    textTV0.setText(latStr + " , " + longStr);
                    progressDialog.dismiss();
                    locationFound = true;
                    VolleyFunction(latStr, longStr);
                }
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
                            JSONArray jsonArray = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");
                            for (Integer i=0;i<jsonArray.length();i++) {
                                if (jsonArray.getJSONObject(i).getString("types").equals("[\"locality\",\"political\"]")) {
                                    city = jsonArray.getJSONObject(i).getString("long_name");
                                }
                                else if (jsonArray.getJSONObject(i).getString("types").equals("[\"administrative_area_level_1\",\"political\"]")) {
                                    state = jsonArray.getJSONObject(i).getString("long_name");
                                }
                                else if (jsonArray.getJSONObject(i).getString("types").equals("[\"country\",\"political\"]")) {
                                    country = jsonArray.getJSONObject(i).getString("long_name");
                                }
                                else if (jsonArray.getJSONObject(i).getString("types").equals("[\"postal_code\"]")) {
                                    postalCode = jsonArray.getJSONObject(i).getString("long_name");
                                }
                                else {
                                    locality = locality + jsonArray.getJSONObject(i).getString("long_name") + " ";
                                }
                            }
                            Log.d("checkkkk", "onResponse: " + city);
                            Log.d("checkkkk", "onResponse: " + state);
                            Log.d("checkkkk", "onResponse: " + country);
                            Log.d("checkkkk", "onResponse: " + postalCode);
                            Log.d("checkkkk", "onResponse: " + locality);

                            String result = "";
                            result = result + "Locality : " + locality + "\n";
                            result = result + "City : " + city + "\n";
                            result = result + "State : " + state + "\n";
                            result = result + "Country : " + country + "\n";
                            result = result + "Postal Code : " + postalCode;
//                            result = result + "\nFull Address : " + response.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                            textTV.setText(result);
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
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    public void findPlace() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {

        } catch (GooglePlayServicesNotAvailableException e) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                latStr=String.valueOf(place.getLatLng().latitude);
                longStr=String.valueOf(place.getLatLng().longitude);
                textTV0.setText(latStr+" , "+longStr);

                VolleyFunction(latStr, longStr);

            }
            else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("TAG", status.getStatusMessage());

            }
            else if (resultCode == RESULT_CANCELED) {
            }
        }
    }
}