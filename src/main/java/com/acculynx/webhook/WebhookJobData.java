package com.acculynx.webhook;

public class WebhookJobData {
    // Webhook envelope fields
    public String eventDateTime;
    public String eventId;
    public String subscriptionId;
    public String topicName;

    // Job fields (from event object)
    public String jobId;
    public String jobName;
    public String createdOn;

    // Address fields
    public String street;
    public String city;
    public String state;
    public String zipCode;

    // Contact fields
    public String contactId;
    public String contactFirstName;
    public String contactLastName;
    public String contactEmail;
    public String contactPhone;
}