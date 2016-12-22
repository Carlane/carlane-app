
package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alok on 15/5/16.
 */
public class UniversalAsyncTask extends AsyncTask<ArrayList<String>,Void,HashMap<String,String>>
{
    private int userIdBkEnd = 0;
    ProgressDialog progressDialog;
    String typeOfRequest;
    String userUrl = new String();
    HashMap<String,String> outputHashData = new HashMap<String,String>();
    public String outputJasonString = new String();
    Boolean asynctaskResult = false;
    String asyncTaskResultReason = new String();
    JSONArray outputGetData = new JSONArray();
    Handler uiHandler = null;
    String urlParams = null;
    String outputReason = new String();
    String userProfileStatus = new String();
    public UniversalAsyncTask(String URL , String requestType , String urlParameter , Handler handler)
    {
        typeOfRequest = requestType;
     userUrl = "http://54.213.65.235/"+ URL;
    //    userUrl = "http://192.168.0.101:8000/"+ URL;
        uiHandler =  handler;
        urlParams = urlParameter;
    }

    @Override
    protected void onPostExecute(HashMap<String,String> postExecuteData)
    {
        outputHashData = postExecuteData;
        //progressDialog.cancel();
        Message msgObj = uiHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("taskstatus", "complete");
        msgObj.setData(b);
        uiHandler.sendMessage(msgObj);
    }
    @Override
    protected HashMap<String,String> doInBackground(ArrayList<String>... params)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String outputJsonStr = null;
        HashMap<String,String> postExecuteData = new HashMap<String,String>();
        try {
            // Construct the URL for the Django CarLanez query
            URL url = new URL(userUrl);
            try
            {
                        urlConnection = (HttpURLConnection) url.openConnection();
                        if(typeOfRequest == "POST")
                            {
                                urlConnection.setRequestMethod(typeOfRequest);
                                urlConnection.setConnectTimeout(5000);
                                urlConnection.setDoOutput(true);
                            }
                        urlConnection.connect();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                urlConnection.disconnect();
                urlConnection = null;
            }
            if(urlConnection != null) {
                if (typeOfRequest == "POST" && urlParams != null) {
                    DataOutputStream dStream = new DataOutputStream(urlConnection.getOutputStream());
                    dStream.writeBytes(urlParams);
                    dStream.flush();
                    dStream.close();
                }
                int responseCode = urlConnection.getResponseCode();

                String message = urlConnection.getResponseMessage();
                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + typeOfRequest);
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                if (responseCode == 200 || responseCode == 201) {
                    try {
                        outputJasonString = responseOutput.toString();
                        ParseReturnOutput();

                    } catch (Exception error) {

                    }
                }
            }
        }
        catch (IOException e)
        {
            Log.e("Carlanez","Preparing IOException",null);
        }
        finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (final IOException e)
                {
                    Log.e("Carlanez", "Error closing stream", e);
                }
            }
            postExecuteData.put("error","false");
            postExecuteData.put("reason","Successfully added");
        }
        postExecuteData.put("error","false");
        postExecuteData.put("reason","Successfully added");
        return postExecuteData;
    }

    public void ParseReturnOutput()
    {
        String outputStr = outputJasonString.toString();
        JSONObject jsonRootObject = null;
        try
        {
            jsonRootObject = new JSONObject(outputJasonString);
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
        userIdBkEnd = Integer.parseInt(jsonObject.optString("id").toString());
        SharedData.SetUserId(userIdBkEnd);
        asynctaskResult = Boolean.parseBoolean(jsonObject.optString("error").toString());
        outputGetData = jsonObject.optJSONArray("responsedata");
        outputReason =  jsonObject.optString("reason").toString();
        userProfileStatus = jsonObject.optString("user_status").toString();
    }
    public String ResultReason()
    {
        if(outputReason!= null)
        {
            return outputReason;
        }
        return null;
    }
    public Boolean IsSuccess()
    {
        return !asynctaskResult;
    }

    public String GetUserStatus()
    {
        return userProfileStatus;
    }
    public int GetUserId()
    {
      return userIdBkEnd;
    }
    public JSONArray GetOutputResult()
    {
        return outputGetData;
    }


}
