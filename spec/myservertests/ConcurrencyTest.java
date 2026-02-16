package myservertests;

import myserver.MyServer;
import myserver.Route;
import myserver.routes.Ping;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static myservertests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrencyTest {
  static MyServer server;
  Socket socket;
  OutputStream outputStream;
  String pattern;
  SimpleDateFormat simpleDateFormat;
  String start;
  Calendar calendar;
  static HashMap<String, Route> routes = new HashMap<>();

  @BeforeAll
  static void startServer() {
    routes.put("/ping", new Ping());
    server = new MyServer(1238, "testroot", routes);
    server.start();
  }

  @BeforeEach
  void setup() throws IOException {
    pattern = "yyyy-MM-dd HH:mm:ss";
    simpleDateFormat = new SimpleDateFormat(pattern);
    start = simpleDateFormat.format(new Date());
    calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    socket = new Socket("127.0.0.1", 1238);
    outputStream = socket.getOutputStream();
  }

  @AfterEach
  void closeSocket() throws IOException {
    if (socket != null) socket.close();
  }

  @Test
  void currentTime() throws IOException {
    outputStream.write(("GET /ping HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + start + "</li>"));
    assertTrue(response.contains("<li>end time: " + start + "</li>"));
  }

  @Test
  void waitOneSec() throws IOException {
    calendar.add(Calendar.SECOND, 1);
    Date delay = calendar.getTime();
    String end = simpleDateFormat.format(delay);

    outputStream.write(("GET /ping/1 HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + start + "</li>"));
    assertTrue(response.contains("<li>end time: " + end + "</li>"));
  }

  @Test
  void waitTwoSecs() throws IOException {
    calendar.add(Calendar.SECOND, 2);
    Date delay = calendar.getTime();
    String end = simpleDateFormat.format(delay);

    outputStream.write(("GET /ping/2 HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());

    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + start + "</li>"));
    assertTrue(response.contains("<li>end time: " + end + "</li>"));
  }

  @Test
  void concurrent() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(5);
    long startTime = System.currentTimeMillis();

    List<Future<String>> futures = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      futures.add(executor.submit(() -> {
        Socket s = new Socket("127.0.0.1", 1238);
        OutputStream os = s.getOutputStream();
        os.write("GET /ping/1 HTTP/1.1\r\n\r\n".getBytes());
        os.flush();
        String response = parseInputStream(s.getInputStream());
        s.close();
        return response;
      }));
    }

    List<String> responses = new ArrayList<>();
    for (Future<String> future : futures) {
      responses.add(future.get());
    }

    long elapsed = System.currentTimeMillis() - startTime;
    executor.shutdown();

    for (String response : responses) {
      assertTrue(response.contains("<h2>Ping</h2>"));
    }

    assertTrue(elapsed < 3000, "Concurrent requests took too long: " + elapsed + "ms");
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
