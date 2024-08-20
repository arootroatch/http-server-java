package MyServer;

import MyServer.routes.*;
import MyServer.routes.File;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import static MyServer.routes.RouteMap.getRoutes;

public final class Request {
  private Request(){}

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

  private static void handleRequest(String request, OutputStream outputStream, String rootDir) {
    String resource = parseResource(request);
    String resourceStart = resource.split("[?/]").length > 0 ? resource.split("[?/]")[1] : "";
    String route = "/" + resourceStart;
    HashMap<String, String> connData = bundleConnData(request, resource, rootDir);
    HashMap<String, Route> routes = getRoutes(connData, outputStream);

    if (routes.containsKey(route)) {
      routes.get(route).serve();
    } else if (!resource.contains(".")) {
      new Folder(connData, outputStream).serve();
    } else {
      new File(connData, outputStream).serve();
    }
  }

  private static HashMap<String, String> bundleConnData(String request, String resource, String rootDir) {
    HashMap<String, String> connData = new HashMap<>();
    connData.put("resource", resource);
    connData.put("request", request);
    connData.put("rootDir", rootDir);
    return connData;
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

    if (line != null) {
      if (line.contains("GET")) {
        parseGetRequest(line, request, br);
      } else {
        parsePostRequest(line, request, br);
      }
    }

    return request.toString();
  }

  private static void parseGetRequest(String line, StringBuilder request, BufferedReader br) {
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

  private static void parsePostRequest(String line, StringBuilder request, BufferedReader br) {
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
