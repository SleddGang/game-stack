package com.sleddgang.gameStackClient.gameLogic;

import com.sleddgang.gameStackClient.util.enums.Option;

public interface GameLogic {

  boolean setPlayerOneOption(String option);

  void setBotOption();

  void evaluateResults(Option playerOption, Option botOption);

}
