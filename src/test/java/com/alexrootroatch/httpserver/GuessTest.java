package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.GameSession;
import com.alexrootroatch.httpserver.routes.Guess;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class GuessTest {
  private GameSession gameSession;

  @BeforeEach
  void setup() {
    gameSession = new GameSession();
  }

  @Test
  void startPage() {
    String response = TestHelper.serve(new Guess(gameSession), TestHelper.get("/guess"));
    String body = TestHelper.responseBody(response);

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
    String response = TestHelper.serve(new Guess(gameSession), TestHelper.get("/guess"));
    assertTrue(response.contains("Set-Cookie: session="));
    assertTrue(response.contains("HttpOnly"));
    assertFalse(response.contains("Set-Cookie: tries-left="));
  }

  @Test
  void doesNotSetSessionCookieIfRequestContainsCookie() {
    String sessionId = gameSession.createSession(50);
    byte[] body = "guess=50".getBytes();
    String response = TestHelper.serve(new Guess(gameSession),
        TestHelper.postWithCookie("/guess", body, "session", sessionId));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void decrementsTriesLeft() {
    String sessionId = gameSession.createSession(5);
    byte[] body = "guess=9".getBytes();
    String response = TestHelper.serve(new Guess(gameSession),
        TestHelper.postWithCookie("/guess", body, "session", sessionId));
    assertTrue(response.contains("6 tries left"));
    assertFalse(response.contains("Set-Cookie: session="));
    assertFalse(response.contains("Set-Cookie: tries-left="));
  }

  @Test
  void tooHigh() {
    String sessionId = gameSession.createSession(5);
    byte[] body = "guess=105".getBytes();
    String response = TestHelper.serve(new Guess(gameSession),
        TestHelper.postWithCookie("/guess", body, "session", sessionId));
    assertTrue(response.contains("<p>Too high!</p>"));
    assertTrue(response.contains("6 tries left"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void tooLow() {
    String sessionId = gameSession.createSession(5);
    byte[] body = "guess=-1".getBytes();
    String response = TestHelper.serve(new Guess(gameSession),
        TestHelper.postWithCookie("/guess", body, "session", sessionId));
    assertTrue(response.contains("<p>Too low!</p>"));
    assertTrue(response.contains("6 tries left"));
    assertFalse(response.contains("Set-Cookie: session="));
  }

  @Test
  void guessEquals() {
    String sessionId = gameSession.createSession(50);
    byte[] body = "guess=50".getBytes();
    String response = TestHelper.serve(new Guess(gameSession),
        TestHelper.postWithCookie("/guess", body, "session", sessionId));
    assertTrue(response.contains("<p>That's it! You win!</p>"));
    assertTrue(response.contains("<p>The number is 50.</p>"));
    assertFalse(response.contains("Set-Cookie: session="));
    assertFalse(response.contains("<button id=\"submit\">Submit</button>"));
    assertNull(gameSession.getNumber(sessionId));
  }

  @Test
  void sessionRemovedMidRequestStartsNewGame() {
    String sessionId = gameSession.createSession(50);
    // Simulate another thread removing the session between lookup and use
    gameSession.removeSession(sessionId);

    byte[] body = "guess=50".getBytes();
    String response = TestHelper.serve(new Guess(gameSession),
        TestHelper.postWithCookie("/guess", body, "session", sessionId));
    // Should start a new game, not crash or show stale data
    assertTrue(response.contains("Set-Cookie: session="));
    assertTrue(response.contains("7 tries left"));
  }

  @Test
  void nonNumericGuess() {
    String sessionId = gameSession.createSession(50);
    byte[] body = "guess=abc".getBytes();
    String response = TestHelper.serve(new Guess(gameSession),
        TestHelper.postWithCookie("/guess", body, "session", sessionId));
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("7 tries left"));
  }
}
