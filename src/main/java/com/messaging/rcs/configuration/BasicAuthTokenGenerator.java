package com.messaging.rcs.configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuthTokenGenerator {

    public static String generateBasicAuthToken(String username, String password) {
        String authString = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(authString.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    public static void main(String[] args) {
        // Test with the provided credentials
        String token = generateBasicAuthToken("uYhGMTzS6k7gVUK6", "SA2TgAJ3ai8YYoJsIPyIyyo9hcQhNt8V");
        System.out.println("Generated Basic Token: " + token);
    }
}