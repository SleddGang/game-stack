package com.sleddgang.gameStackGameServer.schemas;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sleddgang.gameStackGameServer.schemas.replies.ErrorReply;

/**
 * Different types of errors that can be sent over a WebSocket.
 *
 * @see ErrorReply
 * @author Benjamin
 */
// @JsonFormat tells jackson to serialize the enum properties not the name;
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Error {
    /**
     * Default error. It is only used in the error event default constructor. See {@link ErrorReply}.
     */
    DEFAULT(0, ""),
    /**
     * Used to indicate that the sever did not recognize the event type that was sent.
     */
    UNKNOWN_MESSAGE(1, "Unknown message type."),
    /**
     * Indicates that the reqid that the client used has already been used.
     */
    INVALID_REQID(2, "Invalid request id. Make sure you are using a request id not previously used."),
    /**
     * Indicates that the client is unable to join a match as they are already in that match.
     */
    CLIENT_ALREADY_IN_MATCH(3, "The client is already in a match"),
    /**
     * Indicates that a client is that authorized to join any match.
     */
    INVALID_CLIENT(4, "The client is not authorized to join a match"),
    /**
     * Indicates to a matchmaking server that the game sever is unable to create the request match.
     * If this error shows up I have made a grave mistake making this server.
     */
    INVALID_MATCH_ERROR(5, "The game server is unable to make the requested match."),
    /**
     * Indicates to a matchmaking server that the game server is unable to make a match because
     * the server already has the max number of matches running.
     */
    MATCHES_FULL(6, "The game server is already full of matches."),
    /**
     * Indicates to a matchmaking server that the game server is unable to make a match because
     * a match with the requested uuid already exists.
     */
    DUPLICATE_CLIENT(7, "The game server already has a client with that uuid"),
    /**
     * Indicates to a client that the server ran into an error while running the match, and they should
     * join a new match
     */
    MATCH_ERROR(8, "The game server had an issue processing the match. Please join a new match.");

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
