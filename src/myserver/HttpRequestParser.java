package myserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {

  public static HttpRequest parse(InputStream inputStream) {
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    String requestLine;
    try {
      requestLine = br.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (requestLine == null || requestLine.isBlank()) {
      return new HttpRequest("", "", "", Map.of(), "");
    }

    String[] parts = requestLine.split(" ");
    String method = parts[0];
    String fullPath = parts.length > 1 ? parts[1] : "";

    String path;
    String queryString;
    int qIndex = fullPath.indexOf('?');
    if (qIndex >= 0) {
      path = fullPath.substring(0, qIndex);
      queryString = fullPath.substring(qIndex + 1);
    } else {
      path = fullPath;
      queryString = "";
    }

    Map<String, String> headers = new HashMap<>();
    try {
      String line;
      while ((line = br.readLine()) != null && !line.isBlank()) {
        int colonIdx = line.indexOf(':');
        if (colonIdx > 0) {
          String name = line.substring(0, colonIdx).trim();
          String value = line.substring(colonIdx + 1).trim();
          headers.put(name, value);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    String body = "";
    String contentLengthStr = headers.get("Content-Length");
    if (contentLengthStr != null) {
      int contentLength = Integer.parseInt(contentLengthStr.trim());
      if (contentLength > 0) {
        char[] buffer = new char[contentLength];
        int totalRead = 0;
        try {
          while (totalRead < contentLength) {
            int read = br.read(buffer, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        body = new String(buffer, 0, totalRead);
      }
    }

    return new HttpRequest(method, path, queryString, headers, body);
  }
}
