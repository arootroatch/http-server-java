package MyServer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static MyServer.Response.*;

public class Request {
  public static String parseGetRequest(InputStream inputStream) {
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder request = new StringBuilder();
    String line;
    try {
      line = br.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    while (line != null) {
      System.out.println(line);
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

  public static String parsePostRequest(InputStream inputStream) throws IOException {
    InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder request = new StringBuilder();
    String line;
    int i = 80;
    try {
      line = br.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    while (i > 0) {
      request.append(line).append("\r\n");
      try {
        line = br.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      System.out.println(line);
      i--;
    }
    return request.toString();
  }

  public static String parseResource(String request) {
    String firstLine = request.split("\r\n")[0];
    String resource = "";
    if (!firstLine.isBlank()) resource = firstLine.split(" ")[1];
    return resource;
  }

  public static void handleRequest(InputStream inputStream, OutputStream outputStream, String rootDir) {
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
          request = parsePostRequest(inputStream);
          String html = parseHTML(file);
//          headersToHTML(request.toString());
//          String addHTML = queryParamsToHTML
        }
        if (resource.contains("?")) {
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
    StringBuilder html = new StringBuilder();
    Date end = new Date();

    html.append("<html>");
    html.append("<h2>Ping</h2>");
    html.append("<ul>");
    html.append("<li>start time: ").append(start).append("</li>");
    html.append("<li>end time: ").append(end).append("</li>");
    html.append("</ul>");
    html.append("</html>");
    return html.toString();
  }
}
