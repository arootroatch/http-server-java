package myserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static myserver.Print.printConfig;
import static myserver.routes.Utils.send500;

public class MyServer {
  ServerSocket serverSocket;
  private volatile boolean running = false;
  private Thread thread;
  private ExecutorService executor;
  private final int port;
  private final String rootDir;
  private final Map<String, Route> routes;

  public MyServer(int port, String rootDir, Map<String, Route> routes) {
    this.port = port;
    this.rootDir = rootDir;
    this.routes = routes;
  }

  public void start() {
    this.running = true;
    this.executor = Executors.newFixedThreadPool(20);
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
    executor.shutdown();
    try {
      this.serverSocket.close();
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
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
    executor.submit(() -> {
      try (client) {
        OutputStream outputStream = client.getOutputStream();
        try {
          HttpRequest request = HttpRequestParser.parse(client.getInputStream());
          RequestDispatcher.dispatch(request, rootDir, routes, outputStream);
        } catch (Exception e) {
          send500(outputStream);
        }
      } catch (IOException e) {
        // client socket failed to open or close; nothing to send
      }
    });
  }

  private Socket openSocketConnection() {
    Socket client = null;
    try {
      client = this.serverSocket.accept();
      client.setSoTimeout(30_000);
    } catch (IOException e) {
      if (this.running) {
        System.err.println("Socket error");
        e.printStackTrace(System.err);
      }
    }
    return client;
  }
}
