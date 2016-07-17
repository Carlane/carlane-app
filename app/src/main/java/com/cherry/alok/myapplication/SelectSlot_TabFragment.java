package com.cherry.alok.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectSlot_TabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectSlot_TabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectSlot_TabFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    Button confirm_request;
    public SelectSlot_TabFragment()
    {

    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectSlot_TabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectSlot_TabFragment newInstance(String param1, String param2) {
        SelectSlot_TabFragment fragment = new SelectSlot_TabFragment();
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
    String[] slotArray = {"9 AM -12 PM ","12 PM - 2 PM","2PM - 4M","4PM - 6PM"};

    Integer[] imageId = {
            R.drawable.correct_signal_black,
            R.drawable.correct_signal_black,
            R.drawable.correct_signal_black,
            R.drawable.correct_signal_black,
            R.drawable.correct_signal_black,
            R.drawable.correct_signal_black,
            R.drawable.correct_signal_black

    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_slot__tab, container, false);
        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_listview, slotArray);

        CustomList new_adapter = new
                CustomList(getActivity(), slotArray, imageId);
         ListView listView = (ListView) rootView.findViewById(R.id.slot_list);
         listView.setAdapter(new_adapter);
        confirm_request = (Button)getActivity().findViewById(R.id.confirm_request);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView selected_imageView = (ImageView) view.findViewById(R.id.img);
                ListView parentRow = (ListView)view.getParent();
                TextView subtit =  (TextView)parentRow.getChildAt((int)id).findViewById(R.id.subtitle);
                int bkEndSlot = (int)id +1;//LisView starts with Zero but backend key start from 1
                if(subtit != null &&  SharedData.slotsInfo[bkEndSlot] == false)
                {
                    subtit.setVisibility(View.VISIBLE);
                    subtit.setText("Not Available");
                    subtit.setTextColor(Color.RED);
                    SharedData.SetTimeSlot(-1);
                    if(confirm_request != null)
                    {
                        confirm_request.setEnabled(false);
                    }

                    for (int i = 0; i < slotArray.length; i++) {
                        if(i==id) continue;
                        ImageView other_imageView = (ImageView) parentRow.getChildAt((int)i).findViewById(R.id.img);

                        if(other_imageView != null)
                        {
                            other_imageView.setVisibility(View.GONE);
                            other_imageView.setImageResource(R.drawable.correct_signal_black);
                        }

                    }
                    return;
                }
                else
                {
                    if(subtit != null)
                    {
                        subtit.setVisibility(View.VISIBLE);
                        subtit.setTextColor(Color.GREEN);
                        subtit.setText("Available");
                    }
                }
                SharedData.SetTimeSlot((int)id+1);
                 if(selected_imageView  != null)
                {
                    selected_imageView.setVisibility(View.VISIBLE);
                    selected_imageView .setImageResource(R.drawable.correct_signal_blue);
                }

                for (int i = 0; i < slotArray.length; i++) {
                    if(i==id) continue;
                    if(confirm_request != null &&  SharedData.slotsInfo[bkEndSlot])
                    {
                        confirm_request.setEnabled(true);
                    }
                    ImageView other_imageView = (ImageView) parentRow.getChildAt(i).findViewById(R.id.img);

                    if(other_imageView != null)
                    {
                        other_imageView.setVisibility(View.GONE);
                        other_imageView.setImageResource(R.drawable.correct_signal_black);
                    }

                }
            }
        });


        //Inflate the layout for this fragment
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
       /* if (context instanceof OnFragmentInteractionListener) {
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
