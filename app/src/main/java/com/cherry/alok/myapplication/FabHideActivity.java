package com.cherry.alok.myapplication;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
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


public class FabHideActivity extends AppCompatActivity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // Animation animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);

        setContentView(R.layout.activity_fab_hide);

        toolbar = (Toolbar) findViewById(R.id.toolbar);



        //  FAB margin needed for animation
        fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);


        toolbarContainer = (LinearLayout) findViewById(R.id.fabhide_toolbar_container);
        recyclerView = (RecyclerView) findViewById
                (R.id.recyclerview);

        /* Set top padding= toolbar height.
         So there is no overlap when the toolbar hides.
         Avoid using 0 for the other parameters as it resets padding set via XML!*/
        recyclerView.setPadding(recyclerView.getPaddingLeft(), toolbarHeight,
                recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Adding list data thrice for a more comfortable scroll.
        InitServiceHistoryInfoReq();


        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                toolbarContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                if (fadeToolbar)
                    toolbarContainer.animate().alpha(1).setInterpolator(new DecelerateInterpolator(1)).start();
                fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
                toolbarContainer.animate().translationY(-toolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                if (fadeToolbar)
                    toolbarContainer.animate().alpha(0).setInterpolator(new AccelerateInterpolator(1)).start();
                fab.animate().translationY(fab.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });

        fab = (FrameLayout) findViewById(R.id.myfab_main);
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
        });

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
