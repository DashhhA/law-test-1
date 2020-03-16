package com.lawtest.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.lawtest.R;
import com.lawtest.ui.admin.new_service.NewServiceActivity;
import com.lawtest.ui.admin.new_specialist.NewSpecialistActivity;

public class AdminActivity extends AppCompatActivity {

    private FloatingActionButton fabSpec;
    private FloatingActionButton fabService;

    public void hideSpec(){
        if ( fabSpec.isClickable() ) {
            fabSpec.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_hide));
            fabSpec.setClickable(false);
        }
    }

    public void showSpec(){
        if ( !fabSpec.isClickable() ) {
            fabSpec.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_show));
            fabSpec.setClickable(true);
        }
    }

    public void hideService(){
        if ( fabService.isClickable() ) {
            fabService.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_hide));
            fabService.setClickable(false);
        }
    }

    public void showService(){
        if ( fabService.getVisibility() == View.GONE ) fabService.setVisibility(View.VISIBLE);
        if ( !fabService.isClickable() ) {
            fabService.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_show));
            fabService.setClickable(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // настройка ViewPager
        MFragmentPagerAdapter fragmentPagerAdapter =
                new MFragmentPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(1);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);

        fabSpec = findViewById(R.id.adminFAB);
        fabSpec.setClickable(true);
        fabSpec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewSpecialistActivity.class);
                startActivity(intent);
            }
        });

        fabService = findViewById(R.id.adminFABService);
        fabService.setClickable(false);
        fabService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, NewServiceActivity.class);
                startActivity(intent);
            }
        });
    }
}
