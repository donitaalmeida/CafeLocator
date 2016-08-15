package com.bignerdranch.android.cafelocator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class NearByCafeActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Defining Variables
    private LocationManager locManager;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView mEmail;
    private Snackbar snackbarGpsNetwork = null;
    private ArrayList<PlaceInfo> placeList = new ArrayList<>();
    private static final String GOOGLE_API_KEY = "AIzaSyCPlTx3VCnAHgCmmUPidLL7_Jfu4ntJiqE";
    private LocationManager locationManager;
    private GoogleApiClient mGoogleApiClient;
    private double latitude = 0;
    private double longitude = 0;
    private int PROXIMITY_RADIUS = 2500;
    private Location mLocation;
    private RecyclerView recList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras= getIntent().getExtras();
        if(extras!=null){
            latitude=extras.getDouble("myLatitude");
            longitude=extras.getDouble("myLongitude");
        }
        setContentView(R.layout.activity_nearbycafe);
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerLayout = navigationView.getHeaderView(0) ;
        mEmail=(TextView)headerLayout.findViewById(R.id.email);
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                Log.d("account",account.name);
                mEmail.setText(account.name);
                break;
            }
        }
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                //Closing drawer on item click
                drawerLayout.closeDrawers();
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {

                    case R.id.searchCafe: {
                    Toast.makeText(getApplicationContext(), "Searching Cafe", Toast.LENGTH_SHORT).show();
                    Intent intent;
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    }
                    return true;
                    case R.id.nearByCafes:
                        Snackbar snackbar = Snackbar
                                .make(navigationView, "Already in Nearby Cafe", Snackbar.LENGTH_SHORT).setActionTextColor(Color.RED);
                        snackbar.show();
                        return true;

                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };
        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        PlaceAdapter placeAdapter=new PlaceAdapter(placeList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(placeAdapter);
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(com.google.android.gms.location.places.Places.GEO_DATA_API)
                    .addApi(com.google.android.gms.location.places.Places.PLACE_DETECTION_API)
                    .enableAutoManage(this,this)
                    .build();
        }
    }
    //async task to get nearby places
    public class MyTask extends AsyncTask<Object, Integer, String> {
        String googlePlacesData = null;
        @Override
        protected String doInBackground(Object... inputObj) {
            try{
                String googlePlacesUrl = (String) inputObj[0];
                Http http = new Http();
                googlePlacesData = http.read(googlePlacesUrl);
                Log.d("googlePlacesData",googlePlacesData);
            } catch (Exception e) {
                Log.d("Google Place Read Task", e.toString());
            }
            return googlePlacesData;
        }
        @Override
        protected void onPostExecute(String result) {
            MyDisplayTask myDisplayTask=new MyDisplayTask();
            myDisplayTask.execute(result);
        }
    }
    //async task to display the places in the recycler view
    public class MyDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {
        JSONObject googlePlacesJson;
        @Override
        protected List<HashMap<String, String>> doInBackground(Object... inputObj) {
            List<HashMap<String, String>> googlePlacesList = null;
            Places placeJsonParser = new Places();
            try {
                googlePlacesJson = new JSONObject((String) inputObj[0]);
                googlePlacesList = placeJsonParser.parse(googlePlacesJson);
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return googlePlacesList;
        }
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            placeList.clear();
            ArrayList<PlaceInfo> temp=new ArrayList<>();
            RecyclerView xyz=(RecyclerView)findViewById(R.id.cardList);
            PlaceAdapter abc=(PlaceAdapter)xyz.getAdapter();
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, String> googlePlace = list.get(i);
                String placeName = googlePlace.get("place_name");
                float ratings=Float.parseFloat(googlePlace.get("ratings"));
                String address=googlePlace.get("vicinity");
                boolean open_now=Boolean.parseBoolean(googlePlace.get("open_now"));
                int price_level=Integer.parseInt(googlePlace.get("price_level"));
                temp.add(new PlaceInfo(placeName,address,ratings,open_now,price_level));
            }
            PlacesSorter s=new PlacesSorter(temp);
            placeList.addAll(s.getSortedByRatings());
            abc.notifyDataSetChanged();
            if(placeList.size()==0){
                Toast.makeText(getApplicationContext(), "No Cafes found", Toast.LENGTH_SHORT).show();
            }
        }
    }

   //method to get the nearby places list
    private void preparePlaceList(){
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&keyword=" + "cafe");
        googlePlacesUrl.append("&sensor=false");
        googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
        MyTask myTask = new MyTask();
        Object toPass[] = new Object[1];
        toPass[0] = googlePlacesUrl.toString();
        Log.d("Url recycler",googlePlacesUrl.toString());
        myTask.execute(toPass);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(latitude==0&&longitude==0) {
            updateLocation();
        }
        showGpsNetworkSnackbar();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(navigationView, "Could not connect to play services", Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.RED)
                .show();
    }
    protected void onStart() {
        Log.d("Debug","onStart");
        mGoogleApiClient.connect();
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(navigationView!=null){
            showGpsNetworkSnackbar();
        }
        super.onStart();
    }

    protected void onStop() {
        Log.d("Debug","onStop");
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    public void updateLocation(){
        try {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            latitude=mLocation.getLatitude();
            longitude=mLocation.getLongitude();
        }catch (SecurityException e){
            Log.d("Debug","Security exception update Location");
        }
    }
    //check if network is enabled
    private boolean isNetworkEnabled() {
        NetworkConnectionDetector networkConnectionDetector=new NetworkConnectionDetector(getApplicationContext());
        if (networkConnectionDetector != null) {
            return networkConnectionDetector.isConnectingToInternet();
        } else {
            return false;
        }
    }
    //check and prompt when network is unavailable
    private void showGpsNetworkSnackbar() {
        if(!isNetworkEnabled()){
            navigationView = (NavigationView) findViewById(R.id.navigation_view);
            snackbarGpsNetwork = Snackbar.make(navigationView,"Network not enabled", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Enable", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                    });
            snackbarGpsNetwork.show();
        }
        else{
            if (snackbarGpsNetwork != null) {
                snackbarGpsNetwork.dismiss();
            }
            preparePlaceList();
        }

    }
}