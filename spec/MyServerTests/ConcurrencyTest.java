package MyServerTests;

import MyServer.MyServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;

import static MyServerTests.URLConnection.connectToURL;
import static MyServerTests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrencyTest {
  static MyServer server;

  @BeforeAll
  static void setup() {
    server = new MyServer(1238, "testroot");
    server.start();
  }

  @Test
  void currentTime() throws IOException {
    Date date = new Date();
    HttpURLConnection connection = connectToURL("http://localhost:1238/ping");
    StringBuilder response = parseInputStream(connection.getInputStream());
    assertTrue(response.toString().contains("<h2>Ping</h2>"));
    assertTrue(response.toString().contains("<li>start time: " + date + "</li>"));
    assertTrue(response.toString().contains("<li>end time: " + date + "</li>"));
  }

  @Test
  void waitOneSec() throws IOException {
    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, 1);
    Date delay = calendar.getTime();

    HttpURLConnection connection = connectToURL("http://localhost:1238/ping/1");
    StringBuilder response = parseInputStream(connection.getInputStream());
    assertTrue(response.toString().contains("<h2>Ping</h2>"));
    assertTrue(response.toString().contains("<li>start time: " + date + "</li>"));
    assertTrue(response.toString().contains("<li>end time: " + delay + "</li>"));
  }

  @Test
  void waitTwoSecs() throws IOException {
    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, 2);
    Date delay = calendar.getTime();

    HttpURLConnection connection = connectToURL("http://localhost:1238/ping/2");
    StringBuilder response = parseInputStream(connection.getInputStream());
    assertTrue(response.toString().contains("<h2>Ping</h2>"));
    assertTrue(response.toString().contains("<li>start time: " + date + "</li>"));
    assertTrue(response.toString().contains("<li>end time: " + delay + "</li>"));
  }

  @Test
  void concurrent() throws IOException {
    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, 1);
    Date delay = calendar.getTime();

    class MyThread extends Thread {
      public void run(){
        try {
          HttpURLConnection conn = connectToURL("http://localhost:1238/ping/1");
          StringBuilder response = parseInputStream(conn.getInputStream());
          System.out.println(response);
          assertTrue(response.toString().contains("<h2>Ping</h2>"));
          assertTrue(response.toString().contains("<li>start time: " + date + "</li>"));
          assertTrue(response.toString().contains("<li>end time: " + delay + "</li>"));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    MyThread thread = new MyThread();
    thread.start();
    HttpURLConnection conn = connectToURL("http://localhost:1238/ping/1");
    StringBuilder response = parseInputStream(conn.getInputStream());
    System.out.println(response);
    assertTrue(response.toString().contains("<h2>Ping</h2>"));
    assertTrue(response.toString().contains("<li>start time: " + date + "</li>"));
    assertTrue(response.toString().contains("<li>end time: " + delay + "</li>"));
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
