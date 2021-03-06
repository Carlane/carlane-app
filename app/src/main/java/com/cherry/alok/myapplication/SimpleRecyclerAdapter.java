package com.cherry.alok.myapplication;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
        }
        else
        {
                    if(usercarDetailsMap != null) {
                        HashMap<String, String> currentDetail = usercarDetailsMap.get(i);
                        versionViewHolder.brand.setText(currentDetail.get("brand") +" " + currentDetail.get("model"));
                        versionViewHolder.model.setText(currentDetail.get("model"));
                        versionViewHolder.regNo.setText(currentDetail.get("regno"));
                        String model = versionViewHolder.model.getText().toString();
                        SetImage(versionViewHolder, model);
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
    private boolean loadImageFromStorage(String model ,  ImageView imgView) {

        try {
            ContextWrapper cw = new ContextWrapper(context);
            File path1 = cw.getDir("carimages", Context.MODE_PRIVATE);
            File f = new File(path1, model+".png");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            imgView.setImageBitmap(b);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void SetImage(VersionViewHolder versionViewHolder , String model)
        {
            if(loadImageFromStorage(model , versionViewHolder.carImage ) == false)
            {
                LoadProfileImage lfi =  new LoadProfileImage(versionViewHolder.carImage , model);
                if(lfi != null) {
                    lfi.execute("https://s3.ap-south-1.amazonaws.com/carlane-misc/icon_premium.png");
                }

            }
            return;
            /*switch(model)
            {
                case "A-Star":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.astar);
                }
                break;
                case "Alto":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.alto);
                }
                break;
                case "Alto K10":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.alto);
                }
                break;
                case "Baleno":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.baleno);
                }
                break;
                case "Celerio":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.celerio);
                }
                break;
                case "Estilo":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.estilo);
            }
            break;
                case "Ertiga":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.ertiga);
                }
                break;
                case "Gypsy":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.gypsy);
                }
                break;
                case "Maruti Suzuki 800":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.maruti800);
                }
                break;
                case "Maruti Suzuki Omni":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.omni);
                }
                break;
                case "Swift":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.swift);
                }
                break;
                case "Swift Dezire":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.dzire);
                }
                break;
                case "SX4":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.sx4);
                }
                break;
                case "Wagon R":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.wagonr);
                }
                break;
                case "Ritz":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.ritz);
                }
                break;
                case "Creta":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_creta);
                }
                break;
                case "Eon":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_eon);
                }
                break;
                case "Elantra":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_elantra);
                }
                break;
                case "i20":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_i20);
                }
                break;
                case "i20 Active":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_i20_active);
                }
                break;
                case "i20 Elite":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_i20_elite);
                }
                break;
                case "Sante Fe":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_santefe);
                }
                break;
                case "Verna":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_verna);
                }
                break;
                case "Verna 4S Fluidic":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_verna_4sfluidic);
                }
                break;
                case "Xcent":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hyundai_xcent);
                }
                break;
                case "Aveo UVA":
                {
                  *//*  LoadProfileImage lfi =  new LoadProfileImage(versionViewHolder.carImage);
                    if(lfi != null) {
                        String aResponse = "https://doc-00-8c-docs.googleusercontent.com/docs/securesc/0jkq71fpli9a695n1bef14hh97r2t073/4hbnqhuvuesqv7knoenomj4sp6q7t3hs/1471255200000/11634506716492073008/11634506716492073008/0B-r8xPUnEGoEeF9OTkp2NDBpUzQ?e=download&nonce=vu5u80g55soa0&user=11634506716492073008&hash=94coasc651tailnpheohfrohi7hmam24";
                        lfi.execute(aResponse);
                    }*//*
                    versionViewHolder.carImage.setImageResource(R.drawable.chevy_aveo);
                }
                break;
                case "Beat":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.chevy_beat);
            }
            break;case "Captiva":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.chevy_captiva);
            }
            break;case "Cruze":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.chevy_cruze);
            }
            break;case "Enjoy":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.chevy_enjoy);
            }
            break;case "Sail":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.chevy_sail);
            }
            break;case "Sail Hatchback":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.chevy_sail_hatchback);
            }
            break;
            case "Spark":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.chevy_spark);
            }
            break;
            //////
                case "Avventura":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.fiat_avventura);
                }
                break;
                case "Punto":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.fiat_punto);
                }
                break;
                case "Punto Abarth":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.fiat_abarth_);
                }
                break;
                case "Punto Evo":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.fiatevo);
                }
                break;
                case "Linea":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.fiat_linea);
                }
                break;
                case "Linea Classic":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.fiat_linea_classic);
            }
            break;
                case "Classic":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.ford_classic);
                }
                break;
                case "Ecosport":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.ford_ecosport);
                }
                break;
                case "Endeavour":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.ford_endevour);
                }
                break;
                case "Fiesta":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.ford_fiesta);
                }
                break;
                case "Figo":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.ford_figo);
                }
                break;
                case "Accord":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.hondaccord);
                }
                break;
                case "Amaze":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.honda_amaze);
                }
                break;
                case "Brio":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.honda_brio);
                }
                break; case "City":
            {
                versionViewHolder.carImage.setImageResource(R.drawable.honda_city);
            }
            break;
                case "CR-V":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.honda_crv);
                }
                break;
                case "Bolero":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_bolero);
                }
                break;
                case "E20":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_e20);
                }
                break;
                case "KUV / TUV":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_tuv);
                }
                break;
                case "Quantro":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_quantro);
                }
                break;
                //////////////////
                case "Scorpio":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_scorpio);
                }
                break;
                case "Scorpio Getaway":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_scorpio_getaway);
                }
                break;
                case "Thar":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_thar);
                }
                break;
                case "Verito":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.verito);
                }
                break;
                case "Verito Vibe CS":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_vibe_cs);
                }
                break;
                case "XUV 500":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_xuv);
                }
                break;
                case "Xylo":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.mahindra_xylo);
                }
                break;
                case "Datsun":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.nissan_datsun);
                }
                break;
                case "Aria":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_aria);
                }
                break;
                case "Bolt":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_bolt);
                }
                break;
                case "Indica":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_indica);
                }
                break;
                case "Indica V2":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_indica);
                }
                break;
                case "Indica eV2":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_indica_ev2);
                }
                break;
                case "Nano":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_nano);
                }
                break;
                case "Sumo":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_sumo);
                }
                break;
                case "Safari":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_safari);
                }
                break;
                case "Safari Storme":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tatastorme);
                }
                break;
                case "Tiago":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_tiago);
                }
                break;
                case "Zest":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.tata_zest2);
                }
                break;
                case "Camry":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.toyota_camry);
                }
                break;
                case "Corolla Altis":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.toyota_corolla_altis);
                }
                break;
                case "Etios":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.toyota_etios);
                }
                break;
                case "Etios Liva":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.toyota_etios_liva);
                }
                break;
                case "Fortuner":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.toyota_fortuner);
                }
                break;
                case "Innova":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.toyota_innova);
                }
                break;
                case "Laura":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.skoda_laura);
                }
                break;
                case "Octavia":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.skoda_octavia);
                }
                break;
                case "Rapid":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.skoda_rapid);
                }
                break;
                case "Superb":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.skoda_superb);
                }
                break;
                case "Ameo":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.vw_ameo);
                }
                break;
                case "Beetle":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.vw_beetle);
                }
                break;
                case "Jetta":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.vw_jetta);
                }
                break;
                case "Polo":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.vw_polo);
                }
                break;
                case "Polo Cross":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.vw_polo_cross);
                }
                break;
                case "Vento":
                {
                    versionViewHolder.carImage.setImageResource(R.drawable.vw_vento);
                }
                break;

            }*/
        }

    private final Handler mainhandler = new Handler() {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void handleMessage(Message msg) {

            String aResponse = msg.getData().getString("picsatus");
            if(aResponse == null)
            {

                return;
            }

            if ((null != aResponse)) {
                try {

                    //if(imgProfilePic != null)
                    {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            else
            {
            }

        }
    };
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Bitmap bitmap;
        Handler uiHandler = null;
        String carModel;

        public LoadProfileImage(ImageView bmImage , String model) {
            if(bmImage!= null)
            {
                this.bmImage = bmImage;
            }
            if(model != null)
            {
                carModel = model;
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
            SavePhoto(user_pic , carModel);
            return user_pic;
        }

        public void SavePhoto(Bitmap resizedbitmap , String model)
        {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("carimages", Context.MODE_PRIVATE);
            if (!directory.exists()) {
                directory.mkdir();
            }
            File mypath = new File(directory, carModel + ".png");

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
            if(bmImage!= null)
            {
               // RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),result);
               // drawable.setCircular(true);
                bitmap = result;

                //bmImage.setImageBitmap(result);
                loadImageFromStorage(carModel , bmImage);
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

            //cardItemLayout = (CardView) itemView.findViewById(R.id.cardlist_item);

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
                //checkImage.setImageResource(R.drawable.car_selected_check);
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
