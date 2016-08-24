package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackActivity extends AppCompatActivity {


    UniversalAsyncTask uniTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        //toolbar.setVisibility(View.GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RegisterSubmitButton();
    }
    public void RegisterSubmitButton()
    {
        Button submit_button = (Button)findViewById(R.id.feedback_submit);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                SubmitFeedback();

            }
        });
    }

    private ProgressDialog mProgressDialog;
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Thanks for your Feedback. Just wait a while");
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

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,feedbackSubmitSlotHandler);
        uniTask.execute();

    }


    private Handler feedbackSubmitSlotHandler = new Handler() {

        public void handleMessage(Message msg) {
            String aresponse = msg.getData().getString("taskstatus");
            if(aresponse != null)
            {
                PostRequestOperation();
                hideProgressDialog();
            }
            else {



            }
        }
    };


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
