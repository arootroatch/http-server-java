package MyServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Response {
  public static void sendFile(FileInputStream file, String filetype, OutputStream outputStream) {
    try {
      outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
      outputStream.write(setContentType(filetype).getBytes());
      outputStream.write(("Server: My MacBook Pro\r\n\r\n").getBytes());
      outputStream.write(file.readAllBytes());
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sendFile(String file, String filetype, OutputStream outputStream, String addHTML) {
    String newHTML = file.split("</html>")[0] + addHTML + "</html>";
    try {
      outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
      outputStream.write(setContentType(filetype).getBytes());
      outputStream.write(("Server: My MacBook Pro\r\n\r\n").getBytes());
      outputStream.write(newHTML.getBytes());
      outputStream.flush();
    } catch (IOException e) {
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
