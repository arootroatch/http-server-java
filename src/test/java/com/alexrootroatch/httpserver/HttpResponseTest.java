package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpResponseTest {

  @Test
  void send404() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.send404(out);
    String response = out.toString();
    assertTrue(response.contains("404 Not Found"));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void sendString() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.sendString("<html>hello</html>", "html", out);
    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("Content-Type: text/html"));
    assertTrue(response.contains("<html>hello</html>"));
  }

  @Test
  void send500() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.send500(out);
    String response = out.toString();
    assertTrue(response.contains("500 Internal Server Error"));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void send405() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.send405(out);
    String response = out.toString();
    assertTrue(response.contains("405 Method Not Allowed"));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void contentTypeTxt() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.sendString("hello", "txt", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: text/plain"));
  }

  @Test
  void contentTypeDefault() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.sendString("data", "xyz", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: application/octet-stream"));
  }

  @Test
  void contentTypeJson() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.sendString("{}", "json", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: application/json"));
  }

  @Test
  void contentTypeCss() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.sendString("body{}", "css", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: text/css"));
  }

  @Test
  void sendStringUtf8ContentLength() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpResponse.sendString("caf\u00e9", "txt", out);
    String response = out.toString();
    // "café" is 5 bytes in UTF-8 (4 ASCII + 1 two-byte char)
    assertTrue(response.contains("Content-Length: 5"));
  }
}
