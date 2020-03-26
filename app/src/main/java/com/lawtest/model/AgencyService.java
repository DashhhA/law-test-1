package com.lawtest.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

// модель данных для услуги
public class AgencyService {
    public String id;
    public String name;
    public String description;
    @Exclude
    public static final String DATABASE_ENTRY = "services";

    public AgencyService() {} // для firebase DataSnapshot.getValue(AgencyService.class)

    public AgencyService(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.description = (String) map.get("description");
    }
}
