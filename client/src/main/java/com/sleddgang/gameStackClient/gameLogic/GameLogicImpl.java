package com.sleddgang.gameStackClient.gameLogic;

import com.sleddgang.gameStackClient.util.enums.Option;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static com.sleddgang.gameStackClient.config.GameValues.playerOneOption;
import static com.sleddgang.gameStackClient.config.GameValues.botOption;

@Component
@Log4j2
public class GameLogicImpl implements GameLogic {

  // == fields ==
  // public static Option playerOneOption;
  // public static Option botOption;
  private final Random rand = new Random();

  private final MessageGenerator messageGenerator;

  // == constructors ==
  public GameLogicImpl(MessageGeneratorImpl messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  @Override
  public boolean setPlayerOneOption(String option) {
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

  @Override
  public void setBotOption() {
    botOption = Option.getOption(Integer.toString(rand.nextInt(3) + 1));
    messageGenerator.printSelectionMessage("Your opponent", botOption.getLabel());
  }

  @Override
  public void evaluateResults(Option playerOneOption, Option botOption) {
    log.debug("Player Chose -- {}. Bot Chose -- {}", playerOneOption, botOption);
    String result;
    if (playerOneOption.equals(botOption)) {
      result = "tied.";
    } else {
      result = didPlayerWin(playerOneOption, botOption) ? "won!" : "lost :(";
    }
    messageGenerator.printResultMessage(result);
  }

  // == private methods ==
  private boolean didPlayerWin(Option playerOneOption, Option botOption) {
    return (playerOneOption.equals(Option.ROCK) && botOption.equals(Option.SCISSORS))
        || (playerOneOption.equals(Option.PAPER) && botOption.equals(Option.ROCK))
        || (playerOneOption.equals(Option.SCISSORS) && botOption.equals(Option.PAPER));
  }
}
