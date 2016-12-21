package com.cherry.alok.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.msg91.sendotp.library.SendOtpVerification;
import com.msg91.sendotp.library.Verification;
import com.msg91.sendotp.library.VerificationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddCarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddCarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCarFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback, VerificationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    UniversalAsyncTask uniTask = null;
    enum AsyncActivities
    {
        NONE,
        GETCARS_MODELS,
        SAVEPROFILE,
    };
    private Verification mVerification;//OTP

   // String[] items = new String[]{"Brand","Maruti", "Hyundai"};//, "Chevrolet","Renault","Fiat","Datsun","BMW","Volkswagon","Mercedes"};

    List<String> carbrandList = new ArrayList<String>();



    ArrayAdapter<String> carmodeladapter = null;//new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, marutiModels);
    int userId =0;
    ArrayList<String> selecteditem = new ArrayList<>();
    Spinner carbrand;

    private OnFragmentInteractionListener mListener;

    public AddCarFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddCarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddCarFragment newInstance(String param1, String param2) {
        AddCarFragment fragment = new AddCarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    private ProgressDialog mProgressDialog;
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("message");
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    View addCarView = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        addCarView = inflater.inflate(R.layout.fragment_add_car, container, false);
        // Inflate the layout for this fragment
        carbrand = (Spinner)addCarView.findViewById(R.id.CarBrand);

        userId = SharedData.GetUserId();


        carbrand = (Spinner)addCarView.findViewById(R.id.CarBrand);

        AutoCompleteTextView registrationNumberText = (AutoCompleteTextView)addCarView.findViewById(R.id.registration_text);
        registrationNumberText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        //Evetn Handler for the first spinner for Car Brands
        carbrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                //If the selection is Legit , populate the second spiner with data and display it to user
                if(position >= 0)
                {
                    final Spinner carmodel = (Spinner)addCarView.findViewById(R.id.CarModel);
                    carmodeladapter  = (GetCarModelAdapter(position));
                    carmodel.setAdapter(carmodeladapter);


                    //Event Handler for Selection Changed Event in the second Spinner for CarModels
                    carmodel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            if(position > 0)
                            {
                                SetCarRegistrationTextVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {

                        }

                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //SetAddButtonVisibility(View.VISIBLE);
            }

        });

        RegisterInitiateSMSVerification();
        tryAndPrefillPhoneNumber();
        GetCarBrandsAndModels();

        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(SmsMessage message) {
                String sender = message.getDisplayOriginatingAddress();
                String messageBody = message.getMessageBody();
                if(sender.contains("MCLOTP"))
                {
                    //inputCode_text
                    String text[] = messageBody.split("\\s+");
                    EditText textbox = (EditText) addCarView.findViewById(R.id.inputCode_text);
                    textbox.setText(text[0]);
                    if (mVerification != null) {
                        mVerification.verify(text[0]);
                    }
                }
            }
        });
        return addCarView;
    }

    public void GetCarBrandsAndModels()
    {
        String url = "getcars/"+SharedData.GetUserId()+"/";

        uniTask = new UniversalAsyncTask(url,"GET","",handler);

        ArrayList<String> dummy = new ArrayList<String>();
        current_task =  AsyncActivities.GETCARS_MODELS;
        uniTask.execute(dummy);
        showProgressDialog("Fetching Cars");
    }

    final int READPHONESTATE = 1;
    final int READSMS =2;
    private void tryAndPrefillPhoneNumber() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager manager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            AutoCompleteTextView mPhoneNumber = (AutoCompleteTextView)addCarView.findViewById(R.id.mobile);
            mPhoneNumber.setText(manager.getLine1Number());
        } else {
            InitiatePermissionsForREADINGPHONESTATE(false);
        }
    }


    private void InitiatePermissionsForREADINGPHONESTATE(boolean showRationale)
    {

        if (showRationale) {

           // ShowPermissionAlertDialog();

        } else {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    READPHONESTATE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode)
        {
            case READPHONESTATE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tryAndPrefillPhoneNumber();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0])) {
                        Snackbar.make(addCarView, "MyCarLane needs this info for autofilling your form", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            }
            break;
            case READSMS:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0])) {
                        Toast.makeText(getActivity(), "This application needs permission to read your SMS to automatically verify your "
                                + "phone, you may disable the permission once you have been verified.", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                initiateVerificationAndSuppressPermissionCheck();
            }
            break;
        }



    }





    AsyncActivities current_task = AsyncActivities.NONE;


    public boolean ProfileCheck() {
        AutoCompleteTextView registrationNumberText = (AutoCompleteTextView) addCarView.findViewById(R.id.registration_text);
        registrationNumberText.clearFocus();
        //Validate Registration
        RegistrationNumberValidator numberValidator = new RegistrationNumberValidator();
        if (numberValidator.validate(registrationNumberText.getText().toString())) {

            if (ValidateMobileInfo()) {
                SetBrandInSelectedArray();
                SetModelInSelectedArray();
                selecteditem.add(registrationNumberText.getText().toString());
                selecteditem.add(GetMobileNumber());
                if (selecteditem.get(0).toLowerCase().equals("brand") || selecteditem.get(1).toLowerCase().equals("models")) {
                    Snackbar.make(addCarView, "Car Brand and Model selection is not finished", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return false;
                }
                //
            } else {
                Snackbar.make(addCarView, "Invalid Mobile Number", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }


        } else {
            Snackbar.make(addCarView, "Invalid Registration Number .. ?", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return false;
        }
        return true;
    }

    public void SaveProfile()
    {
        showProgressDialog("Saving Profile");
        current_task = AsyncActivities.SAVEPROFILE;
        String url = "CarInfo/" + userId + "/";
        String urlParameters = String.format("carbrand=%s&carmodel=%s&registration_number=%s&mobile_number=%s", selecteditem.get(0), selecteditem.get(1), selecteditem.get(2).toUpperCase(),selecteditem.get(3));
        uniTask = new UniversalAsyncTask(url, "POST", urlParameters, handler);
        uniTask.execute(selecteditem);
    }

    public boolean ValidateMobileInfo()
    {
        RegistrationNumberValidator numberValidator = new RegistrationNumberValidator();

        return numberValidator.validateMobile(GetMobileNumber());

    }
    public String GetMobileNumber()
    {
        AutoCompleteTextView textView = (AutoCompleteTextView)addCarView.findViewById(R.id.mobile);
        return textView.getText().toString();
    }
    public void SetBrandInSelectedArray()
    {
        Spinner carbrand = (Spinner)addCarView.findViewById(R.id.CarBrand);
        selecteditem.clear();
        selecteditem.add(carbrand.getSelectedItem().toString());
    }

    public void SetModelInSelectedArray()
    {
        //one item should already be in in this arraylist by now
        if(selecteditem == null || selecteditem.size() ==0)
            return;
        Spinner carmodel = (Spinner)addCarView.findViewById(R.id.CarModel);
        selecteditem.add(carmodel.getSelectedItem().toString());
    }

    public void SetCarRegistrationTextVisibility(int visibility)
    {
        AutoCompleteTextView registrationNumberText = (AutoCompleteTextView)addCarView.findViewById(R.id.registration_text);
        registrationNumberText.setVisibility(visibility);
    }

    public void SetCarRegistrationTextEditable(boolean allow)
    {
        AutoCompleteTextView registrationNumberText = (AutoCompleteTextView)addCarView.findViewById(R.id.registration_text);
        registrationNumberText.setEnabled(allow);
    }

    public void SetMobileTextEditable(boolean allow)
    {
        AutoCompleteTextView mPhoneNumber = (AutoCompleteTextView)addCarView.findViewById(R.id.mobile);
        mPhoneNumber.setEnabled(allow);
    }

    public ArrayAdapter<String> GetCarModelAdapter(int carmodels)
    {
        //Get The Car Model Adapter Here
        String carbrand = carbrandList.get(carmodels);
        carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, GetCaModelFromBrand(carbrand));
        return carmodeladapter;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    boolean phoneupdate = false;
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
        phoneupdate = Boolean.parseBoolean(jsonObject.optString("phoneresult").toString());

    }


    /*Verify BY OTP*/
    public void RegisterInitiateSMSVerification()
    {
        Button sms_verification = (Button)addCarView.findViewById(R.id.btn_init_verify_getOTP);
        sms_verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ProfileCheck())
                {
                    SetProgressBarVisibility(true);
                    SetCarRegistrationTextEditable(false);
                    SetMobileTextEditable(false);

                    createVerification(GetMobileNumber(),false,"91");
                }
            }
        });
    }



    void createVerification(String phoneNumber, boolean skipPermissionCheck, String countryCode) {
        if (!skipPermissionCheck && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_SMS}, READSMS);

        } else {
            mVerification = SendOtpVerification.createSmsVerification(getContext(), phoneNumber, this, countryCode, true);
            mVerification.initiate();
        }
    }

    void initiateVerificationAndSuppressPermissionCheck() {
        createVerification(GetMobileNumber(), true, "+91");
    }

    @Override
    public void onInitiated(String response) {
        Button btn_init_verify_getOTP = (Button)addCarView.findViewById(R.id.btn_init_verify_getOTP);
        btn_init_verify_getOTP.setEnabled(false);
        Toast.makeText(getContext(), "You will receive OTP by SMS", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitiationFailed(Exception exception) {
        SetProgressBarVisibility(true);
        Button btn_init_verify_getOTP = (Button)addCarView.findViewById(R.id.btn_init_verify_getOTP);
        btn_init_verify_getOTP.setEnabled(true);
        SetCarRegistrationTextEditable(true);
        SetMobileTextEditable(true);
        Toast.makeText(getContext(), "OTP Initialization Failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVerified(String response) {
        /*Log.d(TAG, "Verified!\n" + response);
        hideProgressBarAndShowMessage(R.string.verified);
        showCompleted();*/
        SaveProfile();
        Toast.makeText(getContext(), "Mobile Number Verification Successful", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVerificationFailed(Exception exception) {

        Button btn_init_verify_getOTP = (Button)addCarView.findViewById(R.id.btn_init_verify_getOTP);
        btn_init_verify_getOTP.setEnabled(true);
        Toast.makeText(getContext(), "Mobile Number Verification Failed . Try Again", Toast.LENGTH_LONG).show();
    }

    /**/

    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("taskstatus");
            hideProgressDialog();
            switch(current_task)
            {
                case SAVEPROFILE:
                {
                    if ((null != aResponse)) {
                        PostRequestOperation();

                        if(uniTask != null)
                        {
                            if(!uniTask.IsSuccess())
                            {

                                String reason = uniTask.ResultReason();
                                Toast.makeText(getContext(), "Failed to Add Car - "+reason, Toast.LENGTH_LONG).show();

                            }
                            else
                            {
                                if(!SharedData.InsertUserCar(selecteditem.get(1),selecteditem.get(0),selecteditem.get(2),"2015"))
                                {
                                    Snackbar.make(addCarView, "Failed To Add Car Info In App", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                                //Update User Car and User Phone Number in DB
                                if(phoneupdate)
                                {
                                    SharedData.UpdateUserPhone(selecteditem.get(3));
                                }

                                SharedData.HandleNavigation(R.id.nav_location,getActivity());

                            }
                        }
                    }
                    else
                    {
                        Toast.makeText(getContext(), "Not Got Response From Server.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case GETCARS_MODELS:
                {
                    if ((null != aResponse)) {
                        PostGetCarOperation();
                    }
                }
                break;
            }




        }
    };

    public void SetProgressBarVisibility(boolean value)
    {
        ProgressBar bar = (ProgressBar)addCarView.findViewById(R.id.my_progressBar);
        if(value)
        {
            bar.setVisibility(View.VISIBLE);
        }
        else
        {
            bar.setVisibility(View.INVISIBLE);

        }

    }

    public void PostGetCarOperation() {


        JSONArray reposnejSonArray = uniTask.GetOutputResult();

        int count = reposnejSonArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject jsonresponseObject = null;
            try {
                jsonresponseObject = reposnejSonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Iterator<?> keys = jsonresponseObject.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                carbrandList.add(key);


                String value = null;
                try {
                    value = jsonresponseObject.getString(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //key contains both name and id
            }

            Collections.sort(carbrandList, String.CASE_INSENSITIVE_ORDER);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(addCarView.getContext(),
                    android.R.layout.simple_spinner_item, carbrandList);
            dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
            carbrand.setAdapter(dataAdapter);
            carbrand.setSelection(0);
        }
    }

    public List<String> GetCaModelFromBrand(String key) {


        JSONArray reposnejSonArray = uniTask.GetOutputResult();
        List<String> carModels = new ArrayList<String>() ;
        int count = reposnejSonArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject jsonresponseObject = null;
            try {
                jsonresponseObject = reposnejSonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String value = null;
            try {
                value = jsonresponseObject.getString(key);
                //Get all models from this but remove the first [ and last ] symbol
                String newValue = value.substring(1, value.length()-1);
                String[] allmodels = newValue.split(",");
                for(int ii= 0; ii< allmodels.length ; ii++)
                {
                    carModels.add(allmodels[ii].substring(1,allmodels[ii].length()-1));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //key contains both name and id
        }

            Collections.sort(carModels, String.CASE_INSENSITIVE_ORDER);
        return carModels;


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
