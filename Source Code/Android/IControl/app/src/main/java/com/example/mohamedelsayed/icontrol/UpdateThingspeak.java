package com.example.mohamedelsayed.icontrol;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mohamed Elsayed on 05/05/2017.
 */

/*
    This class used for update current location (latitude, longitude) of this app to use it when ..
    I'm in my car and when my car is near to Home the garage will open automatically
*/

public class UpdateThingspeak extends AsyncTask<Void, Void, String> {

    double lat, lon;

    UpdateThingspeak(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(Void... urls) {
        try {
            URL url = new URL("https://api.thingspeak.com/update?api_key=" + "5W3437O1L5JPXIOC" + "&field1=" + lat + "&field2=" + lon);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            return null;
        }
    }

    protected void onPostExecute(String response) {
    }
}

