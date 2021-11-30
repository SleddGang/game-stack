package com.sleddgang.gameStackGameServer.schemas;


public class ResultEvent extends GameServerEvent {
    public Result result;

    public ResultEvent(Result result) {
        this.result = result;
    }

    public ResultEvent() {
        this.result = Result.TIE;
    }
}

