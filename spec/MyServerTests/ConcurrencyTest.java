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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static MyServerTests.URLConnection.connectToURL;
import static MyServerTests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrencyTest {
  static MyServer server;
  static Socket socket;
  static OutputStream outputStream;
  Date date;
  String pattern;
  SimpleDateFormat simpleDateFormat;
  String start;
  Calendar calendar;

  @BeforeAll
  static void setup() throws IOException {
    server = new MyServer(1238, "testroot");
    server.start();

  }

  @BeforeEach
  void dateSetup(){
    date = new Date();
    pattern = "yyyy-MM-dd hh:mm:ss";
    simpleDateFormat = new SimpleDateFormat(pattern);
    start = simpleDateFormat.format(date);
    calendar = Calendar.getInstance();
    calendar.setTime(date);
  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1238);
    outputStream = socket.getOutputStream();
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
  void concurrent() throws IOException {
    calendar.add(Calendar.SECOND, 1);
    Date delay = calendar.getTime();
    String end = simpleDateFormat.format(delay);

    class MyThread extends Thread {
      public void run() {
        try {
          HttpURLConnection conn = connectToURL("http://localhost:1238/ping/1");
          String response = parseInputStream(conn.getInputStream());
//          outputStream.write(("GET /ping/1 HTTP/1.1\r\n\r\n").getBytes());
//          outputStream.flush();
//          String response = parseInputStream(socket.getInputStream());
          assertTrue(response.contains("<h2>Ping</h2>"));
          assertTrue(response.contains("<li>start time: " + start + "</li>"));
          assertTrue(response.contains("<li>end time: " + end + "</li>"));
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
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time: " + start + "</li>"));
    assertTrue(response.contains("<li>end time: " + end + "</li>"));
  }

  @AfterAll
  static void teardown() throws IOException {
    server.stop();
    socket.close();
  }
}
