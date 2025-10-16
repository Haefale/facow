package com.cookandroid.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 2; // 총 페이지 수는 14개입니다.

    public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FirstPageFragment();
            case 1:
                return new SecondPageFragment();
            default:
                return new FirstPageFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}