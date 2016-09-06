package com.cherry.alok.myapplication;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignInFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener,View.OnClickListener ,GoogleApiClient.ConnectionCallbacks{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "SignInFrag";
    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    Context globalContext;
    UniversalAsyncTask uniTask = null;
    private static final int PROFILE_PIC_SIZE = 250;
    private ImageView imgProfilePic;
    Handler sendMessageToMain;
    private OnFragmentInteractionListener mListener;
    GoogleSignInAccount userGoogAcct;

    public SignInFragment()
    {
        if(mGoogleApiClient != null)
        {
            try {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignInFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void SetMainUiHandler(Handler handle)
    {
        sendMessageToMain = handle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public  void onConnected (Bundle connectionHint)
    {
        Log.d(TAG, "onConnectionFailed:");
    }

    @Override
    public  void onConnectionSuspended(int cause)
    {
        Log.d(TAG, "onConnectionFailed:" );
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View signInRootView = inflater.inflate(R.layout.fragment_sign_in, container, false);;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = SharedData.GetGoogleApiClient();
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    //.enableAutoManage(getActivity() /* FragmentActivity */, (GoogleApiClient.OnConnectionFailedListener) this /* OnConnectionFailedListener */)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
            SignInButton signInButton = (SignInButton) signInRootView.findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.COLOR_DARK|SignInButton.SIZE_WIDE);

        signInButton.setScopes(gso.getScopeArray());
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });




        // Inflate the layout for this fragment
        return signInRootView;
    }

    protected void setGooglePlusButtonText(SignInButton signInButton,
                                           String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextSize(15);
                tv.setTypeface(null, Typeface.NORMAL);
                tv.setText(buttonText);
                return;
            }
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        globalContext = context;
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }




    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(mGoogleApiClient.isConnected() ==false) mGoogleApiClient.connect();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
/*
        if (opr.isDone()) {

        }*/
             /*
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            try {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                // [START_EXCLUDE]
                                //SharedData.DeleteAllUser();
                               // updateUI(false);
                                // [END_EXCLUDE]
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
            GoogleSignInAccount acct = result.getSignInAccount();
            userGoogAcct = acct;
            String firstname= acct.getDisplayName();
            String lastname=acct.getDisplayName();
            String email=acct.getEmail();
            String mobile = "+91877794940";
            String token= acct.getId().toString();

            // ADding code for insert this data in database
            //SharedData.InsertUser(firstname+lastname,email,mobile,token);


            String url = "UserSignUp/";
            SharedData.SetUserName(firstname);
            SharedData.SetUserEmail(email);
            SharedData.SetUserToken(acct.getIdToken());
            String urlParameters = String.format("firstname=%s&lastname=%s&email=%s&mobile=%s&token=%s",firstname,lastname,email,mobile,token);//  firstname+"&"+lastname+"&"+email+"&"+mobile+"&"+token;

            uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,handler);
            ArrayList<String> newParams = new ArrayList<String>();
            uniTask.execute(newParams);
           // updateUI(false);
        } else*/
        {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.`
            //showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();
            userGoogAcct = acct;
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.

            updateUI(false);
        }
    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 9001);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
       /* userGoogAcct = null;
        Message msgObj = sendMessageToMain.obtainMessage();
        Bundle b = new Bundle();
        b.putString("picsatusremove", "RemovePic");
        msgObj.setData(b);
        sendMessageToMain.sendMessage(msgObj);*/
        try {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // [START_EXCLUDE]
                            //SharedData.DeleteAllUser();
                            updateUI(false);
                            // [END_EXCLUDE]
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
/*            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;*/
        }
    }
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        SharedData.DeleteAllUser();
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        try {
            if (signedIn) {
               // getView().findViewById(R.id.sign_in_button).setVisibility(View.GONE);
               // getView().findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            } else {
                //mStatusTextView.setText(R.string.signed_out);

               // getView().findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
               // getView().findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        //   GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                if(mGoogleApiClient != null)
                {
                    SharedData.SetGoogleApiClient(mGoogleApiClient);
                }


                GoogleSignInAccount acct = result.getSignInAccount();
                userGoogAcct = acct;
                updateUI(true);
                showProgressDialog();
                // Get account information
                // mFullName = acct.getDisplayName();
                // mEmail = acct.getEmail();
                ArrayList<String> accountStrings = new ArrayList<>();
                accountStrings.add(acct.getDisplayName());
                accountStrings.add(acct.getEmail());
                accountStrings.add(acct.getId());
                 /*Get Firebase Token*/
                FirebaseInstanceIDService fb_inst = new FirebaseInstanceIDService();
                String firebasetoken = fb_inst.GetFireBaseToken();

                ArrayList<String> newParams = new ArrayList<String>();
                String firstname= acct.getDisplayName().split("\\s+")[0];
                String lastname=acct.getDisplayName().split("\\s+")[1];
                String email=acct.getEmail();
                String mobile = "+91877794940";
                String token= acct.getId().toString();
                SharedData.SetUserName(firstname);
                SharedData.SetUserEmail(email);
                SharedData.SetUserToken(acct.getIdToken());
                SharedData.InsertUser(firstname,mobile ,email,token);

                boolean val = SharedData.CheckUserExistByName(firstname);

                String urlParameters = String.format("firstname=%s&lastname=%s&email=%s&mobile=%s&token=%s&firebasetoken=%s",firstname,lastname,email,mobile,token,firebasetoken);//  firstname+"&"+lastname+"&"+email+"&"+mobile+"&"+token;

                String url = "UserSignUp/";
                uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,handler);
                uniTask.execute(newParams);
            }
            else
            {
                Toast.makeText(getContext(), "Sign In FAIL",Toast.LENGTH_LONG).show();
            }
        }
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
    private final Handler handler = new Handler() {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("taskstatus");

            if ((null != aResponse)) {
                hideProgressDialog();

                if(uniTask != null)
                {
                    if(uniTask.IsSuccess())
                    {
                        String profile = uniTask.GetUserStatus();
                        int userId = uniTask.GetUserId();
                        SharedData.UpdateUserIdInDb(userId);

                        if(Objects.equals(profile, "NewProfile")) {
                            SharedData.UpdateUserStatusInDb(1);
                            SharedData.HandleNavigation(R.id.nav_add_car,getActivity(),true);

                        }
                        else if(Objects.equals(profile, "CarProfile"))
                        {
                            //BackEnd says there is a car , also check the DB. If not in DB then get the details from backEnd and save it in DB then proceed
                            //this case will come when user after sign up and providing car details uninstalls the app , if he installs and uses app again
                            if(CheckCarInDb() == false)
                            {
                                SharedData.HandleNavigation(R.id.nav_manage , getActivity(),true);
                            }
                            else
                            {
                                //There is car information in DB as well as backend , so go to request page
                                SharedData.HandleNavigation(R.id.nav_location, getActivity(),true);
                            }
                        }
                        else if(Objects.equals(profile, "RequestPending"))
                        {
                            SharedData.UpdateUserStatusInDb(3);
                            SharedData.HandleNavigation(R.id.nav_order, getActivity(),true);
                        }
                        else if(Objects.equals(profile,"FeedbackPending"))
                        {
                            SharedData.UpdateUserStatusInDb(4);
                            SharedData.HandleNavigation(R.id.nav_feedback,getActivity(),true);
                        }
                        hideProgressDialog();

                    }
                    if(userGoogAcct != null) {
                        if (userGoogAcct.getPhotoUrl() != null)
                        {
                            String photoUrl = userGoogAcct.getPhotoUrl().toString();
                        if (photoUrl != null) {
                            photoUrl = photoUrl.substring(0,
                                    photoUrl.length() - 2)
                                    + PROFILE_PIC_SIZE;
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("IMGURL", photoUrl).commit();
                            Message msgObj = sendMessageToMain.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("picsatus", photoUrl);
                            msgObj.setData(b);
                            sendMessageToMain.sendMessage(msgObj);
                        }
                    }
                    }
                }
            }
            else
            {
                Toast.makeText(getActivity(), "Not Got Response From Server.", Toast.LENGTH_SHORT).show();
            }

        }
    };


    public boolean CheckCarInDb()
    {
        return SharedData.FetchUserCarDetailsFromDb();
    }

}
