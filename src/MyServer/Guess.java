package MyServer;

import java.util.Arrays;

public class Guess {
  private final int n;

  public Guess() {
//    this.n = (int) Math.floor(Math.random() * 100) + 1;
    this.n = 37;
  }

  public String renderGuessHTML(String[] queryParams) {
    StringBuilder html = new StringBuilder();
    int triesLeft = queryParams.length > 0 ? Integer.parseInt(queryParams[0].split("=")[1]) : 7;
    Integer guess = queryParams.length > 0 ? Integer.parseInt(queryParams[1].split("=")[1]) : null;

    html.append("<html>" +
        "<h2>Guess the number!<h2>");

    if (guess != null) {
      if (guess > this.n)
        html.append("<p> Too high!");
      else if (guess < this.n) {
        html.append("<p> Too low!");
      } else html.append("<p>That's it! You win!</p>");
    }

    html.append("<p>You have ").append(triesLeft).append(" tries left.</p>");

    if (triesLeft == 0) html.append("<p>The number is ").append(this.n).append("</p>");

    html.append("<form method=\"get\" action=\"/guess\">")
        .append("<input type=\"hidden\" name=\"triesLeft\" value=\"").append(triesLeft - 1).append("\"/>")
        .append(" <input type=\"text\" name=\"guess\"/>");

    if (triesLeft > 0) html.append("<input type=\"submit\" value=\"Submit\"/>");

    html.append("</form></html>");

    return html.toString();
  }
}
