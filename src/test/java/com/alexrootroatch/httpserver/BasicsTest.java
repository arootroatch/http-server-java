package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BasicsTest {

  @Test
  void startAndStop() {
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
    MyServer server = new MyServer(0, "testroot", new HashMap<>());
    server.start();
    assertTrue(server.isRunning());
    assertNotNull(server.getThread());
    assertTrue(server.getThread().isAlive());

    server.stop();
    System.setOut(originalOut);
    assertFalse(server.isRunning());
    assertNull(server.getThread());
  }

  @Test
  void startFailsOnInvalidPort() {
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
    MyServer server = new MyServer(-1, "testroot", new HashMap<>());
    try {
      assertThrows(RuntimeException.class, () -> server.start());
      assertFalse(server.isRunning());
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  void servesIndex() {
    String response = TestHelper.dispatch(
        new RequestDispatcher(TestHelper.TESTROOT, Map.of()),
        TestHelper.get("/"));
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h1>Hello, World!</h1>"));
  }

  @Test
  void servesIndexHTML() {
    String response = TestHelper.serve(
        new com.alexrootroatch.httpserver.routes.StaticFile(),
        TestHelper.get("/index.html"));
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("Content-Type: text/html"));
    assertTrue(response.contains("<h1>Hello, World!</h1>"));
  }

  @Test
  void status404() {
    String response = TestHelper.dispatch(
        new RequestDispatcher(TestHelper.TESTROOT, Map.of()),
        TestHelper.get("/blah"));
    assertTrue(response.contains("404 Not Found"));
  }

  @Test
  void serverHeader() {
    String response = TestHelper.dispatch(
        new RequestDispatcher(TestHelper.TESTROOT, Map.of()),
        TestHelper.get("/"));
    assertTrue(response.contains("Server: My Server"));
  }
}
