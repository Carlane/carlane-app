package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddCarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddCarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCarFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    UniversalAsyncTask uniTask = null;
    String[] items = new String[]{"Brand","Maruti", "Hyundai"};//, "Chevrolet","Renault","Fiat","Datsun","BMW","Volkswagon","Mercedes"};
    String[] marutiModels = new String[]{"Model","Swift Dezire" , "Swift","Baleno","Celerio","Alto 800"};

    List<String> carbrandList = new ArrayList<String>();
    List<String> carMarutiList = new ArrayList<String>();
    List<String> carChevroletList = new ArrayList<String>();
    List<String> carFiatList = new ArrayList<String>();
    List<String> carFordList = new ArrayList<String>();
    List<String> carHondaList = new ArrayList<String>();
    List<String> carHyundList = new ArrayList<String>();
    List<String> carMahindraList = new ArrayList<String>();
    List<String> carNissanList = new ArrayList<String>();
    List<String> carTataList = new ArrayList<String>();
    List<String> carToyotaList = new ArrayList<String>();
    List<String> carSkotaList = new ArrayList<String>();
    List<String> carVWaList = new ArrayList<String>();


    String[] hyundaiModels = new String[]{"Model","i20" ,"i10" ,"Creta","Verna"};
    ArrayAdapter<String> adapter = null;//new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
    ArrayAdapter<String> carmodeladapter = null;//new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, marutiModels);
    int userId =0;
    ArrayList<String> selecteditem = new ArrayList<>();
    Spinner carbrand;

    private OnFragmentInteractionListener mListener;

    public AddCarFragment() {
        // Required empty public constructor

        carbrandList.add("Brand");
        carbrandList.add("Chevrolet");
        carbrandList.add("Fiat");
        carbrandList.add("Ford");
        carbrandList.add("Honda");
        carbrandList.add("Hyundai");
        carbrandList.add("Mahindra");
        carbrandList.add("Maruti");
        carbrandList.add("Nissan");
        carbrandList.add("Tata");
        carbrandList.add("Toyota");
        carbrandList.add("Skoda");
        carbrandList.add("Volkswagen");


        /////// MARUTI   ////////
        carMarutiList.add("Models");
        carMarutiList.add("A-Star");
        carMarutiList.add("Alto");
        carMarutiList.add("Alto K10");
        carMarutiList.add("Baleno");
        carMarutiList.add("Celerio");
        carMarutiList.add("Estilo");
        carMarutiList.add("Ertiga");
        carMarutiList.add("Gypsy");
        carMarutiList.add("Maruti Suzuki 800");
        carMarutiList.add("Maruti Suzuki Omni");
        carMarutiList.add("Swift");
        carMarutiList.add("Swift Dezire");
        carMarutiList.add("SX4");
        carMarutiList.add("Wagon R");
        carMarutiList.add("Ritz");


        /////// HYNDAI   ///////

        carHyundList.add("Models");
        carHyundList.add("Creta");
        carHyundList.add("Eon");
        carHyundList.add("Elantra");
        carHyundList.add("i20");
        carHyundList.add("i20 Active");
        carHyundList.add("i20 Elite");
        carHyundList.add("Sante Fe");
        carHyundList.add("Verna");
        carHyundList.add("Verna 4S Fluidic");
        carHyundList.add("Xcent");
        ////////////////CHEVROLET///////////////////////

        carChevroletList.add("Models");
        carChevroletList.add("Aveo UVA");
        carChevroletList.add("Beat");
        carChevroletList.add("Captiva");
        carChevroletList.add("Cruze");
        carChevroletList.add("Enjoy");
        carChevroletList.add("Sail");
        carChevroletList.add("Sail Hatchback");
        carChevroletList.add("Spark");
        carChevroletList.add("Tavera");

        ///////////////   FIAT ///////////////////////

        carFiatList.add("Models");
        carFiatList.add("Avventura");
        carFiatList.add("Punto");
        carFiatList.add("Punto Abarth");
        carFiatList.add("Punto Evo");
        carFiatList.add("Linea");
        carFiatList.add("Linea Classic");

        //////////////////////    FORD    /////////////////////

        carFordList.add("Models");
        carFordList.add("Classic");
        carFordList.add("Ecosport");
        carFordList.add("Endeavour");
        carFordList.add("Fiesta");
        carFordList.add("Figo");

        //////// HONDA ////////
        carHondaList.add("Models");
        carHondaList.add("Accord");
        carHondaList.add("Amaze");
        carHondaList.add("Brio");
        carHondaList.add("City");
        carHondaList.add("CR-V");


        ///////   MAHINDRA ///////

        carMahindraList.add("Models");
        carMahindraList.add("Bolero");
        carMahindraList.add("E20");
        carMahindraList.add("KUV / TUV");
        carMahindraList.add("Quantro");
        carMahindraList.add("Scorpio");
        carMahindraList.add("Scorpio Getaway");
        carMahindraList.add("Thar");
        carMahindraList.add("Verito");
        carMahindraList.add("Verito Vibe CS");
        carMahindraList.add("XUV 500");
        carMahindraList.add("Xylo");



        /////////  NISSAN ///////
        carNissanList.add("Models");
        carNissanList.add("Datsun");

        ////// TATA
        carTataList.add("Models");
        carTataList.add("Aria");
        carTataList.add("Bolt");
        carTataList.add("Indica");
        carTataList.add("Indica V2");
        carTataList.add("Indica eV2");
        carTataList.add("Indigo");
        carTataList.add("Nano");
        carTataList.add("Sumo");
        carTataList.add("Safari");
        carTataList.add("Safari Storme");
        carTataList.add("Tiago");
        carTataList.add("Zest");

        /////TOYOTA
        carToyotaList.add("Models");
        carToyotaList.add("Camry");
        carToyotaList.add("Corolla Altis");
        carToyotaList.add("Etios");
        carToyotaList.add("Etios Liva");
        carToyotaList.add("Fortuner");
        carToyotaList.add("Innova");

        /////// SKODA
        carSkotaList.add("Models");
        carSkotaList.add("Laura");
        carSkotaList.add("Octavia");
        carSkotaList.add("Rapid");
        carSkotaList.add("Superb");
        ///// Volkswagen

        carVWaList.add("Models");
        carVWaList.add("Ameo");
        carVWaList.add("Beetle");
        carVWaList.add("Jetta");
        carVWaList.add("Polo");
        carVWaList.add("Polo Cross");
        carVWaList.add("Vento");


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
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Saving User Details");
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
        List<String> list = new ArrayList<String>();
        list.add("list 1");
        list.add("list 2");
        list.add("list 3");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(addCarView.getContext(),
                android.R.layout.simple_spinner_item, carbrandList);
        dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        carbrand.setAdapter(dataAdapter);
        carbrand.setSelection(1);
        AutoCompleteTextView registrationNumberText = (AutoCompleteTextView)addCarView.findViewById(R.id.registration_text);
        registrationNumberText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        //Obtain the User Id which is passed from parent activity

/*        carbrand.setAdapter(new ArrayAdapter<String>(addCarView.getContext(), android.R.layout.simple_spinner_dropdown_item, items));*/
        RegisterButtonClickHandler();
        //Evetn Handler for the first spinner for Car Brands
        carbrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                //If the selection is Legit , populate the second spiner with data and display it to user
                if(position > 0)
                {
                    final Spinner carmodel = (Spinner)addCarView.findViewById(R.id.CarModel);
                    //SetCarModelSpinnerVisibility(View.VISIBLE);
                    //SetAddButtonVisibility(View.INVISIBLE);
                    //SetCarRegistrationTextVisibility(View.INVISIBLE);
                    carmodeladapter  = (GetCarModelAdapter(position));
                    carmodel.setAdapter(carmodeladapter);


                    //Event Handler for Selection Changed Event in the second Spinner for CarModels
                    carmodel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            if(position > 0)
                            {

                                //SetAddButtonVisibility(View.VISIBLE);
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

        return addCarView;
    }

    public void RegisterButtonClickHandler()
    {
        Button addButton = (Button)addCarView.findViewById(R.id.addCarbutton);
        addButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                AutoCompleteTextView registrationNumberText = (AutoCompleteTextView)addCarView.findViewById(R.id.registration_text);
                registrationNumberText.clearFocus();
                //Validate Registration
                RegistrationNumberValidator numberValidator = new RegistrationNumberValidator();
                if(numberValidator.validate(registrationNumberText.getText().toString()))
                {

                    if(ValidateMobileInfo()) {
                        SetBrandInSelectedArray();
                        SetModelInSelectedArray();
                        selecteditem.add(registrationNumberText.getText().toString());
                        selecteditem.add(GetMobileNumber());
                        if(selecteditem.get(0).toLowerCase().equals("brand") || selecteditem.get(1).toLowerCase().equals("models"))
                        {
                            Snackbar.make(addCarView, "Car Brand and Model selection is not finished", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        }
                        showProgressDialog();
                        String url = "CarInfo/" + userId + "/";
                        String urlParameters = String.format("carbrand=%s&carmodel=%s&registration_number=%s&mobile_number=%s", selecteditem.get(0).toLowerCase(), selecteditem.get(1), selecteditem.get(2).toUpperCase(),selecteditem.get(3));
                        uniTask = new UniversalAsyncTask(url, "POST", urlParameters, handler);
                        uniTask.execute(selecteditem);
                    }
                    else
                    {
                        Snackbar.make(addCarView, "Invalid Mobile Number", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }


                }
                else
                {
                    Snackbar.make(addCarView, "Invalid Registration Number .. ?", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //Toast.makeText(getContext(),"Invalid RegistraionNumber",Toast.LENGTH_SHORT).show();
                }
            }
        });
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
    public void SetCarModelSpinnerVisibility(int visibility)
    {
        Spinner carmodel = (Spinner)addCarView.findViewById(R.id.CarModel);
        carmodel.setVisibility(visibility);
    }

    public void SetAddButtonVisibility(int visibility)
    {
        Button addButton = (Button)addCarView.findViewById(R.id.addCarbutton);
        addButton.setVisibility(visibility);
    }

    public ArrayAdapter<String> GetCarModelAdapter(int carmodels)
    {
        switch(carmodels)
        {
            case 1:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carChevroletList);
            }
            break;
            case 2:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carFiatList);
            }
            break;
            case 3:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carFordList);

            }
            break;
            case 4:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carHondaList);

            }
            break;
            case 5:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carHyundList);
            }
            break;
            case 6:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carMahindraList);

            }
            break;
            case 7:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carMarutiList);

            }
            break;
            case 8:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carNissanList);

            }
            break;
            case 9:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carTataList);

            }
            break;
            case 10:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carToyotaList);

            }
            break;
            case 11:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carSkotaList);

            }
            break;
            case 12:
            {
                carmodeladapter= new ArrayAdapter<String>(getActivity(), R.layout.custom_spinner_dropdown_item, carVWaList);

            }
            break;

        }

        return carmodeladapter;
    }

    public ArrayAdapter<String> GetCarBrandAdapter()
    {
        carmodeladapter= new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.custom_spinner_dropdown_item, items);
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

    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("taskstatus");
            hideProgressDialog();
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
    };
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
