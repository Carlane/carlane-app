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

/**
 * Created by Suleiman on 14-04-2015.
 */
public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.VersionViewHolder> {
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

    public SimpleRecyclerAdapter(Context context) {
        isHomeList = true;
        this.context = context;
        setHomeActivitiesList(context);
    }


    public SimpleRecyclerAdapter(List<String> versionModels) {
        isHomeList = false;
        this.versionModels = versionModels;

    }

    public SimpleRecyclerAdapter(Context context , List<HashMap<String,String>> userCarMapData , boolean value) {
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
        if(isHomeList)
        {
            view =  LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_services_list, viewGroup, false);
        }
        else
        {
            view =  LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_item, viewGroup, false);
        }

        VersionViewHolder viewHolder = new VersionViewHolder(view);
        viewVersionList.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int i) {
        if (isHomeList) {
            versionViewHolder.brand.setText(user_service_list.get(i));
            versionViewHolder.model.setText(user_service__captions_list.get(i));
            //versionViewHolder.cardItemLayout.setCardBackgroundColor(R.color.red);


        }
        else
        {
                    if(usercarDetailsMap != null) {
                        HashMap<String, String> currentDetail = usercarDetailsMap.get(i);
                        versionViewHolder.brand.setText(currentDetail.get("brand"));
                        versionViewHolder.model.setText(currentDetail.get("model"));
                        versionViewHolder.regNo.setText(currentDetail.get("regno"));

                        if (versionViewHolder.brand.getText().equals("Maruti")) {
                            versionViewHolder.carImage.setImageResource(R.drawable.maruti_swift_desire);
                        } else if (versionViewHolder.brand.getText().equals("hyundai")) {
                            versionViewHolder.carImage.setImageResource(R.drawable.hyundai_cars);
                        }
                    }
            else
                    {//test-data
                            versionViewHolder.brand.setText("mauti"+i);
                            versionViewHolder.model.setText("alto");
                            versionViewHolder.regNo.setText("UP15ag2435");

                            if (versionViewHolder.brand.getText().equals("maruti")) {
                                versionViewHolder.carImage.setImageResource(R.drawable.maruti_swift_desire);
                            } else if (versionViewHolder.brand.getText().equals("hyundai")) {
                                versionViewHolder.carImage.setImageResource(R.drawable.hyundai_cars);
                            }
                    }


        }
    }

    @Override
    public int getItemCount() {
        if (isHomeList)
            return user_service_list == null ? 0 : user_service_list.size();
        else
            return usercarDetailsMap == null ? 1 : usercarDetailsMap.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItemLayout;
        CardView imgCardView;
        TextView brand;
        TextView model;
        TextView regNo;
        ImageView carImage;

        public VersionViewHolder(View itemView) {
            super(itemView);

            cardItemLayout = (CardView) itemView.findViewById(R.id.cardlist_item);

            brand = (TextView) itemView.findViewById(R.id.carbrand_label_clps);
            model = (TextView) itemView.findViewById(R.id.carmodel_label_clps);
            regNo = (TextView) itemView.findViewById(R.id.carreg_label_clps);
            if(!isHomeList) {
                carImage = (ImageView) itemView.findViewById(R.id.car_imageView_clps);
                carImage.setVisibility(View.VISIBLE);
            }
                if (isHomeList) {

                itemView.setOnClickListener(this);
            } else {
                    itemView.setOnClickListener(this);
            }

        }


        @Override
        public void onClick(View v) {
            if (isHomeList) {
                ImageView checkImage = (ImageView) v.findViewById(R.id.car_imageView_check);
                for (int i = 0; i < viewVersionList.size(); i++) {
                    if (i != getLayoutPosition()) {
                        ImageView otherImage = (ImageView) viewVersionList.get(i).itemView.findViewById(R.id.car_imageView_check);
                        otherImage.setImageDrawable(null);

                    }
                }
                checkImage.setImageResource(R.drawable.car_selected_check);
            }
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
