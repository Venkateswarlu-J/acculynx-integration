package com.acculynx.webhook;

import com.acculynx.service.AccuLynxClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final GoogleSheetsService sheetsService;
    private final AccuLynxClient      acculynxClient;
    private final ObjectMapper        objectMapper = new ObjectMapper();

    public WebhookController(GoogleSheetsService sheetsService,
                             AccuLynxClient acculynxClient) {
        this.sheetsService  = sheetsService;
        this.acculynxClient = acculynxClient;
    }

    @PostMapping("/job-created")
    public ResponseEntity<String> handleJobCreated(@RequestBody String rawPayload) {
        System.out.println("\n[WEBHOOK] ══════════════════════════════════");
        System.out.println("[WEBHOOK] Received Job Created event");
//        System.out.println("[WEBHOOK] Raw payload: " + rawPayload);
        System.out.println("[WEBHOOK] ══════════════════════════════════");

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            WebhookJobData data = extractJobData(root);
            logJobData(data);
            sheetsService.appendRow(data);
            System.out.println("[WEBHOOK] ✔ Saved to Google Sheet.");
            return ResponseEntity.ok("Webhook received and processed.");

        } catch (Exception e) {
            System.err.println("[WEBHOOK] ✘ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("Webhook received (processing error logged).");
        }
    }

    private WebhookJobData extractJobData(JsonNode root) {
        WebhookJobData data = new WebhookJobData();

        // ── Envelope fields ──
        data.eventDateTime  = safe(root, "eventDateTime");
        data.eventId        = safe(root, "eventId");
        data.subscriptionId = safe(root, "subscriptionId");
        data.topicName      = safe(root, "topicName");

        // ── event.job ──
        JsonNode job = root.path("event").path("job");
        data.jobId   = safe(job, "id");
        data.jobName = safe(job, "jobName");

        // ── event.job.locationAddress ──
        JsonNode address = job.path("locationAddress");
        data.street  = safe(address, "street1");
        data.city    = safe(address, "city");
        data.state   = safe(address, "state");
        data.zipCode = safe(address, "zipCode");

        // ── event.job.contacts — find primary contact ──
        JsonNode contacts = job.path("contacts");
        if (contacts.isArray() && contacts.size() > 0) {
            JsonNode primaryContact = contacts.get(0).path("contact");
            for (JsonNode c : contacts) {
                if (c.path("isPrimary").asBoolean()) {
                    primaryContact = c.path("contact");
                    break;
                }
            }
            data.contactId        = safe(primaryContact, "id");
            data.contactFirstName = safe(primaryContact, "firstName");
            data.contactLastName  = safe(primaryContact, "lastName");
        }

        // ── Enrich with email + phone via sub-endpoints ──
        if (data.contactId != null && !data.contactId.equals("N/A")) {
            try {
                JsonNode contactDetails = acculynxClient.getContact(data.contactId);

                // Phone — call the _link from phoneNumbers[0]
                JsonNode phones = contactDetails.path("phoneNumbers");
                if (phones.isArray() && phones.size() > 0) {
                    String phoneLink = safe(phones.get(0), "_link");
                    JsonNode phoneDetails = acculynxClient.getByUrl(phoneLink);
                    data.contactPhone = safe(phoneDetails, "number");
                } else {
                    data.contactPhone = "N/A";
                }

                // Email — call the _link from emailAddresses[0]
                JsonNode emails = contactDetails.path("emailAddresses");
                if (emails.isArray() && emails.size() > 0) {
                    String emailLink = safe(emails.get(0), "_link");
                    JsonNode emailDetails = acculynxClient.getByUrl(emailLink);
                    data.contactEmail = safe(emailDetails, "address");
                } else {
                    data.contactEmail = "N/A";
                }

                System.out.println("[WEBHOOK] ✔ Contact details enriched via API.");
            } catch (Exception e) {
                System.err.println("[WEBHOOK] ⚠ Could not fetch contact details: "
                        + e.getMessage());
                data.contactEmail = "N/A";
                data.contactPhone = "N/A";
            }
        } else {
            data.contactEmail = "N/A";
            data.contactPhone = "N/A";
        }

        return data;
    }

    private void logJobData(WebhookJobData data) {
        System.out.println("[WEBHOOK] Event ID     : " + data.eventId);
        System.out.println("[WEBHOOK] Event Time   : " + data.eventDateTime);
        System.out.println("[WEBHOOK] Topic        : " + data.topicName);
        System.out.println("[WEBHOOK] Job ID       : " + data.jobId);
        System.out.println("[WEBHOOK] Job Name     : " + data.jobName);
        System.out.println("[WEBHOOK] City         : " + data.city);
        System.out.println("[WEBHOOK] Zip Code     : " + data.zipCode);
        System.out.println("[WEBHOOK] Contact      : " + data.contactFirstName
                + " " + data.contactLastName);
        System.out.println("[WEBHOOK] Contact ID   : " + data.contactId);
        System.out.println("[WEBHOOK] Email        : " + data.contactEmail);
        System.out.println("[WEBHOOK] Phone        : " + data.contactPhone);
    }

    private String safe(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return (n.isMissingNode() || n.isNull()) ? "N/A" : n.asText();
    }
}