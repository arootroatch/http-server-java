package myservertests;

import myserver.routes.Utils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  void sendString() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendString("<html>hello</html>", "html", out);
    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("Content-Type: text/html"));
    assertTrue(response.contains("<html>hello</html>"));
  }

  @Test
  void send500() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.send500(out);
    String response = out.toString();
    assertTrue(response.contains("500 Internal Server Error"));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void escapeHtmlNull() {
    assertEquals("", Utils.escapeHtml(null));
  }

  @Test
  void escapeHtmlNoSpecialChars() {
    assertEquals("hello world", Utils.escapeHtml("hello world"));
  }

  @Test
  void escapeHtmlAngleBrackets() {
    assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;",
        Utils.escapeHtml("<script>alert(1)</script>"));
  }

  @Test
  void escapeHtmlAmpersand() {
    assertEquals("foo &amp; bar", Utils.escapeHtml("foo & bar"));
  }

  @Test
  void escapeHtmlQuotes() {
    assertEquals("&quot;hello&quot; &#x27;world&#x27;",
        Utils.escapeHtml("\"hello\" 'world'"));
  }

  @Test
  void escapeHtmlMixed() {
    assertEquals("&lt;a href=&quot;x&quot;&gt;foo &amp; bar&lt;/a&gt;",
        Utils.escapeHtml("<a href=\"x\">foo & bar</a>"));
  }

  @Test
  void send405() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.send405(out);
    String response = out.toString();
    assertTrue(response.contains("405 Method Not Allowed"));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void contentTypeTxt() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendString("hello", "txt", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: text/plain"));
  }

  @Test
  void contentTypeDefault() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendString("data", "xyz", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: application/octet-stream"));
  }

  @Test
  void contentTypeJson() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendString("{}", "json", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: application/json"));
  }

  @Test
  void contentTypeCss() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.sendString("body{}", "css", out);
    String response = out.toString();
    assertTrue(response.contains("Content-Type: text/css"));
  }
}
