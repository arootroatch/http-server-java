package MyServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Response {
  public static void sendFile(FileInputStream file, String filetype, OutputStream outputStream) {
    byte[] fileBytes = null;
    try {
      fileBytes = file.readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    writeToOutputStream(filetype, outputStream, fileBytes);
  }

  public static void sendFile(String file, String filetype, OutputStream outputStream) {
    byte[] fileBytes = file.getBytes();
    writeToOutputStream(filetype, outputStream, fileBytes);
  }

  public static void sendFile(String file, String filetype, OutputStream outputStream, String addHTML) {
    String newHTML = file.split("</html>")[0] + addHTML + "</html>";
    byte[] fileBytes = newHTML.getBytes();
      writeToOutputStream(filetype, outputStream, fileBytes);
  }

  private static void writeToOutputStream(String filetype, OutputStream outputStream, byte[] fileBytes) {
    try {
      int byteCount = fileBytes.length;
      outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
      outputStream.write(setContentType(filetype).getBytes());
      outputStream.write(("Content-Length: " + byteCount + "\r\n").getBytes());
      outputStream.write(("Server: My MacBook Pro\r\n\r\n").getBytes());
      outputStream.write(fileBytes);
      outputStream.flush();
    } catch (IOException e){
      throw new RuntimeException(e);
    }
  }

  private static String setContentType(String filetype) {
    return switch (filetype) {
      case "jpg", "jpeg" -> "Content-Type: image/jpeg\r\n";
      case "txt", "html" -> "Content-Type: text/html\r\n";
      case "png" -> "Content-Type: image/png\r\n";
      case "pdf" -> "Content-Type: application/pdf\r\n";
      default -> "";
    };
  }

  public static void sendHTMLString(String html, OutputStream outputStream) {
    try {
      outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
      outputStream.write(("Content-Type: text/html\r\n").getBytes());
      outputStream.write(("Server: My MacBook Pro\r\n\r\n").getBytes());
      outputStream.write(html.getBytes());
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void send404(OutputStream outputStream) {
    try {
      outputStream.write(("HTTP/1.1 404 Not Found\r\n").getBytes());
      outputStream.write(("Content-Type: text/html\r\n").getBytes());
      outputStream.write(("Server: My MacBook Pro\r\n\r\n").getBytes());
      outputStream.write(("<h1>Error 404: Not Found</h1>").getBytes());
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
