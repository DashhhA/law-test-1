package com.lawtest.ui.admin.new_specialist;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.model.AgencyService;

import java.util.ArrayList;
import java.util.Map;

public class NewSpecialistViewModel extends ViewModel {
    private String fName;
    private String sName;
    private String surName;
    private String email;
    private MutableLiveData<ArrayList<AgencyService>> service;
    private DatabaseReference database;
    private FirebaseAuth auth;

    private ValueEventListener servicesEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<AgencyService> services = new ArrayList<>();
            for (DataSnapshot data: dataSnapshot.getChildren()) {
                AgencyService service = data.getValue(AgencyService.class);
                services.add(service);
            }
            service.postValue(services);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // TODO
        }
    };

    public NewSpecialistViewModel() {
        database = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        service = new MutableLiveData<>();
        database.child(AgencyService.DATABASE_ENTRY)
                .addValueEventListener(servicesEventListener);
    }

    public String getfName() {
        return fName;
    }

    public String getsName() {
        return sName;
    }

    public String getSurName() {
        return surName;
    }

    public String getEmail() {
        return email;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LiveData<ArrayList<AgencyService>> getService() {
        if (service ==  null) service = new MutableLiveData<>();
        return service;
    }

    public DatabaseReference getDatabase() {
        return database;
    }

    public FirebaseAuth getAuth() { return auth; }

}
