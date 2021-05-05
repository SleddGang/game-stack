package com.sleddgang.gameStackClient.config;

import com.sleddgang.gameStackClient.annotations.GameMenu;
import com.sleddgang.gameStackClient.annotations.GoodbyeMessage;
import com.sleddgang.gameStackClient.annotations.InvalidMessage;
import com.sleddgang.gameStackClient.annotations.MainMenu;
import com.sleddgang.gameStackClient.annotations.ResultMessage;
import com.sleddgang.gameStackClient.annotations.SelectionMessage;
import com.sleddgang.gameStackClient.annotations.WelcomeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/client.properties")
@ComponentScan(basePackages = "com.sleddgang.gameStackClient")
public class ClientConfig {

  // == fields ==
  @Value("${client.welcomeMessage}")
  private String welcomeMessage;

  @Value("${client.mainMenu}")
  private String mainMenu;

  @Value("${client.gameMenu}")
  private String gameMenu;

  @Value("${client.selectionMessage}")
  private String selectionMessage;

  @Value("${client.invalidMessage}")
  private String invalidMessage;

  @Value("${client.resultMessage}")
  private String resultMessage;

  @Value("${client.goodbyeMessage}")
  private String goodbyeMessage;

  // == bean methods ==

  @Bean
  @WelcomeMessage
  public String welcomeMessage() {
    return welcomeMessage;
  }

  @Bean
  @GameMenu
  public String gameMenu() {
    return gameMenu;
  }

  @Bean
  @MainMenu
  public String mainMenu() {
    return mainMenu;
  }

  @Bean
  @SelectionMessage
  public String selectionMessage() {
    return selectionMessage;
  }

  @Bean
  @InvalidMessage
  public String invalidMessage() {
    return invalidMessage;
  }

  @Bean
  @ResultMessage
  public String resultMessage() {
    return resultMessage;
  }

  @Bean
  @GoodbyeMessage
  public String goodbyeMessage() {
    return goodbyeMessage;
  }

}
