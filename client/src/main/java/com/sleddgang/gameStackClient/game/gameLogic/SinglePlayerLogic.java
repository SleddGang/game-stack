package com.sleddgang.gameStackClient.game.gameLogic;

import com.sleddgang.gameStackClient.game.MessageGenerator;
import com.sleddgang.gameStackClient.game.MessageGeneratorImpl;
import com.sleddgang.gameStackClient.util.enums.Option;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import static com.sleddgang.gameStackClient.config.GameValues.playerOneOption;

import java.util.Random;
import static java.lang.String.format;

import static com.sleddgang.gameStackClient.config.GameValues.botOption;

@Log4j2
@Component
public class SinglePlayerLogic implements CoreLogic {

  // == fields ==
  private final MessageGenerator messageGenerator;
  private final Random rand = new Random();

  SinglePlayerLogic(MessageGeneratorImpl messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  @Override
  public boolean setPlayerOption(String player, String option) {
    log.debug(format("%s chose %s", player, option));
    try {
      playerOneOption = Option.getOption(option);
    } catch (IllegalArgumentException e) {
      log.debug(e);
      messageGenerator.printInvalidSelectionMessage("option");
      return false;
    }
    if (!playerOneOption.equals(Option.MAIN_MENU)) {
      messageGenerator.printSelectionMessage("You", playerOneOption.getLabel());
    }
    return true;
  }

  public void setBotOption() {
    botOption = Option.getOption(Integer.toString(rand.nextInt(3) + 1));
    messageGenerator.printSelectionMessage("Your opponent", botOption.getLabel());
  }

  @Override
  public void evaluateResults(Option playerOneOption, Option playerTwoOption) {
    log.debug("Player 1 Chose -- {}. The bot Chose -- {}", playerOneOption, playerTwoOption);
    String result;
    if (playerOneOption.equals(playerTwoOption)) {
      result = "You tied.";
    } else {
      result = didPlayerOneWin(playerOneOption, playerTwoOption) ? "You won!" : "You lost :(";
    }
    messageGenerator.printResultMessage(result);
  }

  @Override
  public void refreshValues() {
    playerOneOption = null;
    botOption = null;
  }
  
}
