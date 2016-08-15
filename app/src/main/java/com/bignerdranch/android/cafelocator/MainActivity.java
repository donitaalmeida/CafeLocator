
/**
 * Created by donita on 11-07-2016.
 * First activity with map view and search bar
 */
package com.bignerdranch.android.cafelocator;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private LocationManager locManager;
    private GoogleMap mGoogleMap;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView mEmail;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 125;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final String GOOGLE_API_KEY = "AIzaSyCPlTx3VCnAHgCmmUPidLL7_Jfu4ntJiqE";
    private int PROXIMITY_RADIUS = 2400;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private Snackbar snackbarGpsNetwork = null;
    private PlaceAutocompleteFragment autocompleteFragment;
    private Place cafeSearched;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //handling absent GPS sensor error

        if(!checkGPSSensorPresent()){
            Snackbar.make(navigationView, "App cannot run without GPS sensor", Snackbar.LENGTH_SHORT)
                    .setActionTextColor(Color.RED)
                    .show();
        }else{
            //handling GooglePlayservices framework absent error
            if(isGooglePlayServicesAvailable()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                } else {
                    //if all ok start app
                    startApp();
                }
            }
        }
    }

    private boolean checkGPSSensorPresent(){
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    //check if gps is enabled
    private boolean isGpsEnabled() {
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locManager != null) {
            return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else {
            return false;
        }
    }
    // check if network is enabled
    private boolean isNetworkEnabled() {
        NetworkConnectionDetector networkConnectionDetector=new NetworkConnectionDetector(getApplicationContext());
        if (networkConnectionDetector != null) {
            return networkConnectionDetector.isConnectingToInternet();
        } else {
            return false;
        }
    }
    //check if GPS and Netork are enabled and promt user to enable the same if required
    private void showGpsNetworkSnackbar() {
        boolean flag = false;
        StringBuilder msg = new StringBuilder();
        if(!isGpsEnabled()){
            flag = true;
            msg.append("Please Enable GPS");
        }
        if(!isNetworkEnabled()){
            if (flag) {
                msg.append(" & Internet");
            } else {
                flag = true;
                msg.append("Please Enable Internet");
            }
        }
        if (flag) {
            navigationView = (NavigationView) findViewById(R.id.navigation_view);
            snackbarGpsNetwork = Snackbar.make(navigationView, msg.toString(), Snackbar.LENGTH_INDEFINITE);
            if(!isNetworkEnabled()){
                snackbarGpsNetwork.setAction("Enable Internet", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }
                });
            }
            if(!isGpsEnabled()){
                snackbarGpsNetwork.setAction("Enable GPS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
            }
            snackbarGpsNetwork.show();
        } else {
            if (snackbarGpsNetwork != null) {
                snackbarGpsNetwork.dismiss();
                recreate();
            }
        }
    }

    private void startApp() {
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //initailize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerLayout = navigationView.getHeaderView(0);
        mEmail = (TextView) headerLayout.findViewById(R.id.email);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission","No for read contacts");
            requestPermissions(new String[]{android.Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);//After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
            Account[] accounts = AccountManager.get(this).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    Log.d("account",account.name);
                    mEmail.setText(account.name);
                    break;
                }
            }
        }
        // Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {

                    case R.id.searchCafe: {
                        Snackbar.make(navigationView, "Already in Search Cafe", Snackbar.LENGTH_SHORT)
                                .setActionTextColor(Color.RED)
                                .show();
                    }
                    return true;

                    case R.id.nearByCafes:
                        Toast.makeText(getApplicationContext(), "Nearby Cafes", Toast.LENGTH_SHORT).show();
                        Intent intent;
                        intent = new Intent(getApplicationContext(), NearByCafeActivity.class);
                        if(cafeSearched != null){
                            intent.putExtra("myLatitude",cafeSearched.getLatLng().latitude);
                            intent.putExtra("myLongitude",cafeSearched.getLatLng().longitude);
                        }
                        else{

                            intent.putExtra("myLatitude",mLocation.getLatitude());
                            intent.putExtra("myLongitude",mLocation.getLongitude());

                        }
                        startActivity(intent);
                        return true;
                    default:
                        Snackbar.make(navigationView, "Somethings wrong", Snackbar.LENGTH_LONG)
                                .setActionTextColor(Color.RED)
                                .show();
                        return true;
                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        //search bar
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build();

        autocompleteFragment.setFilter(typeFilter);
        //attach autocomplete listner
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("info", "Place: " + place.getName());
                cafeSearched = place;
                mGoogleMap.clear();
                setMarker(place.getLatLng(),place.getName().toString()+place.getAddress());
                StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json?");
                googlePlacesUrl.append("location=" + place.getLatLng().latitude + "," + place.getLatLng().longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&keyword=" + "cafe");
                googlePlacesUrl.append("&sensor=false");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
                Object[] toPass = new Object[2];
                toPass[0] = mGoogleMap;
                toPass[1] = googlePlacesUrl.toString();
                Log.d("googlePlacesUrl",googlePlacesUrl.toString());
                googlePlacesReadTask.execute(toPass);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("error", "An error occurred: " + status);
            }
        });
        updateLocation();
        //TODO arrow function
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("CONN","onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(navigationView, "Could not connect to play services", Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.RED)
                .show();
    }
    //when permission are granted/denied this method is called
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startApp();
                } else {
                    navigationView = (NavigationView) findViewById(R.id.navigation_view);
                    Snackbar.make(navigationView, "Location access is required to show coffee shops nearby.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                    }
                                }
                            }).setActionTextColor(Color.RED).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pattern emailPattern = Patterns.EMAIL_ADDRESS;
                    Account[] accounts = AccountManager.get(this).getAccounts();
                    for (Account account : accounts) {
                        if (emailPattern.matcher(account.name).matches()) {
                            Log.d("account", account.name);
                            mEmail.setText(account.name);
                            break;
                        }
                    }
                }
            }
            return;
        }
    }

    //method to check if google place service is available
    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
    //method to set marker on map fragment
    public void setMarker(LatLng current, String address){
        float zoomLevel = 14.0f;
        mGoogleMap.clear();
        mGoogleMap.addMarker(new MarkerOptions().position(current).title(address));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,zoomLevel));
    }

    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
    //update location to current location
    public void updateLocation() {
        showGpsNetworkSnackbar();
        try {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLocation == null) {
                mLocation = new Location("SJSU");
                mLocation.setLatitude(37.3351895);
                mLocation.setLongitude(-121.8821658);
                setMarker(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), "SJSU(Default)");
            } else {
                Log.d("curlat",mLocation.getLatitude()+"");
                Log.d("curlng",mLocation.getLongitude()+"");
                setMarker(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), "You are here");
            }
            autocompleteFragment.setBoundsBias(setBounds(mLocation,3000));
        }catch (SecurityException e){
            e.printStackTrace();
        }

    }
    // method to set boundry for the searchbar suggestions
    private LatLngBounds setBounds(Location location, int mDistanceInMeters ){
        double latRadian = Math.toRadians(location.getLatitude());
        double degLatKm = 110.574235;
        double degLongKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLongKm;
        double minLat = location.getLatitude() - deltaLat;
        double minLong = location.getLongitude() - deltaLong;
        double maxLat = location.getLatitude() + deltaLat;
        double maxLong = location.getLongitude() + deltaLong;
        return  new LatLngBounds(new LatLng(minLat, minLong), new LatLng(maxLat, maxLong));
    }

}