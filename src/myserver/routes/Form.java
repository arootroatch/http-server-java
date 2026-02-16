package myserver.routes;

import myserver.ConnectionData;
import myserver.HttpRequest;
import myserver.Route;

import java.io.*;

import static myserver.routes.Utils.send404;
import static myserver.routes.Utils.sendHtmlString;

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
      sendHtmlString(newHTML, "html", outputStream);
    } else if (!queryString.isEmpty()) {
      String addHTML = queryParamsToHTML(getQueryParams(queryString));
      String newHTML = html.split("</html>")[0] + addHTML + "</html>";
      sendHtmlString(newHTML, "html", outputStream);
    } else {
      sendHtmlString(html, "html", outputStream);
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
      String name = s.split("=")[0];
      String value = s.split("=")[1];
      String li = String.format("<li>%s: %s</li>", name, value);
      html.append(li);
    }
    html.append("</ul>\r\n");
    return html.toString();
  }

  private String postRequestHTML(HttpRequest request) {
    String body = request.body();
    String fileName = getFileName(body);
    String contentType = getContentType(body);
    int fileSize = getFileSize(request);

    return "<ul>" +
        "<li>file name: " + fileName + "</li>" +
        "<li>content type: " + contentType + "</li>" +
        "<li>file size: " + fileSize + "</li>" +
        "</ul>";
  }

  private String getFileName(String body) {
    String[] bodyParts = body.split("\r\n\r\n", 2);
    String[] metadata = bodyParts[0].split("\r\n");
    return metadata[1].split(";")[2].split("=")[1].split("\"")[1];
  }

  private String getContentType(String body) {
    String[] bodyParts = body.split("\r\n\r\n", 2);
    String[] metadata = bodyParts[0].split("\r\n");
    return metadata[2].split(": ")[1];
  }

  private int getFileSize(HttpRequest request) {
    String body = request.body();
    String[] bodyParts = body.split("\r\n\r\n", 2);
    int metadataBytes = bodyParts[0].trim().getBytes().length;
    int contentLength = Integer.parseInt(request.header("Content-Length").trim());
    String fileAndFooter = bodyParts.length > 1 ? bodyParts[1] : "";
    String[] lines = fileAndFooter.split("\r\n");
    int footerBytes = lines[lines.length - 1].getBytes().length;
    return contentLength - footerBytes - metadataBytes - 8;
  }
}
