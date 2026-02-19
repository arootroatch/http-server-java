package myservertests;

import myserver.ConnectionData;
import myserver.HttpRequest;
import myserver.routes.Ping;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PingTest {

  @Test
  void rendersHTML() {
    HttpRequest request = new HttpRequest("GET", "/ping", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Ping().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time:"));
    assertTrue(response.contains("<li>end time:"));
  }

  @Test
  void invalidDelay() {
    HttpRequest request = new HttpRequest("GET", "/ping/abc", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Ping().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h2>Ping</h2>"));
  }

  @Test
  void negativeDelay() {
    HttpRequest request = new HttpRequest("GET", "/ping/-5", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Ping().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h2>Ping</h2>"));
  }

}
