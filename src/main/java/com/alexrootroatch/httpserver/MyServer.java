package com.alexrootroatch.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.alexrootroatch.httpserver.Print.printConfig;
import static com.alexrootroatch.httpserver.routes.HttpResponse.send500;
import static com.alexrootroatch.httpserver.routes.HttpResponse.sendError;

public class MyServer {
  private static final Logger logger = Logger.getLogger(MyServer.class.getName());
  private ServerSocket serverSocket;
  private volatile boolean running = false;
  private Thread thread;
  private ExecutorService executor;
  private final int port;
  private final String rootDir;
  private final RequestDispatcher dispatcher;

  public MyServer(int port, String rootDir, Map<String, Route> routes) {
    this.port = port;
    this.rootDir = rootDir;
    this.dispatcher = new RequestDispatcher(rootDir, routes);
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
      logger.log(Level.SEVERE, "Error closing server socket", e);
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
          dispatcher.dispatch(request, outputStream);
        } catch (MalformedRequestException e) {
          sendError(400, "Bad Request", outputStream);
        } catch (Exception e) {
          send500(outputStream);
        }
      } catch (IOException e) {
        logger.log(Level.FINE, "Client socket error", e);
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
        logger.log(Level.WARNING, "Socket accept error", e);
      }
    }
    return client;
  }
}
