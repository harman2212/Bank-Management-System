package com.bank.app;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class StartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("local.server.port");
        if (port == null || port.isBlank()) {
            port = env.getProperty("server.port", "8080");
        }

        String url = "http://localhost:" + port;

        System.out.println();
        System.out.println("==============================");
        System.out.println("Bank Management Web App started successfully.");
        System.out.println("Opening this URL in your browser:");
        System.out.println("    " + url);
        System.out.println("==============================");
        System.out.println();

        openBrowser(url);
    }

    private void openBrowser(String url) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            return;
        }

        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            System.err.println("Failed to open browser: " + e.getMessage());
        }
    }
}
