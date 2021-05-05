package com.sleddgang.gameStackClient.gameLogic.gameModeLogic;

import java.util.Random;
import java.util.Scanner;

import com.sleddgang.gameStackClient.annotations.GameMenu;
import com.sleddgang.gameStackClient.gameLogic.GameLogic;
import com.sleddgang.gameStackClient.gameLogic.GameLogicImpl;
import com.sleddgang.gameStackClient.util.enums.Option;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SinglePlayerGame {

  private final GameLogic gameLogic;
  private final String gameMenu;
  private final Random rand = new Random();

  public SinglePlayerGame(GameLogicImpl gameLogic, @GameMenu String gameMenu) {
    this.gameLogic = gameLogic;
    this.gameMenu = gameMenu;
  }

  public void playSinglePlayerGame(Scanner keyboard) {

    while (true) {
      // Displays welcome message
      gameLogic.printGameMenu(gameMenu);

      // Grabs user's option choice
      Option playerOption;
      try {
        playerOption = Option.getOption(keyboard.nextLine());
      } catch (IllegalArgumentException e) {
        log.info("Please enter a valid option from the list");
        log.debug(e);
        continue;
      }

      // Breaks out of while loop if player enters "Quit"
      if (playerOption.equals(Option.QUIT)) {
        log.debug("Exiting game");
        break;
      }

      // Sets the bot's option
      Option botOption = Option.getOption(rand.nextInt(3) + 1);

      // Evaluates and prints the game's result
      gameLogic.evaluateResults(playerOption, botOption);
    }
  }

}
