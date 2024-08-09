package MyServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

import static MyServer.Print.printConfig;

public class MyServer {
  private Boolean running = false;
  private Thread thread;
  private final int port;
  private final String rootDir;

  public MyServer(int port, String rootDir) {
    this.port = port;
    this.rootDir = rootDir;
  }

  public void start() {
    this.running = true;
    printConfig(port, rootDir);
    this.thread = new Thread(this::serve);
    thread.start();
  }

  public void stop() {
    this.running = false;
    this.thread = null;
  }

  public boolean isRunning() {
    return this.running;
  }

  public Thread getThread() {
    return this.thread;
  }

  private void serve() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (this.running) {
        try (Socket client = serverSocket.accept()) {
          StringBuilder request = parseRequest(client.getInputStream());
          handleRequest(request, client.getOutputStream());
        }
      }
    } catch (IOException e) {
      System.err.println("Error handling client request: " + e.getMessage());
    }
  }

  private StringBuilder parseRequest(InputStream inputStream) throws IOException {
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder request = new StringBuilder();
    String line = br.readLine();

    while (line != null) {
      if (line.isBlank()) break;
      request.append(line).append("\r\n");
      line = br.readLine();
    }
    return request;
  }

  private void handleRequest(StringBuilder request, OutputStream outputStream) {
    String resource = parseResource(request);

    if (resource.equals("/") || resource.equals("/hello")) {
      try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
        String filetype = resource.split("\\.")[1];
        sendFile(file, filetype, outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }

    } else if (resource.equals("/listing")) {
      sendHTMLString(renderContentsAsHTML(getContentsOfDir(rootDir)), outputStream);

    } else if (resource.contains("/listing/")) {
      String dir = resource.split("/")[2];
      Object[] contents = getContentsOfDir(rootDir + "/" + dir);
      sendHTMLString(renderContentsAsHTML(dir, contents), outputStream);

    } else {
      try (FileInputStream file = new FileInputStream(rootDir + resource)) {
        String filetype = resource.split("\\.")[1];
        sendFile(file, filetype, outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }
    }
  }

  private String parseResource(StringBuilder request) {
    String firstLine = request.toString().split("\r\n")[0];
    return firstLine.split(" ")[1];
  }

  private void sendFile(FileInputStream file, String filetype, OutputStream outputStream) {
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

  private String setContentType(String filetype) {
    return switch (filetype) {
      case "jpg", "jpeg" -> "Content-Type: image/jpeg\r\n";
      case "txt", "html" -> "Content-Type: text/html\r\n";
      case "png" -> "Content-Type: image/png\r\n";
      case "pdf" -> "Content-Type: application/pdf\r\n";
      default -> "";
    };
  }

  private void sendHTMLString(String html, OutputStream outputStream) {
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

  private void send404(OutputStream outputStream) {
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

  public static Object[] getContentsOfDir(String dir) {
    File d = new File(dir);
    return Arrays.stream(Objects.requireNonNull(d.listFiles())).map(File::getName).toArray();
  }

  public static String renderContentsAsHTML(Object[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (Object i : contents) {
      String li;
      if (i.toString().contains(".")) {
        li = String.format("<li><a href=\"/%s\">%s</a></li>", i, i);
      } else {
        li = String.format("<li><a href=\"/listing/%s\">%s</a></li>", i, i);
      }
      html.append(li);
    }
    html.append("</ul>\r\n");
    return html.toString();
  }

  public static String renderContentsAsHTML(String dir, Object[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (Object i : contents) {
      String li;
      if (i.toString().contains(".")) {
        li = String.format("<li><a href=\"/%s/%s\">%s</a></li>", dir, i, i);
      } else {
        li = String.format("<li><a href=\"/listing/%s/%s\">%s</a></li>", dir, i, i);
      }
      html.append(li);
    }
    html.append("</ul>");
    return html.toString();
  }
}
