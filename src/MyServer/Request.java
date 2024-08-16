package MyServer;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static MyServer.HTML.*;
import static MyServer.Response.*;

public class Request {
  public static String parseRequest(InputStream inputStream) {
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder request = new StringBuilder();
    String line;

    try {
      line = br.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (line.contains("GET")){
      parseGetRequest(line, request, br);
    } else {
      parsePostRequest(line, request, br);
    }

    return request.toString();
  }

  private static void parseGetRequest(String line, StringBuilder request, BufferedReader br){
    while (line != null) {
      if (line.isBlank()) break;
      request.append(line).append("\r\n");
      try {
        line = br.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void parsePostRequest(String line, StringBuilder request, BufferedReader br){
    char[] buffer;
    int contentLength = 0;
    int numRead;

    while (line != null) {
      request.append(line).append("\r\n");
      try {
        if (line.contains("Content-Length")) {
          contentLength += Integer.parseInt(line.split(": ")[1]);
          break;
        } else line = br.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    if (contentLength > 0) {
      buffer = new char[contentLength];
      try {
        numRead = br.read(buffer);
        request.append(buffer, 0, numRead);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String parseResource(String request) {
    String firstLine = request.split("\r\n")[0];
    String resource = "";
    if (!firstLine.isBlank()) resource = firstLine.split(" ")[1];
    return resource;
  }

  public static void handleRequest(Socket client, String rootDir) {
    InputStream inputStream;
    OutputStream outputStream;
    try {
      inputStream = client.getInputStream();
      outputStream = client.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String request = parseRequest(inputStream);
    String resource = parseResource(request);

    if (resource.equals("/") || resource.equals("/hello")) {
      try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
        sendFile(file, "html", outputStream);
      } catch (IOException e) {
        send404(outputStream);
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
          String html = parseHTML(file);
          String addHTML = postRequestHTML(request);
          sendFile(html, "html", outputStream, addHTML);

        } else if (resource.contains("?")) {
          String html = parseHTML(file);
          String addHTML = queryParamsToHTML(getQueryParams(resource));
          sendFile(html, "html", outputStream, addHTML);
        } else sendFile(file, "html", outputStream);
      } catch (IOException e) {
        send404(outputStream);
      }

    } else if (resource.contains("/ping")) {
      boolean delayed = resource.split("/").length > 2;
      int delay = delayed ? Integer.parseInt(resource.split("/")[2]) : 0;
      String pattern = "yyyy-MM-dd HH:mm:ss";
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
      String start = simpleDateFormat.format(new Date());
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
      }
    }
  }
}
