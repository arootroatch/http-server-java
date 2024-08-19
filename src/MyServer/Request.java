package MyServer;

import MyServer.Routes.Form;
import MyServer.Routes.Hello;
import MyServer.Routes.Listing;
import MyServer.Routes.Ping;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static MyServer.Response.*;

public class Request {
  public static void handleConnection(Socket client, String rootDir) {
    InputStream inputStream;
    OutputStream outputStream;
    try {
      inputStream = client.getInputStream();
      outputStream = client.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String request = parseRequest(inputStream);
    handleRequest(request, outputStream, rootDir);
  }

  private static void handleRequest(String request, OutputStream outputStream, String rootDir){
    String resource = parseResource(request);

    if (resource.equals("/hello")) {
      new Hello(rootDir, outputStream).serveHello();

    } else if (resource.contains("/listing")) {
      new Listing(rootDir, outputStream, resource).serveListing();

    } else if (resource.contains("/form")) {
      new Form(rootDir, request, resource, outputStream).serveForm();

    } else if (resource.contains("/ping")) {
      new Ping(resource, outputStream).ping();

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

  private static String parseRequest(InputStream inputStream) {
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder request = new StringBuilder();
    String line;

    try {
      line = br.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (line != null){
      if (line.contains("GET")) {
        parseGetRequest(line, request, br);
      } else {
        parsePostRequest(line, request, br);
      }
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

  private static String parseResource(String request) {
    String firstLine = request.split("\r\n")[0];
    String resource = "";
    if (!firstLine.isBlank()) resource = firstLine.split(" ")[1];
    return resource;
  }
}
