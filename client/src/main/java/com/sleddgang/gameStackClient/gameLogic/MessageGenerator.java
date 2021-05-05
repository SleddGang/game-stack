package com.sleddgang.gameStackClient.gameLogic;

public interface MessageGenerator {

  void printWelcomeMessage();

  void printMainMenu();

  void printGameMenu();

  void printGoodbyeMessage();

  void printSelectionMessage(String user, String selection);

  void printResultMessage(String result);

  void printInvalidSelectionMessage(String type);

}
