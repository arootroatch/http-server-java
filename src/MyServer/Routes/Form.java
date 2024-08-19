package MyServer.Routes;

import MyServer.Route;

import java.io.*;
import java.util.Arrays;

public class Form implements Route {
  String rootDir;
  String request;
  OutputStream outputStream;
  String resource;
  String html;
  FileInputStream file;
  String addHTML;

  public Form(String rootDir, String request, String resource, OutputStream outputStream) {
    this.rootDir = rootDir;
    this.file = getFile();
    this.request = request;
    this.resource = resource;
    this.html = parseHTML();
    this.outputStream = outputStream;
  }

  public void serve(){
    if (request.contains("POST")) {
      addHTML = postRequestHTML();
      String newHTML = html.split("</html>")[0] + addHTML + "</html>";
      sendHtmlString(newHTML, "html", outputStream);
    } else if (this.resource.contains("?")) {
      addHTML = queryParamsToHTML(getQueryParams(resource));
      String newHTML = html.split("</html>")[0] + addHTML + "</html>";
      sendHtmlString(newHTML, "html", outputStream);
    } else sendHtmlString(html, "html", outputStream);
  }


  private FileInputStream getFile(){
    try {
      return new FileInputStream(rootDir + "/forms.html");
    } catch (FileNotFoundException ex) {
      send404(outputStream);
      return null;
    }
  }

  private String parseHTML() {
    InputStreamReader isr = new InputStreamReader(file);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder html = new StringBuilder();
    String line = null;
    try {
      line = br.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    while (line != null) {
      html.append(line).append("\r\n");
      try {
        line = br.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return html.toString();
  }

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

  private String postRequestHTML() {
    String fileName = getFileName();
    String contentType = getContentType();
    int fileSize = getFileSize();

    return "<ul>" +
        "<li>file name: " + fileName + "</li>" +
        "<li>content type: " + contentType + "</li>" +
        "<li>file size: " + fileSize + "</li>" +
        "</ul>";
  }


  private String getFileName(){
    String[] multiparts = request.split("\r\n\r\n");
    String[] metadata = multiparts[1].split("\r\n");
    return metadata[1].split(";")[2].split("=")[1].split("\"")[1];
  }

  private String getContentType(){
    String[] multiparts = request.split("\r\n\r\n");
    String[] metadata = multiparts[1].split("\r\n");
    return metadata[2].split(": ")[1];
  }

  private int getFileSize(){
    String[] multiparts = request.split("\r\n\r\n");
    int metadataBytes = multiparts[1].trim().getBytes().length;
    return getContentLength() - getFooterBytes() - metadataBytes - 8;
  }

  private int getContentLength(){
    return Integer.parseInt(Arrays.stream(request.split("\r\n"))
        .filter(s -> s.contains("Content-Length")).toArray()[0].toString().split(":")[1].trim());
  }

  private int getFooterBytes(){
    String[] multiparts = request.split("\r\n\r\n");
    String[] endOfInput = multiparts[2].split("\r\n");
    return endOfInput[endOfInput.length - 1].getBytes().length;
  }
}
