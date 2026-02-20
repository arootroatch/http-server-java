package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.Route;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.alexrootroatch.httpserver.routes.HttpResponse.sendString;

public class Ping implements Route {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String start = LocalDateTime.now().format(FORMATTER);

    String path = connData.request().path();
    String[] segments = path.split("/");
    int delay = 0;
    if (segments.length > 2) {
      try {
        delay = Integer.parseInt(segments[2]);
      } catch (NumberFormatException e) {
        delay = 0;
      }
    }
    if (delay < 0) delay = 0;
    if (delay > 30) delay = 30;

    try {
      Thread.sleep(delay * 1000L);
      String end = LocalDateTime.now().format(FORMATTER);
      sendString(renderPingHTML(start, end), "html", outputStream);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private String renderPingHTML(String start, String end) {
    return "<html>" +
        "<h2>Ping</h2>" +
        "<ul>" +
        "<li>start time: " + start + "</li>" +
        "<li>end time: " + end + "</li>" +
        "</ul>" +
        "</html>";
  }
}
