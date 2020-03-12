package com.lawtest.ui.specialist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lawtest.MainActivity;
import com.lawtest.model.PersonRepository;
import com.lawtest.model.Specialist;

public class SpecialistViewModel extends ViewModel {
    private PersonRepository<Specialist> repository;

    public SpecialistViewModel(){
        repository = new PersonRepository<>(
                MainActivity.getInstance().getAuth(),
                MainActivity.getInstance().getDatabase(),
                MainActivity.getInstance().getStorage(),
                Specialist.class
        );
    }

    LiveData<Specialist> getPerson(String email, String password) {
        return repository.getPerson(email, password);
    }
}
