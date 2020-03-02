package com.lawtest.ui.new_user;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

public class NewUserViewModel extends ViewModel {
    private String fName;
    private String sName;
    private String surName;
    private String email;
    private Uri imgUri;
    private Uri avatarUri;
    private boolean isRemember = true;

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

    public Uri getImgUri() {
        return imgUri;
    }

    public Uri getAvatarUri() {
        return avatarUri;
    }

    public boolean isRemember() {
        return isRemember;
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

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public void setAvatarUri(Uri avatarUri) {
        this.avatarUri = avatarUri;
    }

    public void setRemember(boolean remember) {
        isRemember = remember;
    }
}
