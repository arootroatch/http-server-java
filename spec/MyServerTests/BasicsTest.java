package MyServerTests;

import MyServer.MyServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;

import static MyServerTests.URLConnection.connectToURL;
import static MyServerTests.URLConnection.parseResponse;
import static org.junit.jupiter.api.Assertions.*;

public class BasicsTest {
  static MyServer server;

  @BeforeAll
  static void setup() {
    server = new MyServer(1234, "testroot");
    server.start();
  }

  @Test
  void start() {
    server.stop();
    assertFalse(server.isRunning());
    assertNull(server.getThread());
    server.start();
    assertTrue(server.isRunning());
    assertNotNull(server.getThread());
    assertTrue(server.getThread().isAlive());
  }

  @Test
  void servesIndex() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234");
    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertEquals(200, responseCode);
  }

  @Test
  void servesIndexSlash() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234/");
    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertEquals(200, responseCode);
  }

  @Test
  void servesIndexHTML() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234/index.html");
    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertEquals(200, responseCode);
  }

  @Test
  void status404() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234/blah");
//    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

//    assertTrue(response.toString().contains("<h1>Error 404: Not Found</h1>"));
    assertEquals(404, responseCode);
  }

  @Test
  void serverHeader() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234/");
    String header = connection.getHeaderField("Server");
    assertNotNull(header);
    assertEquals("My MacBook Pro", header);
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
