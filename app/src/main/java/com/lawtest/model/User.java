package com.lawtest.model;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.lawtest.MainActivity;
import com.lawtest.util.crypto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class User {
    final static String TAG = "user";
    String fName;
    String sName;
    String surName;
    String email;
    crypto.PassSalt passSalt;
    private String imgUri;
    private String avatarUri;
    boolean isRemember = true;

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
        this.passSalt = passSalt;
        if (imgUri != null) this.imgUri = imgUri.toString();
        if (avatarUri != null) this.avatarUri = avatarUri.toString();
        this.isRemember = isRemember;
    }

    public Uri getImgUri() {
        return Uri.parse(imgUri);
    }

    public Uri getAvatarUri() {
        return Uri.parse(avatarUri);
    }

    public void setImgUri(Uri imgUri) {
        if (imgUri != null) this.imgUri = imgUri.toString(); else this.imgUri = null;
    }

    public void setAvatarUri(Uri avatarUri) {
        if (avatarUri != null) this.avatarUri = avatarUri.toString(); else this.avatarUri = null;
    }
}
