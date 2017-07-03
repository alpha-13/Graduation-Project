package com.example.mohamedelsayed.icontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;

import java.util.Calendar;

/*
    This class is used for setting alarm and we can also start cafe machine before alarm starts ..
     and open bedroom window after alarm
*/

public class alarm extends AppCompatActivity implements View.OnClickListener {

    Button btnConfirm, btnCancel;
    TimePicker TPAlarm;

    CheckBox CBCafe, CBWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alarm);

        btnConfirm = (Button)findViewById(R.id.btnConfirmAlarm);
        btnCancel = (Button) findViewById(R.id.btnCancelAlarm);
        TPAlarm = (TimePicker) findViewById(R.id.TPicker);
        CBCafe = (CheckBox) findViewById(R.id.CBCafe);
        CBWindow = (CheckBox) findViewById(R.id.CBWindow);

        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        int H = calendar.get(Calendar.HOUR);
        int M = calendar.get(Calendar.MINUTE);

        TPAlarm.setCurrentHour(H);
        TPAlarm.setCurrentMinute(M);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnConfirmAlarm:{
                Calendar calendar = Calendar.getInstance();

                String AlarmStr = (TPAlarm.getCurrentHour()>12? 24 - TPAlarm.getCurrentHour():TPAlarm.getCurrentHour())+":"+TPAlarm.getCurrentMinute()+ (TPAlarm.getCurrentHour()>12? " PM": " AM");
                // Toast.makeText(this, AlarmStr, Toast.LENGTH_LONG).show();

                int DiffH = TPAlarm.getCurrentHour() - calendar.get(Calendar.HOUR_OF_DAY);
                int DiffM = TPAlarm.getCurrentMinute() - calendar.get(Calendar.MINUTE);
                int Days = 0;

                if(DiffH < 0)
                    DiffH = -DiffH;

                if(DiffM < 0) {
                    DiffM = 60 + DiffM;
                    DiffH--;
                    if(DiffH < 0)
                        DiffH = 23;
                }

                if(DiffH == 0 && DiffM == 0)
                    Days =1;

                String Message = "Alarm D:" + Days + ":H:" + DiffH + ":M:" + DiffM + ":C:" + (CBCafe.isChecked()? 1 : 0) + ":W:"+(CBWindow.isChecked()? 1: 0);

                showAlertDialog("Setting Alarm", "Alarm was set at " + AlarmStr + (CBCafe.isChecked()? "\nStarting cafe machine": "")+(CBWindow.isChecked()?"\nRoom window will open": ""), R.drawable.alarm_on);

                new SendToServer(this, Message).execute();

            }break;
            case R.id.btnCancelAlarm:{
                Intent I = new Intent(this, MainActivity.class);
                startActivity(I);
            }break;
        }
    }

    private void showAlertDialog(String title, String Message, int iconID) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(Message);
        alertDialog.setIcon(iconID);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        alertDialog.show();
    }
}
