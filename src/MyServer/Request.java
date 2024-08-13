package MyServer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static MyServer.Response.*;

public class Request {
  public static String parseGetRequest(InputStream inputStream) {
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder request = new StringBuilder();
    String line;
    try {
      line = br.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    while (line != null) {
      if (line.isBlank()) break;
      request.append(line).append("\r\n");
      try {
        line = br.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return request.toString();
  }

  public static String parsePostRequest(InputStream inputStream, Integer contentLength) throws IOException {
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder request = new StringBuilder();
    char[] buffer = new char[contentLength];
    int numRead = br.read(buffer, 0, contentLength);
    request.append(buffer, 0, numRead);

    return request.toString();
  }

  public static String parseResource(String request) {
    String firstLine = request.split("\r\n")[0];
    String resource = "";
    if (!firstLine.isBlank()) resource = firstLine.split(" ")[1];
    return resource;
  }

  public static void handleRequest(Socket client, String rootDir, Guess guess) {
    InputStream inputStream;
    OutputStream outputStream;
    try {
      inputStream =  client.getInputStream();
      outputStream = client.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String request = parseGetRequest(inputStream);
    String resource = parseResource(request);

    if (resource.equals("/") || resource.equals("/hello")) {
      try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
        sendFile(file, "html", outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }
    } else if (resource.equals("/listing")) {
      sendHTMLString(renderContentsAsHTML(getContentsOfDir(rootDir)), outputStream);
    } else if (resource.contains("/listing/")) {
      String[] split = resource.split("/");
      String dir;
      if (split.length > 2) {
        dir = resource.split("/")[2];
        Object[] contents = getContentsOfDir(rootDir + "/" + dir);
        sendHTMLString(renderContentsAsHTML("/" + dir, contents), outputStream);
      } else sendHTMLString(renderContentsAsHTML(getContentsOfDir(rootDir)), outputStream);

    } else if (resource.contains("/form")) {
      try (FileInputStream file = new FileInputStream(rootDir + "/forms.html")) {
        if (request.contains("POST")) {
          Integer contentLength = Integer.parseInt(Arrays.stream(request.split("\r\n"))
              .filter(s -> s.contains("Content-Length")).toArray()[0].toString().split(":")[1].trim());
          request = parsePostRequest(inputStream, contentLength);
          String html = parseHTML(file);
          String addHTML = postRequestHTML(request, contentLength);
          sendFile(html, "html", outputStream, addHTML);

        } else if (resource.contains("?")) {
          String html = parseHTML(file);
          String addHTML = queryParamsToHTML(getQueryParams(resource));
          sendFile(html, "html", outputStream, addHTML);
        } else sendFile(file, "html", outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }

    } else if (resource.contains("/ping")) {
      boolean delayed = resource.split("/").length > 2;
      int delay = delayed ? Integer.parseInt(resource.split("/")[2]) : 0;
      Date start = new Date();
      try {
        Thread.sleep(delay * 1000L);
        sendFile(renderPingHTML(start), "html", outputStream);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

    } else if (resource.contains("/guess")){
      String[] queryParams = getQueryParams(resource);
      sendFile(guess.renderGuessHTML(queryParams), "html", outputStream);

    } else if (!resource.contains(".")) {
      Object[] contents = getContentsOfDir(rootDir + resource);
      if (Arrays.asList(contents).contains("index.html")) {
        try (FileInputStream file = new FileInputStream(rootDir + resource + "/index.html")) {
          sendFile(file, "html", outputStream);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else if (contents.length == 0) {
        send404(outputStream);
      } else {
        sendHTMLString(renderContentsAsHTML(resource, contents), outputStream);
      }

    } else {
      try (FileInputStream file = new FileInputStream(rootDir + resource)) {
        String filetype = resource.split("\\.")[1];
        sendFile(file, filetype, outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }
    }
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
    String[] fileInfo = request.substring(0, 150).split("\r\n")[1].split(";");
    String fileName = fileInfo[2].split("=")[1];
    return "<ul>" +
        "<li>file name: " + fileName + "</li>" +
        "<li>content type: application/octet-stream</li>" +
        "<li>file size: " + contentLength + "</li>" +
        "</ul>";
  }
}
