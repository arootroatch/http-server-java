package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.HttpRequest;
import com.alexrootroatch.httpserver.Route;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

    GameSession.SessionData session = gameSession.getSession(sessionId);
    boolean isNewGame = method.equals("GET") || sessionId.isEmpty() || session == null;

    Integer guessReceived = null;
    if (method.equals("POST") && request.body().length > 0) {
      String body = request.bodyAsString().trim();
      String[] parts = body.split("=", 2);
      if (parts.length == 2 && parts[0].equals("guess")) {
        try {
          guessReceived = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
          guessReceived = null;
        }
      }
    }

    int numberToGuess;
    if (isNewGame) {
      numberToGuess = (int) Math.floor(Math.random() * 100) + 1;
      sessionId = gameSession.createSession(numberToGuess);
    } else {
      numberToGuess = session.number();
    }

    int triesLeft;
    if (isNewGame) {
      triesLeft = gameSession.getTriesLeft(sessionId);
    } else if (guessReceived != null && guessReceived == numberToGuess) {
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

    List<String> extraHeaders = new ArrayList<>();
    if (isNewGame) {
      extraHeaders.add("Set-Cookie: session=" + sessionId + "; HttpOnly");
    }

    sendString(html, "html", outputStream, extraHeaders);
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
