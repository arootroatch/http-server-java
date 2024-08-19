package MyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static MyServer.Print.printConfig;
import static MyServer.Request.handleConnection;

public class MyServer {
  ServerSocket serverSocket;
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
    try {
      this.serverSocket = new ServerSocket(this.port);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    printConfig(port, rootDir);
    this.thread = new Thread(this::serve);
    thread.start();
  }

  public void stop() {
    this.running = false;
    try {
      this.serverSocket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.thread = null;
  }

  public boolean isRunning() {
    return this.running;
  }

  public Thread getThread() {
    return this.thread;
  }

  private void serve() {
    while (this.running) {
      Socket client = openSocketConnection();
      if (client != null) createRequestThread(client);
    }
  }

  private void createRequestThread(Socket client) {
    new Thread(() -> {
      handleConnection(client, rootDir);
      try {
        client.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }

  private Socket openSocketConnection() {
    Socket client = null;
    try {
     client = this.serverSocket.accept();
    } catch (IOException e) {
      if (this.running) {
        System.err.println("Socket error");
        e.printStackTrace(System.err);
      }
    }
    return client;
  }
}

