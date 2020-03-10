package com.lawtest.model;

import java.util.Map;

public class AgencyService {
    public String id;
    public String name;
    public String description;
    public static final String DATABASE_ENTRY = "services";

    public AgencyService() {} // для firebase DataSnapshot.getValue(AgencyService.class)

    public AgencyService(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.description = (String) map.get("description");
    }
}
