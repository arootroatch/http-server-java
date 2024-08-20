package MyServerTests;

import MyServer.MyServer;
import MyServer.Route;
import MyServer.routes.Listing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import static MyServerTests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesTest {
  static MyServer server;
  static Socket socket;
  static OutputStream outputStream;
  static HashMap<String, Route> routes = new HashMap<>();

  @BeforeAll
  static void setup() {
    routes.put("/listing", new Listing());
    server = new MyServer(1235, "testroot", routes);
    server.start();
  }

  @BeforeEach
  void openSocket() throws IOException {
    socket = new Socket("127.0.0.1", 1235);
    outputStream = socket.getOutputStream();
  }

  @Test
  void listing() throws IOException {
    outputStream.write(("GET /listing HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    String body = response.split("\r\n\r\n")[1];
    int i = body.length();

    assertEquals("<ul>", body.substring(0, 4));
    assertEquals("</ul>\r\n", body.substring(i - 7));
    assertTrue(body.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(body.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(body.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingSlash() throws IOException {
    outputStream.write(("GET /listing/ HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    String body = response.split("\r\n\r\n")[1];
    int i = body.length();

    assertEquals("<ul>", body.substring(0, 4));
    assertEquals("</ul>\r\n", body.substring(i - 7));
    assertTrue(body.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(body.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(body.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingImg() throws IOException {
    outputStream.write(("GET /listing/img HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    String body = response.split("\r\n\r\n")[1];
    int i = body.length();

    assertEquals("<ul>", body.substring(0, 4));
    assertEquals("</ul>\r\n", body.substring(i - 7));
    assertTrue(body.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void img() throws IOException {
    outputStream.write(("GET /img HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    String body = response.split("\r\n\r\n")[1];
    int i = body.length();

    assertEquals("<ul>", body.substring(0, 4));
    assertEquals("</ul>\r\n", body.substring(i - 7));
    assertTrue(body.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void dirIndex() throws IOException {
    MyServer server1 = new MyServer(1236, "root", routes);
    server1.start();
    Socket socket1 = new Socket("127.0.0.1", 1236);
    OutputStream outputStream1 = socket1.getOutputStream();

    outputStream1.write(("GET /test-dir HTTP/1.1\r\n\r\n").getBytes());
    outputStream1.flush();
    String response = parseInputStream(socket1.getInputStream());

    assertTrue(response.contains("<h1>Hello, World!</h1>"));
    assertTrue(response
        .contains("<p>You have reached the index.html file in root/test-dir of the http-spec project.</p>"));

    server1.stop();
    socket1.close();
  }

  @Test
  void servesHTML() throws IOException {
    outputStream.write(("GET /index.html HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());
    String file = parseInputStream(new FileInputStream("testroot/index.html"));

    assertTrue(response.contains(file));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void servesJPG() throws IOException {
    outputStream.write(("GET /img/autobot.jpg HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());

    File file = new File("testroot/img/autobot.jpg");
    FileInputStream fileInputStream = new FileInputStream(file);
    String image = parseInputStream(fileInputStream);

    assertTrue(response.contains(image));
    assertTrue(response.contains("Content-Type: image/jpeg"));
  }

  @Test
  void servesPNG() throws IOException {
    outputStream.write(("GET /img/decepticon.png HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());

    File file = new File("testroot/img/decepticon.png");
    FileInputStream fileInputStream = new FileInputStream(file);
    String image = parseInputStream(fileInputStream);

    assertTrue(response.contains(image));
    assertTrue(response.contains("Content-Type: image/png"));
  }

  @Test
  void servesPDF() throws IOException {
    outputStream.write(("GET /hello.pdf HTTP/1.1\r\n\r\n").getBytes());
    outputStream.flush();
    String response = parseInputStream(socket.getInputStream());

    File file = new File("testroot/hello.pdf");
    FileInputStream fileInputStream = new FileInputStream(file);
    String image = parseInputStream(fileInputStream);

    assertTrue(response.contains(image));
    assertTrue(response.contains("Content-Type: application/pdf"));
  }


  @AfterAll
  static void teardown() {
    server.stop();
  }
}
