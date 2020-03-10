package com.lawtest.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpecialistForList{
    public String fName;
    public String sName;
    public String surName;
    public String email;
    private ArrayList<String> services;
    private String avatarUri;
    private MutableLiveData<SpecialistForList> data;
    private StorageReference storage;
    public String key;
    private OnCompleteListener<byte []> avatarListener = new OnCompleteListener<byte[]>() {
        @Override
        public void onComplete(@NonNull Task<byte[]> task) {
            if (task.isSuccessful()) {
                utils.saveBytesToFile(getAvatarUri(), task.getResult());
            }
            data.postValue(SpecialistForList.this);
        }
    };
    private ValueEventListener databaseListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            initWithDataSnapshot(dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // TODO
        }
    };

    public SpecialistForList(DataSnapshot dataSnapshot, StorageReference storage, String key) {
        data = new MutableLiveData<>();
        this.storage = storage;
        this.key = key;
        initWithDataSnapshot(dataSnapshot);
        dataSnapshot.getRef().addValueEventListener(databaseListener);
    }

    public Uri getAvatarUri() {
        if (avatarUri != null) return Uri.parse(avatarUri);
        return null;
    }

    public void setAvatarUri(Uri avatarUri) {
        if (avatarUri != null) this.avatarUri = avatarUri.toString(); else this.avatarUri = null;
    }

    public LiveData<SpecialistForList> getSpecialist() {
        return data;
    }

    private void initWithDataSnapshot(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<Map<String, Object>> typeIndicator =
                new GenericTypeIndicator<Map<String, Object>>() {};
        Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
        fName = (String) map.get("fName");
        sName = (String) map.get("sName");
        surName = (String) map.get("surName");
        email = (String) map.get("email");
        services = (ArrayList<String>) map.get("services");
        avatarUri = (String) map.get("avatarUri");
        if (avatarUri == null) {
            data.postValue(SpecialistForList.this);
        } else {
            storage.child(Specialist.DATABASE_AVA_FOLDER)
                    .getBytes(utils.MAX_DOWNLOAD_BYTES)
                    .addOnCompleteListener(avatarListener);
        }
    }
}
