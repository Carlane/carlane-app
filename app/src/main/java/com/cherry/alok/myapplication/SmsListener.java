package com.cherry.alok.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by alok on 20/12/16.
 */
public interface SmsListener {
    public void messageReceived(SmsMessage messageText);
}


