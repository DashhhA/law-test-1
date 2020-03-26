package com.lawtest.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Map;

// базова модель данных, от которой наследуются User и Specialist.
// Нужна, чтобы один и тот же класс мог работать и с User и с Specialist
public abstract class BasePerson {
    public String avatarUri;
    public String email;
    public String fName;
    public String surName;
    public byte[] pass;
    public byte[] salt;
    public ArrayList<String> appointments;

    public abstract Uri getAvatarUri();
    public abstract void setAvatarUri(Uri avatarUri);
    public abstract Map toMap();
    public abstract void fromMap(Map<String, Object> map);
    public BasePerson(Map<String, Object> map) {}
    public BasePerson() {}
}
