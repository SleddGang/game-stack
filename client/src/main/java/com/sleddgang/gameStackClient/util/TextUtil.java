package com.sleddgang.gameStackClient.util;

import static java.lang.String.format;

public class TextUtil {

  public static String prettifyText(String text) {
    int length = 0;
    for (String line : text.split("\n")) {
      if (line.length() > length) {
        length = line.length();
      }
    }
    String buffer = "=".repeat(length);
    return format("%s%n%s%n%s", buffer, text, buffer);
  }

}
