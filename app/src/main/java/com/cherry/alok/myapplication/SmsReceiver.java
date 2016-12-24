package com.cherry.alok.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle data  = intent.getExtras();

            Object[] pdus = (Object[]) data.get("pdus");
            SmsMessage smsMessage;
            for(int i=0;i<pdus.length;i++){

                String format = data.getString("format");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i] , format);
                }
                else
                {
                     smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                String sender = smsMessage.getDisplayOriginatingAddress();
                //You must check here if the sender is your provider and not another one with same text.

                String messageBody = smsMessage.getMessageBody();

                //Pass on the text to our listener.
                if(mListener!= null)mListener.messageReceived(smsMessage);
            }
        } catch (Exception e) {
            //suppressing it for now
            e.printStackTrace();
        }

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}
