package com.sleddgang.gameStackClient.game;

public interface MessageGenerator {

  void printWelcomeMessage();

  void printMainMenu();

  void printGameMenu(String player);

  default void printGameMenu() {
    printGameMenu("");
  }

  void printGoodbyeMessage();

  void printSelectionMessage(String user, String selection);

  void printResultMessage(String result);

  void printInvalidSelectionMessage(String type);

}
