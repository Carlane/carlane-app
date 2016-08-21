package com.cherry.alok.myapplication.dummy;

import android.content.Context;
import android.media.Image;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cherry.alok.myapplication.R;
import com.cherry.alok.myapplication.SharedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alok on 18/8/16.
 */
public class ServiceRecyclerAdapter extends RecyclerView.Adapter<ServiceRecyclerAdapter.VersionViewHolder> {
    List<String> versionModels;

    public static List<String> user_service_list = new ArrayList<String>();
    public static List<String> user_service__captions_list= new ArrayList<String>();
    Context context;
    OnItemClickListener clickListener;
    List<HashMap<String,String>> usercarDetailsMap = new ArrayList<>();


    public void setHomeActivitiesList(Context context) {
        String[] listArray = context.getResources().getStringArray(R.array.user_service);
        String[] subTitleArray = context.getResources().getStringArray(R.array.user_service_captions);
        for (int i = 0; i < listArray.length; ++i) {
            user_service_list.add(listArray[i]);
            user_service__captions_list.add(subTitleArray[i]);
        }
    }

    public ServiceRecyclerAdapter(Context context) {
        this.context = context;
        setHomeActivitiesList(context);
    }


    public ServiceRecyclerAdapter(List<String> versionModels) {
        this.versionModels = versionModels;

    }

    public ServiceRecyclerAdapter(Context context , List<HashMap<String,String>> userCarMapData , boolean value) {
        this.versionModels = versionModels;
        usercarDetailsMap = userCarMapData;
        this.context = context;

    }

    public void SetCarMap( List<HashMap<String,String>> userCarMapData )
    {
        usercarDetailsMap = userCarMapData;
    }
    ArrayList<VersionViewHolder> viewVersionList = new ArrayList<VersionViewHolder>();

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view ;

        {
            view =  LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_service_type, viewGroup, false);
        }

        VersionViewHolder viewHolder = new VersionViewHolder(view);
        viewVersionList.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int i) {
        {
            if(usercarDetailsMap != null) {
                switch (i)
                {
                    case 0:
                    {
                        versionViewHolder.service_name.setText("Basic");
                        versionViewHolder.services_description_small.setText("A power wash for your car with focus mainly on the exteriors surface.");
                    }

                    break;
                    case 1:
                    {
                        versionViewHolder.service_name.setText("Premium");
                        versionViewHolder.services_description_small.setText("A balance of exterior and interior cleaning of your car.");
                    }
                    break;
                    case 2:
                    {
                        versionViewHolder.service_name.setText("Platinum");
                        versionViewHolder.services_description_small.setText("A thorough cleaning inside and out.");

                    }
                    break;

                }


            }
        }
    }



    @Override
    public int getItemCount() {
        return 3;
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView service_selected;
        TextView service_name;
        TextView services_description_small;

        TextView text_moredetails ;//= (TextView)findViewById(R.id.text_moredetails);



        public VersionViewHolder(View itemView) {
            super(itemView);
            service_name = (TextView) itemView.findViewById(R.id.service_name);
            service_selected = (ImageView) itemView.findViewById(R.id.services_select_icon);
            services_description_small = (TextView) itemView.findViewById(R.id.services_description_small);
            text_moredetails = (TextView)itemView.findViewById(R.id.text_moredetails);

            text_moredetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(v, getLayoutPosition()+999);

                }
            });
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {
            service_selected.setImageResource(R.drawable.checkgreen);
            SetDefaultOtherServiceSelectIcon();
            clickListener.onItemClick(v, getLayoutPosition());
        }

        public void SetDefaultOtherServiceSelectIcon()
        {
            for(int i=0 ;i< viewVersionList.size();i++)
            {
                if(viewVersionList.get(i).equals(this))
                {
                    continue;
                }
                viewVersionList.get(i).service_selected.setImageResource(R.drawable.check_light);

            }
        }


    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

}