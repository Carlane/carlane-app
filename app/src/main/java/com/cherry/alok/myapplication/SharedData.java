package com.cherry.alok.myapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by alok on 3/6/16.
 */
public class SharedData {

    //static ArrayList<Boolean> slotsInfo = new ArrayList<Boolean>();

    public enum UserStatus
    {
      NewProfile(1),
      CarProfile(2),
      RequestPending(3),
      FeedbackPending(4);
        private int id;

        UserStatus(int id){
            this.id = id;
        }

        public int getID(){
            return id;
        }

    };
    static Boolean[] slotsInfo = new Boolean[]{false, false, false, false, false, false, false, false, false, false};

    private static int USER_LOG_ID;
    private static String USER_NAME;
    private static String USER_EMAIL;
    private static Bitmap USER_PIC;
    private static int SERVICE_TYPE;
    private static String USER_TOKEN;
    private static LatLng USER_SERVICE_LATLNG;
    private static String USER_DEFAULT_CAR_REG = null;
    private static String USER_DEFAULT_CAR_MODEL = null;
    private static String USER_DEFAULT_CAR_BRAND = null;
    private static int REQUEST_TIME_SLOT = -1;

    public static void ReInitSlots() {
        for (int i = 0; i < 10; i++) {
            slotsInfo[i] = false;
        }
    }

    public static void SetUserEmail(String email) {
        USER_EMAIL = email;
    }

    public static void SetUserName(String name) {
        USER_NAME = name;
    }

    public static void SetUserId(int user_id) {
        if (user_id == 0) {
            return;

        }
        USER_LOG_ID = user_id;
    }

    public static void SetUserpic(Bitmap pic) {
        USER_PIC = pic;
    }

    public static void SetUserToken(String tokendata) {
        USER_TOKEN = tokendata;
    }

    public static void SetService(int car_service) {
        SERVICE_TYPE = car_service;
    }


    public static String GetUserEmail() {
        return USER_EMAIL;
    }

    public static String GetUserName() {
        return USER_NAME;
    }

    public static int GetUserId() {

        USER_LOG_ID = myDbHelper.GetUserId();
        return USER_LOG_ID;
    }

    public static Bitmap GetUserpic() {
        return USER_PIC;
    }

    public static int GetService() {
        return SERVICE_TYPE == 0 ? 1 : SERVICE_TYPE;
    }

    public static String GetServiceName()
    {
        String name="Basic";
        switch (GetService())
        {
            case 1:
                name = "Basic";
                break;
            case 2:
                name ="Premium";
                break;
            case 3:
                name="Platinum";
                break;
        }
        return name;

    }

    public static  String GetRequestStatus(int status_id)
    {
        String request_status = "Request Placed";
        switch (status_id)
        {
            case 1:
            {
                request_status = "Request Placed";
            }
            break;
            case 2:
            {
                request_status = "Driver Allocated";
            }
            break;
            case 3:
            {
                request_status = "Driver OnWay to Garage";
            }
            break;
            case 4:
            {
                request_status = "Vehicle Picked Up";
            }
            break;
            case 5:
            {
                request_status = "Vehicle At Garage";
            }
            break;
            case 6:
            {
                request_status = "Vehicle Servicing Start";
            }
            break;
            case 7:
            {
                request_status = "Vehicle On Way back";
            }
            break;
            case 8:
            {
                request_status = "Request Completed";
            }
            break;
        }
        return request_status;
    }

    public static String GetSlotName(int slot)
    {
        String slot_name = "9AM - 12 AM";
        switch(slot)
        {
            case 1:
            {
                slot_name = "9AM - 12 AM";
            }
            break;
            case 2:
            {
                slot_name = "12 PM - 2PM";
            }
            break;
            case 3:
            {
                slot_name = "2PM - 4PM";
            }
            break;
            case 4:
            {
                slot_name = "4PM - 6PM";
            }
            break;

        }
        return slot_name;
    }


    public static String SetUserToke() {
        return USER_TOKEN;
    }

    public static void SetRequestLocation(LatLng address) {
        USER_SERVICE_LATLNG = address;
    }

    public static LatLng GetRequestLocation() {
        return USER_SERVICE_LATLNG;
    }

    public static void SetTimeSlot(int value) {
        REQUEST_TIME_SLOT = value;
    }

    public static int GetTimeSlot() {
        return REQUEST_TIME_SLOT;
    }

    public static String GetDefaultCarNo() {
        if(USER_DEFAULT_CAR_REG == null)
        {
            FetchUserCarDetailsFromDb();
        }
        return USER_DEFAULT_CAR_REG;
    }
    public static  String GetDefaultCarModel()
    {
        if(USER_DEFAULT_CAR_MODEL == null)
        {
            FetchUserCarDetailsFromDb();
        }
        return USER_DEFAULT_CAR_MODEL;
    }
    public static String GetDefaultCarBrand()
    {
        if(USER_DEFAULT_CAR_BRAND == null)
        {
            FetchUserCarDetailsFromDb();
        }
        return USER_DEFAULT_CAR_BRAND;
    }

    static Activity currentActivity;
    static Intent intent_addCar;
    static Intent intent_location;
    static Intent intent_usercar;
    static Intent intent_services;
    static Intent intent_startMessage;
    static Intent intent_selectSlot;
    static Intent intent_pastorders;
    static Intent intent_feedback;
    static DataBaseHelper myDbHelper;

    private static Handler sharedDataHandler = new Handler() {

        public void handleMessage(Message msg) {

            int id = msg.getData().getInt("navigateTo");
            Bundle dataToSend  = msg.getData().getBundle("DataForActivity");

            if ((-1 != id)) {

                if (id == R.id.nav_add_car) {

                    if (intent_addCar == null) {
                        intent_addCar = new Intent(currentActivity, Activity_AddCar.class);
                    }
                    if(clearStackOfLastActivity)
                    {
                        clearStackOfLastActivity = false;
                        intent_addCar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent_addCar.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                    currentActivity.startActivity(intent_addCar);

                    // Handle the camera action
                } else if (id == R.id.nav_location) {
                    if (intent_location == null) {
                        intent_location = new Intent(currentActivity, LocationActivityMap.class);
                    }
                    if(clearStackOfLastActivity)
                    {
                        clearStackOfLastActivity = false;
                        intent_location.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent_location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                    currentActivity.startActivity(intent_location);


                } else if (id == R.id.nav_manage) {
                    if (intent_usercar == null) {
                        intent_usercar = new Intent(currentActivity, UserCar_CollapseHeader.class);
                    }
                    if(clearStackOfLastActivity)
                    {
                        clearStackOfLastActivity = false;
                        intent_usercar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent_usercar.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }

                    currentActivity.startActivity(intent_usercar);


                } else if (id == R.id.nav_service_request) {
                    if (intent_services == null) {
                        intent_services = new Intent(currentActivity, Activity_Services.class);
                    }
                    currentActivity.startActivity(intent_services);


                } else if (id == R.id.nav_share) {
                    if (intent_startMessage == null) {
                        intent_startMessage = new Intent(currentActivity, PagerActivity.class);
                    }
                    currentActivity.startActivity(intent_startMessage);

                } else if (id == R.id.nav_selectslot) {
                    if (intent_selectSlot == null) {
                        intent_selectSlot = new Intent(currentActivity, SelectSlotActivity.class);
                    }
                    currentActivity.startActivity(intent_selectSlot);
                }
                else if(id == R.id.nav_order)
                {
                    if (intent_pastorders == null) {
                        intent_pastorders = new Intent(currentActivity, OrderActivity.class);
                    }
                        intent_pastorders.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent_pastorders.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        if(dataToSend != null)
                        {
                            intent_pastorders.putExtras(dataToSend);
                        }
                        currentActivity.startActivity(intent_pastorders);

                }
                else if(id == R.id.nav_feedback)
                {
                    if(intent_feedback == null)
                    {
                        intent_feedback = new Intent(currentActivity , FeedbackActivity.class);
                            intent_feedback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent_feedback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                    currentActivity.startActivity(intent_feedback);

                }

                //currentActivity.finish();;
                currentActivity.overridePendingTransition(R.anim.right_in, R.anim.right_out);

            }
        }
    };

    public static void CleanUpOnExit()
    {
        currentActivity = null;
        intent_pastorders =null;
        intent_selectSlot = null;
        intent_startMessage = null;
        intent_services = null;
        intent_addCar = null;
        intent_location = null;
    }
    static boolean clearStackOfLastActivity = false;
    public static void HandleNavigation(int id, Activity object , boolean clearStack) {
        clearStackOfLastActivity = clearStack;
        currentActivity = object;
        Message msgObj = sharedDataHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("navigateTo", id);
        msgObj.setData(b);
        sharedDataHandler.sendMessageDelayed(msgObj, 400);
    }

    public static void HandleNavigation(int id, Activity object) {
        currentActivity = object;
        Message msgObj = sharedDataHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("navigateTo", id);
        msgObj.setData(b);
        sharedDataHandler.sendMessageDelayed(msgObj, 400);
    }

    public static void HandleNavigation(int id, Activity object , Bundle bundle) {
        currentActivity = object;
        Message msgObj = sharedDataHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("navigateTo", id);
        if(bundle != null)
        {
            b.putBundle("DataForActivity" , bundle);
        }
        msgObj.setData(b);
        sharedDataHandler.sendMessageDelayed(msgObj, 400);
    }

    public static void CreateDataBase() {


        try {

            SharedData.myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            SharedData.myDbHelper.openDataBase();
            Cursor rs = SharedData.myDbHelper.getStatus();

            rs.moveToFirst();
            while (rs.isAfterLast() == false) {
                String nam = rs.getString(rs.getColumnIndex("status"));

                rs.moveToNext();
            }

        } catch (SQLException sqle) {

            throw sqle;

        }
    }

    public static void DeleteAllUser()
    {
        myDbHelper.DeleteAllUser();
    }

    public static void DeleteAllUserCar()
    {
        myDbHelper.DeleteAllUserCars();
    }


    public static boolean InsertUser(String name, String phone, String email, String token) {
        return myDbHelper.InsertUser(name, phone, email, token);
    }

    public static boolean CheckUserExistByName(String name)
    {
        return myDbHelper.CheckUser(name);
    }

    public static boolean InsertUserCar(String model , String brand , String regno , String yom)
    {
        return myDbHelper.InsertCar(model,brand,regno,yom);
    }

    public static boolean UpdateUserPhone (String phone)
    {
        return myDbHelper.UpdateUserPhone(phone);
    }

    public static boolean FetchUserCarDetailsFromDb()
    {
        HashMap<String,String> crdetailsmap = myDbHelper.GetUserCarDetails();
        try {
            USER_DEFAULT_CAR_REG = crdetailsmap.get("regno");
            USER_DEFAULT_CAR_BRAND = crdetailsmap.get("model");
            USER_DEFAULT_CAR_MODEL = crdetailsmap.get("brand");
        } catch (Exception e) {
            USER_DEFAULT_CAR_REG = null;
            USER_DEFAULT_CAR_BRAND = null;
            USER_DEFAULT_CAR_MODEL = null;
            return false;
        }
        return crdetailsmap.size() > 0 ? true : false;
    }

    public static boolean UpdateUserStatusInDb(int status)
    {
        return myDbHelper.UpdateUserStatus(status);
    }


    public static boolean UpdateUserIdInDb(int id)
    {
        return myDbHelper.UpdateUserId(id);
    }

    public static HashMap<String , String> FetchUser()
    {
        return myDbHelper.FetchUser();
    }
}


