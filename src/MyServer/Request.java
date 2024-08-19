package MyServer;

import MyServer.Routes.*;
import MyServer.Routes.File;

import java.io.*;
import java.net.Socket;

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
      new Hello(rootDir, outputStream).serve();

    } else if (resource.contains("/listing")) {
      new Listing(rootDir, outputStream, resource).serve();

    } else if (resource.contains("/form")) {
      new Form(rootDir, request, resource, outputStream).serve();

    } else if (resource.contains("/ping")) {
      new Ping(resource, outputStream).serve();

    } else if (!resource.contains(".")) {
      new Folder(rootDir, resource, outputStream).serve();

    } else {
      new File(rootDir, resource, outputStream).serve();
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
