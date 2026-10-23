package com.travis.customclicker.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class UpdateService {
    public static final String RELEASES_URL = "https://github.com/1999Travis250/CustomClicker/releases/latest";
    private static final URI API_URL = URI.create(
            "https://api.github.com/repos/1999Travis250/CustomClicker/releases/latest");

    public enum Status { AVAILABLE, CURRENT, NO_RELEASE }
    public record Result(Status status, String version) {}
    record Response(int status, String body) {}

    @FunctionalInterface
    interface Transport {
        Response fetch() throws IOException, InterruptedException;
    }

    private final Transport transport;

    public UpdateService() {
        this(() -> {
            try (HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10)).build()) {
                HttpRequest request = HttpRequest.newBuilder(API_URL)
                        .timeout(Duration.ofSeconds(15))
                        .header("Accept", "application/vnd.github+json")
                        .header("User-Agent", "CustomClicker")
                        .GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return new Response(response.statusCode(), response.body());
            }
        });
    }

    UpdateService(Transport transport) {
        this.transport = transport;
    }

    public Result check(String currentVersion) throws IOException, InterruptedException {
        Response response = transport.fetch();
        if (response.status() == 404) return new Result(Status.NO_RELEASE, null);
        if (response.status() == 403 || response.status() == 429)
            throw new IOException("GitHub has limited update requests. Please try again later.");
        if (response.status() != 200)
            throw new IOException("GitHub returned HTTP " + response.status() + ". Please try again later.");
        try {
            JsonObject release = JsonParser.parseString(response.body()).getAsJsonObject();
            if (release.get("draft").getAsBoolean() || release.get("prerelease").getAsBoolean())
                return new Result(Status.NO_RELEASE, null);
            String latest = release.get("tag_name").getAsString();
            Status status = compareVersions(latest, currentVersion) > 0 ? Status.AVAILABLE : Status.CURRENT;
            return new Result(status, latest);
        } catch (RuntimeException e) {
            throw new IOException("GitHub returned an unrecognized release version. Please check the Releases page.", e);
        }
    }

    // Release packaging requires three numeric version components.
    static int compareVersions(String left, String right) {
        String[] a = versionParts(left);
        String[] b = versionParts(right);
        for (int i = 0; i < 3; i++) {
            int result = new BigInteger(a[i]).compareTo(new BigInteger(b[i]));
            if (result != 0) return result;
        }
        return 0;
    }

    private static String[] versionParts(String version) {
        if (!version.matches("v?\\d+\\.\\d+\\.\\d+"))
            throw new IllegalArgumentException("Unsupported version: " + version);
        return version.replaceFirst("^v", "").split("\\.");
    }

    public static String currentVersion() {
        Properties properties = new Properties();
        try (InputStream input = UpdateService.class.getResourceAsStream("/app.properties")) {
            if (input == null) throw new IllegalStateException("Missing application version resource.");
            properties.load(input);
            String version = properties.getProperty("app.version");
            versionParts(version);
            return version;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read application version.", e);
        }
    }
}
