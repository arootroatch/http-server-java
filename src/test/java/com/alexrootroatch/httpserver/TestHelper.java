package com.alexrootroatch.httpserver;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class TestHelper {
  public static final String TESTROOT = "testroot";

  public static HttpRequest get(String path) {
    return new HttpRequest("GET", path, "", Map.of(), new byte[0]);
  }

  public static HttpRequest getWithQuery(String path, String queryString) {
    return new HttpRequest("GET", path, queryString, Map.of(), new byte[0]);
  }

  public static HttpRequest post(String path, byte[] body) {
    return new HttpRequest("POST", path, "",
        Map.of("Content-Length", String.valueOf(body.length)), body);
  }

  public static HttpRequest postWithCookie(String path, byte[] body, String cookieName, String cookieValue) {
    return new HttpRequest("POST", path, "",
        Map.of("Cookie", cookieName + "=" + cookieValue,
            "Content-Length", String.valueOf(body.length)), body);
  }

  public static String serve(Route route, HttpRequest request) {
    return serve(route, request, TESTROOT);
  }

  public static String serve(Route route, HttpRequest request, String rootDir) {
    ConnectionData connData = new ConnectionData(request, rootDir);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    route.serve(connData, out);
    return out.toString();
  }

  public static String dispatch(RequestDispatcher dispatcher, HttpRequest request) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    dispatcher.dispatch(request, out);
    return out.toString();
  }

  public static String responseBody(String fullResponse) {
    String[] parts = fullResponse.split("\r\n\r\n", 2);
    return parts.length > 1 ? parts[1] : "";
  }

  public static String responseHeaders(String fullResponse) {
    String[] parts = fullResponse.split("\r\n\r\n", 2);
    return parts[0];
  }
}
