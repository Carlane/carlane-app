package com.cherry.alok.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    enum AsyncActivities
    {
      NONE,
      SELECT_SLOT,
      INIT_REQ,
      SHOW_ANIM,
    };
    AsyncActivities current_task = AsyncActivities.NONE;

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


         /*Set up the Bottom Sheet containing types of services*/
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.selectslot_cordlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.selectslot_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {

                    case BottomSheetBehavior.STATE_DRAGGING:

                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:

                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;


                }

            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }

        });

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
        confirm_request.setEnabled(false);
        confirm_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitRequest();
            }
        });
        GetSlotInformation();

    }


    public void InitRequest()
    {
        current_task = AsyncActivities.INIT_REQ;

        String url = "request/"+SharedData.GetUserId()+"/";
        String urlParameters = String.format("serviceid=%s&timeslot_id=%s&carno=%s&daysahead=%s" ,Integer.toString(SharedData.GetService()) , Integer.toString(SharedData.GetTimeSlot()) ,SharedData.GetDefaultCarNo(),currentTabPosition);

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters ,selectSlotHandler);
        uniTask.execute();
    }

    public void GetSlotInformation()
    {
        SharedData.ReInitSlots();
        showProgressDialog();
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
            if(error_in_Result == true)
            {
                return;
            }
            String driver_name = jsonObject.optString("driver");
            String joint_name = jsonObject.optString("joint");
            int time_slot  =  Integer.parseInt(jsonObject.optString("time_slot_id").toString());
            int request_status_id = Integer.parseInt(jsonObject.optString("request_status").toString());
            String date  = jsonObject.optString("request_date");
            Bundle order_screen_data = new Bundle();
                order_screen_data.putString("driver",driver_name);
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
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Fetching Available Slots");
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

    private  Handler selectSlotHandler = new Handler() {

        public void handleMessage(Message msg) {
            String aresponse = msg.getData().getString("taskstatus");
            if(aresponse != null)
            {
                if(current_task == AsyncActivities.SELECT_SLOT)
                {
                    hideProgressDialog();
                    viewPager.setCurrentItem(currentTabPosition);
                    PostOperation();
                }
                else if(current_task == AsyncActivities.INIT_REQ)
                {
                    PostRequestOperation();
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
