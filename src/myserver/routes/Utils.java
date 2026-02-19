package myserver.routes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public final class Utils {
  private Utils() {
  }

  public static void send404(OutputStream outputStream) {
    try {
      outputStream.write(("HTTP/1.1 404 Not Found\r\n").getBytes());
      outputStream.write(("Content-Type: text/html\r\n").getBytes());
      outputStream.write(("Server: My Server\r\n\r\n").getBytes());
      outputStream.write(("<h1>Error 404: Not Found</h1>").getBytes());
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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

  public static void sendHtmlString(String content, String filetype, OutputStream outputStream) {
    sendHtmlString(content, filetype, outputStream, List.of());
  }

  public static void sendHtmlString(String content, String filetype, OutputStream outputStream,
                                    List<String> extraHeaders) {
    byte[] fileBytes = content.getBytes();
    writeToOutputStream(filetype, outputStream, fileBytes, extraHeaders);
  }

  private static void writeToOutputStream(String filetype, OutputStream outputStream,
                                          byte[] fileBytes, List<String> extraHeaders) {
    try {
      int byteCount = fileBytes.length;
      outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
      outputStream.write(setContentType(filetype).getBytes());
      for (String header : extraHeaders) {
        outputStream.write((header + "\r\n").getBytes());
      }
      outputStream.write(("Content-Length: " + byteCount + "\r\n").getBytes());
      outputStream.write(("Server: My Server\r\n\r\n").getBytes());
      outputStream.write(fileBytes);
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static String setContentType(String filetype) {
    return switch (filetype) {
      case "jpg", "jpeg" -> "Content-Type: image/jpeg\r\n";
      case "png" -> "Content-Type: image/png\r\n";
      case "gif" -> "Content-Type: image/gif\r\n";
      case "svg" -> "Content-Type: image/svg+xml\r\n";
      case "ico" -> "Content-Type: image/x-icon\r\n";
      case "txt" -> "Content-Type: text/plain\r\n";
      case "html", "htm" -> "Content-Type: text/html\r\n";
      case "css" -> "Content-Type: text/css\r\n";
      case "js" -> "Content-Type: application/javascript\r\n";
      case "json" -> "Content-Type: application/json\r\n";
      case "xml" -> "Content-Type: application/xml\r\n";
      case "pdf" -> "Content-Type: application/pdf\r\n";
      default -> "Content-Type: application/octet-stream\r\n";
    };
  }
}
