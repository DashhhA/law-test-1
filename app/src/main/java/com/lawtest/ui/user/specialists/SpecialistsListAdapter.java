package com.lawtest.ui.user.specialists;

import android.app.Activity;

import com.lawtest.model.SpecialistForList;
import com.lawtest.ui.base.BaseSpecialistListAdapter;

// ListAdapter для списка специалистов, наследуемый от базового
public class SpecialistsListAdapter extends BaseSpecialistListAdapter {
    SpecialistsListAdapter(Activity context) {
        super(context);
    }

    @Override
    public void onButtonClicked(SpecialistForList specialist) {

    }
}
