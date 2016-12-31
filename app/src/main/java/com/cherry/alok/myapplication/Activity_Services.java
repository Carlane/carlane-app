package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cherry.alok.myapplication.dummy.ServiceRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Activity_Services extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    CollapsingToolbarLayout collapsingToolbar;
    RecyclerView recyclerView;
    int mutedColor = R.attr.colorPrimary;
    SimpleRecyclerAdapter simpleRecyclerAdapter;
    ServiceRecyclerAdapter serviceRecyclerAdapter;
    UniversalAsyncTask uniTask = null;
    List<HashMap<String,String>> usercarDetailsMap = new ArrayList<>();
    BottomSheetBehavior behavior;
    BottomSheetBehavior behavior_smallbottomsheet;
    Boolean shownext = false;
    ArrayList<Integer> service_id_list = new ArrayList<Integer>();
    enum AsyncActivities
    {
        NONE,
        SERVICE_VERSION,
    };

    AsyncActivities current_task = AsyncActivities.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_collapse_header);
        Toolbar toolbar = (Toolbar) findViewById(R.id.services_anim_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        final HashMap<Integer, String> services_status = new HashMap<Integer , String>();
        if(bundle != null)
        {
            shownext = bundle.getBoolean("shownext");
        }

        //SetImageOnDrawer();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.services_collapse_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                SetImageOnDrawer(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                HideOnGoingRequestIfRequired();
            }

        };
        RegisterSignOut();
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.services_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

       /* collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.services_collapsing_toolbar);
        collapsingToolbar.setTitle("Select A Packages");*/

        //ImageView header = (ImageView) findViewById(R.id.services_header);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.header2);

       // if(header != null)

        {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP)
            {
             //   header.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.welcome , getApplicationContext().getTheme()));
            }
            else
            {
             //   header.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.welcome));
            }
        }

        /*Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {

                mutedColor = palette.getMutedColor(R.color.primary_500);
                collapsingToolbar.setContentScrimColor(mutedColor);
                collapsingToolbar.setStatusBarScrimColor(R.color.black_trans80);
            }
        });*/

        recyclerView = (RecyclerView) findViewById(R.id.services_scrollableview);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        if (serviceRecyclerAdapter == null) {
            serviceRecyclerAdapter = new ServiceRecyclerAdapter(this , services_status);

        }

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.services_cordinatorLayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.services_bottom_sheet);
        View smallbottomSheet = coordinatorLayout.findViewById(R.id.services_smallbottom_sheet);
        ImageView bottom_sheet_down = (ImageView)findViewById(R.id.down_bottomsheet) ;
        bottom_sheet_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });



        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior_smallbottomsheet = BottomSheetBehavior.from(smallbottomSheet);
        if(!shownext)
        {
            behavior_smallbottomsheet.setPeekHeight(0);
            behavior_smallbottomsheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        behavior_smallbottomsheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        Button next_button = (Button)findViewById(R.id.service_select_next);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNavigation();

            }
        });



        GetServicesFromSharedPrefs();
        if(serviceRecyclerAdapter != null && serviceRecyclerAdapter.GetServiceIdListCount() ==0)
        {
            //User does not have any version of services present in his machine , so fire REST API and get some
            GetServiceVersion();

        }
        else
        {
            SetRecyclerAdapter();
        }


        serviceRecyclerAdapter.SetOnItemClickListener(new ServiceRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TextView service_cost_small_btmsheet = (TextView)findViewById(R.id.service_cost_small_btmsheet);
                //TextView services_user_notice = (TextView)findViewById(R.id.services_user_notice);
                Button next_button = (Button)findViewById(R.id.service_select_next);
                switch (position) {
                    case 0:
                        //view.animate().translationY(view.getHeight()*1).setDuration(500).start();
                      //  behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                        {
                            next_button.setEnabled(true);
                        }


                        SetTexts(position);
                        SharedData.SetService(position+1);
                        break;
                    case 1:
                        //SharedData.SetService(position+1);
                      //  behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        {
                            next_button.setEnabled(true);
                        }
                        {

                        }
                        SetTexts(position);
                        SharedData.SetService(position+1);
                        break;
                    case 2:
                       // behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        {
                            next_button.setEnabled(true);
                        }

                        //SharedData.SetService(position+1);
                        SetTexts(position);
                        SharedData.SetService(position+1);
                        break;
                    case 3:
                        break;
                    case 999:
                    {
                        SetTexts(position - 999);
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    break;
                    case 1000:
                    {
                        SetTexts(position -999);
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    break;
                    case 1001:
                    {
                        SetTexts(position -999);
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    break;


                    default:
                        Toast.makeText(getBaseContext(), "Undefined Click!", Toast.LENGTH_SHORT).show();
                }
               /* String url = "CarInfo/"+SharedData.GetUserId()+"/";
                uniTask = new UniversalAsyncTask(url,"GET","" ,handler);

                ArrayList<String> dummy = new ArrayList<String>();
                uniTask.execute(dummy);*/


            }
        });


    }

    public void SetImageOnDrawer(View drawerView)
    {
        ImageView nav_back = (ImageView)drawerView.findViewById(R.id.imageView);
        Bitmap navImg = SharedData.GetUserpic();
        if(nav_back!= null)
        {
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),navImg);
            drawable.setCircular(true);
            nav_back.setImageDrawable(drawable);
            //bmImage.setImageBitmap(result);
        }

        TextView txt = (TextView)findViewById(R.id.userNameText);
        if(txt != null)
        {
            txt.setText(SharedData.GetUserName());
        }


    }

    public void RegisterSignOut()
    {
        Button singoutBtn = (Button)findViewById(R.id.services_signout);
        singoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedData.DeleteAllUser();
                SharedData.DeleteAllUserCar();
                SharedData.SignOutGoogle();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

    }



    public void SetRecyclerAdapter()
    {
        if(serviceRecyclerAdapter != null && recyclerView != null)
            recyclerView.setAdapter(serviceRecyclerAdapter);
    }

    public void GetServiceVersion()
    {
        String url = "services_version/"+SharedData.GetUserId()+"/";
        String val = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("service_version" , "-5.0000");
        String urlParameters = String.format("service_version=%s" , val);

        uniTask = new UniversalAsyncTask(url,"POST",urlParameters,handler);

        ArrayList<String> dummy = new ArrayList<String>();
        current_task =  AsyncActivities.SERVICE_VERSION;
        uniTask.execute(dummy);
        showProgressDialog("Getting Services in your Area");
    }

    private ProgressDialog mProgressDialog;
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public JSONObject GetServiceAttributes(int i )
    {

        JSONObject attributes= SharedData.ParseServiceAttributes(getApplicationContext() ,service_id_list.get(i).toString());
        return attributes;
    }

    public void GetServicesFromSharedPrefs()
    {
        service_id_list = SharedData.GetSortedServicesFromSharedPrefs(getApplicationContext());
    }

    private void SetNavigation()
    {
        Intent selectSlot = new Intent(getApplicationContext(), SelectSlotActivity.class);
        startActivity(selectSlot);
    }
    public void SetTexts(int i)
    {
        try {
            JSONObject attributes = GetServiceAttributes(i);
            TextView service_name_bottomsheet = (TextView)findViewById(R.id.service_name_bottomsheet);
            ImageView washlogo = (ImageView)findViewById(R.id.wash_logo);
            TextView internal_text1 = (TextView)findViewById(R.id.text_1);
            TextView internal_text2 = (TextView)findViewById(R.id.text_2);
            TextView internal_text3 = (TextView)findViewById(R.id.text_3);
            TextView internal_text4 = (TextView)findViewById(R.id.text_4);
            TextView internal_text5 = (TextView)findViewById(R.id.text_5);
            TextView internal_text6 = (TextView)findViewById(R.id.text_6);


            TextView external_text1 = (TextView)findViewById(R.id.text_external_1);
            TextView external_text2 = (TextView)findViewById(R.id.text_external_2);
            TextView external_text3 = (TextView)findViewById(R.id.text_external_3);
            TextView external_text4 = (TextView)findViewById(R.id.text_external_4);
            TextView external_text5 = (TextView)findViewById(R.id.text_external_5);
            TextView external_text6 = (TextView)findViewById(R.id.text_external_6);
            TextView external_text7 = (TextView)findViewById(R.id.text_external_7);
            TextView external_text8 = (TextView)findViewById(R.id.text_external_8);
            TextView external_text9 = (TextView)findViewById(R.id.text_external_9);


            TextView text_cost_hatch_Sedan = (TextView)findViewById(R.id.cost_text_hatch_sedan);
            TextView text_cost_suv = (TextView)findViewById(R.id.cost_text_suv);
            //TextView service_cost_small_btmsheet = (TextView)findViewById(R.id.service_cost_small_btmsheet);
           // service_cost_small_btmsheet.setText( attributes.getString("cost"));
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("service_cost" ,  attributes.getString("cost")).commit();
            switch(i)
            {
                case 0:
                {
                    service_name_bottomsheet.setText(attributes.getString("name") + "WASH");
                    internal_text1.setText("1. "+ attributes.getString("attribute_1"));
                    internal_text2.setText("");
                    internal_text3.setText("");
                    internal_text4.setText("");
                    internal_text5.setText("");
                    internal_text6.setText("");


                    external_text1.setText("1. " + attributes.getString("attribute_11"));
                    external_text2.setText("2. " + attributes.getString("attribute_12"));
                    external_text3.setText("3. " + attributes.getString("attribute_13"));
                    external_text4.setText("4. " + attributes.getString("attribute_14"));
                    external_text5.setText("5. " + attributes.getString("attribute_15"));

                    external_text6.setText("");
                    external_text7.setText("");
                    external_text8.setText("");
                    external_text9.setText("");
                    washlogo.setImageResource(R.drawable.lightening);

                }
                break;
                case 1:
                {
                    service_name_bottomsheet.setText(attributes.getString("name") + "WASH");
                    internal_text1.setText("1. " + attributes.getString("attribute_1"));
                    internal_text2.setText("2. " + attributes.getString("attribute_2"));
                    internal_text3.setText("3. " + attributes.getString("attribute_3"));
                    internal_text4.setText("");
                    internal_text5.setText("");
                    internal_text6.setText("");


                    external_text1.setText("1. " + attributes.getString("attribute_11"));
                    external_text2.setText("2. " + attributes.getString("attribute_12"));
                    external_text3.setText("3. " + attributes.getString("attribute_13"));
                    external_text4.setText("4. " + attributes.getString("attribute_14"));
                    external_text5.setText("5. " + attributes.getString("attribute_15"));
                    external_text6.setText("");
                    external_text7.setText("");
                    external_text8.setText("");
                    external_text9.setText("");
                    washlogo.setImageResource(R.drawable.balance);

                }
                break;
                case 2:
                {
                    service_name_bottomsheet.setText(attributes.getString("name") + "WASH");
                    internal_text1.setText("1. " + attributes.getString("attribute_1"));
                    internal_text2.setText("2. " + attributes.getString("attribute_2"));
                    internal_text3.setText("3. " + attributes.getString("attribute_3"));
                    internal_text4.setText("4. " + attributes.getString("attribute_4"));
                    internal_text5.setText("5. " + attributes.getString("attribute_5"));
                    internal_text6.setText("6. " + attributes.getString("attribute_6"));

                    external_text1.setText("1. " + attributes.getString("attribute_11"));
                    external_text2.setText("2. " + attributes.getString("attribute_12"));
                    external_text3.setText("3. " + attributes.getString("attribute_13"));
                    external_text4.setText("");
                    external_text4.setText("4. " + attributes.getString("attribute_14"));
                    external_text5.setText("5. " + attributes.getString("attribute_15"));
                    external_text6.setText("6. " + attributes.getString("attribute_16"));
                    external_text7.setText("7. " + attributes.getString("attribute_17"));
                    external_text8.setText("8. " + attributes.getString("attribute_18"));
                    external_text9.setText("");
                    washlogo.setImageResource(R.drawable.star);


                }
                break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            SharedData.HandleNavigation(id,this);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.services_collapse_drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

    public void HideOnGoingRequestIfRequired() {
        NavigationView nav = (NavigationView) findViewById(R.id.services_nav_view);
        Menu nav_Menu = nav.getMenu();
        HashMap<String, String> userdetailsFromDB = SharedData.FetchUser();
        int userStatus = Integer.parseInt(userdetailsFromDB.get("status"));
        if (userStatus == SharedData.UserStatus.RequestPending.getID()) {
            nav_Menu.findItem(R.id.nav_order).setVisible(true);
        } else {
            nav_Menu.findItem(R.id.nav_order).setVisible(false);
        }
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return super.onOptionsItemSelected(item);
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
                        Toast.makeText(getApplicationContext(), "Failed to Fetch Data- "+reason, Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        switch(current_task)
                        {
                            case SERVICE_VERSION:
                            {

                                hideProgressDialog();
                                if(IsUpdateRequired())
                                {
                                    PostOperation();
                                    GetServicesFromSharedPrefs();
                                    GetImagesFromData();
                                }
                                else
                                {
                                    GetServicesFromSharedPrefs();
                                    SetRecyclerAdapter();
                                }

                            }
                            break;


                        }




                    }
                }
            }
            else
            {
                //Toast.makeText(getBaseContext(), "Not Got Response From Server.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    public void GetImagesFromData()
    {
        try {
            //GetServicesFromSharedPrefs();
            for (int i = 0; i < service_id_list.size(); i++) {
                JSONObject attributes = GetServiceAttributes(i);
                String logo = attributes.getString("logo");
                int j=0;
                j++;
                LoadProfileImage lfi =  new LoadProfileImage(i);
                if(lfi != null) {
                    lfi.execute(logo);
                }

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    String updatedVersion = "";
    public boolean IsUpdateRequired()
    {
        JSONObject jsonRootObject = null;
        try
        {
            jsonRootObject = new JSONObject(uniTask.outputJasonString);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        JSONArray jsonArray = jsonRootObject.optJSONArray("response");
        JSONObject jsonObject = null;
        try
        {
            jsonObject = jsonArray.getJSONObject(0);
            Boolean isversionupdated = jsonObject.getBoolean("isversionupdated");
            updatedVersion = jsonObject.getString("system_version");
            return isversionupdated;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    static int count = 0;

    public void PostOperation()
    {




        JSONArray reposnejSonArray = uniTask.GetOutputResult();

        int count = reposnejSonArray.length();
        for(int i=0;i<count;i++)
        {
            Bundle local_bundle = new Bundle();
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
            int service_count =0 ;
            ClearSavedServiceIds(getApplicationContext());
            while( keys.hasNext() )
            {
                String key = (String)keys.next();
                String[] service_ids = key.split("~");
                String service_tocheck ;

                try {
                    //this is the service id+ service name , it will be used as key in shared pre
                    Integer.parseInt(service_ids[1]);//if this gives exception means other one has value of service id
                    service_tocheck = service_ids[1];
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    service_tocheck = service_ids[0];
                }


                String value = null;
                try
                {
                    value = jsonresponseObject.getString(key);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                //key contains both name and id
                if(key.contains(service_tocheck))
                {
                    //put service id string as key and response map as value
                    SharedData.UpdateSharedPref(getApplicationContext() , service_tocheck , value);
                    //Also prepare another shared pref for storing services supported
                    String saved_service_ids = SharedData.GetSharedPreString(getApplicationContext() , "saved_service_ids");//PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("saved_service_ids" , "");
                    // add the latest service id
                    saved_service_ids = saved_service_ids + "~" + service_tocheck;

                    SharedData.UpdateSharedPref(getApplicationContext() , "saved_service_ids" , saved_service_ids);
                    service_count ++;

                }
            }
            SharedData.UpdateSharedPref(getApplicationContext(), "service_count" , service_count);
            //system_version ,  service_version
            if(updatedVersion != "")
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("service_version" , updatedVersion).commit();

            String test1 = SharedData.GetSharedPreString(getApplicationContext() , "1");
            String test2 = SharedData.GetSharedPreString(getApplicationContext() , "2");
            String test3 = SharedData.GetSharedPreString(getApplicationContext() , "3");
            String saved_test = SharedData.GetSharedPreString(getApplicationContext() , "saved_service_ids");
            int test_count = SharedData.GetSharedPrefValue(getApplicationContext() , "service_count");

        }
        if(updatedVersion != "")
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("service_version" , updatedVersion).commit();
    }


    public void ClearSavedServiceIds(Context context)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("saved_service_ids" , "").commit();

    }

    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        int service_id;

        public LoadProfileImage(int i) {
            service_id = i;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap user_pic = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                user_pic = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            SavePhoto(user_pic , service_id);
            return user_pic;
        }

        public void SavePhoto(Bitmap resizedbitmap , int i)
        {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("profile", Context.MODE_PRIVATE);
            if (!directory.exists()) {
                directory.mkdir();
            }
            File mypath = new File(directory, i + ".png");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                resizedbitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                Log.e("SAVE_IMAGE", e.getMessage(), e);
            }
        }

        protected void onPostExecute(Bitmap result) {
            count++;
            if(count == service_id_list.size())
            {
                SetRecyclerAdapter();

            }
           /* if(bmImage!= null)
            {
                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),result);
                drawable.setCircular(true);
                bmImage.setImageDrawable(drawable);
                TextView txt = (TextView)findViewById(R.id.userNameText);
                if(txt != null)
                {
                    txt.setText(SharedData.GetUserName());
                }
                invalidateOptionsMenu();
                //bmImage.setImageBitmap(result);
            }*/
        }
    }

}
