package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.HttpRequest;
import com.alexrootroatch.httpserver.Route;

import com.alexrootroatch.httpserver.routes.http.HtmlUtil;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.alexrootroatch.httpserver.routes.http.HttpResponse.send404;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.sendString;

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
      String[] parts = s.split("=", 2);
      String name = HtmlUtil.escapeHtml(URLDecoder.decode(parts[0], StandardCharsets.UTF_8));
      String value = parts.length > 1 ? HtmlUtil.escapeHtml(URLDecoder.decode(parts[1], StandardCharsets.UTF_8)) : "";
      String li = String.format("<li>%s: %s</li>", name, value);
      html.append(li);
    }
    html.append("</ul>\r\n");
    return html.toString();
  }

  private String postRequestHTML(HttpRequest request) {
    byte[] body = request.body();
    String fileName = MultipartParser.getFileName(body);
    String contentType = MultipartParser.getContentType(body);
    int fileSize = MultipartParser.getFileSize(body);

    if (fileName.equals("(unknown)") && contentType.equals("(unknown)") && fileSize == 0) {
      return "<p>Invalid upload</p>";
    }

    return "<ul>" +
        "<li>file name: " + HtmlUtil.escapeHtml(fileName) + "</li>" +
        "<li>content type: " + HtmlUtil.escapeHtml(contentType) + "</li>" +
        "<li>file size: " + fileSize + "</li>" +
        "</ul>";
  }
}
