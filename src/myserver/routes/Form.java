package myserver.routes;

import myserver.ConnectionData;
import myserver.HttpRequest;
import myserver.Route;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static myserver.routes.Utils.send404;
import static myserver.routes.Utils.sendString;

public class Form implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    HttpRequest request = connData.request();
    String rootDir = connData.rootDir();
    String queryString = request.queryString();

    String html = readFormsHtml(rootDir, outputStream);
    if (html == null) return;

    if (request.method().equals("POST")) {
      String addHTML = postRequestHTML(request);
      String newHTML = html.split("</html>")[0] + addHTML + "</html>";
      sendString(newHTML, "html", outputStream);
    } else if (!queryString.isEmpty()) {
      String addHTML = queryParamsToHTML(getQueryParams(queryString));
      String newHTML = html.split("</html>")[0] + addHTML + "</html>";
      sendString(newHTML, "html", outputStream);
    } else {
      sendString(html, "html", outputStream);
    }
  }

  private String readFormsHtml(String rootDir, OutputStream outputStream) {
    try (FileInputStream fis = new FileInputStream(rootDir + "/forms.html");
         InputStreamReader isr = new InputStreamReader(fis);
         BufferedReader br = new BufferedReader(isr)) {
      StringBuilder html = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        html.append(line).append("\r\n");
      }
      return html.toString();
    } catch (FileNotFoundException ex) {
      send404(outputStream);
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String[] getQueryParams(String queryString) {
    if (queryString == null || queryString.isEmpty()) return new String[0];
    return queryString.split("&");
  }

  public static String queryParamsToHTML(String[] params) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (String s : params) {
      String name = Utils.escapeHtml(s.split("=")[0]);
      String value = Utils.escapeHtml(s.split("=")[1]);
      String li = String.format("<li>%s: %s</li>", name, value);
      html.append(li);
    }
    html.append("</ul>\r\n");
    return html.toString();
  }

  private String postRequestHTML(HttpRequest request) {
    byte[] body = request.body();
    String metadata = extractMetadata(body);
    if (metadata.isEmpty()) return "<p>Invalid upload</p>";
    String fileName = getFileName(metadata);
    String contentType = getContentType(metadata);
    int fileSize = getFileSize(body);

    return "<ul>" +
        "<li>file name: " + Utils.escapeHtml(fileName) + "</li>" +
        "<li>content type: " + Utils.escapeHtml(contentType) + "</li>" +
        "<li>file size: " + fileSize + "</li>" +
        "</ul>";
  }

  private String extractMetadata(byte[] body) {
    int pos = findDoubleCRLF(body);
    if (pos == -1) return "";
    return new String(body, 0, pos, StandardCharsets.US_ASCII);
  }

  private String getFileName(String metadata) {
    try {
      String[] lines = metadata.split("\r\n");
      return lines[1].split(";")[2].split("=")[1].split("\"")[1];
    } catch (ArrayIndexOutOfBoundsException e) {
      return "(unknown)";
    }
  }

  private String getContentType(String metadata) {
    try {
      String[] lines = metadata.split("\r\n");
      return lines[2].split(": ")[1];
    } catch (ArrayIndexOutOfBoundsException e) {
      return "(unknown)";
    }
  }

  private int getFileSize(byte[] body) {
    int headerEnd = findDoubleCRLF(body);
    if (headerEnd == -1) return 0;
    int fileStart = headerEnd + 4;

    int fileEnd = body.length;
    for (int i = body.length - 4; i >= fileStart; i--) {
      if (body[i] == '\r' && body[i + 1] == '\n' && body[i + 2] == '-' && body[i + 3] == '-') {
        fileEnd = i;
        break;
      }
    }
    return fileEnd - fileStart;
  }

  private int findDoubleCRLF(byte[] data) {
    for (int i = 0; i < data.length - 3; i++) {
      if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
        return i;
      }
    }
    return -1;
  }
}
