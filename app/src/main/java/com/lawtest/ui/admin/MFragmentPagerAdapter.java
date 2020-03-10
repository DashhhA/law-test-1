package com.lawtest.ui.admin;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.lawtest.R;
import com.lawtest.ui.admin.posts.PostsFragment;
import com.lawtest.ui.admin.specialists.SpecialistsListFragment;

public class MFragmentPagerAdapter extends FragmentPagerAdapter {
    private Fragment[] fragments = new Fragment[2];
    private String[] titles;

    public MFragmentPagerAdapter(FragmentManager manager, Context context) {
        super(manager);

        titles = new String[]{
                context.getString(R.string.admin_specialists_title),
                context.getString(R.string.admin_posts_title)
        };
    }

    @Override
    public Fragment getItem(int position) {
        if (fragments[position] == null) {
            switch (position){
                case 0:
                    fragments[position] = new SpecialistsListFragment();
                    break;
                case 1:
                    fragments[position] = new PostsFragment();
                    break;
            }
        }
        return fragments[position];
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
