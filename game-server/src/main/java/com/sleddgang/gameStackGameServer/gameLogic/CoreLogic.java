package com.sleddgang.gameStackGameServer.gameLogic;

public class CoreLogic {
    public LogicResult evaluate(Option playerOne, Option playerTwo) {
        if (playerOne == playerTwo)
            return LogicResult.TIE;
        else if (didPlayerOneWin(playerOne, playerTwo))
            return LogicResult.PLAYERONE;
        else
            return LogicResult.PLAYERTWO;
    }

    private boolean didPlayerOneWin(Option playerOneOption, Option playerTwoOption) {
        return (playerOneOption.equals(Option.ROCK) && playerTwoOption.equals(Option.SCISSORS))
                || (playerOneOption.equals(Option.PAPER) && playerTwoOption.equals(Option.ROCK))
                || (playerOneOption.equals(Option.SCISSORS) && playerTwoOption.equals(Option.PAPER));
    }
}
