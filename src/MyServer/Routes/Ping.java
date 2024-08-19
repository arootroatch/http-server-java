package MyServer.Routes;

import MyServer.Route;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ping implements Route {
  boolean delayed;
  int delay;
  String pattern = "yyyy-MM-dd HH:mm:ss";
  SimpleDateFormat simpleDateFormat;
  String start;
  OutputStream outputStream;

  public Ping(String resource, OutputStream outputStream) {
    this.delayed = resource.split("/").length > 2;
    this.delay = delayed ? Integer.parseInt(resource.split("/")[2]) : 0;
    this.simpleDateFormat = new SimpleDateFormat(this.pattern);
    this.start = simpleDateFormat.format(new Date());
    this.outputStream = outputStream;
  }

  public void serve() {
    try {
      Thread.sleep(delay * 1000L);
      sendHtmlString(renderPingHTML(start), "html", outputStream);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private String renderPingHTML(String start) {
    String pattern = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    String endDate = simpleDateFormat.format(new Date());

    return "<html>" +
        "<h2>Ping</h2>" +
        "<ul>" +
        "<li>start time: " + start + "</li>" +
        "<li>end time: " + endDate + "</li>" +
        "</ul>" +
        "</html>";
  }
}
