package MyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static MyServer.Print.printConfig;
import static MyServer.Request.handleRequest;

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
  }

  public boolean isRunning() {
    return this.running;
  }

  public Thread getThread() {
    return this.thread;
  }

  private void serve() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);
      while (this.running) {
        try {
          Guess guess = new Guess();
          Socket client = serverSocket.accept();
          new Thread(() ->
          {
            handleRequest(client, rootDir, guess);
          }).start();

        } catch (IOException e) {
          if (this.running) {
            System.err.println("Socket error");
            e.printStackTrace(System.err);
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Error handling client request: " + e.getMessage());
    }
  }
}
