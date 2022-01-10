package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

/**
 * Different types of errors
 *
 * @see ErrorResponse
 * @author Benjamin
 */
public enum HandlerError {
    /**
     * Indicates that there was a problem getting the match. This should never happen.
     */
    INVALID_MATCH,
    /**
     * Indicates that the server is already at it's maximum number of matches anc can't make a new one.
     */
    MATCHES_FULL,
    /**
     * Indicates that there was an attempt to make a match that already exists.
     */
    DUPLICATE_CLIENT,
}
