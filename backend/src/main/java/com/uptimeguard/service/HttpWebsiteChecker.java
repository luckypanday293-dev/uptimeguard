package com.uptimeguard.service;

import com.uptimeguard.model.Monitor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpWebsiteChecker implements WebsiteChecker {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public ProbeResult check(Monitor monitor) {
        long started = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(monitor.getUrl()))
                    .timeout(Duration.ofMillis(monitor.getTimeoutMs()))
                    .GET()
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            boolean ok = response.statusCode() == monitor.getExpectedStatus();
            String error = ok ? null : "Expected HTTP " + monitor.getExpectedStatus() + " but received " + response.statusCode();
            return new ProbeResult(ok, response.statusCode(), elapsedMs, error);
        } catch (Exception exception) {
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            String message = exception.getClass().getSimpleName() + ": " +
                    (exception.getMessage() == null ? "request failed" : exception.getMessage());
            return new ProbeResult(false, null, elapsedMs, message);
        }
    }
}
