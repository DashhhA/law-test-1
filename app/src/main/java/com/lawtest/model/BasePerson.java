package com.lawtest.model;

import android.net.Uri;

import java.util.Map;

public abstract class BasePerson {
    private String avatarUri;

    public abstract Uri getAvatarUri();
    public abstract void setAvatarUri(Uri avatarUri);
    public abstract Map toMap();
    public BasePerson(Map<String, Object> map) {}
    public BasePerson() {}
}
