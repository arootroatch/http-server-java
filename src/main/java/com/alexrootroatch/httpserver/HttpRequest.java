package com.alexrootroatch.httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public record HttpRequest(String method, String path, String queryString,
                          Map<String, String> headers, byte[] body) {

  public String bodyAsString() {
    return new String(body, StandardCharsets.UTF_8);
  }

  public String header(String name) {
    return headers.getOrDefault(name, "");
  }

  public String cookieValue(String name) {
    String cookies = header("Cookie");
    if (cookies.isEmpty()) return "";
    return Arrays.stream(cookies.split(";\\s*"))
        .filter(c -> c.startsWith(name + "="))
        .map(c -> c.split("=", 2)[1])
        .findFirst()
        .orElse("");
  }
}
