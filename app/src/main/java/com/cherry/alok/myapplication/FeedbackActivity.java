package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {


    UniversalAsyncTask uniTask;
    enum AsyncActivities
    {
        NONE,
        GET_CAR_DETAILS,
        GETORDER_DETAILS,
        SUBMIT,
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        //toolbar.setVisibility(View.GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RegisterSubmitButton();
      //  Init_StatusUpdate();
        if (SharedData.GetDefaultCarNo() != null) {
            Init_StatusUpdate();
        } else {
            InitCarDetailsInfoReq();
        }
    }

    public void InitCarDetailsInfoReq()
    {
        String url = "CarInfo/"+SharedData.GetUserId()+"/";
        //Toast.makeText(this , url, Toast.LENGTH_SHORT);
        uniTask = new UniversalAsyncTask(url,"GET","" ,feedbackSubmitSlotHandler);
        current_task = AsyncActivities.GET_CAR_DETAILS;
        ArrayList<String> dummy = new ArrayList<String>();
        uniTask.execute(dummy);
        showProgressDialog("Fetching Your Order Details");
    }
    public void RegisterSubmitButton()
    {
        Button submit_button = (Button)findViewById(R.id.feedback_submit);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Thanks for your Feedback. Just wait a while");
                SubmitFeedback();

            }
        });
    }

    private ProgressDialog mProgressDialog;
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

    AsyncActivities current_task = AsyncActivities.NONE;

    private void Init_StatusUpdate()
    {
        // showProgressDialog("Fetching Order Details");
        String url = "requestatusinfo/"+SharedData.GetUserId()+"/";
        int request_status = 8;
        String urlParameters = String.format("car_reg=%s&request_status=%s" ,SharedData.GetDefaultCarNo(),Integer.toString(request_status));
        current_task = AsyncActivities.GETORDER_DETAILS;
        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,feedbackSubmitSlotHandler);
        uniTask.execute();
    }

    public void SubmitFeedback()
    {
        RatingBar washratingbar = (RatingBar)findViewById(R.id.washrating);
        RatingBar driverratingbar = (RatingBar)findViewById(R.id.driverrating);
        RatingBar overallratingbar = (RatingBar)findViewById(R.id.overallrating);
        String washratevalue=String.valueOf(washratingbar.getRating());
        String driverratingvalue=String.valueOf(driverratingbar.getRating());
        String overallratingvalue =String.valueOf(overallratingbar.getRating());
        EditText feedbackText = (EditText)findViewById(R.id.feedback_editText);


        String url = "feedback/"+SharedData.GetUserId()+"/";
        String urlParameters = String.format("washrating=%s&driverrating=%s&overallrating=%s&feedback=%s" ,washratevalue , driverratingvalue ,overallratingvalue,feedbackText.getText());
        current_task = AsyncActivities.SUBMIT;
        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,feedbackSubmitSlotHandler);
        uniTask.execute();

    }


    private Handler feedbackSubmitSlotHandler = new Handler() {

        public void handleMessage(Message msg) {
            String aresponse = msg.getData().getString("taskstatus");
            if(aresponse != null)
            {
                if(current_task == AsyncActivities.SUBMIT) {
                    PostRequestOperation();

                }
                else if(current_task == AsyncActivities.GETORDER_DETAILS)
                {
                    PostStatusUpdateOperation();
                }
                else  if(current_task == AsyncActivities.GET_CAR_DETAILS)
                {
                    PostCarDetailsOperation();
                    if(SharedData.FetchUserCarDetailsFromDb() == false) {
                        SaveCarInfoInDb();
                    }
                    Init_StatusUpdate();
                }
            }
            else {



            }
            hideProgressDialog();
        }
    };
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
            if(jsonRootObject == null)
            {
                Snackbar.make(findViewById(R.id.scroll) , "Seems Like There is Network Connectivity Problem. Try Again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

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
            String service = jsonObject.optString("service_typeid").toString();
            String car = jsonObject.optString("car_reg");

            String driver = jsonObject.optString("driverfirstname").toString() +" "+ jsonObject.optString("driverlastname").toString();
           // UpdateDriverNameText(driver);
            String driver_mobile = jsonObject.optString("drivermobile").toString() ;//drivermobile
            String date = jsonObject.optString("date").toString();
            int slot =  Integer.parseInt(jsonObject.optString("timeslot").toString());
            TextView text = (TextView)findViewById(R.id.label_orderdetails);
            text.setText("Please provide feedback for Your " + service + " Order Placed on " + date + " for Car " + car +". Your vehicle was picked up by "+ driver);
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
        }


    }

    List<HashMap<String,String>> usercarDetailsMap = new ArrayList<>();
    public void PostCarDetailsOperation()
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
    void PostRequestOperation()
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
            String driver_name = jsonObject.optString("feedbackid");

            SharedData.UpdateUserStatusInDb(2);
            SharedData.HandleNavigation(R.id.nav_location,this,true);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }
}
