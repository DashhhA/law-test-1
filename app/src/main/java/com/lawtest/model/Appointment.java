package com.lawtest.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

public class Appointment {
    @Exclude
    public static final String DATABASE_REF = "appointments";
    @Exclude
    public static final String PERSON_REF = "appointments";
    @Exclude
    public static final String STATUS_SENT = "sent";
    @Exclude
    public static final String STATUS_ACCEPTED = "accepted";
    @Exclude
    public static final String STATUS_REJECTED = "rejected";

    public String id;
    public String userId;
    public String specialistId;
    public ArrayList<String> ServiceIds;
    public String status;
    public String userComment;
    public String specialistComment;
    public DateTime dateTime;

    public Appointment() {} // для firebase DataSnapshot.getValue(AgencyService.class)

    public static class DateTime {
        public int year;
        public int month;
        public int day;

        public int hour;
        public int minute;

        public DateTime() {}
    }
}
