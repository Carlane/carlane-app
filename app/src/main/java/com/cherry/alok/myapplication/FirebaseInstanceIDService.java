package com.cherry.alok.myapplication;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by alok on 26/7/16.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    static String  device_firebase_token;
    @Override
    public void onTokenRefresh() {
        device_firebase_token = FirebaseInstanceId.getInstance().getToken();
    }

    public String GetFireBaseToken()
    {
        if(device_firebase_token == null)
        {
            onTokenRefresh();
        }
        return device_firebase_token;
    }
}
