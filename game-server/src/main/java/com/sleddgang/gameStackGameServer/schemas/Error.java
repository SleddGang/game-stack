package com.sleddgang.gameStackGameServer.schemas;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

// @JsonFormat tells jackson to serialize the enum properties not the name;
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Error {
    DEFAULT(0, ""),
    UNKNOWN_EVENT(1, "Unknown event type."),
    INVALID_REQID(2, "Invalid request id. Make sure you are using a request id not previously used."),
    CLIENT_ALREADY_IN_MATCH(3, "The client is already in a match"),
    INVALID_CLIENT(4, "The client is not authorized to join a match"),
    INVALID_MATCH_ERROR(5, "The game server is unable to make the requested match."),
    MATCH_ERROR(6, "The game server had an issue processing the match. Please join a new match.");

    public int id;
    public String message;
    Error(int id, String message) {
        this.id = id;
        this.message = message;
    }

    //forValues enables jackson to tell what value the enum is.
    @JsonCreator
    public static Error forValues(@JsonProperty("id") int id, @JsonProperty("message") String message) {
        for (Error error : Error.values()) {
            if (error.id == id && error.message.equals(message)) {
                return error;
            }
        }
        return null;
    }
}
