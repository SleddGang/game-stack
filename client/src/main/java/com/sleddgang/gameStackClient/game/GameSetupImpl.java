package com.sleddgang.gameStackClient.game;

import com.sleddgang.gameStackClient.util.enums.GameMode;
import java.util.Scanner;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

import static com.sleddgang.gameStackClient.config.GameValues.gameMode;

@Component
@Log4j2
public class GameSetupImpl implements GameSetup {

  // == fields ==
  public final MessageGenerator messageGenerator;

  // == constructors ==
  public GameSetupImpl(MessageGeneratorImpl messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  // == public methods ==
  @Override
  public boolean setGameMode(String mode) {
    try {
      gameMode = GameMode.getGameMode(mode);
    } catch (IllegalArgumentException e) {
      log.debug(e);
      messageGenerator.printInvalidSelectionMessage("game mode");
      return false;
    }
    return true;
  }

  @Override
  public void exitGame(Scanner keyboard) {
    keyboard.close();
    messageGenerator.printGoodbyeMessage();
    System.exit(0);
  }

}
