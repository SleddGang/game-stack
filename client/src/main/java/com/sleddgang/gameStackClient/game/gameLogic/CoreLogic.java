package com.sleddgang.gameStackClient.game.gameLogic;

import com.sleddgang.gameStackClient.util.enums.Option;

public interface CoreLogic {
  
  boolean setPlayerOption(String player, String option);

  void evaluateResults(Option playerOneOption, Option playerTwoOption);

  void refreshValues();

  default boolean didPlayerOneWin(Option playerOneOption, Option playerTwoOption) {
    return (playerOneOption.equals(Option.ROCK) && playerTwoOption.equals(Option.SCISSORS))
        || (playerOneOption.equals(Option.PAPER) && playerTwoOption.equals(Option.ROCK))
        || (playerOneOption.equals(Option.SCISSORS) && playerTwoOption.equals(Option.PAPER));
  }

}
