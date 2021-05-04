package com.sleddgang.gameStackClient.config;

import com.sleddgang.gameStackClient.annotations.GameMenu;
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
  @Value("${client.gameMenu}")
  private String gameMenu;

  // == bean methods ==
  @Bean
  @GameMenu
  public String gameMenu() {
    return gameMenu;
  }

}
