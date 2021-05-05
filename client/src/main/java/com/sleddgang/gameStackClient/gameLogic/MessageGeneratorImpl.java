package com.sleddgang.gameStackClient.gameLogic;

import com.sleddgang.gameStackClient.annotations.GameMenu;
import com.sleddgang.gameStackClient.annotations.GoodbyeMessage;
import com.sleddgang.gameStackClient.annotations.InvalidMessage;
import com.sleddgang.gameStackClient.annotations.MainMenu;
import com.sleddgang.gameStackClient.annotations.ResultMessage;
import com.sleddgang.gameStackClient.annotations.SelectionMessage;
import com.sleddgang.gameStackClient.annotations.WelcomeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static com.sleddgang.gameStackClient.util.TextUtil.equalsTextWrapper;
import static com.sleddgang.gameStackClient.util.TextUtil.hashTextWrapper;
import static java.lang.String.format;

@Log4j2
@Component
public class MessageGeneratorImpl implements MessageGenerator {

  // == fields ==
  private final String welcomeMessage;
  private final String mainMenu;
  private final String gameMenu;
  private final String selectionMessage;
  private final String invalidMessage;
  private final String resultMessage;
  private final String goodbyeMessage;

  // == constructors ==
  public MessageGeneratorImpl(@WelcomeMessage String welcomeMessage,
      @MainMenu String mainMenu,
      @GameMenu String gameMenu,
      @SelectionMessage String selectionMessage,
      @InvalidMessage String invalidMessage,
      @ResultMessage String resultMessage,
      @GoodbyeMessage String goodbyeMessage) {
    this.welcomeMessage = welcomeMessage;
    this.mainMenu = mainMenu;
    this.gameMenu = gameMenu;
    this.selectionMessage = selectionMessage;
    this.invalidMessage = invalidMessage;
    this.resultMessage = resultMessage;
    this.goodbyeMessage = goodbyeMessage;
  }

  // == public methods ==
  @Override
  public void printWelcomeMessage() {
    log.info("{}\n", hashTextWrapper(welcomeMessage));
  }

  @Override
  public void printMainMenu() {
    log.info("{}\n", equalsTextWrapper(mainMenu));
  }

  @Override
  public void printGameMenu() {
    log.info("{}\n", equalsTextWrapper(gameMenu));
  }

  @Override
  public void printGoodbyeMessage() {
    log.info("{}\n", goodbyeMessage);
  }

  @Override
  public void printSelectionMessage(String user, String selection) {
    log.info("{}", format(selectionMessage, user, selection));
  }

  @Override
  public void printResultMessage(String result) {
    log.info("{}\n", format(resultMessage, result));
  }

  @Override
  public void printInvalidSelectionMessage(String type) {
    log.info("{}\n", format(invalidMessage, type));
  }

}
