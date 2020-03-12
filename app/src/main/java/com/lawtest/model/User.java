package com.lawtest.model;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.lawtest.util.crypto;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User extends BasePerson{
    public final static String TAG = "user";
    public final static String DATABASE_TAG = "users";
    final static String DATABASE_AVA_FOLDER = "ava_imgs";
    public String fName;
    public String sName;
    public String surName;
    private String imgUri;
    boolean isRemember = true;
    long V;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(
            String fName,
            @Nullable String sName,
            String surName,
            String email,
            crypto.PassSalt passSalt,
            @Nullable Uri imgUri,
            @Nullable Uri avatarUri,
            boolean isRemember
    ) {
        this.fName = fName;
        this.sName = sName;
        this.surName = surName;
        this.email = email;
        this.salt = passSalt.salt;
        this.pass = passSalt.pass;
        if (imgUri != null) this.imgUri = imgUri.toString();
        if (avatarUri != null) this.avatarUri = avatarUri.toString();
        this.isRemember = isRemember;
        V = 0;
    }

    public Uri getImgUri() {
        if (imgUri != null) return Uri.parse(imgUri);
        return null;
    }

    public Uri getAvatarUri() {
        if (avatarUri != null) return Uri.parse(avatarUri);
        return null;
    }

    public void setImgUri(Uri imgUri) {
        if (imgUri != null) this.imgUri = imgUri.toString(); else this.imgUri = null;
    }

    public void setAvatarUri(Uri avatarUri) {
        if (avatarUri != null) this.avatarUri = avatarUri.toString(); else this.avatarUri = null;
    }

    public Map toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("fName", fName);
        map.put("sName", sName);
        map.put("surName", surName);
        map.put("email", email);
        map.put("imgUri", imgUri);
        map.put("avatarUri", avatarUri);
        map.put("salt", utils.bytesToArray(salt));
        map.put("pass", utils.bytesToArray(pass));
        map.put("V", V);

        return map;
    }

    User(Map<String, Object> map){
        fromMap(map);
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        fName = (String) map.get("fName");
        sName = (String) map.get("sName");
        surName = (String) map.get("surName");
        email = (String) map.get("email");
        imgUri = (String) map.get("imgUri");
        avatarUri = (String) map.get("avatarUri");
        salt = utils.arrayToBytesL((ArrayList<Long>) map.get("salt"));
        pass = utils.arrayToBytesL((ArrayList<Long>) map.get("pass"));
        V = (long) map.get("V");
    }
}
