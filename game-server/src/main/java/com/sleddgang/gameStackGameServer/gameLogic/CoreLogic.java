package com.sleddgang.gameStackGameServer.gameLogic;

/**
 * This class contains the rock paper scissors logic.
 * <p>See {@link com.sleddgang.gameStackGameServer.handler.Match} for its use.</p>
 *
 * @author Benjamin
 */
public class CoreLogic {
    /**
     * Evaluate the moves made by both players and find who won.
     * First check if it is a tie and if not then check if player one won.
     *
     * @param playerOne Player one's move.
     * @param playerTwo Player two's move.
     * @return          Who one. Either player one, player two, or tie.
     */
    public LogicResult evaluate(Option playerOne, Option playerTwo) {
        if (playerOne == playerTwo)
            return LogicResult.TIE;
        else if (didPlayerOneWin(playerOne, playerTwo))
            return LogicResult.PLAYERONE;
        else
            return LogicResult.PLAYERTWO;
    }

    /**
     * Check if the first player wins. This function does not handle ties.
     *
     * @param playerOneOption   Player one's move.
     * @param playerTwoOption   Player two's move.
     * @return                  Returns true if player one wins and false if player two wins or it is a tie.
     */
    private boolean didPlayerOneWin(Option playerOneOption, Option playerTwoOption) {
        return (playerOneOption.equals(Option.ROCK) && playerTwoOption.equals(Option.SCISSORS))
                || (playerOneOption.equals(Option.PAPER) && playerTwoOption.equals(Option.ROCK))
                || (playerOneOption.equals(Option.SCISSORS) && playerTwoOption.equals(Option.PAPER));
    }
}
