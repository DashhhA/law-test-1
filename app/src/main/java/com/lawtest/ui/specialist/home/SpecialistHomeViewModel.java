package com.lawtest.ui.specialist.home;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lawtest.MainActivity;
import com.lawtest.MainViewModel;
import com.lawtest.model.PersonRepository;
import com.lawtest.model.Specialist;

// ViewModel, предоставляющая доступ к данным о текущем специалисте
public class SpecialistHomeViewModel extends ViewModel {
    private LiveData<Specialist> specialist;

    public SpecialistHomeViewModel() {
        specialist = MainActivity.getInstance().getViewModel().getSpecialist();
    }

    public LiveData<Specialist> getSpecialist() {
        return specialist;
    }
}
