package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.HttpRequest;
import com.alexrootroatch.httpserver.HttpRequestParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestParserTest {

  @Test
  void parseGetRequest() throws Exception {
    String raw = "GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/hello", request.path());
    assertEquals("", request.queryString());
    assertEquals("localhost", request.header("Host"));
    assertEquals(0, request.body().length);
  }

  @Test
  void parseGetWithQueryString() throws Exception {
    String raw = "GET /form?foo=1&bar=2 HTTP/1.1\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/form", request.path());
    assertEquals("foo=1&bar=2", request.queryString());
  }

  @Test
  void parsePostRequest() throws Exception {
    String raw = "POST /form HTTP/1.1\r\nContent-Length: 7\r\n\r\nhello=1";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("POST", request.method());
    assertEquals("/form", request.path());
    assertEquals("hello=1", request.bodyAsString());
  }

  @Test
  void parseEmptyRequest() throws Exception {
    String raw = "";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("", request.method());
  }

  @Test
  void parseCookies() throws Exception {
    String raw = "GET /guess HTTP/1.1\r\nCookie: session=abc123; tries-left=5\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("abc123", request.cookieValue("session"));
    assertEquals("5", request.cookieValue("tries-left"));
  }

  @Test
  void parseMultipleHeaders() throws Exception {
    String raw = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: text/html\r\nConnection: keep-alive\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("localhost", request.header("Host"));
    assertEquals("text/html", request.header("Accept"));
    assertEquals("keep-alive", request.header("Connection"));
  }

  @Test
  void missingCookieReturnsEmpty() throws Exception {
    String raw = "GET / HTTP/1.1\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("", request.cookieValue("session"));
  }

  @Test
  void parseRequestWithBareCarriageReturn() throws Exception {
    String raw = "GET /hello HTTP/1.1\r\rHost: localhost\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/hello", request.path());
  }

  @Test
  void parseRequestWithLFOnly() throws Exception {
    String raw = "GET /hello HTTP/1.1\nHost: localhost\n\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/hello", request.path());
    assertEquals("localhost", request.header("Host"));
  }

  @Test
  void rejectsOversizedContentLength() throws Exception {
    String raw = "POST /upload HTTP/1.1\r\nContent-Length: 20000000\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals(0, request.body().length);
  }

  @Test
  void throwsOnInvalidContentLength() {
    String raw = "POST /form HTTP/1.1\r\nContent-Length: abc\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    MalformedRequestException ex = assertThrows(MalformedRequestException.class,
        () -> HttpRequestParser.parse(is));
    assertTrue(ex.getMessage().contains("Invalid Content-Length"));
  }

  @Test
  void throwsOnNegativeContentLength() {
    String raw = "POST /form HTTP/1.1\r\nContent-Length: -5\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    MalformedRequestException ex = assertThrows(MalformedRequestException.class,
        () -> HttpRequestParser.parse(is));
    assertTrue(ex.getMessage().contains("Negative Content-Length"));
  }

  @Test
  void parseGetWithEncodedPath() throws Exception {
    String raw = "GET /hello%20world HTTP/1.1\r\n\r\n";
    HttpRequest request = HttpRequestParser.parse(new ByteArrayInputStream(raw.getBytes()));
    assertEquals("/hello world", request.path());
  }
}
