package MyServerTests;

import MyServer.MyServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static MyServerTests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.*;

public class BasicsTest {
  static MyServer server;
  static Socket socket;
  static OutputStream outputStream;

  @BeforeAll
  static void setup() throws IOException {
    server = new MyServer(1234, "testroot");
    server.start();

    socket = new Socket("127.0.0.1", 1234);
    outputStream = socket.getOutputStream();
  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1234);
    outputStream = socket.getOutputStream();
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
  static void teardown() throws IOException {
    server.stop();
    socket.close();
  }
}
