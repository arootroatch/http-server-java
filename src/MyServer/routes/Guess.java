package MyServer.routes;

import MyServer.Route;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class Guess implements Route {
  String resource;
  String request;
  String html;
  OutputStream outputStream;
  Boolean needsNewNumber;
  int triesLeft;
  int numberToGuess;
  Integer guessReceived;

  public Guess() {
  }

  @Override
  public void serve(HashMap<String, String> connData, OutputStream outputStream) {
    resource = connData.get("resource");
    request = connData.get("request");
    this.outputStream = outputStream;
    needsNewNumber = !request.contains("Cookie: number=") || request.contains("GET");
    guessReceived = request.contains("guess=") ?
        Integer.parseInt(request.split("\r\n\r\n")[1].split("=")[1]) : null;
    numberToGuess = setNumberToGuess();
    triesLeft = setTriesLeft();
    html = renderGuessHTML();

    writeToOutputStream();
  }

  private String renderGuessHTML() {
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

  private void writeToOutputStream() {
    try {
      byte[] fileBytes = html.getBytes();
      int byteCount = fileBytes.length;
      outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
      outputStream.write(("text/html\r\n").getBytes());
      if (needsNewNumber) outputStream.write(("Set-Cookie: number=" + numberToGuess + "\r\n").getBytes());
      outputStream.write(("Set-Cookie: tries-left=" + triesLeft + "\r\n").getBytes());
      outputStream.write(("Content-Length: " + byteCount + "\r\n").getBytes());
      outputStream.write(("Server: My MacBook Pro\r\n\r\n").getBytes());
      outputStream.write(fileBytes);
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private int setTriesLeft() {
    if (request.contains("GET")) return 7;
    else if ((guessReceived != null) && (guessReceived == numberToGuess)) return 0;
    else if (request.contains("tries-left")) {
      String cookieHeader = Arrays.stream(request.split("\r\n"))
          .filter(header -> header.contains("Cookie:")).toArray()[0].toString();

      String triesCookie = Arrays.stream(cookieHeader.split(" "))
          .filter(cookie -> cookie.contains("tries-left")).toArray()[0].toString();

      return Integer.parseInt(triesCookie.split("[=;]")[1]) - 1;
    } else return 7;
  }

  private int setNumberToGuess() {
    if (request.contains("GET")) return (int) Math.floor(Math.random() * 100) + 1;
    if (request.contains("Cookie: number")) {
      String cookieHeader = Arrays.stream(request.split("\r\n"))
          .filter(header -> header.contains("Cookie: number")).toArray()[0].toString();

      String numberCookie = Arrays.stream(cookieHeader.split(" "))
          .filter(cookie -> cookie.contains("number")).toArray()[0].toString();

      return Integer.parseInt(numberCookie.split("[=;]")[1]);
    } else return (int) Math.floor(Math.random() * 100) + 1;
  }
}
