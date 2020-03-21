package com.lawtest.ui.admin.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.lawtest.MainActivity;
import com.lawtest.model.AgencyService;

import java.util.ArrayList;

public class ServicesListViewModel extends ViewModel {
    private ArrayList<AgencyService> services;
    private MutableLiveData<ArrayList<AgencyService>> data;
    private AgencyService current;

    public ServicesListViewModel() {
        services = new ArrayList<>();
        data = new MutableLiveData<>();

        DatabaseReference database = MainActivity.getInstance().getViewModel().getDatabase();
        database.child(AgencyService.DATABASE_ENTRY)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        AgencyService service = dataSnapshot.getValue(AgencyService.class);
                        services.add(service);
                        data.postValue(services);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        removeServiceByKey(dataSnapshot.getKey());
                        AgencyService service = dataSnapshot.getValue(AgencyService.class);
                        services.add(service);
                        data.postValue(services);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        removeServiceByKey(dataSnapshot.getKey());
                        data.postValue(services);
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public LiveData<ArrayList<AgencyService>> getServices() {
        return data;
    }

    private void removeServiceByKey(String key) {
        ArrayList<AgencyService> toRemove = new ArrayList<>();
        for (AgencyService service: services) {
            if (service.id.equals(key)) {
                toRemove.add(service);
                break;
            }
        }
        services.removeAll(toRemove);
    }
}
