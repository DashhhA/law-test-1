package com.lawtest.ui.user.new_appointment;

import android.app.Activity;

import com.lawtest.model.SpecialistForList;
import com.lawtest.ui.base.BaseSpecialistListAdapter;

public class NewAppointmentSpecListAdapter extends BaseSpecialistListAdapter {

    public NewAppointmentSpecListAdapter(Activity context) {
        super(context);
    }

    @Override
    public void onButtonClicked(SpecialistForList specialist) {

    }

    public SpecialistForList getById(long id) {
        for (int i=0; i< getCount(); i++) {
            if (getItemId(i) == id) {
                return getItem(i);
            }
        }
        return null;
    }
}
