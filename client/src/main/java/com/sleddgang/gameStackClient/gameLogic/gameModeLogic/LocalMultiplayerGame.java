package com.sleddgang.gameStackClient.gameLogic.gameModeLogic;

import java.util.Objects;
import java.util.Scanner;

import com.sleddgang.gameStackClient.gameLogic.GameLogic;
import com.sleddgang.gameStackClient.gameLogic.GameLogicImpl;
import com.sleddgang.gameStackClient.gameLogic.MessageGenerator;
import com.sleddgang.gameStackClient.gameLogic.MessageGeneratorImpl;
import com.sleddgang.gameStackClient.util.enums.Option;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import static com.sleddgang.gameStackClient.config.GameValues.playerOneOption;
import static com.sleddgang.gameStackClient.config.GameValues.playerTwoOption;

@Log4j2
@Component
public class LocalMultiplayerGame {

  // == fields ==
  private final GameLogic gameLogic;
  private final MessageGenerator messageGenerator;

  // == constructors ==
  public LocalMultiplayerGame(GameLogicImpl gameLogic, MessageGeneratorImpl messageGenerator) {
    this.gameLogic = gameLogic;
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  public void playLocalMultiplayerGame(Scanner keyboard) {

    while(true) {
      // Displays Game Menu
      messageGenerator.printGameMenu();

      // Sets the user's choice
      // If the player enters an invalid option, it resets the loop
      if (Objects.isNull(playerOneOption)) {
        if (!gameLogic.setPlayerOneOption(keyboard.nextLine())) {
          continue;
        }
      }

      // Breaks out of while loop if player enters "Main Menu"
      if (playerOneOption.equals(Option.MAIN_MENU)) {
        log.debug("Returning to main menu");
        gameLogic.refreshValues();
        break;
      }

      // Sets the user's choice
      // If the player enters an invalid option, it resets the loop
      if (!gameLogic.setPlayerTwoOption(keyboard.nextLine())) {
        continue;
      }

      // Evaluates and prints the game's result
      gameLogic.evaluateResults(playerOneOption, playerTwoOption);
      gameLogic.refreshValues();
    }
  }
    
}
