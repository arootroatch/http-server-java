package com.alexrootroatch.httpserver;

import org.junit.jupiter.api.Test;

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
    assertTrue(RequestDispatcher.isPathSafe(TestHelper.TESTROOT, "/index.html"));
    assertTrue(RequestDispatcher.isPathSafe(TestHelper.TESTROOT, "/img/autobot.jpg"));
  }

  @Test
  void pathSafeTraversal() {
    assertFalse(RequestDispatcher.isPathSafe(TestHelper.TESTROOT, "/../../etc/passwd"));
    assertFalse(RequestDispatcher.isPathSafe(TestHelper.TESTROOT, "/../etc/passwd"));
  }

  @Test
  void pathSafeRoot() {
    assertTrue(RequestDispatcher.isPathSafe(TestHelper.TESTROOT, "/"));
  }

  @Test
  void rejectsUnrecognizedMethod() {
    HttpRequest request = new HttpRequest("HACK", "/", "", Map.of(), new byte[0]);
    String response = TestHelper.dispatch(
        new RequestDispatcher(TestHelper.TESTROOT, Map.of()), request);
    assertTrue(response.contains("405 Method Not Allowed"));
  }

  @Test
  void acceptsGetMethod() {
    String response = TestHelper.dispatch(
        new RequestDispatcher(TestHelper.TESTROOT, Map.of()),
        TestHelper.get("/"));
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void acceptsPostMethod() {
    HttpRequest request = new HttpRequest("POST", "/", "", Map.of(), new byte[0]);
    String response = TestHelper.dispatch(
        new RequestDispatcher(TestHelper.TESTROOT, Map.of()), request);
    assertFalse(response.contains("405"));
  }
}
