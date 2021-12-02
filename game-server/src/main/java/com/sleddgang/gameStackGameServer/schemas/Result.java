package com.sleddgang.gameStackGameServer.schemas;

/**
 * Result is sent to each client in a match to inform them of the results of the match.
 *
 * @see ResultEvent
 * @author Benjamin
 */
public enum Result {
    WIN,
    LOSS,
    TIE;
}
