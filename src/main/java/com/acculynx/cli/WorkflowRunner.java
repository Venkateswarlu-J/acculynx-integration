package com.acculynx.cli;

import com.acculynx.model.InputValidator;
import com.acculynx.service.AccuLynxClient;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * CLI entry point.
 *
 * Collects user input field-by-field, validates it,
 * and delegates to IntegrationService to run the API workflow.
 *
 * Responsibilities:
 *   - Display prompts to the user
 *   - Read input from stdin
 *   - Validate and re-prompt on errors
 *   - Print success/failure results
 */
@Component
public class WorkflowRunner {

    private final AccuLynxClient client;
    private final Scanner scanner = new Scanner(System.in);

    public WorkflowRunner(AccuLynxClient client) {
        this.client = client;
    }

    public void run() {

        printHeader();

        // ── Collect Contact Info ──
        System.out.println("\n=== CONTACT INFORMATION ===");
        String firstName = ask("First Name",    null);               // only empty check
        String lastName  = ask("Last Name",     null);
        String phone     = askWithValidator("Phone Number",
                v -> InputValidator.validatePhone(v));                          //Checking empty and validating by passing lambda function as  argument
        String email     = askWithValidator("Email Address",
                v -> InputValidator.validateEmail(v));

        // ── Collect Job Info ──
        System.out.println("\n=== JOB INFORMATION ===");
        String street  = ask("Street Address", null);
        String city    = ask("City",           null);
        String state   = askWithValidator("State",
                v -> InputValidator.validateState(v));
        String zipCode = askWithValidator("Zip Code",
                v -> InputValidator.validateZip(v));

        // ── Run API Workflow ──
        System.out.println();
        try {
            String contactId = client.createContact(firstName, lastName, phone, email);
            String jobId     = client.createJob(contactId, street, city, state, zipCode);
            printSuccess(contactId, jobId);

        } catch (RuntimeException e) {
            System.out.println("\n  ✖ Error: " + e.getMessage());
            System.out.println("  Please check your inputs or API key and try again.\n");
        }
    }

    //  Ask with only empty validation
    private String ask(String fieldName, Object unused) {
        while (true) {
            System.out.print(fieldName + ": ");
            String value = scanner.nextLine().trim();
            String error = InputValidator.isBlank(value, fieldName);
            if (error == null) return value;
            System.out.println("  ⚠  " + error);
        }
    }

    //  Ask with custom validator (lambda)
    private String askWithValidator(String fieldName,
                                    java.util.function.Function<String, String> validator) {
        while (true) {
            System.out.print(fieldName + ": ");
            String value = scanner.nextLine().trim();
            String error = validator.apply(value);
            if (error == null) return value;
            System.out.println("  ⚠  " + error);
        }
    }

    //  Print helpers
    private void printHeader() {
        System.out.println();
        System.out.println("====AccuLynx REST API Integration Tool====");
    }

    private void printSuccess(String contactId, String jobId) {
        System.out.println();
        System.out.println("=======Workflow Completed Successfully!========");
        System.out.println("  Contact ID : " + contactId);
        System.out.println("  Job ID     : " + jobId);
        System.out.println();
    }
}

