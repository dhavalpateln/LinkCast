package com.dhavalpateln.linkcast.myanimelist.ui.main;

import android.content.Context;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MyAnimelistSectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    public MyAnimelistSectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0: return AnimeInfoFragment.newInstance();
            case 1: return AnimeSynopsisFragment.newInstance();
        }
        return AnimeInfoFragment.newInstance();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "Info";
            case 1: return "Synopsis";
        }
        return "Info";
        //return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }
}