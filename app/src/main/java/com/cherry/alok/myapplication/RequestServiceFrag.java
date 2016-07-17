package com.cherry.alok.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestServiceFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestServiceFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestServiceFrag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    String[] serviceTypes = new String[]{"Select a Wash Type","Basic", "Premium","Platimun"};
    String[] user_cars = new String[50];
    ArrayAdapter<String> carwastypesadapter = null;
    ArrayAdapter<String> usercaradapter = null;
    UniversalAsyncTask uniTask = null;
    int userId =0 ;
    View rootView;
    Spinner userCars;
    List<String> usercarList = new ArrayList<>();
    List<HashMap<String,String>> usercarDetailsMap = new ArrayList<>();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RequestServiceFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestServiceFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestServiceFrag newInstance(String param1, String param2) {
        RequestServiceFrag fragment = new RequestServiceFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_request, container, false);
        final Spinner carbrand = (Spinner)rootView.findViewById(R.id.carwash);
        userCars = (Spinner)rootView.findViewById(R.id.usercars);
        carbrand.setAdapter(GetCarWashAdapter());
        //Event Handler for Selection Changed Event in the second Spinner for CarModels
        carbrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position > 0)
                {

                    String url = "CarInfo/"+userId+"/";
                    uniTask = new UniversalAsyncTask(url,"GET","" ,handler);

                    ArrayList<String> dummy = new ArrayList<String>();
                    uniTask.execute(dummy);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });



        userId = getArguments().getInt("CURRENT_USER_ID");
        return rootView;
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public ArrayAdapter<String> GetCarWashAdapter()
    {
        carwastypesadapter= new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, serviceTypes);
        return carwastypesadapter;
    }

    public void SetUSerCarVisibility()
    {
       // final Spinner usercars = (Spinner)rootView.findViewById(R.id.usercars);
        //usercars.setVisibility(View.VISIBLE);
        UserCarListFragment carListFrag = new UserCarListFragment();
        carListFrag.SetUserCars(usercarList , usercarDetailsMap);

        /*FragmentTransaction transaction = getFragmentManager().beginTransaction();// getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, carListFrag);
        transaction.addToBackStack(null);
        transaction.commit();*/
       // getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,carListFrag).commit();

        Intent mapintent = new Intent(getContext(), UserCar_CollapseHeader.class);
        mapintent.putExtra("userCarMap", (Serializable) usercarDetailsMap);
        startActivity(mapintent);
    }
    public void SetCarAdapter()
    {

        userCars.setAdapter(GetUserCarAdapter());
    }

    public ArrayAdapter<String> GetUserCarAdapter()
    {
        usercaradapter= new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, usercarList);
        return usercaradapter;
    }

    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("taskstatus");

            if ((null != aResponse)) {

                if(uniTask != null)
                {
                    if(!uniTask.IsSuccess())
                    {

                        String reason = uniTask.ResultReason();
                        Toast.makeText(getContext(), "Failed to Fetch Data- "+reason, Toast.LENGTH_LONG).show();

                    }
                    else
                    {

                        PostOperation();
                        SetUSerCarVisibility();
                        SetCarAdapter();

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
            user_cars[i] = data;
            usercarList.add(data);
        }
    }
}
