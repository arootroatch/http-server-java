package myservertests;

import myserver.MyServer;
import myserver.Route;
import myserver.routes.GameSession;
import myserver.routes.Guess;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class GuessTest {
  static MyServer server;
  Socket socket;
  OutputStream outputStream;
  static HashMap<String, Route> routes = new HashMap<>();

  @BeforeAll
  static void setup() {
    routes.put("/guess", new Guess());
    server = new MyServer(1239, "testroot", routes);
    server.start();
  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1239);
    outputStream = socket.getOutputStream();
  }

  @AfterEach
  void closeSocket() throws IOException {
    if (socket != null) socket.close();
  }

  @Test
  void startPage() throws IOException {
    outputStream.write(("GET /guess HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    String body = response.split("\r\n\r\n")[1];
    int i = body.length();

    assertEquals("<html>", body.substring(0, 6));
    assertTrue(response.contains("<h2>Guess the number!</h2>"));
    assertTrue(response.contains("<p>You have 7 tries left.</p>"));
    assertTrue(response.contains("<input name=\"guess\" placeholder=\"Enter your guess 1-100\">"));
    assertTrue(response.contains("<button id=\"submit\">Submit</button>"));
    assertTrue(response.contains("<form method=\"post\" action=\"/guess\">"));
    assertEquals("</form></html>\r\n", body.substring(i - 16));
  }

  @Test
  void setsCookie() throws IOException {
    outputStream.write(("GET /guess HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("Set-Cookie: session="));
    assertTrue(response.contains("HttpOnly"));
    assertTrue(response.contains("Set-Cookie: tries-left=7"));
  }

  @Test
  void doesNotSetSessionCookieIfRequestContainsCookie() throws IOException {
    String sessionId = GameSession.createSession(50);
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 8\r\n").getBytes());
    outputStream.write(("Cookie: session=" + sessionId + "; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=50").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void decrementsTriesLeftCookie() throws IOException {
    String sessionId = GameSession.createSession(5);
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 7\r\n").getBytes());
    outputStream.write(("Cookie: session=" + sessionId + "; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=9").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void tooHigh() throws IOException {
    String sessionId = GameSession.createSession(5);
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 9\r\n").getBytes());
    outputStream.write(("Cookie: session=" + sessionId + "; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=105").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<p>Too high!</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void tooLow() throws IOException {
    String sessionId = GameSession.createSession(5);
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 8\r\n").getBytes());
    outputStream.write(("Cookie: session=" + sessionId + "; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=-1").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<p>Too low!</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void guessEquals() throws IOException {
    String sessionId = GameSession.createSession(50);
    outputStream.write(("POST /guess HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 8\r\n").getBytes());
    outputStream.write(("Cookie: session=" + sessionId + "; tries-left=7\r\n\r\n").getBytes());
    outputStream.write(("guess=50").getBytes());
    outputStream.flush();
    String response = URLConnection.parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<p>That's it! You win!</p>"));
    assertTrue(response.contains("<p>The number is 50.</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=0"));
    assertFalse(response.contains("Set-Cookie: session="));
    assertFalse(response.contains("<button id=\"submit\">Submit</button>"));
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
