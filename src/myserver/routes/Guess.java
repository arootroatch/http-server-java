package myserver.routes;

import myserver.ConnectionData;
import myserver.HttpRequest;
import myserver.Route;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static myserver.routes.Utils.sendHtmlString;

public class Guess implements Route {

  @Override
  public void serve(ConnectionData connData, OutputStream outputStream) {
    HttpRequest request = connData.request();
    String method = request.method();
    String sessionId = request.cookieValue("session");

    boolean isNewGame = method.equals("GET") || sessionId.isEmpty()
        || GameSession.getNumber(sessionId) == null;

    Integer guessReceived = null;
    if (method.equals("POST") && request.body().length > 0) {
      String body = request.bodyAsString().trim();
      String[] parts = body.split("=", 2);
      if (parts.length == 2 && parts[0].equals("guess")) {
        guessReceived = Integer.parseInt(parts[1].trim());
      }
    }

    int numberToGuess;
    if (isNewGame) {
      numberToGuess = (int) Math.floor(Math.random() * 100) + 1;
      sessionId = GameSession.createSession(numberToGuess);
    } else {
      numberToGuess = GameSession.getNumber(sessionId);
    }

    int triesLeft;
    if (isNewGame) {
      triesLeft = 7;
    } else if (guessReceived != null && guessReceived == numberToGuess) {
      triesLeft = 0;
    } else {
      String triesCookie = request.cookieValue("tries-left");
      triesLeft = triesCookie.isEmpty() ? 7 : Integer.parseInt(triesCookie) - 1;
    }

    String html = renderGuessHTML(guessReceived, numberToGuess, triesLeft);

    List<String> extraHeaders = new ArrayList<>();
    if (isNewGame) {
      extraHeaders.add("Set-Cookie: session=" + sessionId + "; HttpOnly");
    }
    extraHeaders.add("Set-Cookie: tries-left=" + triesLeft);

    sendHtmlString(html, "html", outputStream, extraHeaders);
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
