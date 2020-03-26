package com.lawtest.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

// модель данных для встречи
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

        @Exclude
        @Override
        public boolean equals(Object obj) {
            if ( obj.getClass().isAssignableFrom(DateTime.class) ) {
                return ((DateTime) obj).minute == this.minute &&
                        ((DateTime) obj).hour == this.hour &&
                        ((DateTime) obj).day == this.day &&
                        ((DateTime) obj).month == this.month &&
                        ((DateTime) obj).year == this.year;

            }
            return false;
        }
    }

    @Exclude
    public static ArrayList<DateTime> getAvailableOnDay(DateTime day) {
        ArrayList<DateTime> times = new ArrayList<>();
        for ( int hour = 8; hour < 18; hour++ ) {
            if ( hour != 13 ) {
                DateTime available = new DateTime();
                available.year = day.year;
                available.month = day.month;
                available.day = day.day;
                available.hour = hour;
                available.minute = 0;
                times.add(available);
            }
        }

        return times;
    }
}
