package com.cherry.alok.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.text.Spannable;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

public class SelectSlotActivity extends AppCompatActivity {
    Calendar cal = Calendar.getInstance();
    BottomSheetBehavior behavior;
    static ViewPager viewPager;
    static int currentTabPosition;
    static View slotsView;
    CountDownTimer progressBarTimer;
    Button confirm_request;
    UniversalAsyncTask uniTask;
    BottomSheetBehavior behavior_smallbottomsheet;
    enum AsyncActivities
    {
      NONE,
      SELECT_SLOT,
      INIT_REQ,
      SHOW_ANIM,
    };
    AsyncActivities current_task = AsyncActivities.NONE;
    boolean additional_inst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_slot);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        viewPager = (ViewPager) findViewById(R.id.tabdate_viewpager);
        setupTabs();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabdate_tabs);
        //tabLayout.setupWithViewPager(viewPager);


        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                confirm_request.setEnabled(false);
                currentTabPosition = tab.getPosition();
                //viewPager.setCurrentItem(currentTabPosition);
                GetSlotInformation();
                //showProgress(true);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        final EditText editText = (EditText)findViewById(R.id.addinst_editText);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    //bottomSheet.animate().translationY(slotsView.getHeight()*0).setDuration(1000).start();
                }
                else
                {
                    //bottomSheet.animate().translationY(slotsView.getHeight()*1).setDuration(1000).start();
                }
            }
        });

/*
        TextView atv = (TextView)findViewById(R.id.text_add_inst);

        atv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                additional_inst = true;
                RollDowntheUi();
            }
        });

        Button save_instruction = (Button)findViewById(R.id.save_add_inst);
        save_instruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                additional_inst = false;
                RollUpTheUi();

            }
        });
*/

        ((LinearLayout)findViewById(R.id.toplayout)).
                setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        TextView car_reg_text = (TextView)findViewById(R.id.request_car_text);
        String carNo = SharedData.GetDefaultCarNo();
        if(carNo != null)
        {
            if (car_reg_text != null) {
                car_reg_text.setText(carNo);
            }
        }
        Button caredit = (Button)findViewById(R.id.carselect);
        caredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedData.HandleNavigation(R.id.nav_manage,SelectSlotActivity.this);
            }
        });

       //mProgressView = findViewById(R.id.loadingdata_progress);
        slotsView = findViewById(R.id.slot_form);
        progressBarTimer = new CountDownTimer(5000,0) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
              //  mProgressView.setVisibility(View.GONE);
                viewPager.setCurrentItem(currentTabPosition);

            }
        };

       confirm_request = (Button)findViewById(R.id.confirm_request);
        confirm_request.setText("CHECKOUT ");  ;
        confirm_request.setEnabled(false);
        confirm_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               HashMap<String , String> userdetailsFromDB = SharedData.FetchUser();
                int userStatus = Integer.parseInt(userdetailsFromDB.get("status"));
                if(userStatus > SharedData.UserStatus.CarProfile.getID())
                {
                    showSettingsAlert("Request Ongoing Already","Sorry ! Currently we support only one active request at a time");
                    return;
                }
                //InitRequest();
                Intent intent =  new Intent(getApplicationContext() , Activity_Payment.class);
                String urlParameters = String.format("serviceid=%s&timeslot_id=%s&carno=%s&daysahead=%s&latt=%s&longg=%s&inst=%s" ,Integer.toString(SharedData.GetService()) , Integer.toString(SharedData.GetTimeSlot()) ,SharedData.GetDefaultCarNo(),currentTabPosition,SharedData.GetRequestLocation().latitude , SharedData.GetRequestLocation().longitude,GetAddtionalInstructions());
                intent.putExtra("request_param",urlParameters);
                startActivity(intent);
            }
        });
        GetSlotInformation();
        SetCarLogo();

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

    @Override
    public void onBackPressed() {
        if(additional_inst)
        {
            additional_inst = false;
            RollUpTheUi();
        }
        else
        {
            Bundle services_screen_data = new Bundle();
            services_screen_data.putBoolean("shownext",true);
            SharedData.clearStackOfLastActivity = true;
            SharedData.HandleNavigation(R.id.nav_services,this,services_screen_data);
            //NavUtils.navigateUpFromSameTask(this);
        }
    }

    public void RollDowntheUi()
    {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.selectslot_cordlayout);
        //View bottomSheet = coordinatorLayout.findViewById(R.id.selectslot_bottom_sheet);
        slotsView.animate().translationY(slotsView.getHeight()*-1).setDuration(700).start();
        //bottomSheet.animate().translationY(bottomSheet.getHeight()*1).setDuration(1500).start();
    }

    public void RollUpTheUi()
    {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.selectslot_cordlayout);
        //View bottomSheet = coordinatorLayout.findViewById(R.id.selectslot_bottom_sheet);
        slotsView.animate().translationY(slotsView.getHeight()*0).setDuration(500).start();
        // behavior.setPeekHeight(0);
        //behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //bottomSheet.animate().translationY(slotsView.getHeight()*0).setDuration(1000).start();
    }

    public void AddtionalInstructions_Clicked()
    {
        int i=0;
        i++;
    }

    public void SetCarLogo()
    {
        ImageView carlogo = (ImageView) findViewById(R.id.carlogo);
        String carbrand = SharedData.GetDefaultCarBrand();

        switch(carbrand.toLowerCase())
        {
            case "maruti":
            {
                carlogo.setImageResource(R.drawable.logomarutisuzuki);
            }
            break;
            case "chevrolet":
            {
                carlogo.setImageResource(R.drawable.logochevy);
            }
            break;
            case "fiat":
            {
                carlogo.setImageResource(R.drawable.logofiat);
            }
            break;
            case "ford":
            {
                carlogo.setImageResource(R.drawable.logoford2);
            }
            break;
            case "honda":
            {
                carlogo.setImageResource(R.drawable.logohonda);
            }
            break;
            case "hyundai":
            {
                carlogo.setImageResource(R.drawable.logohyundai);
            }
            break;
            case "mahindra":
            {
                carlogo.setImageResource(R.drawable.logomahindra);
            }
            break;
            case "nissan":
            {
                carlogo.setImageResource(R.drawable.logonissan);
            }
            break;
            case "tata":
            {
                carlogo.setImageResource(R.drawable.logotata);
            }
            break;
            case "toyota    ":
            {
                carlogo.setImageResource(R.drawable.logotoyota);
            }
            break;
            case "skoda":
            {
                carlogo.setImageResource(R.drawable.logoskoda);
             }
            break;
            case "volkswagen":
            {
                carlogo.setImageResource(R.drawable.logovw);
            }
            break;



        }
    }


    public void InitRequest()
    {
        current_task = AsyncActivities.INIT_REQ;
        showProgressDialog("Find A Car Wash Near You");
        String url = "request/"+SharedData.GetUserId()+"/";
        String urlParameters = String.format("serviceid=%s&timeslot_id=%s&carno=%s&daysahead=%s&latt=%s&longg=%s&inst=%s" ,Integer.toString(SharedData.GetService()) , Integer.toString(SharedData.GetTimeSlot()) ,SharedData.GetDefaultCarNo(),currentTabPosition,SharedData.GetRequestLocation().latitude , SharedData.GetRequestLocation().longitude,GetAddtionalInstructions());

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,selectSlotHandler);
        uniTask.execute();
    }

    public String GetAddtionalInstructions()
    {
        EditText addtional_inst_text = (EditText)findViewById(R.id.addinst_editText);
        String instruction = "";
        if(addtional_inst_text != null)
        {
          instruction = addtional_inst_text.getText().toString();
        }
        return  instruction;
    }

    public void GetSlotInformation()
    {
        SharedData.ReInitSlots();
        showProgressDialog("Fetching Available Slots");
        current_task = AsyncActivities.SELECT_SLOT;
        String url = "slotscheck/"+SharedData.GetUserId()+"/";
        String urlParameters = String.format("serviceid=%s&daysahead=%s" ,Integer.toString(SharedData.GetService()) , currentTabPosition);

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,selectSlotHandler);
        uniTask.execute();
    }

    private void setupTabs() {

        TabLayout slotTabs = (TabLayout) findViewById(R.id.tabdate_tabs);
        for(int i=0 ;i <7;i++ ) {
            slotTabs.addTab(slotTabs.newTab().setText(GetDateDetails(i)));
        }

    }

    public String GetDateDetails(int days_ahead)
    {
        SimpleDateFormat weekday = new SimpleDateFormat("EEE, MMM d");
        cal.add(Calendar.DAY_OF_YEAR, days_ahead);
        String dates=  weekday.format(new Date(cal.getTimeInMillis()));
        cal.add(Calendar.DAY_OF_YEAR, days_ahead*-1);
        String [] date_components = dates.split(",");
        String newdate =  date_components[1]+"\n" + date_components[0];
        //String final_date_text =  Html.fromHtml(newdate);


        final SpannableStringBuilder str = new SpannableStringBuilder(dates);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, dates.length()-4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        String final_date = str.toString();
        return newdate;
    }
    /*private static View mProgressView;
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        //

        slotsView.animate().translationY(show ? slotsView.getHeight()*-1 : 0).setDuration(1000).start();
       // viewPager.setVisibility(show? View.GONE : View.VISIBLE);
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                viewPager.setCurrentItem(currentTabPosition);
            }
        });


     //  if(show)progressBarTimer.start();
        SendMessage();
    }*/

    public void SendMessage()
    {
        Message msgObj = selectSlotHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("AnimationStart",0 );
        msgObj.setData(b);
        selectSlotHandler.sendMessageDelayed(msgObj,5000);
    }

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
                int slotId = Integer.parseInt(key);
                Boolean sloAvailable = Boolean.parseBoolean(value);
                SharedData.slotsInfo[slotId] = sloAvailable;


            }

        }
    }

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
            String driver_name = jsonObject.optString("driver");
            String driver_mobile = jsonObject.optString("driverphone");
            String joint_name = jsonObject.optString("joint");
            int time_slot  =  Integer.parseInt(jsonObject.optString("time_slot_id").toString());
            int request_status_id = Integer.parseInt(jsonObject.optString("request_status").toString());
            String date  = jsonObject.optString("request_date");
            Bundle order_screen_data = new Bundle();
                order_screen_data.putString("driver",driver_name);
                order_screen_data.putString("drivermobile",driver_name);
                order_screen_data.putString("joint",joint_name);
                order_screen_data.putInt("request_status",request_status_id);
                order_screen_data.putInt("slot",time_slot);
                order_screen_data.putInt("days_ahead",currentTabPosition);
            SharedData.UpdateUserStatusInDb(3);
            SharedData.HandleNavigation(R.id.nav_order,this,order_screen_data);

    }

/*    static class ViewPageDaterAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPageDaterAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public static class DateFragment extends Fragment {
        int color;
        SimpleRecyclerAdapter adapter;
        String[] slotArray = {"9 AM -12 Noon ","12 Noon - 2 PM","2PM - 4M","4PM - 6PM"};

        public DateFragment() {
        }

        @SuppressLint("ValidFragment")
        public DateFragment(int color) {
            this.color = color;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.map_bottomsheet_fragment, container, false);

            ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_listview, slotArray);
           // ListView listView = (ListView) getActivity().findViewById(R.id.slot_list);
          //  listView.setAdapter(adapter);

            return view;
        }
    }*/

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

    private  Handler selectSlotHandler = new Handler() {

        public void handleMessage(Message msg) {
            String aresponse = msg.getData().getString("taskstatus");
            if(aresponse != null)
            {
                if(current_task == AsyncActivities.SELECT_SLOT)
                {
                    viewPager.setCurrentItem(currentTabPosition);
                    PostOperation();
                    hideProgressDialog();
                }
                else if(current_task == AsyncActivities.INIT_REQ)
                {
                    PostRequestOperation();
                    hideProgressDialog();
                }
            }
            else {


                int animationStart = msg.getData().getInt("AnimationStart");
                if (animationStart == 0) {
                    //mProgressView.setVisibility(View.GONE);
                    viewPager.setCurrentItem(currentTabPosition);
                    slotsView.animate().translationY(0).setDuration(500).start();
                }
            }
        }
        };





}
