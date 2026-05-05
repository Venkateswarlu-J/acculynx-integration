package com.acculynx.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class JobRequest {

    @JsonProperty("contact")
    private final Map<String, String> contact;          //requires object in create job

    @JsonProperty("locationAddress")
    private final LocationAddress locationAddress;

    public JobRequest(String contactId,
                      String street, String city,
                      String state,  String zipCode) {
        this.contact         = Map.of("id", contactId);
        this.locationAddress = new LocationAddress(street, city, state, zipCode);
    }

    public Map<String, String> getContact()             { return contact; }
    public LocationAddress     getLocationAddress()     { return locationAddress; }

    public static class LocationAddress {

        @JsonProperty("street")
        private final String street;

        @JsonProperty("city")
        private final String city;

        @JsonProperty("state")
        private final String state;

        @JsonProperty("zipCode")
        private final String zipCode;

        public LocationAddress(String street, String city, String state, String zipCode) {
            this.street  = street;
            this.city    = city;
            this.state   = state;
            this.zipCode = zipCode;
        }

        public String getStreet()  { return street; }
        public String getCity()    { return city; }
        public String getState()   { return state; }
        public String getZipCode() { return zipCode; }
    }
}