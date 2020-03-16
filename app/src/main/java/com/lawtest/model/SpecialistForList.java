package com.lawtest.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.lawtest.MainActivity;
import com.lawtest.util.MultiTaskCompleteWatcher;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SpecialistForList{
    public String fName;
    public String sName;
    public String surName;
    public String email;
    public ArrayList<String> services;
    private ArrayList<String> appointments;
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
        //initWithDataSnapshot(dataSnapshot);
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
        appointments = (ArrayList<String>) map.get(Appointment.PERSON_REF);
        if (appointments == null) appointments = new ArrayList<>();
        if (services == null) services = new ArrayList<>();
        avatarUri = (String) map.get("avatarUri");
        if (avatarUri == null) {
            data.postValue(SpecialistForList.this);
        } else {
            storage.child(Specialist.DATABASE_AVA_FOLDER)
                    .child(Uri.parse(avatarUri).getLastPathSegment())
                    .getBytes(utils.MAX_DOWNLOAD_BYTES)
                    .addOnCompleteListener(avatarListener);
        }
    }

    public String getUid() {
        return key;
    }

    public void addAppointment(final Appointment appointment, MultiTaskCompleteWatcher watcher, FragmentActivity activity) {

        final MultiTaskCompleteWatcher.Task databaseTask = watcher.newTask();
        final MultiTaskCompleteWatcher.Task userTask = watcher.newTask();
        final MultiTaskCompleteWatcher.Task specialistTask = watcher.newTask();

        DatabaseReference database = MainActivity.getInstance().getViewModel().getDatabase();
        database.child(Appointment.DATABASE_REF)
                .child(appointment.id)
                .setValue(appointment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) databaseTask.complete();
                        else databaseTask.fail(task.getException());
                    }
                });

        ArrayList<String> userAppointments = MainActivity.getInstance().getViewModel()
                .getUserOnce().appointments;
        userAppointments.add(appointment.id);
        database.child(User.DATABASE_TAG)
                .child(appointment.userId)
                .child(Appointment.PERSON_REF)
                .setValue(userAppointments)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) userTask.complete();
                        else userTask.fail(task.getException());
                    }
                });

        appointments.add(appointment.id);
        database.child(Specialist.DATABASE_TAG)
                .child(appointment.specialistId)
                .child(Appointment.PERSON_REF)
                .setValue(appointments)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) specialistTask.complete();
                        else specialistTask.fail(task.getException());
                    }
                });

    }
}
