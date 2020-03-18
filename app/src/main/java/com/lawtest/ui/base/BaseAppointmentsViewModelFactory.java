package com.lawtest.ui.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lawtest.model.BasePerson;
import com.lawtest.model.User;

public class BaseAppointmentsViewModelFactory implements ViewModelProvider.Factory {
    private LiveData<BasePerson> person;
    private Class tClass;

    public <T extends BasePerson> BaseAppointmentsViewModelFactory(LiveData<T> person, Class tClass) {
        this.person = (LiveData<BasePerson>) person;
        this.tClass = tClass;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new BaseAppointmentsViewModel(person, tClass);
    }
}
