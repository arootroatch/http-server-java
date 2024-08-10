package MyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static MyServer.Print.printConfig;
import static MyServer.Request.handleRequest;
import static MyServer.Request.parseRequest;

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
          handleRequest(request, client.getOutputStream(), rootDir);
        }
      }
    } catch (IOException e) {
      System.err.println("Error handling client request: " + e.getMessage());
    }
  }
}
