package MyServer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

  public static String renderPingHTML(String start) {
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

  public static String postRequestHTML(String request) {
    String[] multiparts = request.split("\r\n\r\n");
    String[] metadata = multiparts[1].split("\r\n");
    int contentLength = Integer.parseInt(Arrays.stream(request.split("\r\n"))
        .filter(s -> s.contains("Content-Length")).toArray()[0].toString().split(":")[1].trim());
    String[] endOfInput = multiparts[2].split("\r\n");
    int footer = endOfInput[endOfInput.length - 1].getBytes().length;
    String fileName = metadata[1].split(";")[2].split("=")[1].split("\"")[1];
    String contentType = metadata[2].split(": ")[1];
    int fileSize = contentLength - footer - multiparts[1].trim().getBytes().length - 8;

    return "<ul>" +
        "<li>file name: " + fileName + "</li>" +
        "<li>content type: " + contentType + "</li>" +
        "<li>file size: " + fileSize + "</li>" +
        "</ul>";
  }
}

