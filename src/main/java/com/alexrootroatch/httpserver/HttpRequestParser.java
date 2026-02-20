package com.alexrootroatch.httpserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {

  public static final int MAX_BODY_SIZE = 10 * 1024 * 1024;

  public static HttpRequest parse(InputStream inputStream) throws MalformedRequestException {
    BufferedInputStream bis = new BufferedInputStream(inputStream);

    String requestLine = readLine(bis);
    if (requestLine == null || requestLine.isBlank()) {
      return new HttpRequest("", "", "", Map.of(), new byte[0]);
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
    path = URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);

    Map<String, String> headers = new HashMap<>();
    String line;
    while ((line = readLine(bis)) != null && !line.isEmpty()) {
      int colonIdx = line.indexOf(':');
      if (colonIdx > 0) {
        String name = line.substring(0, colonIdx).trim();
        String value = line.substring(colonIdx + 1).trim();
        headers.put(name, value);
      }
    }

    byte[] body = new byte[0];
    String contentLengthStr = headers.get("Content-Length");
    if (contentLengthStr != null) {
      int contentLength;
      try {
        contentLength = Integer.parseInt(contentLengthStr.trim());
      } catch (NumberFormatException e) {
        throw new MalformedRequestException("Invalid Content-Length: " + contentLengthStr, e);
      }
      if (contentLength < 0) {
        throw new MalformedRequestException("Negative Content-Length: " + contentLength);
      }
      if (contentLength > MAX_BODY_SIZE) {
        return new HttpRequest(method, path, queryString, headers, new byte[0]);
      }
      if (contentLength > 0) {
        body = new byte[contentLength];
        int totalRead = 0;
        try {
          while (totalRead < contentLength) {
            int read = bis.read(body, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return new HttpRequest(method, path, queryString, headers, body);
  }

  private static String readLine(InputStream in) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int b = -1;
    try {
      while ((b = in.read()) != -1) {
        if (b == '\r') {
          in.mark(1);
          int next = in.read();
          if (next != '\n' && next != -1) {
            in.reset();
          }
          break;
        }
        if (b == '\n') break;
        baos.write(b);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (b == -1 && baos.size() == 0) return null;
    return baos.toString(StandardCharsets.US_ASCII);
  }
}
