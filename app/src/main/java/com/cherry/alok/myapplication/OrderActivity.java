package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OrderActivity extends AppCompatActivity {
    UniversalAsyncTask uniTask;
    private ProgressDialog mProgressDialog;
    private ProgressBar firstBar = null;

    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_order);
            Bundle bundle = getIntent().getExtras();
            // HandleBundleDataForUI(bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Order");

        if (SharedData.GetDefaultCarNo() != null) {
            Init_StatusUpdate();
        } else {
            InitCarDetailsInfoReq();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.querystatus_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Init_StatusUpdate();

            }
        });
        firstBar = (ProgressBar) findViewById(R.id.firstBar);
        firstBar.setMax(8);
    }

 /*   private void HandleBundleDataForUI(Bundle bndl)
    {
        String driver =  bndl.getString("driver");
        String joint =  bndl.getString("joint");
        int request_status =  bndl.getInt("request_status");
        int slot = bndl.getInt("slot");
        int daysAhead = bndl.getInt("days_ahead");

        UpdateDriverNameText(driver);
        UpdateRequestStatus(request_status);
        UpdateServiceTypeText();
        UpdateCarRegText();
        UpdateCarModelText();
        UpdateDateText();
        UpdateSlotText(slot);
    }*/

    public void UpdateDriverNameText(String driver) {
        TextView driver_text = (TextView) findViewById(R.id.driver_name_text);
        driver_text.setText(driver);
    }

    public void UpdateRequestStatus(int request_status) {
        /*TextView status_text = (TextView) findViewById(R.id.current_status_text);
        status_text.setText(SharedData.GetRequestStatus(request_status));*/
    }

    public void UpdateServiceTypeText() {
        TextView service_text = (TextView) findViewById(R.id.service_type);
        service_text.setText(SharedData.GetServiceName());
    }

    public void UpdateDriverMobile(final String driver_mobile) {
        TextView driver_mobile_text = (TextView) findViewById(R.id.driver_phone_value);
        driver_mobile_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + driver_mobile));
                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
            }
        });
        driver_mobile_text.setText(driver_mobile);
    }

    public  void UpdateCarRegText()
    {
        TextView carreg_text = (TextView)findViewById(R.id.car_reg_text);
        carreg_text.setText(SharedData.GetDefaultCarNo());
    }

    public void UpdateCarModelText()
    {
        TextView carmodel_text = (TextView)findViewById(R.id.car_model_text);
        carmodel_text.setText(SharedData.GetDefaultCarModel());
    }
    public void UpdateSlotText(int slot)
    {
        TextView slot_text = (TextView)findViewById(R.id.req_slot_text);
        slot_text.setText(SharedData.GetSlotName(slot));
    }

    public void UpdateDateText(String date)
    {
        TextView date_Text = (TextView)findViewById(R.id.req_date_text);
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DATE,daysAhead);
       // String date[] = cal.getTime().toString().split("\\s+");
        date_Text.setText(date);
    }


    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            SharedData.CleanUpOnExit();
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    private void Init_StatusUpdate()
    {
       // showProgressDialog("Fetching Order Details");
        String url = "requestatusinfo/"+SharedData.GetUserId()+"/";
        String urlParameters = String.format("car_reg=%s" ,SharedData.GetDefaultCarNo());

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,orderScreenHandler);
        uniTask.execute();
    }

    private Handler orderScreenHandler = new Handler() {

        public void handleMessage(Message msg) {
            String aresponse = msg.getData().getString("taskstatus");
            if(aresponse != null)
            {
                PostStatusUpdateOperation();
                hideProgressDialog();
            }

        }
    };

    public void InitCarDetailsInfoReq()
    {
        String url = "CarInfo/"+SharedData.GetUserId()+"/";
        //Toast.makeText(this , url, Toast.LENGTH_SHORT);
        uniTask = new UniversalAsyncTask(url,"GET","" ,orderScreenCarHandler);

        ArrayList<String> dummy = new ArrayList<String>();
        uniTask.execute(dummy);
        showProgressDialog("Fetching Your Order Details");
    }

    void PostStatusUpdateOperation()
    {
        try {
            String outputStr = uniTask.outputJasonString.toString();
            JSONObject jsonRootObject = null;
            try
            {
                jsonRootObject = new JSONObject(uniTask.outputJasonString);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray jsonArray = jsonRootObject.optJSONArray("response");
            JSONObject jsonObject = null;
            try
            {
                jsonObject = jsonArray.getJSONObject(0);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            int userIdBkEnd = Integer.parseInt(jsonObject.optString("id").toString());
            Boolean error_in_Result = Boolean.parseBoolean(jsonObject.optString("error").toString());
            if(error_in_Result == true)
            {
                return;
            }

            int status_id  =  Integer.parseInt(jsonObject.optString("request_status").toString());
            UpdateRequestStatus(status_id);
            firstBar.setProgress(status_id);
            if(status_id ==8)
            {
                //Need to update user status in app db and switch to location map
                SharedData.UpdateUserStatusInDb(2);
                SharedData.HandleNavigation(R.id.nav_location, this);
            }

            String driver = jsonObject.optString("driverfirstname").toString() +" "+ jsonObject.optString("driverlastname").toString();
            UpdateDriverNameText(driver);
            String driver_mobile = jsonObject.optString("drivermobile").toString() ;//drivermobile
            String date = jsonObject.optString("date").toString();
            int slot =  Integer.parseInt(jsonObject.optString("timeslot").toString());


            UpdateServiceTypeText();
            UpdateCarRegText();
            UpdateCarModelText();
            UpdateDateText(date);
            UpdateDriverMobile(driver_mobile);
            UpdateSlotText(slot);

            SharedData.UpdateUserStatusInDb(3);//Update
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
        }


    }
    List<HashMap<String,String>> usercarDetailsMap = new ArrayList<>();

    private  Handler orderScreenCarHandler = new Handler() {

        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("taskstatus");

            if ((null != aResponse)) {

                if(uniTask != null)
                {
                    if(!uniTask.IsSuccess())
                    {

                        String reason = uniTask.ResultReason();
                        Toast.makeText(getApplicationContext(), "Failed to Fetch Data- "+reason, Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        hideProgressDialog();
                        PostOperation();
                        if(SharedData.FetchUserCarDetailsFromDb() == false)
                        {
                            //update this information in Database also
                            SaveCarInfoInDb();

                        }


                        Init_StatusUpdate();
                    }
                }
            }
            else
            {
                //Toast.makeText(getBaseContext(), "Not Got Response From Server.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    public void PostOperation()
    {
        JSONArray reposnejSonArray = uniTask.GetOutputResult();
        int count = reposnejSonArray.length();
        for(int i=0;i<count;i++)
        {
            JSONObject jsonresponseObject = null;
            try
            {
                jsonresponseObject = reposnejSonArray.getJSONObject(i);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            Iterator<?> keys = jsonresponseObject.keys();
            String data = new String();
            HashMap<String,String> responseMap = new HashMap<String,String>();
            while( keys.hasNext() )
            {
                String key = (String)keys.next();
                String value = null;
                try
                {
                    value = jsonresponseObject.getString(key);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }


                responseMap.put(key, value);

                data += value+"-";
            }
            usercarDetailsMap.add(responseMap);
            //user_cars[i] = data;
            //usercarList.add(data);
        }
    }

    public boolean SaveCarInfoInDb()
    {
        try {
            for(int i=0 ;i< usercarDetailsMap.size();i++)
            {
                String brand = usercarDetailsMap.get(i).get("brand");
                String model = usercarDetailsMap.get(i).get("model");
                String reg_no = usercarDetailsMap.get(i).get("regno");
                SharedData.InsertUserCar(model,brand,reg_no,"2000");
            }
        } catch (Exception e) {
            return false;
        }
        return true;

    }


}
