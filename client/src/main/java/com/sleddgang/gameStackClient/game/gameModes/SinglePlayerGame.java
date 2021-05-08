package com.sleddgang.gameStackClient.game.gameModes;

import java.util.Scanner;

import com.sleddgang.gameStackClient.game.MessageGenerator;
import com.sleddgang.gameStackClient.game.MessageGeneratorImpl;
import com.sleddgang.gameStackClient.game.gameLogic.SinglePlayerLogic;
import com.sleddgang.gameStackClient.util.enums.Option;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import static com.sleddgang.gameStackClient.config.GameValues.playerOneOption;
import static com.sleddgang.gameStackClient.config.GameValues.botOption;

@Log4j2
@Component
public class SinglePlayerGame {

  // == fields ==
  private final SinglePlayerLogic singlePlayerLogic;
  private final MessageGenerator messageGenerator;

  // == constructors ==
  public SinglePlayerGame(SinglePlayerLogic singlePlayerLogic, MessageGeneratorImpl messageGenerator) {
    this.singlePlayerLogic = singlePlayerLogic;
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  public void playSinglePlayerGame(Scanner keyboard) {

    // While loop allows the player to play multiple games in a row
    while (true) {
      // Displays Game Menu
      messageGenerator.printGameMenu();

      // Sets the player's choice
      // If the player enters an invalid option, it resets the loop
      if (!singlePlayerLogic.setPlayerOption("Player One", keyboard.nextLine())) {
          continue;
      }

      // Breaks out of while loop if player enters "Main Menu"
      if (playerOneOption.equals(Option.MAIN_MENU)) {
        log.debug("Returning to main menu");
        singlePlayerLogic.refreshValues();
        break;
      }

      // Sets the bot's option
      singlePlayerLogic.setBotOption();

      // Evaluates and prints the game's result
      singlePlayerLogic.evaluateResults(playerOneOption, botOption);
      singlePlayerLogic.refreshValues();
    }
  }

}
