package MyServer;

import MyServer.routes.*;
import MyServer.routes.File;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Request {
  public Request(){}

  public void handleConnection(Socket client, String rootDir, HashMap<String, Route>routes) {
    InputStream inputStream;
    OutputStream outputStream;
    try {
      inputStream = client.getInputStream();
      outputStream = client.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String request = parseRequest(inputStream);
    handleRequest(request, outputStream, rootDir, routes);
  }

  private void handleRequest(String request, OutputStream outputStream,
                                    String rootDir, HashMap<String, Route>routes) {
    String resource = parseResource(request);
    String resourceStart = resource.split("[?/]").length > 1 ? resource.split("[?/]")[1] : "";
    String route = "/" + resourceStart;
    HashMap<String, String> connData = bundleConnData(request, resource, rootDir);

    if (routes.containsKey(route)) {
      routes.get(route).serve(connData, outputStream);
    } else if (!resource.contains(".")) {
      new Folder().serve(connData, outputStream);
    } else {
      new File().serve(connData, outputStream);
    }
  }

  private HashMap<String, String> bundleConnData(String request, String resource, String rootDir) {
    HashMap<String, String> connData = new HashMap<>();
    connData.put("resource", resource);
    connData.put("request", request);
    connData.put("rootDir", rootDir);
    return connData;
  }

  private String parseRequest(InputStream inputStream) {
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

  private void parseGetRequest(String line, StringBuilder request, BufferedReader br) {
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

  private void parsePostRequest(String line, StringBuilder request, BufferedReader br) {
    char[] buffer;
    int contentLength = 0;
    int numRead;

    while (line != null) {
      request.append(line).append("\r\n");
      try {
        if (line.contains("Content-Length")) {
          contentLength += Integer.parseInt(line.split(": ")[1]);
          line = br.readLine();
        } else if (line.isBlank()){
          break;
        }else line = br.readLine();
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

  private String parseResource(String request) {
    String firstLine = request.split("\r\n")[0];
    String resource = "";
    if (!firstLine.isBlank()) resource = firstLine.split(" ")[1];
    return resource;
  }
}
