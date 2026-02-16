package myservertests;

import myserver.MyServer;
import myserver.Route;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import static myservertests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.*;

public class BasicsTest {
  static MyServer server;
  Socket socket;
  OutputStream outputStream;
  static HashMap<String, Route> routes = new HashMap<>();

  @BeforeAll
  static void setup() {
    server = new MyServer(1234, "testroot", routes);
    server.start();
  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1234);
    outputStream = socket.getOutputStream();
  }

  @AfterEach
  void closeSocket() throws IOException {
    if (socket != null) socket.close();
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
    outputStream.write(("GET / HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    InputStream inputStream = socket.getInputStream();

    String response = parseInputStream(inputStream);
    assertTrue(response.contains("<h1>Hello, World!</h1>"));
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void servesIndexHTML() throws IOException {
    outputStream.write(("GET /index.html HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());

    assertTrue(response.contains("<h1>Hello, World!</h1>"));
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void status404() throws IOException {
    outputStream.write(("GET /blah HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());

    assertTrue(response.contains("404 Not Found"));
  }

  @Test
  void serverHeader() throws IOException {
    outputStream.write(("GET / HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    assertTrue(response.contains("Server: My MacBook Pro"));
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
