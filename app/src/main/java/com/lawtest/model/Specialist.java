package com.lawtest.model;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Specialist extends BasePerson {
    public final static String DATABASE_TAG = "specialists";
    public final static String DATABASE_AVA_FOLDER = "ava_imgs_s";
    public String fName;
    public String sName;
    public String surName;
    public String email;
    private ArrayList<String> services;
    public byte[] salt;
    public byte[] pass;
    private String avatarUri;

    public Specialist(Map<String, Object> map) {
        super(map);
        fName = (String) map.get("fName");
        sName = (String) map.get("sName");
        surName = (String) map.get("surName");
        email = (String) map.get("email");
        services = (ArrayList<String>) map.get("services");
        avatarUri = (String) map.get("avatarUri");
        salt = utils.arrayToBytesL((ArrayList<Long>) map.get("salt"));
        pass = utils.arrayToBytesL((ArrayList<Long>) map.get("pass"));
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
        return map;
    }
}
