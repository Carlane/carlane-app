package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v7.graphics.Palette;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UserCar_CollapseHeader extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


        CollapsingToolbarLayout collapsingToolbar;
        RecyclerView recyclerView;
        int mutedColor = R.attr.colorPrimary;
        SimpleRecyclerAdapter simpleRecyclerAdapter;
        List<HashMap<String,String>> usercarDetailsMap = new ArrayList<>();
    UniversalAsyncTask uniTask = null;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_car_collapse_header);

            Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.usercar_collapse_drawer_layout);
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
                    HideOnGoingRequestIfRequired();
                }


            };
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            RegisterSignOut();

            NavigationView navigationView = (NavigationView) findViewById(R.id.user_car_nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
            collapsingToolbar.setTitle("Select Yours");

            ImageView header = (ImageView) findViewById(R.id.header);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.header2);

            if(header != null)
            {
                header.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.header2));
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

            recyclerView = (RecyclerView) findViewById(R.id.scrollableview);

            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);

           // usercarDetailsMap =  (List<HashMap<String,String>> )getIntent().getSerializableExtra("userCarMap");




        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.add_car);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SharedData.HandleNavigation(R.id.nav_add_car , UserCar_CollapseHeader.this );*/
                Snackbar.make(findViewById(R.id.usercar_collapse_drawer_layout), "Support for Multiple Car per account will be supported very soon ! Fingers Crossed ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Check if cars are available in DB , if not go to backend to fetch and display(also store in DB)
        if(SharedData.FetchUserCarDetailsFromDb())
        {
            try {
                HashMap<String,String> carInfo = new HashMap<String,String>();
                carInfo.put("model" , SharedData.GetDefaultCarModel());
                carInfo.put("brand" , SharedData.GetDefaultCarBrand());
                carInfo.put("regno" , SharedData.GetDefaultCarNo());
                usercarDetailsMap.add(carInfo);
                if (simpleRecyclerAdapter == null) {
                    simpleRecyclerAdapter = new SimpleRecyclerAdapter(this, usercarDetailsMap, false);
                }
            } catch (Exception e) {
                usercarDetailsMap.clear();
                SharedData.DeleteAllUserCar();//dont again fall for false DB information , clear the car info
                //in case of any error switch to backend calls
                InitCarDetailsInfoReq();

            }
        }
        else
        {
            //No user car info in DB , go to server
            InitCarDetailsInfoReq();
        }
        SetRecyclerViewAdapter();
    }

    public void RegisterSignOut()
    {
        //
        Button singoutBtn = (Button)findViewById(R.id.usercar_signout);
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

    public void InitCarDetailsInfoReq()
    {
        String url = "CarInfo/"+SharedData.GetUserId()+"/";
        uniTask = new UniversalAsyncTask(url,"GET","" ,handler);

        ArrayList<String> dummy = new ArrayList<String>();
        uniTask.execute(dummy);
        showProgressDialog();
    }

    public  void SetRecyclerViewAdapter()
    {
        if (simpleRecyclerAdapter != null) {
            simpleRecyclerAdapter.SetCarMap(usercarDetailsMap);
            recyclerView.setAdapter(simpleRecyclerAdapter);
        }
        else
        {
            if (simpleRecyclerAdapter == null) {
                simpleRecyclerAdapter = new SimpleRecyclerAdapter(this, usercarDetailsMap, false);
            }
        }
        simpleRecyclerAdapter.SetOnItemClickListener(new SimpleRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position) {
                    case 0:
                        //SharedData.SetService("basic");
                        break;
                    case 1:
                        //SharedData.SetService("premium");
                        break;
                    case 2:
                        //SharedData.SetService("platinum");
                        break;
                    case 3:
                        break;


                    default:
                        //Toast.makeText(getBaseContext(), "Undefined Click!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public void SetUserCars( List<HashMap<String,String>> userMap)
    {

        usercarDetailsMap = userMap;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Fetching Your Cars");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        SharedData.HandleNavigation(id,this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.usercar_collapse_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void HideOnGoingRequestIfRequired()
    {
        NavigationView nav = (NavigationView) findViewById(R.id.user_car_nav_view);
        Menu nav_Menu = nav.getMenu();
        HashMap<String , String> userdetailsFromDB = SharedData.FetchUser();
        int userStatus = Integer.parseInt(userdetailsFromDB.get("status"));
        if(userStatus == SharedData.UserStatus.RequestPending.getID())
        {
            nav_Menu.findItem(R.id.nav_order).setVisible(true);
        }
        else
        {
            nav_Menu.findItem(R.id.nav_order).setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private  Handler handler = new Handler() {

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

                        PostOperation();
                        if(SharedData.FetchUserCarDetailsFromDb() == false)
                        {
                            //update this information in Database also
                            SaveCarInfoInDb();

                        }
                        SetRecyclerViewAdapter();
                        hideProgressDialog();
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
            //user_cars[i] = data;
            //usercarList.add(data);
        }
    }

    public boolean SaveCarInfoInDb()
    {
        try {
            for(int i=0 ;i< usercarDetailsMap.size();i++)
            {
                String brand = usercarDetailsMap.get(i).get("brand");
                String model = usercarDetailsMap.get(i).get("model");
                String reg_no = usercarDetailsMap.get(i).get("regno");
                SharedData.InsertUserCar(model,brand,reg_no,"2000");
            }
        } catch (Exception e) {
            return false;
        }
        return true;

    }
}
