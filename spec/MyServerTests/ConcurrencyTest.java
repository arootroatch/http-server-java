package MyServerTests;

import MyServer.MyServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import static MyServerTests.URLConnection.connectToURL;
import static MyServerTests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrencyTest {
  static MyServer server;
  static Socket socket;
  static OutputStream outputStream;

  @BeforeAll
  static void setup() throws IOException {
    server = new MyServer(1238, "testroot");
    server.start();

  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1238);
    outputStream = socket.getOutputStream();
  }

  @Test
  void currentTime() throws IOException {
    Date date = new Date();
    outputStream.write(("GET /ping HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + date + "</li>"));
    assertTrue(response.contains("<li>end time: " + date + "</li>"));
  }

  @Test
  void waitOneSec() throws IOException {
    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, 1);
    Date delay = calendar.getTime();

    outputStream.write(("GET /ping/1 HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + date + "</li>"));
    assertTrue(response.contains("<li>end time: " + delay + "</li>"));
  }

  @Test
  void waitTwoSecs() throws IOException {
    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, 2);
    Date delay = calendar.getTime();

    outputStream.write(("GET /ping/2 HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + date + "</li>"));
    assertTrue(response.contains("<li>end time: " + delay + "</li>"));
  }

  @Test
  void concurrent() throws IOException {
    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, 1);
    Date delay = calendar.getTime();

    class MyThread extends Thread {
      public void run() {
        try {
          HttpURLConnection conn = connectToURL("http://localhost:1238/ping/1");
          String response = parseInputStream(conn.getInputStream());
//          outputStream.write(("GET /ping/1 HTTP/1.1\r\n\r\n").getBytes());
//          outputStream.flush();
//          String response = parseInputStream(socket.getInputStream());
          assertTrue(response.contains("<h2>Ping</h2>"));
          assertTrue(response.contains("<li>start time: " + date + "</li>"));
          assertTrue(response.contains("<li>end time: " + delay + "</li>"));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    for (int i = 0; i < 5; i++){
      new MyThread().start();
    }

    outputStream.write(("GET /ping/1 HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    System.out.println(response);
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + date + "</li>"));
    assertTrue(response.contains("<li>end time: " + delay + "</li>"));
  }

  @AfterAll
  static void teardown() throws IOException {
    server.stop();
    socket.close();
  }
}
