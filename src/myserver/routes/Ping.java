package myserver.routes;

import myserver.ConnectionData;
import myserver.Route;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static myserver.routes.Utils.sendString;

public class Ping implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String pattern = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String start = sdf.format(new Date());

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
      String end = sdf.format(new Date());
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
