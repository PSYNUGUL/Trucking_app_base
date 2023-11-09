    package com.example.lab6;

    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.os.Bundle;
    import android.util.Log;

    import androidx.annotation.NonNull;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.fragment.app.FragmentActivity;
    import androidx.localbroadcastmanager.content.LocalBroadcastManager;

    import com.android.volley.RequestQueue;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;

    import com.google.firebase.FirebaseApp;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    import java.util.HashMap;
    import java.util.Map;

    //API request imports
    import com.android.volley.Request;
    import com.android.volley.Response;
    import com.android.volley.VolleyError;
    import com.android.volley.toolbox.JsonObjectRequest;
    import com.android.volley.toolbox.Volley;

    import org.json.JSONArray;
    import org.json.JSONObject;


    public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

        private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
        private GoogleMap mMap;
        private Marker currentMarker;
        private LocationManager locationManager;
        private static final String NWS_API_BASE_URL = "https://api.weather.gov/";

        private static String forecastURL = null;

        private static int[] temperature = {0,0};
        private NetworkRequestHandler requestHandler;


        // BroadcastReceiver to handle location updates
        private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("location-update")) {
                    double latitude = intent.getDoubleExtra("latitude", 0.0);
                    double longitude = intent.getDoubleExtra("longitude", 0.0);
                    updateMarkerPosition(latitude, longitude);
                }
            }
        };

        DatabaseReference mDatabase;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            FirebaseApp.initializeApp(this);
            requestHandler = NetworkRequestHandler.getInstance(this);
            mDatabase = FirebaseDatabase.getInstance().getReference("recordings");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }


        private void recordToFirebase(long timestamp, Location location) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> data1 = new HashMap<>();
            data.put("timestamp", timestamp);
            data.put("latitude", location.getLatitude());
            data.put("longitude", location.getLongitude());
            data1.put("temperature this afternoon (°F)", temperature[0]);
            data1.put("temperature tonight (°F)", temperature[1]);
            // put humidity

            // put rain percentage

            // ...


            mDatabase.child("locations").push().setValue(data);
            mDatabase.child("forecast data").push().setValue(data1);


        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            LatLng defaultLocation = new LatLng(0, 0);
            mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
        }

        @Override
        public void onLocationChanged(Location location) {
            long timestamp = System.currentTimeMillis(); // or use location.getTime() if available

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Broadcast this data
            Intent intent = new Intent("location-update");
            intent.putExtra("longitude", longitude);
            intent.putExtra("latitude", latitude);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // Update marker position
            updateMarkerPosition(latitude, longitude);

            // Get data from NWS API
            makeNetworkRequest(latitude, longitude);

            // Record the data to the firebase database
            recordToFirebase(timestamp, location);

        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            // Handle location permission result if needed
        }

        // Method to construct the forecast URL based on latitude and longitude
        private String getData(double latitude, double longitude) {
            return NWS_API_BASE_URL + "points/" + latitude + "," + longitude;
        }

        // Method to retrieve weather data from the National Weather Service API using Volley

        private void getTemperatureData(String forecastURL) {
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Your Custom User-Agent String");

            requestHandler.fetchData(forecastURL, new NetworkRequestHandler.NetworkResponseListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        JSONArray periods = response.getJSONObject("properties").getJSONArray("periods");
                        for (int i = 0; i < periods.length(); i++) {
                            JSONObject period = periods.getJSONObject(i);
                            String name = period.getString("name");
                            if (name.equals("This Afternoon")) {
                                temperature[0] = period.getInt("temperature");
                            }
                            if (name.equals("Tonight")) {
                                temperature[1] = period.getInt("temperature");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Temperature", "Error parsing temperature data: " + e.getMessage());
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Log.e("Temperature", "Error fetching forecast data: " + error.toString());
                }
            }, headers); // Pass the custom headers to the fetchData method
        }

        // Call this method with the constructed forecast URL
        private void makeNetworkRequest(double latitude, double longitude) {
            String dataURL = getData(latitude, longitude);
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Your Custom User-Agent String");

            requestHandler.fetchData(dataURL, new NetworkRequestHandler.NetworkResponseListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        Log.d("API Response", response.toString());
                        forecastURL = response.getJSONObject("properties").optString("forecast");
                        if (forecastURL != null && !forecastURL.isEmpty()) {
                            Log.d("Forecast URL", forecastURL);
                            getTemperatureData(forecastURL);
                            //getHumidityData
                            //...
                        } else {
                            Log.e("Forecast URL", "Forecast URL is empty or null");
                        }
                    } catch (Exception e) {
                        Log.e("Forecast URL", "Error parsing forecast URL: " + e.getMessage());
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Log.e("Forecast URL", "Error fetching weather data: " + error.toString());
                }
            }, headers); // Pass the custom headers to the fetchData method
        }
        private void updateMarkerPosition(double latitude, double longitude) {
            LatLng newLocation = new LatLng(latitude, longitude);
            if (currentMarker == null) {
                currentMarker = mMap.addMarker(new MarkerOptions().position(newLocation).title("Current Location"));
            } else {
                currentMarker.setPosition(newLocation);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 50));
        }

        @Override
        protected void onResume() {
            super.onResume();
            // Start listening for location updates when the activity is resumed
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
            }
            // Register the BroadcastReceiver for location updates
            LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver,
                    new IntentFilter("location-update"));
        }

        @Override
        protected void onPause() {
            super.onPause();
            // Stop listening for location updates when the activity is paused
            locationManager.removeUpdates(this);
            // Unregister the BroadcastReceiver when the activity is paused
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
        }
    }
