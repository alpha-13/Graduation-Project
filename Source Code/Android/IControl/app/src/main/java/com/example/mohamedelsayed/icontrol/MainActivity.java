package com.example.mohamedelsayed.icontrol;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.appyvet.rangebar.RangeBar;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String ServerIPAddress = "192.168.137.100";
    public static Boolean ReceiveState = false;
    public static Boolean SendState = false;
    public static Boolean NotiState = false;

    Boolean Mute = false, PlayMusic = true, LVAuto = false;

    int AirPercentage = 1, CurrentMusicIndex = 0, LRLightVal = 0;
    int BRCheckedPosition = 3, LRCheckedPosition = 3;


    ImageView ImgSystemState, ImgPrev, ImgNext, ImgPlay, ImgStop, ImgSound, ImgLight, ImgAlarm;
    public static ImageView ImgNoti;
    SwitchButton BRLight, BRWindow, KFan, LRAirCondition, LRLight;
    TextView TVLight, TVTemp, TVHum, TVKTemp, txtMusicName, LRLightPercentage;
    ToggleSwitch BRPercentage, LRPercentage;

    ListView LVSetting, LVMusicList;
    RangeBar LRAirPercentage;
    TextClock TCTime;
    SeekBar Volume;
    ToggleButton LRAuto;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        ImgSystemState = (ImageView)findViewById(R.id.ImgSystemState);
        ImgNoti = (ImageView)findViewById(R.id.ImgNoti);
        ImgPrev = (ImageView)findViewById(R.id.Prev);
        ImgPlay = (ImageView)findViewById(R.id.Play);
        ImgNext = (ImageView)findViewById(R.id.Next);
        ImgStop = (ImageView)findViewById(R.id.Stop);
        ImgSound = (ImageView)findViewById(R.id.ImgVolume);
        ImgLight = (ImageView)findViewById(R.id.ImgLight);
        ImgAlarm = (ImageView)findViewById(R.id.ImgAlarm);


        BRLight = (SwitchButton)findViewById(R.id.SBRLight);
        BRWindow = (SwitchButton) findViewById(R.id.BRWindow);
        KFan = (SwitchButton) findViewById(R.id.KFan);
        LRAirCondition = (SwitchButton)findViewById(R.id.LRAirCondition);
        LRLight = (SwitchButton)findViewById(R.id.LRLight);


        TVLight = (TextView)findViewById(R.id.TVLight);
        TVTemp = (TextView)findViewById(R.id.TVTemp);
        TVHum = (TextView)findViewById(R.id.TVHum);
        TVKTemp = (TextView)findViewById(R.id.TVKTemp);
        txtMusicName = (TextView)findViewById(R.id.txtMusicName);
        LRLightPercentage = (TextView)findViewById(R.id.LRLightPercentage);


        BRPercentage = (ToggleSwitch)findViewById(R.id.BRPercentage);
        LRPercentage = (ToggleSwitch)findViewById(R.id.LRPercentage);
        LVSetting = (ListView)findViewById(R.id.MoreControl);
        LVMusicList = (ListView)findViewById(R.id.LVMusicList);


        LRAirPercentage = (RangeBar)findViewById(R.id.LRAirPercentage);
        TCTime = (TextClock) findViewById(R.id.TCTime);
        Volume = (SeekBar)findViewById(R.id.VolumeValue);
        LRAuto = (ToggleButton)findViewById(R.id.LRAuto);

        alertDialog = new AlertDialog.Builder(this).create();

        ImgStop.setOnClickListener(this);
        ImgPrev.setOnClickListener(this);
        ImgPlay.setOnClickListener(this);
        ImgNext.setOnClickListener(this);
        ImgSound.setOnClickListener(this);


        BRLight.setOnCheckedChangeListener(this);
        BRWindow.setOnCheckedChangeListener(this);
        KFan.setOnCheckedChangeListener(this);
        LRAirCondition.setOnCheckedChangeListener(this);
        LRLight.setOnCheckedChangeListener(this);
        LRAuto.setOnCheckedChangeListener(this);


        LVSetting.setOnItemClickListener(this);
        LVMusicList.setOnItemClickListener(this);


        BRPercentage.setLabels(new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Percentage))));
        LRPercentage.setLabels(new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.Percentage))));
        BRPercentage.setCheckedTogglePosition(BRCheckedPosition);
        LRPercentage.setCheckedTogglePosition(LRCheckedPosition);

        LRAirPercentage.setTickStart(AirPercentage);
        LRAirPercentage.setSeekPinByValue(AirPercentage);

        DataBase DB = new DataBase(this);
        if(DB.GetUnAuthorizedUsers() != null)
            NotiState= true;
        else
            NotiState = false;

        if(NotiState)
            ImgNoti.setImageResource(R.drawable.notification);

        TCTime.setFormat12Hour("hh:mm aaa");

        // Check if Receiving Image service is running or not
        Boolean Val = isMyServiceRunning(ReceiveImg.class);
        if(!Val)
            startService(new Intent(getBaseContext(), ReceiveImg.class));


        // Control music player volume

        Volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new SendToServer(getApplicationContext(), "Sound:" + Volume.getProgress()).execute();
                // Toast.makeText(getApplicationContext(), "Value " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });


        for (int i = 0; i < LVSetting.getChildCount(); i++){
            View child = LVSetting.getChildAt(i);
            child.setEnabled(false);
        }


        new SendToServer(getApplicationContext(), "Get").execute();

        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                // Toast.makeText(getApplicationContext(), "R: " + SendToServer.ResponseMessage, Toast.LENGTH_LONG).show();
                UpdateActionBar(SendToServer.ResponseMessage);
            }
        }, 1500);


        new SendToServer(this, "MusicAll").execute();

        final Handler h2 = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                if(SendToServer.MusicFiles != null)
                    LVMusicList.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, new ArrayList<String>(Arrays.asList(SendToServer.MusicFiles.split(":")))));
            }
        }, 1000);

        LVSetting.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Items)));

        // Control Bed Room light
        BRPercentage.setOnToggleSwitchChangeListener(new ToggleSwitch.OnToggleSwitchChangeListener(){

            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                if(position != BRCheckedPosition) {
                    // Toast.makeText(getApplicationContext(), "Bed Room LED:" + getResources().getStringArray(R.array.Percentage)[position], Toast.LENGTH_SHORT).show();
                    new SendToServer(MainActivity.this, "BRLED:" + getResources().getStringArray(R.array.Percentage)[position].replace("%", "")).execute();
                    BRCheckedPosition = position;
                }
            }
        });

        // Control Living Room light
        LRPercentage.setOnToggleSwitchChangeListener(new ToggleSwitch.OnToggleSwitchChangeListener(){
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                if(LRLight.isChecked() && position != LRCheckedPosition) {
                    // Toast.makeText(getApplicationContext(), "Living Room LED:" + getResources().getStringArray(R.array.Percentage)[position], Toast.LENGTH_SHORT).show();
                    new SendToServer(MainActivity.this, "LRLED:" + getResources().getStringArray(R.array.Percentage)[position].replace("%", "")).execute();
                    LRCheckedPosition = position;
                }
            }
        });

        // Living Room air condition percentage
        LRAirPercentage.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {

                if (LRAirCondition.isChecked()) {
                    new SendToServer(MainActivity.this, "LRAir:" + (Integer.valueOf(rightPinValue) * 20)).execute();
                    AirPercentage = Integer.valueOf(rightPinValue);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        // Update actionbar every one minute
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                new SendToServer(getApplicationContext(), "Get").execute();
                final Handler h3 = new Handler();
                h3.postDelayed(new Runnable() {
                    public void run() {
                        UpdateActionBar(SendToServer.ResponseMessage);
                    }
                }, 1000);

                handler.postDelayed(this, 60*1000);

                Refresh();
            }
        }, 60*1000);

        InitActionBar();
    }

    private void Refresh(){
        DrawerLayout layout = (DrawerLayout) findViewById(R.id.Core);

        if(ReceiveState && SendState) {
            ImgSystemState.setImageResource(R.drawable.cloud_done);
            disableEnableControls(true, layout);
        }
        else if(ReceiveState && !SendState) {
            ImgSystemState.setImageResource(R.drawable.cloud_download);

            disableEnableControls(false, layout);
            TextView TVCheckUsers = (TextView) LVSetting.getChildAt(0);
            TVCheckUsers.setEnabled(true);
        }
        else if(!ReceiveState && SendState) {
            ImgSystemState.setImageResource(R.drawable.cloud_upload);

            disableEnableControls(true, layout);
            TextView TVCheckUsers = (TextView) LVSetting.getChildAt(0);
            TVCheckUsers.setEnabled(false);
        }
        else {
            ImgSystemState.setImageResource(R.drawable.cloud_off);
            disableEnableControls(false, layout);

            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected())
                showAlertDialog("Failed connection !", "Failed to connect with the server!\nDo you want try again ?!", R.drawable.sync_problem, false);
            else
                showAlertDialog("Connect Wi-Fi !", "Please make sure you're connect with the same system's WI-FI", R.drawable.sync_disabled, true);
        }

        if(NotiState)
            ImgNoti.setImageResource(R.drawable.notification);
        else
            ImgNoti.setImageBitmap(null);

        Calendar calendar = Calendar.getInstance();
        int H = calendar.get(Calendar.HOUR_OF_DAY);


        if(H >= 4 && H <= 11)
            ImgLight.setImageResource(R.drawable.sunrise);
        else if(H >= 12 && H <= 16)
            ImgLight.setImageResource(R.drawable.sun_icon);
        else if(H >= 17 && H <= 19)
            ImgLight.setImageResource(R.drawable.sunset);
        else
            ImgLight.setImageResource(R.drawable.night_icon);

        // Toast.makeText(getApplicationContext(), "H: "+H, Toast.LENGTH_LONG).show();
    }

    private void disableEnableControls(boolean enable, ViewGroup DL){
        for (int i = 0; i < DL.getChildCount(); i++){
            View child = DL.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup){
                disableEnableControls(enable, (ViewGroup) child);
            }
        }

        LVSetting.setEnabled(true);
        TextView TV = (TextView) LVSetting.getChildAt(LVSetting.getChildCount()-1);
        TV.setEnabled(true);
        TV = (TextView) LVSetting.getChildAt(LVSetting.getChildCount()-2);
        TV.setEnabled(true);
    }

    private void showAlertDialog(String title, String Message, int iconID, boolean isCanelable){
        if(!alertDialog.isShowing()) {
            alertDialog.setTitle(title);
            alertDialog.setMessage(Message);
            alertDialog.setIcon(iconID);
            if (!isCanelable) {
                alertDialog.setButton(Dialog.BUTTON_POSITIVE,"Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RefreshSystemStates();
                    }
                });
                alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            } else {
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            }
            alertDialog.show();
        }
    }

    // Init system state image
    private void InitActionBar() {

        Animation sync = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);

        ImgSystemState.startAnimation(sync);

        Handler H = new Handler();
        H.postDelayed(new Runnable() {
            @Override
            public void run() {
                Refresh();
                if(ReceiveState && !SendState)
                    showAlertDialog("Receive only !", "You can only receive any images from the system!\nDo you want to try connection?!", R.drawable.cloud_download, false);
                else if(!ReceiveState && SendState)
                    showAlertDialog("Send only !", "You can only send control instructions to the system!\nDo you want to try connection?!", R.drawable.cloud_upload, false);

                LVSetting.setEnabled(true);
                TextView TV = (TextView) LVSetting.getChildAt(LVSetting.getChildCount()-1);
                TV.setEnabled(true);
            }
        }, 2500);

        Animation NotiAnm = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.notification);
        ImgNoti.setAnimation(NotiAnm);

    }

    // Update Temperature, Humidity, Light and Music sound
    private void UpdateActionBar(String Res) {
        if(Res != null) {
            String[] Values = Res.replace("\"", "").split(":");
            for (int i=0; i<Values.length; i++){
                if(i % 2 == 1){
                    int x = i;
                    x--;
                    if(Values[x].equals("LDR"))
                        TVLight.setText(Values[i] + " %");
                    else if(Values[x].equals("Temp"))
                        TVTemp.setText(Values[i].substring(0, Values[i].length()-3));
                    else if(Values[x].equals("Hum"))
                        TVHum.setText(Values[i].substring(0, Values[i].length()-3) + " %");
                    else if(Values[x].equals("LM"))
                        TVKTemp.setText(Values[i]);
                    else if(Values[x].equals("Light")) {
                        LRLightPercentage.setText(Values[i]);

                        if(LVAuto) {
                            LRLightVal = Integer.parseInt(Values[i]);
                            int Light = 100 - LRLightVal;
                            if (Light < 10)
                                Light = 0;
                            new SendToServer(MainActivity.this, "LRLED:" + Light).execute();
                        }
                    }
                    else if(Values[x].equals("Sound"))
                        Volume.setProgress(Integer.parseInt(Values[i]));
                    else if(Values[x].equals("Alarm"))
                        if(Values[i].contains("True"))
                            ImgAlarm.setImageResource(R.drawable.alarm_on);
                        else
                            ImgAlarm.setImageResource(R.drawable.alarm_off);
                    else if(Values[x].equals("BR")) {
                        if (Integer.parseInt(Values[i]) == 0)
                            BRLight.setChecked(false);
                        else {
                            int Pos = (Integer.parseInt(Values[i]) / 25) - 1;
                            BRPercentage.setCheckedTogglePosition(Pos);
                            BRLight.setChecked(true);
                        }
                    }
                    else if(Values[x].equals("LR")){
                        if (Integer.parseInt(Values[i]) == 0)
                            LRLight.setChecked(false);
                        else if(!LVAuto){
                            int Pos = (Integer.parseInt(Values[i]) / 25) - 1;
                            LRPercentage.setCheckedTogglePosition(Pos);
                            LRLight.setChecked(true);
                        }
                    }
                    else if(Values[x].equals("BW")){
                        if(Values[i].contains("1"))
                            BRWindow.setChecked(true);
                        else
                            BRWindow.setChecked(false);
                    }
                    else if(Values[x].equals("KF")){
                        if(Values[i].contains("True"))
                            KFan.setChecked(true);
                        else
                            KFan.setChecked(false);
                    }
                    else if(Values[x].equals("Air")){
                        if(Integer.parseInt(Values[i]) == 0)
                            LRAirCondition.setChecked(false);
                        else {
                            AirPercentage = (Integer.parseInt(Values[i]) / 20);
                            LRAirCondition.setChecked(true);
                            LRAirPercentage.setSeekPinByValue(AirPercentage);
                            LRAirPercentage.setVisibility(View.VISIBLE);
                        }

                    }
                }
            }
        }
    }

    // Check if service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // On Click (Stop, Prev, Play, Next, Volume Image) for music player

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Stop:{
                ImgPlay.setImageResource(R.drawable.play);
                new SendToServer(this, "Music:stop:0").execute();
                // Toast.makeText(this, "Stop", Toast.LENGTH_LONG).show();
                PlayMusic = null;
            }break;
            case R.id.Prev:{
                if(CurrentMusicIndex>0)
                    new SendToServer(this, "Music:play:" + --CurrentMusicIndex).execute();
                else {
                    new SendToServer(this, "Music:play:" + (LVMusicList.getCount() - 1)).execute();
                    CurrentMusicIndex = LVMusicList.getCount() - 1;
                }

                PlayMusic = true;

                if(LVMusicList.getCount() > 0)
                    txtMusicName.setText(LVMusicList.getAdapter().getItem(CurrentMusicIndex).toString());
                // Toast.makeText(this, "Prev " + (LVMusicList.getCount() - 1), Toast.LENGTH_LONG).show();
            }break;
            case R.id.Play:{
                if(PlayMusic == null) {
                    new SendToServer(this, "Music:play:" + CurrentMusicIndex).execute();
                    ImgPlay.setImageResource(R.drawable.play);
                    PlayMusic = true;
                }
                else if(PlayMusic.booleanValue() == true) {
                    ImgPlay.setImageResource(R.drawable.pause);
                    // Toast.makeText(this, "Pause", Toast.LENGTH_LONG).show();
                    new SendToServer(this, "Music:pause:0").execute();
                    PlayMusic = false;
                }
                else if (PlayMusic.booleanValue() == false){
                    ImgPlay.setImageResource(R.drawable.play);
                    // Toast.makeText(this, "Resume", Toast.LENGTH_LONG).show();
                    new SendToServer(this, "Music:resume:0").execute();
                    PlayMusic = true;
                }

            }break;
            case R.id.Next:{
                if(CurrentMusicIndex < LVMusicList.getCount() - 1)
                    new SendToServer(this, "Music:play:" + ++CurrentMusicIndex).execute();
                else {
                    new SendToServer(this, "Music:play:0").execute();
                    CurrentMusicIndex = 0;
                }

                PlayMusic = true;

                if(LVMusicList.getCount() > 0)
                    txtMusicName.setText(LVMusicList.getAdapter().getItem(CurrentMusicIndex).toString());

                // Toast.makeText(this, "Next Count " + LVMusicList.getCount(), Toast.LENGTH_LONG).show();
            }break;
            case R.id.ImgVolume:{
                if(!Mute){
                    ImgSound.setImageResource(R.drawable.mute);
                    new SendToServer(this, "Sound:0").execute();
                    Mute = true;
                }
                else {
                    ImgSound.setImageResource(R.drawable.sound);
                    new SendToServer(this, "Sound:" + Volume.getProgress()).execute();
                    Mute = false;
                }
            }break;
        }
    }

    // On Item Click (Check users, Alarm, Outside mode, choose music file)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.MoreControl) {
            switch (position) {
                case 0: {
                    // Check Access

                    if(((TextView)LVSetting.getChildAt(0)).isEnabled()) {
                        Intent I = new Intent(this, Users.class);
                        startActivity(I);
                    }
                    else
                        showAlertDialog("Send only !", "You can only send control instructions to the system!\nDo you want to try connection?!", R.drawable.cloud_upload, false);
                }
                break;
                case 1: {
                    // Alarm

                    if(view.isEnabled()) {
                        Intent I = new Intent(this, alarm.class);
                        startActivity(I);
                    }
                }
                break;

                case 2: {
                    // Outside Mode

                    if(view.isEnabled()) {
                        BRLight.setChecked(false);
                        ImgPlay.setImageResource(R.drawable.play);
                        new SendToServer(this, "Music:stop:0").execute();
                        LRLight.setChecked(false);
                        LRAirCondition.setChecked(false);
                        BRWindow.setChecked(false);
                    }

                    android.app.AlertDialog newDialog = new android.app.AlertDialog.Builder(this).create();
                    final android.app.AlertDialog AlertDialog = new android.app.AlertDialog.Builder(this).create();
                    newDialog.setTitle("Enable location !");
                    newDialog.setMessage("Do you want to enable your location service ?!");
                    newDialog.setIcon(R.mipmap.location);

                    newDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);


                            AlertDialog.setTitle("Internet connection !");
                            AlertDialog.setMessage("To open/close garage automatically please enable internet connection with location service");
                            AlertDialog.setIcon(R.mipmap.warning);
                            AlertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            AlertDialog.show();
                        }
                    });
                    newDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    });

                    newDialog.show();

                }
                case 3:{
                    // Refresh

                    RefreshSystemStates();
                }
            }
        }
        else if(parent.getId() == R.id.LVMusicList){
            new SendToServer(this, "Music:play:" + position).execute();
            CurrentMusicIndex = position;
            if(LVMusicList.getCount() > 0)
                txtMusicName.setText(LVMusicList.getAdapter().getItem(CurrentMusicIndex).toString());
        }
    }

    // on Checked Changed (Bed Room light percentage - Bed Room window state - Kitchen fan state - Living Room light percentage - Living Room fan speed)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(buttonView.getId() == BRLight.getId()){
            if(isChecked) {
                BRPercentage.setVisibility(View.VISIBLE);
                new SendToServer(MainActivity.this, "BRLED:" + getResources().getStringArray(R.array.Percentage)[BRCheckedPosition].replace("%", "")).execute();
                // Toast.makeText(this, "BR: " + isChecked, Toast.LENGTH_LONG).show();
            }
            else {
                BRPercentage.setVisibility(View.INVISIBLE);
                new SendToServer(MainActivity.this, "BRLED:0").execute();
                // Toast.makeText(this, "C: " + isChecked, Toast.LENGTH_LONG).show();
            }

        }
        else if(buttonView.getId() == BRWindow.getId()){
            new SendToServer(MainActivity.this, "BRWin:" + (isChecked? "Open":"Close" )).execute();
            // Toast.makeText(this, "BRWin:" + (isChecked? 1:0 ), Toast.LENGTH_SHORT).show();
        }
        else if(buttonView.getId() == KFan.getId()){
            new SendToServer(MainActivity.this, "KFan:" + (isChecked? "Open":"Close" )).execute();
            // Toast.makeText(this, "KFan:" + (isChecked? 1:0 ), Toast.LENGTH_SHORT).show();
        }
        else if(buttonView.getId() == LRLight.getId()){
            if(isChecked){
                LRPercentage.setVisibility(View.VISIBLE);
                new SendToServer(MainActivity.this, "LRLED:"+ getResources().getStringArray(R.array.Percentage)[LRCheckedPosition].replace("%", "")).execute();
                LVAuto = false;
            }
            else {
                LRPercentage.setVisibility(View.INVISIBLE);
                if(LRAuto.isChecked()) {
                    LVAuto = true;
                    new SendToServer(MainActivity.this, "LRLED:"+ (100 - LRLightVal)).execute();
                }
                else
                    new SendToServer(MainActivity.this, "LRLED:0").execute();
            }
            // Toast.makeText(this, "C: " + isChecked, Toast.LENGTH_LONG).show();
        }
        else if(buttonView.getId() == LRAirCondition.getId()){
            if(isChecked) {
                new SendToServer(MainActivity.this, "LRAir:" + (AirPercentage * 20)).execute();
                // Toast.makeText(this, "Air Condition turned " + (isChecked ? "On" : "Off"), Toast.LENGTH_LONG).show();
                LRAirPercentage.setVisibility(View.VISIBLE);
            }
            else {
                LRAirPercentage.setVisibility(View.INVISIBLE);
                new SendToServer(MainActivity.this, "LRAir:0").execute();
            }
        }
        else if(buttonView.getId() == LRAuto.getId()){
            LRLightVal = Integer.parseInt((String) LRLightPercentage.getText());
            int Light = 100 - LRLightVal;
            if(Light < 10)
                Light = 0;
            if(isChecked) {
                new SendToServer(MainActivity.this, "LRLED:" + Light).execute();
                //Toast.makeText(this, "T", Toast.LENGTH_LONG).show();
            }
            else {
                if(LRPercentage.getVisibility() == View.VISIBLE)
                    new SendToServer(MainActivity.this, "LRLED:" + getResources().getStringArray(R.array.Percentage)[LRCheckedPosition].replace("%", "")).execute();
                else
                    new SendToServer(MainActivity.this, "LRLED:0").execute();
            }
                //Toast.makeText(this, "F", Toast.LENGTH_LONG).show();

            LVAuto = isChecked;
        }

    }

    private void RefreshSystemStates(){

            startService(new Intent(getBaseContext(), ReceiveImg.class));

            new SendToServer(getApplicationContext(), "Get").execute();

            final Handler h = new Handler();
            h.postDelayed(new Runnable() {
                public void run() {
                    UpdateActionBar(SendToServer.ResponseMessage);
                }
            }, 2000);

            new SendToServer(this, "MusicAll").execute();

            h.postDelayed(new Runnable() {
                public void run() {
                    if (SendToServer.MusicFiles != null)
                        LVMusicList.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, new ArrayList<String>(Arrays.asList(SendToServer.MusicFiles.split(":")))));
                }
            }, 1000);

        ImgSystemState.setImageResource(R.drawable.sync_began);

        Animation sync = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);

        ImgSystemState.startAnimation(sync);

        Handler H = new Handler();
        H.postDelayed(new Runnable() {
            @Override
            public void run() {
                Refresh();
            }
        }, 2500);
    }
}
