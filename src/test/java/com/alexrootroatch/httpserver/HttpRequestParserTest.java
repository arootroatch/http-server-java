package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.HttpRequest;
import com.alexrootroatch.httpserver.HttpRequestParser;
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
    assertEquals(0, request.body().length);
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
    assertEquals("hello=1", request.bodyAsString());
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

  @Test
  void parseRequestWithBareCarriageReturn() {
    String raw = "GET /hello HTTP/1.1\r\rHost: localhost\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/hello", request.path());
  }

  @Test
  void parseRequestWithLFOnly() {
    String raw = "GET /hello HTTP/1.1\nHost: localhost\n\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("GET", request.method());
    assertEquals("/hello", request.path());
    assertEquals("localhost", request.header("Host"));
  }

  @Test
  void rejectsOversizedContentLength() {
    String raw = "POST /upload HTTP/1.1\r\nContent-Length: 20000000\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals(0, request.body().length);
  }

  @Test
  void invalidContentLength() {
    String raw = "POST /form HTTP/1.1\r\nContent-Length: abc\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals("", request.method());
  }

  @Test
  void negativeContentLength() {
    String raw = "POST /form HTTP/1.1\r\nContent-Length: -1\r\n\r\n";
    InputStream is = new ByteArrayInputStream(raw.getBytes());
    HttpRequest request = HttpRequestParser.parse(is);
    assertEquals(0, request.body().length);
  }

  @Test
  void parseGetWithEncodedPath() {
    String raw = "GET /hello%20world HTTP/1.1\r\n\r\n";
    HttpRequest request = HttpRequestParser.parse(new ByteArrayInputStream(raw.getBytes()));
    assertEquals("/hello world", request.path());
  }
}
