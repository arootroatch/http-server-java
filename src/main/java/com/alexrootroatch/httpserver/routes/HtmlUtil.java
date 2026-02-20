package com.alexrootroatch.httpserver.routes;

public final class HtmlUtil {
  private HtmlUtil() {
  }

  public static String escapeHtml(String input) {
    if (input == null) return "";
    StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      switch (c) {
        case '&' -> sb.append("&amp;");
        case '<' -> sb.append("&lt;");
        case '>' -> sb.append("&gt;");
        case '"' -> sb.append("&quot;");
        case '\'' -> sb.append("&#x27;");
        default -> sb.append(c);
      }
    }
    return sb.toString();
  }
}
