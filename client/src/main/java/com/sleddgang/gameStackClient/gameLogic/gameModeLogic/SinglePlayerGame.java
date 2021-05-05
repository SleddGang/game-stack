package com.sleddgang.gameStackClient.gameLogic.gameModeLogic;

import com.sleddgang.gameStackClient.gameLogic.MessageGenerator;
import com.sleddgang.gameStackClient.gameLogic.MessageGeneratorImpl;
import java.util.Scanner;

import com.sleddgang.gameStackClient.gameLogic.GameLogic;
import com.sleddgang.gameStackClient.gameLogic.GameLogicImpl;
import com.sleddgang.gameStackClient.util.enums.Option;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SinglePlayerGame {

  // == fields ==
  private final GameLogic gameLogic;
  private final MessageGenerator messageGenerator;

  // == constructors ==
  public SinglePlayerGame(GameLogicImpl gameLogic, MessageGeneratorImpl messageGenerator) {
    this.gameLogic = gameLogic;
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  public void playSinglePlayerGame(Scanner keyboard) {

    // While loop allows the player to play multiple games in a row
    while (true) {
      // Displays Game Menu
      messageGenerator.printGameMenu();

      // Sets the user's choice
      // If the player enters an invalid option, it resets the loop
      if (!gameLogic.setPlayerOption(keyboard.nextLine())) {
        continue;
      }

      // Breaks out of while loop if player enters "Main Menu"
      if (GameLogicImpl.playerOption.equals(Option.MAIN_MENU)) {
        log.debug("Returning to main menu");
        break;
      }

      // Sets the bot's option
      gameLogic.setBotOption();

      // Evaluates and prints the game's result
      gameLogic.evaluateResults(GameLogicImpl.playerOption, GameLogicImpl.botOption);
    }
  }

}
