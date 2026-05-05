package com.acculynx;

import com.acculynx.cli.WorkflowRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


//Entry point for this Integration
//- Default: runs the CLI workflow (create Contact + Job interactively)
//- With --webhook flag: starts the webhook listener server

@SpringBootApplication
public class AccuLynxApplication {

    public static void main(String[] args) throws Exception {
        boolean webhookMode = false;
        for (String arg : args) {
            if ("--webhook".equalsIgnoreCase(arg)) {
                webhookMode = true;
                break;
            }
        }

        SpringApplication app = new SpringApplication(AccuLynxApplication.class);

        if (!webhookMode) {
            // CLI mode — no web server needed
            app.setAdditionalProfiles("cli");
        }

        ApplicationContext context = app.run(args);

        if (!webhookMode) {
            WorkflowRunner runner = context.getBean(WorkflowRunner.class);
            runner.run();
            System.exit(0);
        } else {
            System.out.println("Webhook mode server is running on port 9090");
        }
    }
}