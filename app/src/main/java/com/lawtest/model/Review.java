package com.lawtest.model;

import com.google.firebase.database.Exclude;

public class Review {
    public String userId;
    public String specialistId;
    public String body;

    @Exclude
    public final static String DATABASE_REF = "reviews";
    @Exclude
    public static final String PERSON_REF = "reviews";
}
