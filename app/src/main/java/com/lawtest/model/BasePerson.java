package com.lawtest.model;

import android.net.Uri;

import java.util.Map;

public abstract class BasePerson {
    public String avatarUri;
    public String email;
    public byte[] pass;
    public byte[] salt;

    public abstract Uri getAvatarUri();
    public abstract void setAvatarUri(Uri avatarUri);
    public abstract Map toMap();
    public abstract void fromMap(Map<String, Object> map);
    public BasePerson(Map<String, Object> map) {}
    public BasePerson() {}
}
