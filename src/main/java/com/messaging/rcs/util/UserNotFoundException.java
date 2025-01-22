package com.messaging.rcs.util;

public class UserNotFoundException extends Exception {
    private String username;

    public static UserNotFoundException createWith(String username) {
        return new UserNotFoundException(username);
    }

    public UserNotFoundException(String username) {
        this.username = username;
    }

    @Override
    public String getMessage() {
        return "Invalid Credential.";
    }
}