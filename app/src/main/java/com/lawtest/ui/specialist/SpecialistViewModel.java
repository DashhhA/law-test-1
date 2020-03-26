package com.lawtest.ui.specialist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lawtest.MainActivity;
import com.lawtest.model.PersonRepository;
import com.lawtest.model.Specialist;

// позволяет получить данные по текщему специалисту
public class SpecialistViewModel extends ViewModel {

    public SpecialistViewModel(){

    }

    LiveData<Specialist> getPerson() {
        return MainActivity.getInstance().getViewModel().getSpecialist();
    }
}
