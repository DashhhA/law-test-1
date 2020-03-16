package com.lawtest.model;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Specialist extends BasePerson {
    public final static String TAG = "specialist";
    public final static String DATABASE_TAG = "specialists";
    public final static String DATABASE_AVA_FOLDER = "ava_imgs_s";
    public String fName;
    public String sName;
    public String surName;
    private ArrayList<String> services;
    private ArrayList<String> appointments;

    Specialist() {} //default constructor

    public Specialist(Map<String, Object> map) {
        super(map);
        fromMap(map);
    }

    public Specialist(
            String fName,
            @Nullable String sName,
            String surName,
            String email,
            ArrayList<String> services,
            crypto.PassSalt passSalt,
            @Nullable Uri avatarUri
    ) {
        this.fName = fName;
        this.sName = sName;
        this.surName = surName;
        this.email = email;
        this.services = services;
        this.salt = passSalt.salt;
        this.pass = passSalt.pass;
        if (avatarUri != null) this.avatarUri = avatarUri.toString();
        appointments = new ArrayList<>();
    }

    @Override
    public Uri getAvatarUri() {
        if (avatarUri != null) return Uri.parse(avatarUri);
        return null;
    }

    @Override
    public void setAvatarUri(Uri avatarUri) {
        if (avatarUri != null) this.avatarUri = avatarUri.toString(); else this.avatarUri = null;
    }

    @Override
    public Map toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fName", fName);
        map.put("sName", sName);
        map.put("surName", surName);
        map.put("email", email);
        map.put("services", services);
        map.put("avatarUri", avatarUri);
        map.put("salt", utils.bytesToArray(salt));
        map.put("pass", utils.bytesToArray(pass));
        map.put(Appointment.PERSON_REF, appointments);
        return map;
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        fName = (String) map.get("fName");
        sName = (String) map.get("sName");
        surName = (String) map.get("surName");
        email = (String) map.get("email");
        services = (ArrayList<String>) map.get("services");
        avatarUri = (String) map.get("avatarUri");
        salt = utils.arrayToBytesL((ArrayList<Long>) map.get("salt"));
        pass = utils.arrayToBytesL((ArrayList<Long>) map.get("pass"));
        appointments = (ArrayList<String>) map.get(Appointment.PERSON_REF);
        if (appointments == null) appointments = new ArrayList<>();
        if (services == null) services = new ArrayList<>();
    }
}
