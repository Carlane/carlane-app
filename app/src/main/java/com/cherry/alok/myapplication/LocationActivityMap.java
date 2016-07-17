package com.cherry.alok.myapplication;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LocationActivityMap extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener,
        NavigationView.OnNavigationItemSelectedListener, LocationListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap;
    private Marker mapMarker;
    UniversalAsyncTask uniTask = null;
    private LocationManager locationManager;
    TextView locationText;
    BottomSheetBehavior behavior;
    Boolean launchingMapActivity ;


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
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.map_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*Set up the Bottom Sheet containing types of services*/
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.laymap_cordinatorLayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.map_bottom_sheet);

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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

        /*Set Up the Tabs In Bottom Sheet*/
        final ViewPager viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(viewPager);
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
        });

        /*This is the centre black label , on clicking it takes to other screen with current locaion*/
        locationText = (TextView)findViewById(R.id.locationMarkertext);
        locationText.startAnimation(animation);
        locationText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LatLng center = mMap.getCameraPosition().target;
                Intent selectSlot = new Intent(getApplicationContext(), SelectSlotActivity.class);
                startActivity(selectSlot);

                // TODO Auto-generated method stub
                //DO you work here
                /*SharedData.SetRequestLocation(mapMarker.getPosition());
                String url = "request/"+SharedData.GetUserId()+"/";
                String urlParameters = String.format("servicename=%s&servicetype=%s&timeSlotfrom=%s&timeSlotto=%s&geo=%s",SharedData.GetService(),"3","09:00:00","12:00:00","3");
                uniTask = new UniversalAsyncTask(url,"POST",urlParameters,handler);
                ArrayList<String> dummy = new ArrayList<String>();
                uniTask.execute(dummy);*/
            }
        });

        /*Google Places API Search Box*/
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (IsLocationPermissionAllowed())
        {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1000, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }



    }

    public void AnimateCameraToLocation(Location loc)
    {
        LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
        if(mapMarker == null || mMap == null) return;
        mapMarker.setPosition(latlng);
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
                if(gpsOffDlg != null){
                    gpsOffDlg.cancel();
                    gpsOffDlg = null;
                }
                AnimateCameraToLocation(loc);
                UiChangeOnNoLocation(false);
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
        mapMarker.setPosition(latLng);
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




    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
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

    @Override
    public void onPlaceSelected(Place place) {
        //Log.i(TAG, "Place Selected: " + place.getName());

        LatLng userLocation= place.getLatLng();

        mapMarker.setPosition(userLocation);
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
        mapMarker.setPosition(center);

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

/*    @Override
    public boolean onTouch(View v, MotionEvent event)
    {

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                locationText.setVisibility(View.GONE);
                // touch down code
                break;

            case MotionEvent.ACTION_MOVE:
                locationText.setVisibility(View.GONE);
                // touch move code
                break;

            case MotionEvent.ACTION_UP:
                // touch up code
                locationText.setVisibility(View.VISIBLE);
                break;
        }
        return true;
    }*/

    final int MY_PERMISSIONS_LOCATION = 1;
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng hyd = new LatLng(17.361637, 78.374630);
        mapMarker = mMap.addMarker(new MarkerOptions().//icon(BitmapDescriptorFactory.fromResource(R.drawable.flag3)).
                position(hyd).draggable(true).title("Car Lane Pick Up"));
      /*  if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                    ShowPermissionAlertDialog();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION },
                        MY_PERMISSIONS_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return;
        }*/
       // mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hyd));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));

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
        if(fineLocPermResult != PackageManager.PERMISSION_GRANTED || courseLocPermResult != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        return true;
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




                    }
                }
            }
            else
            {
                //Toast.makeText(getBaseContext(), "Not Got Response From Server.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.accent_material_light)), "BASIC");
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.ripple_material_light)), "PREMIUM");
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.button_material_dark)), "PLATINUM");
        viewPager.setAdapter(adapter);
    }

    public static class DummyFragment extends Fragment {
        int color;
        SimpleRecyclerAdapter adapter;

        public DummyFragment() {
        }

        @SuppressLint("ValidFragment")
        public DummyFragment(int color) {
            this.color = color;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.map_bottomsheet_fragment, container, false);



            return view;
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

}

