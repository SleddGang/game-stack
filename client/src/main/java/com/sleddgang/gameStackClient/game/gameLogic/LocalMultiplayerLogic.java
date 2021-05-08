package com.sleddgang.gameStackClient.game.gameLogic;

import com.sleddgang.gameStackClient.game.MessageGenerator;
import com.sleddgang.gameStackClient.game.MessageGeneratorImpl;
import com.sleddgang.gameStackClient.util.enums.Option;

import org.springframework.stereotype.Component;

import static com.sleddgang.gameStackClient.config.GameValues.playerOneOption;
import static com.sleddgang.gameStackClient.config.GameValues.playerTwoOption;
import static java.lang.String.format;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class LocalMultiplayerLogic implements CoreLogic {

  // == fields ==
  private final MessageGenerator messageGenerator;

  // == constructors ==
  public LocalMultiplayerLogic(MessageGeneratorImpl messageGenerator)
 {
   this.messageGenerator = messageGenerator;
 }

 // == public methods ==
  @Override
  public boolean setPlayerOption(String player, String option) {
    log.debug(format("%s chose %s", player, option));
    player = player.replace(" ", "").trim();
    Option playerOption;
    try {
      playerOption = Option.getOption(option);
    } catch (IllegalArgumentException e) {
      log.debug(e);
      messageGenerator.printInvalidSelectionMessage("option");
      return false;
    }
    if (player.equalsIgnoreCase("PlayerOne")) {
      playerOneOption = playerOption;
    } else if (player.equals("PlayerTwo")) {
      playerTwoOption = playerOption;
    }
    return true;
  }

  @Override
  public void evaluateResults(Option playerOneOption, Option playerTwoOption) {
    log.debug("Player 1 Chose -- {}. Player 2 Chose -- {}", playerOneOption, playerTwoOption);
    messageGenerator.printSelectionMessage("Player One", playerOneOption.getLabel());
    messageGenerator.printSelectionMessage("Player Two", playerTwoOption.getLabel());
    String result;
    if (playerOneOption.equals(playerTwoOption)) {
      result = "You tied.";
    } else {
      result = didPlayerOneWin(playerOneOption, playerTwoOption) ? "Player 1 wins!" : "Player 2 wins!";
    }
    messageGenerator.printResultMessage(result);
  }

  @Override
  public void refreshValues() {
    playerOneOption = null;
    playerTwoOption = null;
  }
  
}
