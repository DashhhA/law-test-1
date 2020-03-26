package com.lawtest.ui.admin.specialists;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.lawtest.model.SpecialistForList;
import com.lawtest.ui.base.BaseSpecialistListAdapter;

// адаптер для списка специалистов, просто наследует базовый
public class SpecialistsListAdapter extends BaseSpecialistListAdapter {

    SpecialistsListAdapter(Activity context) {
        super(context);
    }

    @Override
    public void onButtonClicked(SpecialistForList specialist) {

    }
}
