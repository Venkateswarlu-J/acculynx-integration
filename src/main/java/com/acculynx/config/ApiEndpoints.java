package com.acculynx.config;


// Base URL is read from application.properties.
public class ApiEndpoints {

    // POST — Create a new Contact
    public static final String CONTACTS = "/contacts";

    public static final String JOBS = "/jobs";

    // Private constructor — this is a constants class, not instantiable
    private ApiEndpoints() {}
}