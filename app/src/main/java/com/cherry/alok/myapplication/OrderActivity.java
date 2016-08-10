package com.cherry.alok.myapplication;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
    enum AsyncActivities
    {
        NONE,
        UPDATE_STATUS,
        CANCEL_REQUEST,
    };
    AsyncActivities current_task = AsyncActivities.NONE;
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
    boolean activity_creation_first = false;

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
        activity_creation_first = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.order_screen,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.cancel_order:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                showCancelOrderAlert();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

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
        current_task = AsyncActivities.UPDATE_STATUS;
        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,orderScreenHandler);
        uniTask.execute();
        showProgressDialog("Getting Your Order Status");
    }

    private void Init_CancelRequest()
    {
        String url = "requestcancel/"+SharedData.GetUserId()+"/";
        String urlParameters = String.format("car_reg=%s" ,SharedData.GetDefaultCarNo());
        current_task = AsyncActivities.CANCEL_REQUEST;
        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,orderScreenHandler);
        uniTask.execute();
        showProgressDialog("Sending Cancel Request");
    }

    private Handler orderScreenHandler = new Handler() {

        public void handleMessage(Message msg) {
            String aresponse = msg.getData().getString("taskstatus");
            if(aresponse != null)
            {
                if(current_task == AsyncActivities.UPDATE_STATUS) {
                    PostStatusUpdateOperation();
                    hideProgressDialog();
                }
                if(current_task == AsyncActivities.CANCEL_REQUEST)
                {
                    hideProgressDialog();
                    PostCancelRequestOpertion();
                }
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
            //firstBar.setProgress(status_id);
            //31july
            if(status_id ==8)
            {
                //Need to update user status in app db and switch to location map
               // SharedData.UpdateUserStatusInDb(2);
                SharedData.HandleNavigation(R.id.nav_feedback, this);//move to feedback screen and not new request start
            }

            String driver = jsonObject.optString("driverfirstname").toString() +" "+ jsonObject.optString("driverlastname").toString();
            UpdateDriverNameText(driver);
            String driver_mobile = jsonObject.optString("drivermobile").toString() ;//drivermobile
            String date = jsonObject.optString("date").toString();
            int slot =  Integer.parseInt(jsonObject.optString("timeslot").toString());

            UpdateCheckIconAndTextAll(status_id);
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

    void PostCancelRequestOpertion()
    {
        try {
            String outputStr = uniTask.outputJasonString.toString();
            JSONObject jsonRootObject = null;
            try {
                jsonRootObject = new JSONObject(uniTask.outputJasonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray jsonArray = jsonRootObject.optJSONArray("response");
            JSONObject jsonObject = null;
            try {
                jsonObject = jsonArray.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int userIdBkEnd = Integer.parseInt(jsonObject.optString("id").toString());
            String message= jsonObject.optString("reason").toString();
            Boolean error_in_Result = Boolean.parseBoolean(jsonObject.optString("error").toString());
            if (error_in_Result == true) {
                if(message.equalsIgnoreCase("Exception"))
                PrepareSnack("Sorry ! Unable to Process Cancel Request ! Request Can not be Cancelled After Pick Up");
                else
                {
                    PrepareSnack("Sorry ! Can not Cancel After Pick Up");
                }
                return;
            }
            else
            {
                PrepareSnack("Cancel Initiated  !");
                SharedData.UpdateUserStatusInDb(2);
                SharedData.HandleNavigation(R.id.nav_location,this,true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String message = e.getMessage();
        }

    }



    public void UpdateCheckIconAndTextAll(int status_id)
    {
        ImageView request_place_img = (ImageView)findViewById(R.id.img_request_placed);
        TextView request_place_text = (TextView)findViewById(R.id.text_request_placed);
        if(status_id > 0)
        {
            UpdateStatusImage(request_place_img);
            UpdateStatusText(request_place_text) ;
        }


        ImageView img_driver_alloc = (ImageView)findViewById(R.id.img_driver_alloc);
        TextView text_request_placed = (TextView)findViewById(R.id.text_driver_alloc);
        if(status_id > 1)
        {
            UpdateStatusImage(img_driver_alloc);
            UpdateStatusText(text_request_placed) ;
        }


        ImageView img_driver_onway = (ImageView)findViewById(R.id.img_driver_onway);
        TextView text_driver_onway = (TextView)findViewById(R.id.text_driver_onway);
        if(status_id > 2)
        {
            UpdateStatusImage(img_driver_onway);
            UpdateStatusText(text_driver_onway) ;
        }


        ImageView img_vehiclepickedup = (ImageView)findViewById(R.id.img_vehiclepickedup);
        TextView text_vehiclepickedup = (TextView)findViewById(R.id.text_vehiclepickedup);
        if(status_id > 3)
        {
            UpdateStatusImage(img_vehiclepickedup);
            UpdateStatusText(text_vehiclepickedup) ;
        }


        ImageView img_vehicle_garage = (ImageView)findViewById(R.id.img_vehicle_garage);
        TextView text_vehicle_garage = (TextView)findViewById(R.id.text_vehicle_garage);
        if(status_id > 4)
        {
            UpdateStatusImage(img_vehicle_garage);
            UpdateStatusText(text_vehicle_garage) ;
        }


        ImageView img_servicing_start = (ImageView)findViewById(R.id.img_servicing_start);
        TextView text_servicing_start = (TextView)findViewById(R.id.text_servicing_start);
        if(status_id > 5)
        {
            UpdateStatusImage(img_servicing_start);
            UpdateStatusText(text_servicing_start) ;
        }


        ImageView img_vehicle_wayback = (ImageView)findViewById(R.id.img_vehicle_wayback);
        TextView text_vehicle_wayback = (TextView)findViewById(R.id.text_vehicle_wayback);
        if(status_id > 6)
        {
            UpdateStatusImage(img_vehicle_wayback);
            UpdateStatusText(text_vehicle_wayback) ;
        }


        ImageView img_req_complete = (ImageView)findViewById(R.id.img_req_complete);
        TextView text_req_complete = (TextView)findViewById(R.id.text_req_complete);
        if(status_id > 7)
        {
            UpdateStatusImage(img_req_complete);
            UpdateStatusText(text_req_complete) ;
        }
    }

    public void UpdateStatusText(TextView request_place_text)
    {
        if(request_place_text != null )
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                request_place_text.setTextColor(getResources().getColor(R.color.green , null));
            }
            else
            {
                request_place_text.setTextColor(getResources().getColor(R.color.green));
            }
        }
    }
    public void UpdateStatusImage(ImageView request_place_img)
    {
        if(request_place_img != null)
        {
            request_place_img.setImageResource(R.drawable.checkgreen);
        }
    }

    AlertDialog cancelRequestDlg;
    public void showCancelOrderAlert() {
        if(cancelRequestDlg != null && cancelRequestDlg.isShowing())
        {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this , R.style.PauseDialog);

        // Setting Dialog Title
        alertDialog.setTitle("Order Cancel");

        // Setting Dialog Message
        alertDialog.setMessage("Do you really want to cancel the wash request ?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.icon_driver_app);

        // On pressing Settings button
        alertDialog.setPositiveButton("Yes Cancel Request", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Init_CancelRequest();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("No !", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                cancelRequestDlg = null;
                dialog.cancel();
                Snackbar.make(findViewById(R.id.scroll) , "Thanks for chosing our side !!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Showing Alert Message
        cancelRequestDlg = alertDialog.show();
    }

    public void PrepareSnack(String message)
    {
        Snackbar.make(findViewById(R.id.scroll) , message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        if(activity_creation_first == false)
        {
            Init_StatusUpdate();
        }
        activity_creation_first = false;

    }

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
