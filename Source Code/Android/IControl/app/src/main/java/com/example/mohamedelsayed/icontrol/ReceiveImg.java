package com.example.mohamedelsayed.icontrol;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mohamed Elsayed on 04/02/2017.
 */


/*
    This class used for receiving unauthorized images and list for all authorized images ans store them ..
        in our database

   Also it used for update user location (latitude, longitude) when the user outside door
 */

public class ReceiveImg extends Service implements LocationListener {

    DataBase DB;
    public static ArrayList<Person> Users;

    public ReceiveImg() {
        DB = new DataBase(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {

        // Get Location manager service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mprovider = locationManager.getBestProvider(criteria, false);

        // Check if location permission is enabled or not
        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            } else {
                Location location = locationManager.getLastKnownLocation(mprovider);
                locationManager.requestLocationUpdates(mprovider, 15*1000, 1, this);

                if (location != null)
                    onLocationChanged(location);
                else
                    Toast.makeText(getApplicationContext(), "No Location Provider", Toast.LENGTH_SHORT).show();
            }
        }

        Thread T = new Thread() {
            Socket socket = null;
            byte[] response;

            @Override
            public void run() {
                try {
                    final DataBase DB = new DataBase(getApplicationContext());

                    Users = DB.GetAllUsers();
                    if(DB.GetUnAuthorizedUsers() != null)
                        MainActivity.NotiState = true;
                    else
                        MainActivity.NotiState = false;

                    socket = new Socket(MainActivity.ServerIPAddress, 8000);

                    MainActivity.ReceiveState = true;
                } catch (IOException e) {

                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() {
                            // Toast.makeText(getApplicationContext(), "Can not connect to server !", Toast.LENGTH_LONG).show();
                            MainActivity.ReceiveState = false;
                        }
                    });

                    socket = null;
                }

                while (socket != null) {

                    if (socket.isConnected()) {
                        InputStream inputStream = null;

                        try {
                            inputStream = socket.getInputStream();

                            // check if inputStream contains byte data
                            if (inputStream.available() > 0) {
                                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                                byte[] buffer = new byte[1024];

                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != 0) {
                                    byteArrayOutputStream.write(buffer, 0, bytesRead);

                                    if (inputStream.available() == 0) break;
                                }

                                Thread OutPut = new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        response = byteArrayOutputStream.toByteArray();

                                        String str = new String(response);

                                        // && : for all unauthorized images else for users name

                                        if (str.contains("&&")) {

                                            // split between Header && image
                                            final String[] Data = str.split("&&");
                                            String Header = Data[0];

                                            // Read image from >>>
                                            int readFrom = 0;
                                            try {
                                                readFrom = (Header + "&&").getBytes("UTF-8").length;
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                            // Split ImageName && AccessDate
                                            String[] UnInformation = Header.split("::");
                                            final String ImageName = UnInformation[0], CreateDate = UnInformation[1];

                                            byte[] oldResponse = response;
                                            response = Arrays.copyOfRange(oldResponse, readFrom, oldResponse.length);

                                            if (response != null && response.length > 0) {
                                                final Handler h3 = new Handler(Looper.getMainLooper());
                                                h3.postDelayed(new Runnable() {
                                                    public void run() {
                                                        // Convert byte image array to Bitmap image
                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length);

                                                        if (bitmap != null) {
                                                            try {
                                                                DB.AddUnauthorizedUser(Integer.parseInt(ImageName), bitmap, CreateDate);
                                                            } catch (Exception e) {
                                                                Toast.makeText(getApplicationContext(), "Exception occurred", Toast.LENGTH_LONG).show();
                                                            }
                                                            showNotification(bitmap, CreateDate);
                                                        }
                                                    }
                                                }, 500);
                                            }
                                        }
                                        // Get All users information
                                        else {
                                            JSONArray jsonArray = null;
                                            try {
                                                jsonArray = new JSONArray(str);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            if (jsonArray != null) {
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    Person P = new Person();
                                                    try {
                                                        P.ID = (Integer) jsonArray.getJSONArray(i).get(0);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        P.Name = (String) jsonArray.getJSONArray(i).get(1);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    // Check if the user in our database or not !
                                                    Boolean UserExist = false;
                                                    if (Users != null)
                                                        for (Person p : Users) {
                                                            if (p.ID == P.ID) {
                                                                UserExist = true;
                                                                break;
                                                            }
                                                        }

                                                    if (!UserExist)
                                                        DB.AddUser(P.ID, P.Name);
                                                }
                                            }
                                        }
                                    }
                                });

                                OutPut.start();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else
                        try {
                            socket.close();
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        };

        T.start();
        return START_STICKY;
    }

    private NotificationManager mNotificationManager;

    private void showNotification(Bitmap Img, String Creation) {

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Users.class), 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Unauthorized")
                .setContentText("Date:" + Creation).setSmallIcon(R.drawable.notification)
                .setContentIntent(contentIntent).setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.noti))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setStyle(new Notification.BigPictureStyle().bigPicture(Img)).build();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(0, notification);

        MainActivity.NotiState = true;
    }

    LocationManager locationManager;
    String mprovider;

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(getApplicationContext(), "Lat: " + location.getLatitude() + " Lon: " + location.getLongitude(), Toast.LENGTH_LONG).show();
        new UpdateThingspeak(location.getLatitude(), location.getLongitude()).execute();
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
}
