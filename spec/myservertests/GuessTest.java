package myservertests;

import myserver.ConnectionData;
import myserver.HttpRequest;
import myserver.routes.GameSession;
import myserver.routes.Guess;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GuessTest {

  @Test
  void startPage() {
    HttpRequest request = new HttpRequest("GET", "/guess", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Guess().serve(connData, out);

    String response = out.toString();
    String body = response.split("\r\n\r\n")[1];

    assertTrue(body.startsWith("<html>"));
    assertTrue(response.contains("<h2>Guess the number!</h2>"));
    assertTrue(response.contains("<p>You have 7 tries left.</p>"));
    assertTrue(response.contains("<input name=\"guess\" placeholder=\"Enter your guess 1-100\">"));
    assertTrue(response.contains("<button id=\"submit\">Submit</button>"));
    assertTrue(response.contains("<form method=\"post\" action=\"/guess\">"));
    assertTrue(body.endsWith("</form></html>"));
  }

  @Test
  void setsCookie() {
    HttpRequest request = new HttpRequest("GET", "/guess", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Guess().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("Set-Cookie: session="));
    assertTrue(response.contains("HttpOnly"));
    assertTrue(response.contains("Set-Cookie: tries-left=7"));
  }

  @Test
  void doesNotSetSessionCookieIfRequestContainsCookie() {
    String sessionId = GameSession.createSession(50);
    byte[] body = "guess=50".getBytes();
    HttpRequest request = new HttpRequest("POST", "/guess", "",
        Map.of("Cookie", "session=" + sessionId + "; tries-left=7",
            "Content-Length", String.valueOf(body.length)), body);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Guess().serve(connData, out);

    String response = out.toString();
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void decrementsTriesLeftCookie() {
    String sessionId = GameSession.createSession(5);
    byte[] body = "guess=9".getBytes();
    HttpRequest request = new HttpRequest("POST", "/guess", "",
        Map.of("Cookie", "session=" + sessionId + "; tries-left=7",
            "Content-Length", String.valueOf(body.length)), body);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Guess().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void tooHigh() {
    String sessionId = GameSession.createSession(5);
    byte[] body = "guess=105".getBytes();
    HttpRequest request = new HttpRequest("POST", "/guess", "",
        Map.of("Cookie", "session=" + sessionId + "; tries-left=7",
            "Content-Length", String.valueOf(body.length)), body);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Guess().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("<p>Too high!</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void tooLow() {
    String sessionId = GameSession.createSession(5);
    byte[] body = "guess=-1".getBytes();
    HttpRequest request = new HttpRequest("POST", "/guess", "",
        Map.of("Cookie", "session=" + sessionId + "; tries-left=7",
            "Content-Length", String.valueOf(body.length)), body);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Guess().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("<p>Too low!</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=6"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void guessEquals() {
    String sessionId = GameSession.createSession(50);
    byte[] body = "guess=50".getBytes();
    HttpRequest request = new HttpRequest("POST", "/guess", "",
        Map.of("Cookie", "session=" + sessionId + "; tries-left=7",
            "Content-Length", String.valueOf(body.length)), body);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Guess().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("<p>That's it! You win!</p>"));
    assertTrue(response.contains("<p>The number is 50.</p>"));
    assertTrue(response.contains("Set-Cookie: tries-left=0"));
    assertFalse(response.contains("Set-Cookie: session="));
    assertFalse(response.contains("<button id=\"submit\">Submit</button>"));
  }
}
