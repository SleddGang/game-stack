package com.sleddgang.gameStackClient.util;

import static java.lang.String.format;

public class TextUtil {

  // == public methods ==
  public static String equalsTextWrapper(String text) {
    return prettifyText(text, "=");
  }

  public static String hashTextWrapper(String text) {
    return prettifyText(text, "#");
  }

  private static String prettifyText(String text, String wrapper) {
    int length = 0;
    for (String line : text.split("\n")) {
      if (line.length() > length) {
        length = line.length();
      }
    }
    String buffer = wrapper.repeat(length);
    return format("%s%n%s%n%s", buffer, text, buffer);
  }

}
