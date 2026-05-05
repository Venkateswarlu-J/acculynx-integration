package com.acculynx.webhook;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {

    private static final String SHEET_ID = "1ewXeD7NESJfqt6Z4q00Jap8vO9WT-FitkmIUF9hWOZU";
    private static final String RANGE    = "Sheet1!A:M";   // 13 columns A–M
    private static final String APP_NAME = "AccuLynx Integration";

    private final Sheets sheetsClient;

    public GoogleSheetsService() throws Exception {
        InputStream credStream = getClass()
                .getClassLoader()
                .getResourceAsStream("google-credentials.json");

        if (credStream == null) {
            throw new RuntimeException(
                    "google-credentials.json not found in src/main/resources/");
        }

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        sheetsClient = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APP_NAME)
                .build();

        ensureHeaderRow();
    }

    public void appendRow(WebhookJobData data) throws Exception {
        List<Object> row = Arrays.asList(
                data.eventDateTime,
                data.eventId,
                data.subscriptionId,
                data.topicName,
                data.jobId,
                data.jobName,
                data.city,
                data.zipCode,
                data.contactId,
                data.contactFirstName,
                data.contactLastName,
                data.contactEmail,
                data.contactPhone
        );

        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(row));

        sheetsClient.spreadsheets().values()
                .append(SHEET_ID, RANGE, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("[SHEETS] ✔ Row appended successfully.");
    }

    private void ensureHeaderRow() throws Exception {
        ValueRange existing = sheetsClient.spreadsheets().values()
                .get(SHEET_ID, "Sheet1!A1")
                .execute();

        if (existing.getValues() == null || existing.getValues().isEmpty()) {
            List<Object> headers = Arrays.asList(
                    "Event DateTime",
                    "Event ID",
                    "Subscription ID",
                    "Topic",
                    "Job ID",
                    "Job Name",
                    "City",
                    "Zip Code",
                    "Contact ID",
                    "First Name",
                    "Last Name",
                    "Email",
                    "Phone"
            );
            ValueRange header = new ValueRange()
                    .setValues(Collections.singletonList(headers));

            sheetsClient.spreadsheets().values()
                    .update(SHEET_ID, "Sheet1!A1", header)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("[SHEETS] ✔ Header row created.");
        }
    }
}