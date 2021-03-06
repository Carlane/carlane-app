package com.cherry.alok.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class LocationActivityMap extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener,
        NavigationView.OnNavigationItemSelectedListener, LocationListener, GoogleMap.OnCameraChangeListener,GoogleMap.OnMapLongClickListener,GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private Marker mapMarker;
    UniversalAsyncTask uniTask = null;
    private LocationManager locationManager;
    TextView locationText;
    BottomSheetBehavior behavior;
    Boolean launchingMapActivity ;
    Bundle services_bundle = new Bundle();
    enum AsyncActivities
    {
        NONE,
        SERVICE_VERSION,
        LOCATIONBASED_SERVICES,
    };
    AsyncActivities current_task = AsyncActivities.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.map_parent_layout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        launchingMapActivity = true;
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_grow);
        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.map_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                ImageView nav_back = (ImageView)findViewById(R.id.imageView);
                Bitmap navImg = SharedData.GetUserpic();
                if(nav_back!= null)
                {
                    if(navImg == null) {
                        LoadProfileImage lfi = new LoadProfileImage(nav_back);
                        if (lfi != null) {
                            lfi.execute();
                        }
                    }
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),navImg);
                    drawable.setCircular(true);
                    nav_back.setImageDrawable(drawable);
                    //bmImage.setImageBitmap(result);
                }

                TextView txt = (TextView)findViewById(R.id.userNameText);
                if(txt != null)
                {
                    txt.setText(SharedData.GetUserName());
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                HideOnGoingRequestIfRequired();
            }

        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.map_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*Set up the Bottom Sheet containing types of services*/
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.laymap_cordinatorLayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.location_small_bottomsheet);

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setPeekHeight(0);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {

                    case BottomSheetBehavior.STATE_DRAGGING:

                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:

                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;


                }

            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }

        });

/*        *//*Set Up the Tabs In Bottom Sheet*//*
        final ViewPager viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        //setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        //tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        SharedData.SetService(tab.getPosition()+1);
                        break;
                    case 1:
                        SharedData.SetService(tab.getPosition()+1);
                        break;
                    case 2:
                        SharedData.SetService(tab.getPosition()+1);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/

        /*This is the centre black label , on clicking it takes to other screen with current locaion*/
        Button next_button = (Button)findViewById(R.id.location_select_next);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng center = mMap.getCameraPosition().target;
               // LatLng center = center  = new LatLng(17.436922,78.384738);//mMap.getCameraPosition().target;//only for simulartor
                SharedData.SetRequestLocation(center);
                SharedData.SaveCordInPref(getApplicationContext());
                SetNavigation();
                /*Intent selectSlot = new Intent(getApplicationContext(), SelectSlotActivity.class);
                startActivity(selectSlot);*/
                //GetServiceVersion();
                // / GetServicesForUserLocation();

            }
        });


        FloatingActionButton next_fab = (FloatingActionButton)findViewById(R.id.dummy_fab);
        next_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng center = mMap.getCameraPosition().target;//only for simulartor
                SharedData.SetRequestLocation(center);
                SharedData.SaveCordInPref(getApplicationContext());
                /*Intent selectSlot = new Intent(getApplicationContext(), SelectSlotActivity.class);
                startActivity(selectSlot);*/
                SetNavigation();

            }
        });
        locationText = (TextView)findViewById(R.id.locationMarkertext);
        locationText.startAnimation(animation);
        locationText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               /* LatLng center = mMap.getCameraPosition().target;//only for simulartor
                SharedData.SetRequestLocation(center);
                *//*Intent selectSlot = new Intent(getApplicationContext(), SelectSlotActivity.class);
                startActivity(selectSlot);*//*
                SetNavigation();*/

            }
        });

        /*Google Places API Search Box*/
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

       /* AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        autocompleteFragment.setFilter(typeFilter);*/

        autocompleteFragment.setHint("Search Pick Up Location");



        /*Get Location on clicking the floating button*/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.map_fab);
        assert fab != null;
        FloatingActionButton submit_locationfab = (FloatingActionButton) findViewById(R.id.accept_location_fab);

        fab.startAnimation(animation);
        submit_locationfab.startAnimation(animation);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Location loc = getLocation();
                if(loc != null) {
                    GetCity(new LatLng(loc.getLatitude(),loc.getLongitude()));
                    AnimateCameraToLocation(loc);
                }
            }
        });

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);
        Location loc = getLocation();
        if(loc == null)
        {
            fab.setImageResource(R.drawable.gpserror);
        }
        if(loc != null)
        GetCity(new LatLng(loc.getLatitude(),loc.getLongitude()));
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (IsLocationPermissionAllowed())
        {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1000, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }


        RegisterSignOut();
    }

    public void SetNavigation()
    {
        Bundle services_screen_data = new Bundle();
        services_screen_data.putBoolean("shownext",true);
        SharedData.HandleNavigation(R.id.nav_services,this,services_screen_data);
    }

    public void AnimateCameraToLocation(Location loc)
    {
        LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
        if(mMap == null) return;
       // mapMarker.setPosition(latlng);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latlng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                            /*.bearing(90)*/                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }



    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    Location location; // location
    double latitude; // latitude
    double longitude; //
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute


    public Location getLocation() {
        try {
            if (locationManager == null)
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                showSettingsAlert();
                return null;
            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling

                        InitiatePermissionsForLocation(true);
                        return null;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void GetCity(LatLng loc)
    {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<android.location.Address> address = geoCoder.getFromLocation(loc.latitude, loc.longitude, 1);
            int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i=0; i<1; i++) {
                //TextView addressText = (TextView)findViewById(R.id.location_address_text_btmsheet);
                //addressText.setText(address.get(0).getSubLocality() );
                String addressStr = address.get(0).getAddressLine(i);
                String locality = address.get(0).getLocality();
                LatLng hitech_centre =  new LatLng(17.446469, 78.377552 );
                double distance = getDistance(hitech_centre , loc);
                if(distance > 10000) {
                    ServiceUnAvailable(true);
                }
                else
                {
                    ServiceUnAvailable(false);
                }

                builder.append(addressStr);
                builder.append(" ");
            }

            String fnialAddress = builder.toString(); //This is the complete address.
        } catch (IOException e)
        {
            String message = e.getMessage();
        }
        catch (NullPointerException e) {}
    }
    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);
        return distance;

    }

    public void ServiceUnAvailable(boolean value)
    {
        if(value)
            locationText.setText(getResources().getString(R.string.ServiceNotAvailable));
        else
        {
            locationText.setText(getResources().getString(R.string.PinAsLocation));// "Apologies , Our Services Are Not Avaialble in Your Area Right Now");
        }

    }

    public void UiChangeOnNoLocation(boolean gpsOff)
    {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.map_fab);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_grow);
        if(gpsOff)
        {
            fab.setImageResource(R.drawable.gpserror);
        }
        else {
            fab.setImageResource(R.drawable.gpsokay2);
        }
        fab.startAnimation(animation);
    }


    AlertDialog gpsOffDlg;
    public void showSettingsAlert() {
        if(gpsOffDlg != null && gpsOffDlg.isShowing())
        {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this , R.style.PauseDialog);

        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.gpserror);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                gpsOffDlg = null;
                dialog.cancel();
            }
        });

        // Showing Alert Message
        gpsOffDlg = alertDialog.show();
    }

    public void getLocation2() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1000, this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion <= android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            // Do something for lollipop and above versions
            locationPermissionsStatus = true;// this variable will be evaluated only in marshamallow and avove
        }
        if(launchingMapActivity == true)
        {
            launchingMapActivity = false;
            return;
        }
        FloatingActionButton submit_locationfab = (FloatingActionButton) findViewById(R.id.accept_location_fab);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.map_fab);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_grow);

        if(locationPermissionsStatus)
        {
            Location loc = getLocation();
            if(loc != null)
            {
                //GetCity(loc);
                if(gpsOffDlg != null){
                    gpsOffDlg.cancel();
                    gpsOffDlg = null;
                }
                if(placeSelectorCausedPause == false) {
                    AnimateCameraToLocation(loc);

                }

                UiChangeOnNoLocation(false);
                if(placeSelectorCausedPause == false){GetCity(new LatLng(loc.getLatitude(),loc.getLongitude()));}

                placeSelectorCausedPause = false;
            }
            else
            {
                startTimer();
            }

        }
        submit_locationfab.startAnimation(animation);
        fab.startAnimation(animation);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.removeUpdates(this);
    }

    private void handleNewLocation(Location location) {
       // Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
       // mapMarker.setPosition(latLng);
        float zoomLevel = 17.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                /*.bearing(90) */               // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        CameraUpdateFactory.newCameraPosition(cameraPosition);
    }

    public void RegisterSignOut()
    {
        Button singoutbtn = (Button)findViewById(R.id.map_signout);
        singoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedData.DeleteAllUser();
                SharedData.DeleteAllUserCar();
                SharedData.SignOutGoogle();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }




    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    @Override
    public void onLocationChanged(Location location) {
        //handleNewLocation(location);
    }
    @Override
    public void onMapClick(LatLng location) {
        //handleNewLocation(location);
        GetCity(location);
    }

    @Override
    public void onMapLongClick(LatLng location) {
        GetCity(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    boolean placeSelectorCausedPause = false;
    @Override
    public void onPlaceSelected(Place place) {
        //Log.i(TAG, "Place Selected: " + place.getName());

        LatLng userLocation= place.getLatLng();
        GetCity(userLocation);
        placeSelectorCausedPause = true;
        {
            LatLng hitech_centre =  new LatLng(17.446469, 78.377552 );
            double distance = getDistance(hitech_centre , userLocation);
            if(distance > 10000) {
                ServiceUnAvailable(true);
            }
            else
            {
                ServiceUnAvailable(false);
            }
        }
       // mapMarker.setPosition(userLocation);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userLocation)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                /*.bearing(90) */               // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public  void onCameraChange(CameraPosition position)
    {
        LatLng center = mMap.getCameraPosition().target;
      //  mapMarker.setPosition(center);

    }



    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        // Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to format information about a place nicely.
     */
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }



    final int MY_PERMISSIONS_LOCATION = 1;
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Location loc = getLocation();
        LatLng startCord = null;
        if(loc != null)
        {
            startCord = new LatLng(loc.getLatitude() , loc.getLongitude());
        }
        else
        {
            startCord = SharedData.GetLocationFromPref(getApplicationContext());
            if(startCord == null)
            {
                startCord = new LatLng(17.361637, 78.374630);
            }
        }
        //= new LatLng(17.361637, 78.374630);

       // mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startCord));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
               // locationText.setVisibility(View.GONE);
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

    }
    CountDownTimer locationTimer;
    private void startTimer() {

        locationTimer = new CountDownTimer(4000, 1000) {
            int secondsLeft = 0;

            public void onTick(long ms) {
                if (Math.round((float) ms / 1000.0f) != secondsLeft) {
                    secondsLeft = Math.round((float) ms / 1000.0f);

                }
            }

            public void onFinish() {
                FloatingActionButton submit_locationfab = (FloatingActionButton) findViewById(R.id.accept_location_fab);
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.map_fab);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation_grow);

                if(locationPermissionsStatus)
                {
                    Location loc = getLocation();
                    if(loc != null)
                    {
                        //GetCity(loc);
                        if(gpsOffDlg != null){
                            gpsOffDlg.cancel();
                            gpsOffDlg = null;
                        }
                        if(placeSelectorCausedPause == false) {
                            AnimateCameraToLocation(loc);

                        }
                        placeSelectorCausedPause = false;
                        UiChangeOnNoLocation(false);
                        GetCity(new LatLng(loc.getLatitude(),loc.getLongitude()));
                    }

                }
                submit_locationfab.startAnimation(animation);
                fab.startAnimation(animation);

            }
        }.start();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionsStatus = true;
                    FloatingActionButton submit_locationfab = (FloatingActionButton) findViewById(R.id.accept_location_fab);
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_grow);
                    Location loc = getLocation();
                    if(loc != null)
                    {
                       // GetCity(loc);
                        if(gpsOffDlg != null){
                            gpsOffDlg.cancel();
                            gpsOffDlg = null;
                        }
                        AnimateCameraToLocation(loc);
                        UiChangeOnNoLocation(false);
                        submit_locationfab.startAnimation(animation);

                    }

                } else {
                    locationPermissionsStatus = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void ShowPermissionAlertDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this , R.style.PauseDialog);

        // Setting Dialog Title
        alertDialog.setTitle("Location Permissions");

        // Setting Dialog Message
        alertDialog.setMessage("Location Permissions are required for faster and better Car Pick Up for Service");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.gpserror);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                InitiatePermissionsForLocation(false);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    boolean locationPermissionsStatus = false;

    private boolean IsLocationPermissionAllowed()
    {
        int fineLocPermResult = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int courseLocPermResult = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if(fineLocPermResult != PackageManager.PERMISSION_GRANTED && courseLocPermResult != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        return true; //for simulator
    }

    private void InitiatePermissionsForLocation(boolean showRationale)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) && showRationale) {

                ShowPermissionAlertDialog();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_LOCATION);
            }
        }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        SharedData.HandleNavigation(id,this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.map_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void HideOnGoingRequestIfRequired()
    {
        NavigationView nav = (NavigationView) findViewById(R.id.map_nav_view);
        Menu nav_Menu = nav.getMenu();
        HashMap<String , String> userdetailsFromDB = SharedData.FetchUser();
        int userStatus = Integer.parseInt(userdetailsFromDB.get("status"));
        if(userStatus == SharedData.UserStatus.RequestPending.getID())
        {
            nav_Menu.findItem(R.id.nav_order).setVisible(true);
        }
        else
        {
            nav_Menu.findItem(R.id.nav_order).setVisible(false);
        }

    }

    public void GetServicesForUserLocation()
    {
        String url = "locationservices/"+SharedData.GetUserId()+"/";
        if(SharedData.GetRequestLocation() == null)
        {

        }
        String urlParameters = String.format("latt=%s&longg=%s" ,SharedData.GetRequestLocation().latitude , SharedData.GetRequestLocation().longitude);

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,handler);

        ArrayList<String> dummy = new ArrayList<String>();
        uniTask.execute(dummy);
        showProgressDialog("Getting Services in your Area");
    }

    public void GetServiceVersion()
    {
        String url = "services_version/"+SharedData.GetUserId()+"/";
        String val = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("service_version" , "-5.0000");
        String urlParameters = String.format("service_version=%s" , val);

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters,handler);

        ArrayList<String> dummy = new ArrayList<String>();
        current_task =  AsyncActivities.SERVICE_VERSION;
        uniTask.execute(dummy);
        showProgressDialog("Getting Services in your Area");
    }

    private ProgressDialog mProgressDialog;
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("taskstatus");

            if ((null != aResponse)) {

                if(uniTask != null)
                {
                    if(!uniTask.IsSuccess())
                    {

                        String reason = uniTask.ResultReason();
                        Toast.makeText(getApplicationContext(), "Failed to Fetch Data- "+reason, Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        switch(current_task)
                        {
                            case SERVICE_VERSION:
                            {

                                hideProgressDialog();
                                if(IsUpdateRequired())
                                {
                                    PostOperation();
                                }
                                SetNavigation();
                            }
                            break;
                            case LOCATIONBASED_SERVICES:
                            {
                                hideProgressDialog();
                                PostOperation();
                                SetNavigation();
                            }
                            break;

                        }




                    }
                }
            }
            else
            {
                //Toast.makeText(getBaseContext(), "Not Got Response From Server.", Toast.LENGTH_SHORT).show();
            }

        }
    };
    HashMap<String,String> services_distance_map = new HashMap<String,String>();
    String updatedVersion = "";

    public boolean IsUpdateRequired()
    {
        JSONObject jsonRootObject = null;
        try
        {
            jsonRootObject = new JSONObject(uniTask.outputJasonString);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        JSONArray jsonArray = jsonRootObject.optJSONArray("response");
        JSONObject jsonObject = null;
        try
        {
            jsonObject = jsonArray.getJSONObject(0);
            Boolean isversionupdated = jsonObject.getBoolean("isversionupdated");
            updatedVersion = jsonObject.getString("system_version");
            return isversionupdated;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }



    public void PostOperation()
    {




        JSONArray reposnejSonArray = uniTask.GetOutputResult();

        int count = reposnejSonArray.length();
        for(int i=0;i<count;i++)
        {
            Bundle local_bundle = new Bundle();
            JSONObject jsonresponseObject = null;
            try
            {
                jsonresponseObject = reposnejSonArray.getJSONObject(i);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            Iterator<?> keys = jsonresponseObject.keys();
            int service_count =0 ;
            ClearSavedServiceIds(getApplicationContext());
            while( keys.hasNext() )
            {
                String key = (String)keys.next();
                String[] service_ids = key.split("~");
                String service_tocheck ;

                try {
                    //this is the service id+ service name , it will be used as key in shared pre
                    Integer.parseInt(service_ids[1]);//if this gives exception means other one has value of service id
                    service_tocheck = service_ids[1];
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    service_tocheck = service_ids[0];
                }


                String value = null;
                try
                {
                    value = jsonresponseObject.getString(key);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                //key contains both name and id
                if(key.contains(service_tocheck))
                {
                    //put service id string as key and response map as value
                    SharedData.UpdateSharedPref(getApplicationContext() , service_tocheck , value);
                    //Also prepare another shared pref for storing services supported
                    String saved_service_ids = SharedData.GetSharedPreString(getApplicationContext() , "saved_service_ids");//PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("saved_service_ids" , "");
                    // add the latest service id
                    saved_service_ids = saved_service_ids + "~" + service_tocheck;

                    SharedData.UpdateSharedPref(getApplicationContext() , "saved_service_ids" , saved_service_ids);
                    service_count ++;

                }
            }
            SharedData.UpdateSharedPref(getApplicationContext(), "service_count" , service_count);
            //system_version ,  service_version
            if(updatedVersion != "")
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("service_version" , updatedVersion).commit();

            String test1 = SharedData.GetSharedPreString(getApplicationContext() , "1");
            String test2 = SharedData.GetSharedPreString(getApplicationContext() , "2");
            String test3 = SharedData.GetSharedPreString(getApplicationContext() , "3");
            String saved_test = SharedData.GetSharedPreString(getApplicationContext() , "saved_service_ids");
            int test_count = SharedData.GetSharedPrefValue(getApplicationContext() , "service_count");

        }
        if(updatedVersion != "")
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("service_version" , updatedVersion).commit();
    }


    public void ClearSavedServiceIds(Context context)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("saved_service_ids" , "").commit();

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.accent_material_light),0), "BASIC");
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.ripple_material_light),1), "PREMIUM");
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.button_material_dark),2), "PLATINUM");
        viewPager.setAdapter(adapter);
    }

    public static class DummyFragment extends Fragment {
        int color;
        SimpleRecyclerAdapter adapter;
        View view;
        int frag_position =0;

        public DummyFragment() {
        }

        @SuppressLint("ValidFragment")
        public DummyFragment(int color , int position) {
            this.color = color;
            frag_position = position;


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.map_bottomsheet_fragment, container, false);
            SetTexts(frag_position);
            return view;
        }

        public void SetTexts(int i)
        {
            TextView internal_text1 = (TextView)view.findViewById(R.id.text_1);
            TextView internal_text2 = (TextView)view.findViewById(R.id.text_2);
            TextView internal_text3 = (TextView)view.findViewById(R.id.text_3);
            TextView internal_text4 = (TextView)view.findViewById(R.id.text_4);
            TextView internal_text5 = (TextView)view.findViewById(R.id.text_5);
            TextView internal_text6 = (TextView)view.findViewById(R.id.text_5);


            TextView external_text1 = (TextView)view.findViewById(R.id.text_external_1);
            TextView external_text2 = (TextView)view.findViewById(R.id.text_external_2);
            TextView external_text3 = (TextView)view.findViewById(R.id.text_external_3);
            TextView external_text4 = (TextView)view.findViewById(R.id.text_external_4);
            TextView external_text5 = (TextView)view.findViewById(R.id.text_external_5);
            TextView external_text6 = (TextView)view.findViewById(R.id.text_external_6);
            TextView external_text7 = (TextView)view.findViewById(R.id.text_external_7);
            TextView external_text8 = (TextView)view.findViewById(R.id.text_external_8);
            TextView external_text9 = (TextView)view.findViewById(R.id.text_external_9);


            TextView text_cost_hatch_Sedan = (TextView)view.findViewById(R.id.cost_text_hatch_sedan);
            TextView text_cost_suv = (TextView)view.findViewById(R.id.cost_text_suv);

            switch(i)
            {
                case 0:
                {
                    internal_text1.setText("Mats Wash(rubber Only");
                    external_text1.setText(" Car exterior foam wash");
                    external_text2.setText("Car exterior body black fiber parts polish ");
                    external_text3.setText("Tyre cleaning");
                    external_text4.setText("Tyre Polish");
                    external_text5.setText("Glass cleaning");
                    text_cost_hatch_Sedan.setText("Hatch Back and Sedan : INR 300");
                    text_cost_suv.setText("LUV MUV and SUV : INR 400");


                }
                break;
                case 1:
                {
                    internal_text1.setText("Mats Wash(rubber Only");
                    internal_text2.setText("Vaccuming  (except Dickey)");
                    internal_text3.setText("Total interior car wiping ");
                    external_text1.setText("Car exterior foam wash");
                    external_text2.setText("Car exterior body black fiber parts polish ");
                    external_text3.setText("Tyre cleaning");
                    external_text4.setText("Tyre Polish");
                    external_text5.setText("Glass cleaning");
                    text_cost_hatch_Sedan.setText("Hatch Back and Sedan : INR 350");
                    text_cost_suv.setText("LUV MUV and SUV : INR 450");

                }
                break;
                case 2:
                {
                    internal_text1.setText("Mats Wash(rubber Only");
                    internal_text2.setText("Vaccuming  (except Dickey)");
                    internal_text3.setText("Total interior car wiping ");
                    internal_text4.setText("Dash board polish ");
                    internal_text5.setText("Dickey vacuuming ");
                    internal_text6.setText("Stepney cleaning & polish ");

                    external_text1.setText("Car exterior foam wash");
                    external_text2.setText("Car exterior body black fiber parts polish ");
                    external_text3.setText("Tyre cleaning and Tyre Polish");
                    external_text4.setText("");
                    external_text4.setText("Glass cleaning");
                    external_text5.setText("Engine wash");
                    external_text6.setText("Alloy Wheels Cleaning");
                    external_text7.setText("Fuel cap cleaning");
                    external_text8.setText("Wiper water filling");
                    text_cost_hatch_Sedan.setText("Hatch Back and Sedan : INR 500");
                    text_cost_suv.setText("LUV MUV and SUV : INR 550");

                }
                break;
            }


        }
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private class LoadProfileImage extends AsyncTask<Void, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            if(bmImage!= null)
            {
                this.bmImage = bmImage;
            }
        }

        protected Bitmap doInBackground(Void...params) {
            String urldisplay = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("IMGURL", "defaultStringIfNothingFound");
            Bitmap user_pic = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                user_pic = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            SharedData.SetUserpic(user_pic);
            return user_pic;
        }

        protected void onPostExecute(Bitmap result) {
            if(bmImage!= null)
            {
                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),result);
                drawable.setCircular(true);
                bmImage.setImageDrawable(drawable);
                TextView txt = (TextView)findViewById(R.id.userNameText);
                if(txt != null)
                {
                    txt.setText(SharedData.GetUserName());
                }
                invalidateOptionsMenu();
                //bmImage.setImageBitmap(result);
            }
        }


    }

}
