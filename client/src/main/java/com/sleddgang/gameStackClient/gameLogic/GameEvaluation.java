package com.sleddgang.gameStackClient.gameLogic;

import com.sleddgang.gameStackClient.util.enums.Option;

public interface GameEvaluation {

  void printGameMenu(String gameMenu);

  boolean didPlayerWin(Option playerOption, Option botOption);

  void evaluateResults(Option playerOption, Option botOption);

}
