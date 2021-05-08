package com.sleddgang.gameStackClient.game.gameModes;

import java.util.Objects;
import java.util.Scanner;

import com.sleddgang.gameStackClient.game.MessageGenerator;
import com.sleddgang.gameStackClient.game.MessageGeneratorImpl;
import com.sleddgang.gameStackClient.game.gameLogic.LocalMultiplayerLogic;
import com.sleddgang.gameStackClient.util.enums.Option;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import static com.sleddgang.gameStackClient.config.GameValues.playerOneOption;
import static com.sleddgang.gameStackClient.config.GameValues.playerTwoOption;

@Log4j2
@Component
public class LocalMultiplayerGame {

  // == fields ==
  private final LocalMultiplayerLogic localLogic;
  private final MessageGenerator messageGenerator;

  // == constructors ==
  public LocalMultiplayerGame(LocalMultiplayerLogic gameLogic, MessageGeneratorImpl messageGenerator) {
    this.localLogic = gameLogic;
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  public void playLocalMultiplayerGame(Scanner keyboard) {

    EraserThread et = new EraserThread();
      Thread mask = new Thread(et);
      mask.start();
    while(true) {      

      // Sets the user's choice
      // If the player enters an invalid option, it resets the loop
      if (Objects.isNull(playerOneOption)) {
        // Displays Game Menu
        messageGenerator.printGameMenu("Player One");
        if (!localLogic.setPlayerOption("Player One", keyboard.nextLine())) {
          continue;
        }
        // et.stopMasking();
      }

      // Breaks out of while loop if player enters "Main Menu"
      if (playerOneOption.equals(Option.MAIN_MENU)) {
        log.debug("Returning to main menu");
        localLogic.refreshValues();
        break;
      }

      // Displays Game Menu
      messageGenerator.printGameMenu("Player Two");

      // Sets the user's choice
      // If the player enters an invalid option, it resets the loop
      // mask.resume();
      if (!localLogic.setPlayerOption("Player Two", keyboard.nextLine())) {
        continue;
      }

      // Breaks out of while loop if player enters "Main Menu"
      if (playerTwoOption.equals(Option.MAIN_MENU)) {
        log.debug("Returning to main menu");
        localLogic.refreshValues();
        break;
      }

      // Evaluates and prints the game's result
      localLogic.evaluateResults(playerOneOption, playerTwoOption);
      localLogic.refreshValues();
    }
    et.stopMasking();

  }

  private class EraserThread implements Runnable {
    private boolean stop;
 
    public EraserThread() {
    }
 
    public void run () {
       stop = true;
       while (stop) {
          System.out.print("\010 ");
      try {
         Thread.currentThread();
        Thread.sleep(1);
          } catch(InterruptedException ie) {
             ie.printStackTrace();
          }
       }
    }

    public void stopMasking() {
       this.stop = false;
    }
 }
    
}
