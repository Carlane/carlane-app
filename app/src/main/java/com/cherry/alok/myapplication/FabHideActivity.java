package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class FabHideActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Toolbar toolbar;
    RecyclerView recyclerView;
    int fabMargin;
    LinearLayout toolbarContainer;
    int toolbarHeight;
    FrameLayout fab;
    ImageButton fabBtn;
    View fabShadow;
    boolean fadeToolbar = false;
    ServiceHistoryRecyclerAdapter simpleRecyclerAdapter;
    UniversalAsyncTask uniTask = null;
    List<HashMap<String,String>> userServiceHistoryMap = new ArrayList<>();
    AppBarLayout app_bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // Animation animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);

        setContentView(R.layout.activity_past_orders);

        toolbar = (Toolbar) findViewById(R.id.past_order_toolbar);
        app_bar = (AppBarLayout)findViewById(R.id.services_appbar);



        //  FAB margin needed for animation
        fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.pastorder_collapse_drawer_layout);
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
        RegisterSignOut();
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.past_order_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

      //  toolbarContainer = (LinearLayout) findViewById(R.id.fabhide_toolbar_container);
        recyclerView = (RecyclerView) findViewById(R.id.pastorder_recyclerview);
        toolbarHeight = (int)getApplicationContext().getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
        recyclerView.setPadding(recyclerView.getPaddingLeft(), toolbarHeight,
                recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());
        /* Set top padding= toolbar height.
         So there is no overlap when the toolbar hides.
         Avoid using 0 for the other parameters as it resets padding set via XML!*/
        //recyclerView.setPadding(recyclerView.getPaddingLeft(), toolbarHeight,
         //       recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Adding list data thrice for a more comfortable scroll.
        InitServiceHistoryInfoReq();


        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                toolbarHeight = (int)getApplicationContext().getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
               /* app_bar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                if (fadeToolbar)
                    toolbar.animate().alpha(1).setInterpolator(new DecelerateInterpolator(1)).start();
                fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();*/
                app_bar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                //app_bar.animate().alpha(1).setInterpolator(new DecelerateInterpolator(1)).start();
            }

            @Override
            public void hide() {
                toolbarHeight = (int)getApplicationContext().getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
               /* app_bar.animate().translationY(-toolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                if (fadeToolbar)
                    toolbar.animate().alpha(0).setInterpolator(new AccelerateInterpolator(1)).start();
                fab.animate().translationY(fab.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2)).start();*/
                app_bar.animate().translationY(-toolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                //app_bar.animate().alpha(0).setInterpolator(new AccelerateInterpolator(1)).start();
            }
        });

       /* fab = (FrameLayout) findViewById(R.id.myfab_main);
        fabBtn = (ImageButton) findViewById(R.id.myfab_main_btn);
        fabShadow = findViewById(R.id.myfab_shadow);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fabShadow.setVisibility(View.GONE);
            fabBtn.setBackground(getDrawable(R.drawable.ic_action_add));
        }

       // fab.startAnimation(animation);

        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CLICK", "FAB CLICK");
                Toast.makeText(getBaseContext(), "FAB Clicked", Toast.LENGTH_SHORT).show();

            }
        });*/

    }
    public void HideOnGoingRequestIfRequired() {
        NavigationView nav = (NavigationView) findViewById(R.id.past_order_nav_view);
        Menu nav_Menu = nav.getMenu();
        HashMap<String, String> userdetailsFromDB = SharedData.FetchUser();
        int userStatus = Integer.parseInt(userdetailsFromDB.get("status"));
        if (userStatus == SharedData.UserStatus.RequestPending.getID()) {
            nav_Menu.findItem(R.id.nav_order).setVisible(true);
        } else {
            nav_Menu.findItem(R.id.nav_order).setVisible(false);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        SharedData.HandleNavigation(id,this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.pastorder_collapse_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void InitServiceHistoryInfoReq()
    {
        String url = "userrequests/"+SharedData.GetUserId()+"/";
        uniTask = new UniversalAsyncTask(url,"GET","" ,handler);

        ArrayList<String> dummy = new ArrayList<String>();
        uniTask.execute(dummy);
        showProgressDialog();
    }

    private ProgressDialog mProgressDialog;
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Retreiving Order History");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }


    private Handler handler = new Handler() {

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

                        //PostOperation();
                        PostOperation();
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
            userServiceHistoryMap.add(responseMap);
            //user_cars[i] = data;
            //usercarList.add(data);
        }
    }

    public  void SetRecyclerViewAdapter()
    {
        if (simpleRecyclerAdapter != null) {
            simpleRecyclerAdapter.SetCarMap(userServiceHistoryMap);
            recyclerView.setAdapter(simpleRecyclerAdapter);
        }
        else
        {
            if (simpleRecyclerAdapter == null) {
                simpleRecyclerAdapter = new ServiceHistoryRecyclerAdapter(this, userServiceHistoryMap, false);
                recyclerView.setAdapter(simpleRecyclerAdapter);
            }
        }
        simpleRecyclerAdapter.SetOnItemClickListener(new ServiceHistoryRecyclerAdapter.OnItemClickListener() {
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

}
