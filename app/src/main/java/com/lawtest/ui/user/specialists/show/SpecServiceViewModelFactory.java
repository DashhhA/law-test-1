package com.lawtest.ui.user.specialists.show;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lawtest.model.SpecialistForList;

// ViewModelFactory для передачи аргументов в SpecServicesViewModel
public class SpecServiceViewModelFactory implements ViewModelProvider.Factory {
    private SpecialistForList specialist;

    public SpecServiceViewModelFactory(SpecialistForList specialist) {
        this.specialist = specialist;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SpecServicesViewModel(specialist);
    }
}
