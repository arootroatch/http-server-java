package myservertests;

import myserver.RequestDispatcher;
import org.junit.jupiter.api.Test;

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
}
