package myservertests;

import myserver.HttpRequest;
import myserver.RequestDispatcher;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RequestDispatcherTest {

  @Test
  void extractRouteKeySimplePath() {
    assertEquals("/form", RequestDispatcher.extractRouteKey("/form"));
  }

  @Test
  void extractRouteKeyNestedPath() {
    assertEquals("/listing", RequestDispatcher.extractRouteKey("/listing/img"));
  }

  @Test
  void extractRouteKeyRoot() {
    assertEquals("/", RequestDispatcher.extractRouteKey("/"));
  }

  @Test
  void extractRouteKeyEmpty() {
    assertEquals("/", RequestDispatcher.extractRouteKey(""));
  }

  @Test
  void extractRouteKeyFilePath() {
    assertEquals("/img", RequestDispatcher.extractRouteKey("/img/autobot.jpg"));
  }

  @Test
  void pathSafeValidPaths() {
    assertTrue(RequestDispatcher.isPathSafe("testroot", "/index.html"));
    assertTrue(RequestDispatcher.isPathSafe("testroot", "/img/autobot.jpg"));
  }

  @Test
  void pathSafeTraversal() {
    assertFalse(RequestDispatcher.isPathSafe("testroot", "/../../etc/passwd"));
    assertFalse(RequestDispatcher.isPathSafe("testroot", "/../etc/passwd"));
  }

  @Test
  void pathSafeRoot() {
    assertTrue(RequestDispatcher.isPathSafe("testroot", "/"));
  }

  @Test
  void rejectsUnrecognizedMethod() {
    HttpRequest request = new HttpRequest("HACK", "/", "", Map.of(), new byte[0]);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RequestDispatcher("testroot", Map.of()).dispatch(request, out);
    String response = out.toString();
    assertTrue(response.contains("405 Method Not Allowed"));
  }

  @Test
  void acceptsGetMethod() {
    HttpRequest request = new HttpRequest("GET", "/", "", Map.of(), new byte[0]);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RequestDispatcher("testroot", Map.of()).dispatch(request, out);
    String response = out.toString();
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void acceptsPostMethod() {
    HttpRequest request = new HttpRequest("POST", "/", "", Map.of(), new byte[0]);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RequestDispatcher("testroot", Map.of()).dispatch(request, out);
    String response = out.toString();
    assertFalse(response.contains("405"));
  }
}
