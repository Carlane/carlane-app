package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Activity_Payment extends AppCompatActivity {
    String urlparameters = "";
    UniversalAsyncTask uniTask;
    enum AsyncActivities
    {
        NONE,
        SELECT_SLOT,
        INIT_REQ,
        SHOW_ANIM,
    };
    AsyncActivities current_task = AsyncActivities.NONE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_activity__payment);
        urlparameters = getIntent().getStringExtra("request_param");
        RegisterOrderPlacement();
        SetTextsInField();

    }
    public void SetTextsInField()
    {
        TextView final_cost = (TextView)findViewById(R.id.final_cost);
        TextView service_cost = (TextView)findViewById(R.id.service_cost_value);
        TextView total_cost = (TextView)findViewById(R.id.total_value) ;
        String cost =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("service_cost" , "0.0000");
        final_cost.setText("INR " + cost);
        service_cost.setText(cost);
        total_cost.setText(cost);


    }

    public void RegisterOrderPlacement()
    {
        Button order_confirm = (Button)findViewById(R.id.place_order) ;
        order_confirm.setText("PLACE YOUR "+ SharedData.GetServiceName().toUpperCase()+ "ORDER");
        order_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitRequest();
            }
        });
    }

    public void InitRequest()
    {
        current_task = AsyncActivities.INIT_REQ;
        showProgressDialog("Find A Car Wash Near You");
        String url = "request/"+SharedData.GetUserId()+"/";

        uniTask = new UniversalAsyncTask(url,"POST",urlparameters ,selectSlotHandler);
        uniTask.execute();
    }

    private ProgressDialog mProgressDialog;
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(true);
        }
        else
        {
            mProgressDialog.setMessage(message);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }


    private Handler selectSlotHandler = new Handler() {

        public void handleMessage(Message msg) {
            String aresponse = msg.getData().getString("taskstatus");
            if (aresponse != null) {

                if (current_task == AsyncActivities.INIT_REQ) {
                    PostRequestOperation();
                    hideProgressDialog();
                }

            }
        }
    };

    void PostRequestOperation()
    {
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
        String reason = jsonObject.optString("reason");
        if(error_in_Result == true)
        {
            showSettingsAlert("Error in Request",reason);
            return;
        }

        SharedData.UpdateUserStatusInDb(3);
        SharedData.HandleNavigation(R.id.nav_order,this);

    }

    AlertDialog requestalreadyPlaceDlg;
    public void showSettingsAlert(String title , String message) {
        if(requestalreadyPlaceDlg != null && requestalreadyPlaceDlg.isShowing())
        {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this , R.style.PauseDialog);

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.icon_driver_app);

        // On pressing Settings button


        // on pressing cancel button
        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestalreadyPlaceDlg = null;
                dialog.cancel();
            }
        });

        // Showing Alert Message
        requestalreadyPlaceDlg = alertDialog.show();
    }


}
