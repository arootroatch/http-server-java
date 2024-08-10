package MyServerTests;

import MyServer.MyServer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Scanner;

import static MyServerTests.URLConnection.connectToURL;
import static MyServerTests.URLConnection.parseResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesTest {
  static MyServer server;

  @BeforeAll
  static void setup() {
    server = new MyServer(1235, "testroot");
    server.start();
  }

  @Test
  void listing() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/listing");
    StringBuilder response = parseResponse(connection.getInputStream());
    int i = response.toString().length();
    assertEquals("<ul>", response.substring(0, 4));
    assertEquals("</ul>\r\n", response.substring(i - 7));
    assertTrue(response.toString().contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingSlash() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/listing/");
    StringBuilder response = parseResponse(connection.getInputStream());
    int i = response.toString().length();
    assertEquals("<ul>", response.substring(0, 4));
    assertEquals("</ul>\r\n", response.substring(i - 7));
    assertTrue(response.toString().contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingImg() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/listing/img");
    StringBuilder response = parseResponse(connection.getInputStream());
    int i = response.toString().length();
    assertEquals("<ul>", response.substring(0, 4));
    assertEquals("</ul>\r\n", response.substring(i - 7));
    assertTrue(response.toString().contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void img() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/img");
    StringBuilder response = parseResponse(connection.getInputStream());
    int i = response.toString().length();
    assertEquals("<ul>", response.substring(0, 4));
    assertEquals("</ul>\r\n", response.substring(i - 7));
    assertTrue(response.toString().contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(response.toString().contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void dirIndex() throws IOException {
    MyServer server1 = new MyServer(1236, "root");
    server1.start();

    HttpURLConnection connection = connectToURL("http://localhost:1236/test-dir");
    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertTrue(response.toString()
        .contains("<p>You have reached the index.html file in root/test-dir of the http-spec project.</p>"));
    assertEquals(200, responseCode);

    server1.stop();
  }

  @Test
  void servesHTML() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/index.html");
    StringBuilder response = parseResponse(connection.getInputStream());
    String header = connection.getHeaderField("Content-Type");
    String file = readFile("testroot/index.html");
    assertEquals(file, response.toString());
    assertTrue(header.contains("text/html"));
  }

  @Test
  void servesJPG() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/img/autobot.jpg");
    StringBuilder response = parseResponse(connection.getInputStream());
    String header = connection.getHeaderField("Content-Type");

    File file = new File("testroot/img/autobot.jpg");
    FileInputStream fileInputStream = new FileInputStream(file);
    StringBuilder image = parseResponse(fileInputStream);

    assertTrue(image.toString().contains(response));
    assertTrue(header.contains("image/jpeg"));
  }

  @Test
  void servesPNG() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/img/decepticon.png");
    StringBuilder response = parseResponse(connection.getInputStream());
    String header = connection.getHeaderField("Content-Type");

    File file = new File("testroot/img/decepticon.png");
    FileInputStream fileInputStream = new FileInputStream(file);
    StringBuilder image = parseResponse(fileInputStream);

    assertTrue(image.toString().contains(response));
    assertTrue(header.contains("image/png"));
  }

  @Test
  void servesPDF() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1235/hello.pdf");
    StringBuilder response = parseResponse(connection.getInputStream());
    String header = connection.getHeaderField("Content-Type");

    File file = new File("testroot/hello.pdf");
    FileInputStream fileInputStream = new FileInputStream(file);
    StringBuilder image = parseResponse(fileInputStream);

    assertTrue(image.toString().contains(response));
    assertTrue(header.contains("application/pdf"));
  }


  @AfterAll
  static void teardown() {
    server.stop();
  }

  private static String readFile(String path) {
    StringBuilder contents = new StringBuilder();
    try {
      File myObj = new File(path);
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        if (!data.isBlank()) contents.append(data).append("\r\n");
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    return contents.toString();
  }
}
