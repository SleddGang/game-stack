package com.sleddgang.gameStackGameServer.schemas;

public enum Error {
    DEFAULT(0, ""),
    UNKNOWN_EVENT(1, "Unknown event type."),
    INALID_REQID(2, "Invalid request id. Make sure you are using a request id previously used."),
    CLIENT_ALREADY_IN_MATCH(2, "The client is already in a match"),
    INVALID_CLIENT(3, "The client is not authorized to join a match");

    private final int id;
    private final String message;
    Error(int id, String message) {
        this.id = id;
        this.message = message;
    }
}
