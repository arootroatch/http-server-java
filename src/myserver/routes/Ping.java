package myserver.routes;

import myserver.ConnectionData;
import myserver.Route;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static myserver.routes.Utils.sendHtmlString;

public class Ping implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String pattern = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String start = sdf.format(new Date());

    String path = connData.request().path();
    String[] segments = path.split("/");
    int delay = segments.length > 2 ? Integer.parseInt(segments[2]) : 0;

    try {
      Thread.sleep(delay * 1000L);
      String end = sdf.format(new Date());
      sendHtmlString(renderPingHTML(start, end), "html", outputStream);
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
