package com.sleddgang.gameStackGameServer.schemas;

/**
 * ResultEvent is sent to each client in a match to inform them of the results of the match.
 *
 * @see Result
 * @author Benjamin
 */
public class ResultEvent extends GameServerEvent {
    public Result result;

    public ResultEvent(Result result) {
        this.result = result;
    }

    public ResultEvent() {
        this.result = Result.TIE;
    }
}

