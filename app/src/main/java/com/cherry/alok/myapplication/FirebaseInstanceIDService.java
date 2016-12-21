package com.cherry.alok.myapplication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by alok on 26/7/16.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    static String  device_firebase_token;
    @Override
    public void onTokenRefresh() {
        try {
            device_firebase_token = FirebaseInstanceId.getInstance().getToken();
            AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
            Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
            Account[] list = manager.getAccounts();
            String gmail = null;

            for(Account account: list)
            {
                if(account.type.equalsIgnoreCase("com.google"))
                {
                    gmail = account.name;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //getEmiailID(getApplicationContext());
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
