package MyServerTests;

import MyServer.MyServer;
import MyServer.Route;
import MyServer.routes.Form;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

import static MyServerTests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormsTest {
  static MyServer server;
  static Socket socket;
  static OutputStream outputStream;
  static HashMap<String, Route> routes = new HashMap<>();


  @BeforeAll
  static void setup() {
    routes.put("/form", new Form());
    server = new MyServer(1237, "testroot", routes);
    server.start();
  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1237);
    outputStream = socket.getOutputStream();
  }

  @Test
  void form() throws IOException {
    outputStream.write(("GET /form HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    assertTrue(response.contains("<h2>GET Form</h2>"));
    assertTrue(response.contains("<form method=\"get\" action=\"/form\">"));
    assertTrue(response.contains("<label for=\"foo\">Foo:</label>"));
    assertTrue(response.contains("<input type=\"text\" name=\"foo\" id=\"foo\"/>"));
    assertTrue(response.contains("<input type=\"submit\" value=\"Submit\"/>"));
    assertTrue(response.contains("</form>"));

    assertTrue(response.contains("<h2>POST Form</h2>"));
    assertTrue(response.contains("<form method=\"post\" action=\"/form\" enctype=\"multipart/form-data\">"));
    assertTrue(response.contains("<label>File:</label>"));
    assertTrue(response.contains("<input type=\"file\" name=\"file\"/>"));
    assertTrue(response.contains("<input type=\"submit\" value=\"Submit\"/>"));
    assertTrue(response.contains("</form>"));
  }

  @Test
  void formOneParam() throws IOException {
    outputStream.write(("GET /form?foo=1 HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    String body = response.split("\r\n\r\n")[1];
    int i = response.length();

    assertEquals("<html>", body.substring(0, 6));
    assertTrue(response.contains("<h2>GET Form</h2>"));
    assertTrue(response.contains("<li>foo: 1</li>"));
    assertEquals("</html>\r\n", response.substring(i - 9));
  }

  @Test
  void formTwoParams() throws IOException {
    outputStream.write(("GET /form?foo=1&bar=2 HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());

    assertTrue(response.contains("<h2>GET Form</h2>"));
    assertTrue(response.contains("<li>foo: 1</li>"));
    assertTrue(response.contains("<li>bar: 2</li>"));
  }

  @Test
  void post() throws IOException {
    FileInputStream file = new FileInputStream("testroot/img/autobot.jpg");
    byte[] fileBytes = file.readAllBytes();
    outputStream.write(("POST /form HTTP/1.1\r\n").getBytes());
    outputStream.write(("Content-Length: 58638\r\n\r\n").getBytes());
    outputStream.write(("------WebKitFormBoundaryz3skuKJCdTzwsajI\r\n").getBytes());
    outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"autobot.jpg\"\r\n").getBytes());
    outputStream.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
    outputStream.write(fileBytes);
    outputStream.write(("\r\n").getBytes());
    outputStream.write(("------WebKitFormBoundaryz3skuKJCdTzwsajI--\r\n\r\n").getBytes());
    outputStream.flush();

    String response = parseInputStream(socket.getInputStream());
    int i = response.length();

    assertTrue(response.contains("<h2>POST Form</h2>"));
    assertTrue(response.contains("<li>file name: autobot.jpg</li>"));
    assertTrue(response.contains("<li>content type: image/jpeg</li>"));
    assertTrue(response.contains("<li>file size: 58453</li>"));
    assertEquals("</html>\r\n", response.substring(i - 9));
    file.close();
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
