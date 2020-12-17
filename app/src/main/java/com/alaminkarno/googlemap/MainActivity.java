package com.alaminkarno.googlemap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    EditText searchET;
    private String apiKey = "AIzaSyDS7cI5AUV0QZVkO6ellhaGY7kaKTqMPR0";
    private static int AUTOCOMPLETE_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        autoComplete();

        permission();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        supportMapFragment.getMapAsync(this);
    }

    private void autoComplete() {

        searchET.setFocusable(false);
        searchET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fields).build(MainActivity.this);

                startActivityForResult(intent,AUTOCOMPLETE_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == AUTOCOMPLETE_CODE){
            if(resultCode == RESULT_OK){

                Place place = Autocomplete.getPlaceFromIntent(data);

                searchET.setText(place.getAddress());
            }
            else if(resultCode == AutocompleteActivity.RESULT_ERROR){

                Status status = Autocomplete.getStatusFromIntent(data);

                Toast.makeText(this, "Error: "+status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
            else if(resultCode == RESULT_CANCELED){
                //
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init() {

        searchET = findViewById(R.id.searchETid);

        Places.initialize(getApplicationContext(), apiKey);
    }

    private void permission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},0);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map  = googleMap;

        LatLng bd = new LatLng(23.721959028788763, 90.38730230281193);

        googleMap.addMarker(new MarkerOptions().position(bd).title("Dhaka").snippet("Dhaka is the capital of Bangladesh"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bd, 16));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;

            }
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);


       // searchLocation();

    }

    private void searchLocation() {

        searchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER
                )
                {
                    String search = searchET.getText().toString();

                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    List<Address> addressList = new ArrayList<>();

                    try {
                        addressList = geocoder.getFromLocationName(search,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(addressList.size() >0){

                        Address address = addressList.get(0);

                        LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());

                        map.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(0))
                                .snippet(address.getCountryName()+", "+address.getPostalCode()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    }
                }

                return false;
            }
        });
    }

    private void currentLocation() {

        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            
            return;
        }
        Task location = fusedLocationProviderClient.getLastLocation();
        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Location currentLocation = (Location) task.getResult();

                map.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),14));
            }
        });

    }

    public void myLocation(View view) {

        currentLocation();
    }
}