package myservertests;

import myserver.routes.Utils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

  @Test
  void send404() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.send404(out);
    String response = out.toString();
    assertTrue(response.contains("404 Not Found"));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void sendHtmlString() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendHtmlString("<html>hello</html>", "html", out);
    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("Content-Type: text/html"));
    assertTrue(response.contains("<html>hello</html>"));
  }

  @Test
  void contentTypeTxt() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendHtmlString("hello", "txt", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: text/plain"));
  }

  @Test
  void contentTypeDefault() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendHtmlString("data", "xyz", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: application/octet-stream"));
  }

  @Test
  void contentTypeJson() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendHtmlString("{}", "json", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: application/json"));
  }

  @Test
  void contentTypeCss() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendHtmlString("body{}", "css", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: text/css"));
  }
}
