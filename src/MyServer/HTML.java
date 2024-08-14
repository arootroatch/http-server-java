package MyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class HTML {
  public static String[] getQueryParams(String resource) {
    String[] split = resource.split("[?&]");
    String[] params = new String[split.length - 1];
    System.arraycopy(split, 1, params, 0, params.length);
    return params;
  }

  public static String queryParamsToHTML(String[] params) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (String s : params) {
      String name = s.split("=")[0];
      String value = s.split("=")[1];
      String li = String.format("<li>%s: %s</li>", name, value);
      html.append(li);
    }
    html.append("</ul>\r\n");
    return html.toString();
  }

  public static String parseHTML(InputStream inputStream) throws IOException {
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder html = new StringBuilder();
    String line = br.readLine();

    while (line != null) {
      html.append(line).append("\r\n");
      line = br.readLine();
    }
    return html.toString();
  }

  public static String renderPingHTML(Date start) {
    Date end = new Date();

    return "<html>" +
        "<h2>Ping</h2>" +
        "<ul>" +
        "<li>start time: " + start + "</li>" +
        "<li>end time: " + end + "</li>" +
        "</ul>" +
        "</html>";
  }

  public static String postRequestHTML(String request, Integer contentLength){
    String[] fileInfo = request.substring(0, 130).split("\r\n")[1].split(";");
    String fileName = fileInfo[2].split("=")[1];
    return "<ul>" +
        "<li>file name: " + fileName + "</li>" +
        "<li>content type: application/octet-stream</li>" +
        "<li>file size: " + contentLength + "</li>" +
        "</ul>";
  }
}
