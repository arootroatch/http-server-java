package MyServer.Routes;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static MyServer.Response.sendFile;

public class Ping {
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

  public void ping() {
    try {
      Thread.sleep(delay * 1000L);
      sendFile(renderPingHTML(start), "html", this.outputStream);
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
