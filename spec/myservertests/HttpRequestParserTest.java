package myservertests;

import myserver.HttpRequest;
import myserver.HttpRequestParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestParserTest {

  @Test
  void parseGetRequest() {
    String raw = "GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/hello", request.path());
    assertEquals("", request.queryString());
    assertEquals("localhost", request.header("Host"));
    assertEquals("", request.body());
  }

  @Test
  void parseGetWithQueryString() {
    String raw = "GET /form?foo=1&bar=2 HTTP/1.1\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/form", request.path());
    assertEquals("foo=1&bar=2", request.queryString());
  }

  @Test
  void parsePostRequest() {
    String raw = "POST /form HTTP/1.1\r\nContent-Length: 7\r\n\r\nhello=1";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("POST", request.method());
    assertEquals("/form", request.path());
    assertEquals("hello=1", request.body());
  }

  @Test
  void parseEmptyRequest() {
    String raw = "";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("", request.method());
  }

  @Test
  void parseCookies() {
    String raw = "GET /guess HTTP/1.1\r\nCookie: session=abc123; tries-left=5\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("abc123", request.cookieValue("session"));
    assertEquals("5", request.cookieValue("tries-left"));
  }

  @Test
  void parseMultipleHeaders() {
    String raw = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: text/html\r\nConnection: keep-alive\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("localhost", request.header("Host"));
    assertEquals("text/html", request.header("Accept"));
    assertEquals("keep-alive", request.header("Connection"));
  }

  @Test
  void missingCookieReturnsEmpty() {
    String raw = "GET / HTTP/1.1\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("", request.cookieValue("session"));
  }
}
