package com.sleddgang.gameStackClient;

import com.sleddgang.gameStackClient.config.ClientConfig;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

  public static void main(String[] args) {
    ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
        ClientConfig.class);

    context.close();
  }

}
