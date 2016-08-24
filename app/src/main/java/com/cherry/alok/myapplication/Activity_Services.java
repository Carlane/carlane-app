package com.cherry.alok.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_collapse_header);
        Toolbar toolbar = (Toolbar) findViewById(R.id.services_anim_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            shownext = bundle.getBoolean("shownext");
            bundle = null;
        }
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
                ImageView nav_back = (ImageView)findViewById(R.id.imageView);
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
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.services_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.services_collapsing_toolbar);
        collapsingToolbar.setTitle("Packages");

        ImageView header = (ImageView) findViewById(R.id.services_header);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.header2);

        if(header != null)

        {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP)
            {
                header.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.welcome , getApplicationContext().getTheme()));
            }
            else
            {
                header.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.welcome));
            }
        }

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {

                mutedColor = palette.getMutedColor(R.color.primary_500);
                collapsingToolbar.setContentScrimColor(mutedColor);
                collapsingToolbar.setStatusBarScrimColor(R.color.black_trans80);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.services_scrollableview);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        if (serviceRecyclerAdapter == null) {
            serviceRecyclerAdapter = new ServiceRecyclerAdapter(this);
            recyclerView.setAdapter(serviceRecyclerAdapter);
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



        serviceRecyclerAdapter.SetOnItemClickListener(new ServiceRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView service_cost_small_btmsheet = (TextView)findViewById(R.id.service_cost_small_btmsheet);
                Button next_button = (Button)findViewById(R.id.service_select_next);
                switch (position) {
                    case 0:
                        //view.animate().translationY(view.getHeight()*1).setDuration(500).start();
                      //  behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                        service_cost_small_btmsheet.setText("BASIC INR 400");

                        next_button.setEnabled(true);
                        SetTexts(position);
                        SharedData.SetService(position+1);
                        break;
                    case 1:
                        //SharedData.SetService(position+1);
                      //  behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        service_cost_small_btmsheet.setText("PREMIUM INR 500");
                        next_button.setEnabled(true);
                        SetTexts(position);
                        SharedData.SetService(position+1);
                        break;
                    case 2:
                       // behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        service_cost_small_btmsheet.setText("PLATINUM INR 600");
                        next_button.setEnabled(true);
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
    private void SetNavigation()
    {
        Intent selectSlot = new Intent(getApplicationContext(), SelectSlotActivity.class);
        startActivity(selectSlot);
    }
    public void SetTexts(int i)
    {
        TextView service_name_bottomsheet = (TextView)findViewById(R.id.service_name_bottomsheet);
        ImageView washlogo = (ImageView)findViewById(R.id.wash_logo);
        TextView internal_text1 = (TextView)findViewById(R.id.text_1);
        TextView internal_text2 = (TextView)findViewById(R.id.text_2);
        TextView internal_text3 = (TextView)findViewById(R.id.text_3);
        TextView internal_text4 = (TextView)findViewById(R.id.text_4);
        TextView internal_text5 = (TextView)findViewById(R.id.text_5);
        TextView internal_text6 = (TextView)findViewById(R.id.text_5);


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

        switch(i)
        {
            case 0:
            {
                service_name_bottomsheet.setText("Basic Wash");
                internal_text1.setText("1. Mats Wash(rubber Only");
                external_text1.setText("1. Car exterior foam wash");
                external_text2.setText("2. Car exterior body black fiber    parts polish ");
                external_text3.setText("3. Tyre cleaning");
                external_text4.setText("4. Tyre Polish");
                external_text5.setText("5. Glass cleaning");
                washlogo.setImageResource(R.drawable.lightening);

            }
            break;
            case 1:
            {
                service_name_bottomsheet.setText("Premium Wash");
                internal_text1.setText("1. Mats Wash(rubber Only");
                internal_text2.setText("2. Vaccuming  (except Dickey)");
                internal_text3.setText("3. Total interior car wiping ");
                external_text1.setText("1. Car exterior foam wash");
                external_text2.setText("2. Car exterior body black fiber    parts polish ");
                external_text3.setText("3. Tyre cleaning");
                external_text4.setText("4. Tyre Polish");
                external_text5.setText("5. Glass cleaning");
                washlogo.setImageResource(R.drawable.balance);

            }
            break;
            case 2:
            {
                service_name_bottomsheet.setText("Platinum Wash");
                internal_text1.setText("1. Mats Wash(rubber Only");
                internal_text2.setText("2. Vaccuming  (except Dickey)");
                internal_text3.setText("3. Total interior car wiping ");
                internal_text4.setText("4. Dash board polish ");
                internal_text5.setText("5. Dickey vacuuming ");
                internal_text6.setText("6. Stepney cleaning & polish ");

                external_text1.setText("1. Car exterior foam wash");
                external_text2.setText("2. Car exterior body black fiber    parts polish ");
                external_text3.setText("3. Tyre cleaning and Tyre Polish");
                external_text4.setText("");
                external_text4.setText("4. Glass cleaning");
                external_text5.setText("5. Engine wash");
                external_text6.setText("6. Alloy Wheels Cleaning");
                external_text7.setText("7. Fuel cap cleaning");
                external_text8.setText("8. Wiper water filling");
                washlogo.setImageResource(R.drawable.star);


            }
            break;
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

                        /*PostOperation();
                        Intent usercar_intent = new Intent(getApplicationContext(),UserCar_CollapseHeader.class);
                        usercar_intent.putExtra("userCarMap", (Serializable) usercarDetailsMap);
                        startActivity(usercar_intent);*/


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
        }
    }
}
