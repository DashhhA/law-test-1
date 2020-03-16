package com.lawtest.ui.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawtest.model.Specialist;
import com.lawtest.model.SpecialistForList;

import java.util.ArrayList;

public class BaseSpecialistsListViewModel extends ViewModel {
    private ArrayList<SpecialistForList> specialists;
    private StorageReference storage;
    private DatabaseReference database;
    private MutableLiveData<ArrayList<SpecialistForList>> data;
    private ChildEventListener databaseListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            SpecialistForList specialist =
                    new SpecialistForList(dataSnapshot, storage, dataSnapshot.getKey());
            specialists.add(specialist);
            data.postValue(specialists);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            data.postValue(specialists);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            removeSpecialistByKey(dataSnapshot.getKey());
            data.postValue(specialists);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            //TODO
        }
    };

    public BaseSpecialistsListViewModel() {
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        specialists = new ArrayList<>();
        data = new MutableLiveData<>();

        database.child(Specialist.DATABASE_TAG)
                .addChildEventListener(databaseListener);
    }

    private void removeSpecialistByKey(String key) {
        for (SpecialistForList specialist: specialists) {
            if (specialist.key.equals(key)) {
                specialists.remove(specialist);
                break;
            }
        }
    }

    public LiveData<ArrayList<SpecialistForList>> getSpecialists() {
        return data;
    }
}
