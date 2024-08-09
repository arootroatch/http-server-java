package MyServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

    while (!line.isBlank()) {
      request.append(line).append("\r\n");
      line = br.readLine();
    }
    return request;
  }

  private void handleRequest(StringBuilder request, OutputStream outputStream) {
    String resource = parseResource(request);
    FileInputStream file = null;
    if (resource.equals("/") || resource.equals("/hello")) {
      try {
        file = new FileInputStream(rootDir + "/index.html");
      } catch (FileNotFoundException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }
      if (file != null) sendFile(file, outputStream);
    } else {
      try {
        file = new FileInputStream(rootDir + resource);
      } catch (FileNotFoundException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }
      if (file != null) sendFile(file, outputStream);
    }
  }

  private String parseResource(StringBuilder request) {
    String firstLine = request.toString().split("\r\n")[0];
    return firstLine.split(" ")[1];
  }

  private void sendFile(FileInputStream file, OutputStream outputStream) {
    try {
      outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
      outputStream.write(("Server: My MacBook Pro\r\n\r\n").getBytes());
      outputStream.write(file.readAllBytes());
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void send404(OutputStream outputStream) {
    try {
      outputStream.write(("HTTP/1.1 404 Not Found\r\n\r\n").getBytes());
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
