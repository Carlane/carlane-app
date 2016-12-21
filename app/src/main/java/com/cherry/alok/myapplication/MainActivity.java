package com.cherry.alok.myapplication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    SignInFragment signInFrag;
    ImageView imgProfilePic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CreateDataBase();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        HashMap<String , String> userdetailsFromDB = SharedData.FetchUser();
        if(userdetailsFromDB.size() <=0) {
            Intent introIntent = new Intent(this, PagerActivity.class);
            startActivity(introIntent);
            String service_version = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("service_version" , "-0.00001");

            if(Float.parseFloat(service_version )< 0)
            {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("service_version", "0.00000").commit();
            }

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        imgProfilePic = (ImageView)findViewById(R.id.imageView);

        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        //dummy
        //  SharedData.DeleteAllUser();
       // SharedData.DeleteAllUserCar();


        if(userdetailsFromDB.size() <=0)
        {
            /*Intent intent =new Intent(this,TransparentActivity.class);
            startActivity(intent);*/
            if (findViewById(R.id.fragment_container) != null)
                    {
                        if (savedInstanceState != null) {
                            return;
                        }

                        signInFrag = new SignInFragment();
                        signInFrag.SetMainUiHandler(mainhandler);
                        //signInFrag.setArguments(getIntent().getExtras());

                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, signInFrag).commit();


                    }
        }
        else
        {
            int userStatus = Integer.parseInt(userdetailsFromDB.get("status"));
            if(userStatus == SharedData.UserStatus.NewProfile.getID())
            {
                //go to add profile and car page
                SharedData.HandleNavigation(R.id.nav_add_car,this, true);
            }
            if(userStatus == SharedData.UserStatus.CarProfile.getID())
            {
                //go to location screen
                SharedData.HandleNavigation(R.id.nav_location,this , true);

            }
            if(userStatus == SharedData.UserStatus.RequestPending.getID())
            {
                SharedData.HandleNavigation(R.id.nav_order,this , true);
            }
            if(userStatus == SharedData.UserStatus.FeedbackPending.getID())
            {
                SharedData.HandleNavigation(R.id.nav_feedback,this,true);
            }

        }

        getEmiailID(getApplicationContext());


    }

    public void CreateDataBase()
        {
           SharedData.myDbHelper = new DataBaseHelper(this);
           SharedData.CreateDataBase();
        }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        SharedData.HandleNavigation(id,this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void HideOnGoingRequestIfRequired()
    {
        MenuItem item = (MenuItem)findViewById(R.id.nav_order);
        HashMap<String , String> userdetailsFromDB = SharedData.FetchUser();
        int userStatus = Integer.parseInt(userdetailsFromDB.get("status"));
        if(userStatus == SharedData.UserStatus.RequestPending.getID())
        {
            item.setVisible(false);
        }
        else
        {
            item.setVisible(true);
        }

    }

    private String getEmiailID(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);
        if (account == null) {
            Toast.makeText(getApplicationContext(), "NO ACCOUNT", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            Toast.makeText(getApplicationContext(), "account " + account.name, Toast.LENGTH_SHORT).show();
            return account.name;
        }
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }

    private final Handler mainhandler = new Handler() {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("picsatus");
            if(aResponse == null)
            {
                aResponse = msg.getData().getString("picsatusremove");
                if(aResponse != null)
                {
                    if(imgProfilePic == null)
                    {
                        imgProfilePic = (ImageView)findViewById(R.id.imageView);

                    }

                    imgProfilePic.setImageDrawable(null);
                }
                return;
            }

            if ((null != aResponse)) {
                try {
                    if(imgProfilePic == null)
                    {
                        imgProfilePic = (ImageView)findViewById(R.id.imageView);
                    }
                    //if(imgProfilePic != null)
                    {
                        LoadProfileImage lfi =  new LoadProfileImage(imgProfilePic);
                        if(lfi != null) {
                            lfi.execute(aResponse);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Profile Image Could Not be Loaded", Toast.LENGTH_SHORT).show();
                }

            }
            else
            {
                Toast.makeText(getApplicationContext(), "Cant Update User Image", Toast.LENGTH_SHORT).show();
            }

        }
    };
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            if(bmImage!= null)
            {
                this.bmImage = bmImage;
            }
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
            SharedData.SetUserpic(user_pic);
            return user_pic;
        }

        protected void onPostExecute(Bitmap result) {
            if(bmImage!= null)
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
            }
        }
    }
}
