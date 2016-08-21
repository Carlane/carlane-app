package com.cherry.alok.myapplication;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceHistoryRecyclerAdapter extends RecyclerView.Adapter<ServiceHistoryRecyclerAdapter.VersionViewHolder> {
    List<String> versionModels;
    Boolean isHomeList = false;

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

    public ServiceHistoryRecyclerAdapter(Context context) {
        isHomeList = true;
        this.context = context;
        setHomeActivitiesList(context);
    }


    public ServiceHistoryRecyclerAdapter(List<String> versionModels) {
        isHomeList = false;
        this.versionModels = versionModels;

    }

    public ServiceHistoryRecyclerAdapter(Context context , List<HashMap<String,String>> userCarMapData , boolean value) {
        isHomeList = value;
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
            view =  LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_request_history, viewGroup, false);
        }

        VersionViewHolder viewHolder = new VersionViewHolder(view);
        viewVersionList.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int i) {
        {
            if(usercarDetailsMap != null) {
                HashMap<String, String> currentDetail = usercarDetailsMap.get(i);
                int slot =  Integer.parseInt(currentDetail.get("time_slot_id").toString());
                int status = Integer.parseInt(currentDetail.get("current_status_id"));
                String carregno= currentDetail.get("regno");
                String a=  carregno.substring(0,6);
                String b = carregno.substring(6,carregno.length());
                versionViewHolder.service_type.setText(currentDetail.get("servicetype").toUpperCase() + " Wash");
                versionViewHolder.car_details.setText(currentDetail.get("usercarbrand") + " " + currentDetail.get("usercarmodel"));
                versionViewHolder.car_regno.setText("  " + a.replaceAll(".{2}", "$0 ") + " " + b);

                versionViewHolder.service_heading.setText(currentDetail.get("date") + " - " +SharedData.GetSlotName(slot));
                versionViewHolder.service_subdetails.setText(currentDetail.get("drivername"));
                //service_statusdetails
                versionViewHolder.service_statusdetails.setText(SharedData.GetRequestStatus(status));

            }
        }
    }



    @Override
    public int getItemCount() {
        return usercarDetailsMap == null ? 1 : usercarDetailsMap.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView car_details;
        TextView service_type;
        TextView car_regno;
        TextView service_heading;
        TextView service_subdetails;
        TextView service_statusdetails;


        public VersionViewHolder(View itemView) {
            super(itemView);
            service_type = (TextView) itemView.findViewById(R.id.service_servicetype);
            car_details = (TextView) itemView.findViewById(R.id.service_cardetails);
            car_regno = (TextView) itemView.findViewById(R.id.service_car_reg);
            service_heading = (TextView) itemView.findViewById(R.id.service_heading);
            service_subdetails = (TextView) itemView.findViewById(R.id.service_subdetails);
            service_statusdetails = (TextView) itemView.findViewById(R.id.service_statusdetails);
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {

            clickListener.onItemClick(v, getLayoutPosition());
        }


    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

}
