package com.cherry.alok.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserCarListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserCarListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserCarListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public List<String> userCars = new ArrayList<String>();
    List<HashMap<String,String>> userCarDetailsMap = new ArrayList<>();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public UserCarListFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserCarListFragment newInstance(String param1, String param2) {
        UserCarListFragment fragment = new UserCarListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.

    }
    public void SetUserCars(List<String> userCarsArgs , List<HashMap<String,String>> userMap)
    {
        userCars = userCarsArgs;
        userCarDetailsMap = userMap;

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_car_list, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Inflate the layout for this fragment
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

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_text";
        private static final String ARG_CAR_DETAIL_BRAND = "current_car_brand";
        private static final String ARG_CAR_DETAIL_REG = "current_car_reg";
        private static final String ARG_CAR_DETAIL_MODEL = "current_car_model";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String sectionText , HashMap<String,String> currentDetail ) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();

            args.putString(ARG_SECTION_NUMBER, sectionText);
            args.putString(ARG_CAR_DETAIL_BRAND,currentDetail.get("name"));
            args.putString(ARG_CAR_DETAIL_MODEL,currentDetail.get("model"));
            args.putString(ARG_CAR_DETAIL_REG,currentDetail.get("regno"));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user_cars_activty, container, false);
            TextView carRegTextView = (TextView) rootView.findViewById(R.id.carreg_label);
            TextView carBrandextView = (TextView) rootView.findViewById(R.id.carbrand_label);
            TextView carModelTextView = (TextView) rootView.findViewById(R.id.carmodel_label);
            String userCarDetails = getArguments().getString(ARG_SECTION_NUMBER);
            String[] variousDetails = userCarDetails.split("-");

            String carbrand = getArguments().getString(ARG_CAR_DETAIL_BRAND);
            String carModel = getArguments().getString(ARG_CAR_DETAIL_MODEL);
            String carReg = getArguments().getString(ARG_CAR_DETAIL_REG);

            carBrandextView.setText(carbrand);
            carModelTextView.setText(carModel);
            carRegTextView.setText(carReg);
            ImageView imgView = (ImageView)rootView.findViewById(R.id.car_imageView);
            if(carbrand.equals("maruti")) imgView.setImageResource(R.drawable.maruti_swift_desire);
            else if (carbrand.equals("hyundai"))imgView.setImageResource(R.drawable.hyundai_cars);
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            String whichCar = userCars.get(position);
            HashMap<String,String> currentDetail = userCarDetailsMap.get(position);

            return PlaceholderFragment.newInstance(whichCar,currentDetail);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return userCars.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return userCars.get(position);
        }
    }
}
