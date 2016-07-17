package com.cherry.alok.myapplication;

/**
 * Created by alok on 22/6/16.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;

    }

    @Override
    public Fragment getItem(int position) {


                SelectSlot_TabFragment tab3 = new SelectSlot_TabFragment();
                return tab3;

    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
