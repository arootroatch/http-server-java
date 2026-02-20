package com.alexrootroatch.httpserver.routes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class HttpResponse {
  private HttpResponse() {
  }

  public static void sendError(int code, String reason, OutputStream outputStream) {
    try {
      outputStream.write(("HTTP/1.1 " + code + " " + reason + "\r\n").getBytes(StandardCharsets.US_ASCII));
      outputStream.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.US_ASCII));
      outputStream.write("Server: My Server\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
      outputStream.write(("<h1>Error " + code + ": " + reason + "</h1>").getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
    } catch (IOException e) { /* client disconnected */ }
  }

  public static void send404(OutputStream outputStream) {
    sendError(404, "Not Found", outputStream);
  }

  public static void send405(OutputStream outputStream) {
    sendError(405, "Method Not Allowed", outputStream);
  }

  public static void send500(OutputStream outputStream) {
    sendError(500, "Internal Server Error", outputStream);
  }

  public static void sendFile(FileInputStream file, String filetype, OutputStream outputStream) {
    byte[] fileBytes;
    try {
      fileBytes = file.readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    writeToOutputStream(filetype, outputStream, fileBytes, List.of());
  }

  public static void sendString(String content, String filetype, OutputStream outputStream) {
    sendString(content, filetype, outputStream, List.of());
  }

  public static void sendString(String content, String filetype, OutputStream outputStream,
                                    List<String> extraHeaders) {
    byte[] fileBytes = content.getBytes(StandardCharsets.UTF_8);
    writeToOutputStream(filetype, outputStream, fileBytes, extraHeaders);
  }

  private static void writeToOutputStream(String filetype, OutputStream outputStream,
                                          byte[] fileBytes, List<String> extraHeaders) {
    try {
      int byteCount = fileBytes.length;
      outputStream.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.US_ASCII));
      outputStream.write(ContentType.contentTypeHeader(filetype).getBytes(StandardCharsets.US_ASCII));
      for (String header : extraHeaders) {
        outputStream.write((header + "\r\n").getBytes(StandardCharsets.US_ASCII));
      }
      outputStream.write(("Content-Length: " + byteCount + "\r\n").getBytes(StandardCharsets.US_ASCII));
      outputStream.write("Server: My Server\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
      outputStream.write(fileBytes);
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
