package MyServerTests;

import MyServer.MyServer;
import MyServer.Route;
import MyServer.routes.Guess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class GuessTest {
  static MyServer server;
  static Socket socket;
  static OutputStream outputStream;
  static HashMap<String, Route> routes = new HashMap<>();

  @BeforeAll
  static void setup() {
    routes.put("/guess", new Guess());
    server = new MyServer(1235, "testroot", routes);
    server.start();
  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1235);
    outputStream = socket.getOutputStream();
  }

  @Test
  void startPage() throws IOException {
    outputStream.write(("GET /guess HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    String body = response.split("\r\n\r\n")[1];
    int i = body.length();

    assertEquals( "<html>", body.substring(0, 6));
    assertTrue(response.contains("<h2>Guess the number!</h2>"));
    assertTrue(response.contains("<p>You have 7 tries left.</p>"));
    assertTrue(response.contains("<input name=\"guess\" placeholder=\"Enter your guess 1-100\">"));
    assertTrue(response.contains("<button id=\"submit\">Submit</button>"));
    assertTrue(response.contains("<form method=\"post\" action=\"/guess\">"));
    assertEquals( "</form></html>\r\n", body.substring(i-16));
  }

  @Test
  void setsCookie() throws IOException {
    outputStream.write(("GET /guess HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("Set-Cookie: number="));
    assertTrue(response.contains("Set-Cookie: tries-left=7"));
  }

  @Test
  void doesNotSetGameIDCookieIfRequestContainsCookie() throws IOException {
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 8\r\n").getBytes());
    outputStream.write(("Cookie: number=50; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=50\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertFalse(response.contains("Set-Cookie: number="));
  }

  @Test
  void decrementsTriesLeftCookie() throws IOException {
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 7\r\n").getBytes());
    outputStream.write(("Cookie: number=5; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=9\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: number="));
  }

  @Test
  void tooHigh() throws IOException {
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 9\r\n").getBytes());
    outputStream.write(("Cookie: number=5; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=105\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<p>Too high!</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: number="));
  }

  @Test
  void tooLow() throws IOException {
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 8\r\n").getBytes());
    outputStream.write(("Cookie: number=5; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=-1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<p>Too low!</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: number="));
  }

  @Test
  void equals() throws IOException {
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 8\r\n").getBytes());
    outputStream.write(("Cookie: number=50; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=50\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<p>That's it! You win!</p>"));
    assertTrue(response.contains("<p>The number is 50.</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=0"));
    assertFalse(response.contains("Set-Cookie: number="));
    assertFalse(response.contains("<button id=\"submit\">Submit</button>"));
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
