package myservertests;

import myserver.*;
import myserver.routes.Folder;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BasicsTest {

  @Test
  void startAndStop() {
    MyServer server = new MyServer(0, "testroot", new HashMap<>());
    server.start();
    assertTrue(server.isRunning());
    assertNotNull(server.getThread());
    assertTrue(server.getThread().isAlive());

    server.stop();
    assertFalse(server.isRunning());
    assertNull(server.getThread());
  }

  @Test
  void servesIndex() {
    HttpRequest request = new HttpRequest("GET", "/", "", Map.of(), new byte[0]);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RequestDispatcher.dispatch(request, "testroot", Map.of(), out);

    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h1>Hello, World!</h1>"));
  }

  @Test
  void servesIndexHTML() {
    HttpRequest request = new HttpRequest("GET", "/index.html", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new myserver.routes.File().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("Content-Type: text/html"));
    assertTrue(response.contains("<h1>Hello, World!</h1>"));
  }

  @Test
  void status404() {
    HttpRequest request = new HttpRequest("GET", "/blah", "", Map.of(), new byte[0]);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RequestDispatcher.dispatch(request, "testroot", Map.of(), out);

    String response = out.toString();
    assertTrue(response.contains("404 Not Found"));
  }

  @Test
  void serverHeader() {
    HttpRequest request = new HttpRequest("GET", "/", "", Map.of(), new byte[0]);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RequestDispatcher.dispatch(request, "testroot", Map.of(), out);

    String response = out.toString();
    assertTrue(response.contains("Server: My Server"));
  }
}
