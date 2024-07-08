package com.example.googlemapactivity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback , RouteListener {
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap myMap;
    //    private SearchView searchView;
    private EditText latitudeInput;
    private EditText longitudeInput;
    private TextView resultText;
    private LatLng destinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        ViewMapByType();
        latitudeInput = findViewById(R.id.latitude_input);
        longitudeInput = findViewById(R.id.longitude_input);
        Button submitButton = findViewById(R.id.btnSubmit);
        resultText = findViewById(R.id.txtResulst);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String latStr = latitudeInput.getText().toString();
                String lonStr = longitudeInput.getText().toString();
                getMapByCoordinates(latStr,lonStr);
            }
        });
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                String location = searchView.getQuery().toString();
//                List<Address> addressList = null;
//                if (location != null) {
//                    Geocoder geocoder = geo
//                }
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                return false;
//            }
//        });

//        Đừng xóa cmt 2 dòng dưới để test
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
//                    mapFragment.getMapAsync(MainActivity.this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());// Hanoi(21,105):
//        LatLng myLocation = new LatLng(21,105); // Tọa độ Hà Nội :D
        MarkerOptions options = new MarkerOptions().position(myLocation).title("My Location");
        myMap.addMarker(options);
        myMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                myMap.clear();
                destinationLocation = latLng;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(Utils.bitmapDescriptorFromVector(MainActivity.this, R.drawable.location_foreground));
                myMap.addMarker(markerOptions);
                getRoutePoints(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), latLng);
            }
        });

//        myMap.getUiSettings().setZoomGesturesEnabled(false);
//        myMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Please allow permission to get current location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getMapByCoordinates(String latitudeInput, String longitudeInput) {
        if (latitudeInput.isEmpty() || longitudeInput.isEmpty()) {
            resultText.setText("Please enter valid coordinates.");
            return;
        }

        double latitude = Double.parseDouble(latitudeInput);
        double longitude = Double.parseDouble(longitudeInput);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                LatLng location = new LatLng(latitude, longitude);
                MarkerOptions options = new MarkerOptions().position(location).title("Location Found");
                myMap.addMarker(options);
                myMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            } else {
                resultText.setText("No address found for the given coordinates.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultText.setText("Geocoding failed. Check your network connection.");
        }
    }

    private void ViewMapByType() {
        Spinner mapTypeSpinner = findViewById(R.id.mapTypeSpinner);
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (myMap != null) {
                    switch (position) {
                        case 0:
                            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            break;
                        case 1:
                            myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            break;
                        case 2:
                            myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
                mapFragment.getMapAsync(MainActivity.this);
            }
        });
    }

    public void getRoutePoints(LatLng start, LatLng end) {
            RouteDrawing routeDrawing = new RouteDrawing.Builder()
                    .context(MainActivity.this)
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this).alternativeRoutes(true)
                    .waypoints(start, end)
                    .build();
            routeDrawing.execute();
    }

    @Override
    public void onRouteFailure(ErrorHandling e) {
        Log.w("TAG", "onRoutingFailure: " + e);
    }

    @Override
    public void onRouteStart() {
        Log.d("TAG", "yes started");
    }

    @Override
    public void onRouteSuccess(ArrayList<RouteInfoModel> list, int indexing) {
        PolylineOptions polylineOptions = new PolylineOptions();
        ArrayList<Polyline> polylines = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i == indexing) {
//                Log.e("TAG", "onRoutingSuccess: routeIndexing" + routeIndexing);
                polylineOptions.color(Color.BLACK);
                polylineOptions.width(12);
                polylineOptions.addAll(list.get(indexing).getPoints());
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                Polyline polyline = myMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }
        }
    }

    @Override
    public void onRouteCancelled() {
        Log.d("TAG", "route canceled");
    }
}