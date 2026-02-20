package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.HttpRequest;
import com.alexrootroatch.httpserver.Route;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.alexrootroatch.httpserver.routes.HttpResponse.sendString;

public class Guess implements Route {
  private final GameSession gameSession;

  public Guess(GameSession gameSession) {
    this.gameSession = gameSession;
  }

  @Override
  public void serve(ConnectionData connData, OutputStream outputStream) {
    HttpRequest request = connData.request();
    String method = request.method();
    String sessionId = request.cookieValue("session");

    GameSession.SessionData session = (!sessionId.isEmpty()) ? gameSession.getSession(sessionId) : null;
    boolean isNewGame = method.equals("GET") || session == null;

    Integer guessReceived = parseGuess(request);

    List<String> extraHeaders = new ArrayList<>();

    if (isNewGame) {
      int numberToGuess = ThreadLocalRandom.current().nextInt(1, 101);
      sessionId = gameSession.createSession(numberToGuess);
      extraHeaders.add("Set-Cookie: session=" + sessionId + "; HttpOnly");
      String html = renderGuessHTML(null, numberToGuess, GameSession.MAX_TRIES);
      sendString(html, "html", outputStream, extraHeaders);
      return;
    }

    // Existing session — process guess against the snapshot
    int numberToGuess = session.number();
    int triesLeft;

    if (guessReceived != null && guessReceived == numberToGuess) {
      triesLeft = 0;
      gameSession.removeSession(sessionId);
    } else if (guessReceived != null) {
      GameSession.SessionData updated = gameSession.decrementTries(sessionId);
      triesLeft = updated != null ? updated.triesLeft() : 0;
      if (triesLeft == 0) {
        gameSession.removeSession(sessionId);
      }
    } else {
      triesLeft = session.triesLeft();
    }

    String html = renderGuessHTML(guessReceived, numberToGuess, triesLeft);
    sendString(html, "html", outputStream, extraHeaders);
  }

  private Integer parseGuess(HttpRequest request) {
    if (!request.method().equals("POST") || request.body().length == 0) return null;
    String body = request.bodyAsString().trim();
    String[] parts = body.split("=", 2);
    if (parts.length == 2 && parts[0].equals("guess")) {
      try {
        return Integer.parseInt(parts[1].trim());
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  private String renderGuessHTML(Integer guessReceived, int numberToGuess, int triesLeft) {
    StringBuilder sb = new StringBuilder();

    sb.append("<html>").append("<h2>Guess the number!</h2>");

    if (guessReceived != null) {
      if (guessReceived > numberToGuess) sb.append("<p>Too high!</p>");
      else if (guessReceived < numberToGuess) sb.append("<p>Too low!</p>");
      else sb.append("<p>That's it! You win!</p>")
          .append("<p>The number is ").append(numberToGuess).append(".</p>");
    }
    if (triesLeft > 0) sb.append("<p>You have ").append(triesLeft).append(" tries left.</p>");
    if ((triesLeft == 0) && (guessReceived != null) && (numberToGuess != guessReceived))
      sb.append("<p>You lose! Please try again.</p>");

    sb.append("<form method=\"post\" action=\"/guess\">")
        .append("<input name=\"guess\" placeholder=\"Enter your guess 1-100\">");

    if (triesLeft > 0) sb.append("<button id=\"submit\">Submit</button>");
    sb.append("</form></html>");

    return sb.toString();
  }
}
