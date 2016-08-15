package com.bignerdranch.android.cafelocator;

/**
 * Created by donita on 11-07-2016.
 * async task to get nearby cafes using google maps webservice api by making http request
 */
import android.os.AsyncTask;
import com.google.android.gms.maps.GoogleMap;

public class GooglePlacesReadTask extends AsyncTask<Object, Integer, String> {
    String googlePlacesData = null;
    GoogleMap googleMap;
    Object[] toPass = new Object[2];

    @Override
    protected String doInBackground(Object... inputObj) {
        googleMap = (GoogleMap) inputObj[0];
        try {
            String googlePlacesUrl = (String) inputObj[1];
            Http http = new Http();
            googlePlacesData = http.read(googlePlacesUrl);
            } catch (Exception e) {
            }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
            PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
            toPass[0] = googleMap;
            toPass[1] = result;
            placesDisplayTask.execute(toPass);
    }
}