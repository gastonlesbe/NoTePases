package com.gaston.lesbegueris.notepases;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainTabsAdapter extends FragmentStateAdapter {
    private static final int TAB_COUNT = 2;

    public MainTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new Tab1Fragment();
        }
        return new Tab2Fragment();
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
