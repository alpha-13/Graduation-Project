package com.example.mohamedelsayed.icontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Mohamed Elsayed on 04/02/2017.
 */
public class Receiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent I = new Intent(context, ReceiveImg.class);
            context.startService(I);
        }
    }
}
