package MyServerTests;

import MyServer.MyServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Scanner;

import static MyServer.MyServer.getContentsOfDir;
import static MyServer.MyServer.renderContentsAsHTML;
import static MyServerTests.URLConnection.connectToURL;
import static MyServerTests.URLConnection.parseResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesTest {
  MyServer server;

  @BeforeEach
  void setup() {
    server = new MyServer(1234, "testroot");
    server.start();
  }

  @Test
  void listing() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234/listing");
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
    HttpURLConnection connection = connectToURL("http://localhost:1234/listing/img");
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
  void servesHTML() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234/index.html");
    StringBuilder response = parseResponse(connection.getInputStream());
    String header = connection.getHeaderField("Content-Type");
    System.out.println(header);
    String file = readFile("testroot/index.html");
    assertEquals(file, response.toString());
    assertTrue(header.contains("text/html"));
  }

  @Test
  void servesJPG() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1234/img/autobot.jpg");
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
    HttpURLConnection connection = connectToURL("http://localhost:1234/img/decepticon.png");
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
    HttpURLConnection connection = connectToURL("http://localhost:1234/hello.pdf");
    StringBuilder response = parseResponse(connection.getInputStream());
    String header = connection.getHeaderField("Content-Type");

    File file = new File("testroot/hello.pdf");
    FileInputStream fileInputStream = new FileInputStream(file);
    StringBuilder image = parseResponse(fileInputStream);

    assertTrue(image.toString().contains(response));
    assertTrue(header.contains("application/pdf"));
  }

  @Test
  void listContents() {
    Object[] files = getContentsOfDir("testroot");
    assertTrue(Arrays.toString(files).contains("forms.html"));
    assertTrue(Arrays.toString(files).contains("hello.pdf"));
    assertTrue(Arrays.toString(files).contains("index.html"));
    assertTrue(Arrays.toString(files).contains("img"));
  }

  @Test
  void listContentsImg() {
    Object[] files = getContentsOfDir("testroot/img");
    assertTrue(Arrays.toString(files).contains("autobot.jpg"));
    assertTrue(Arrays.toString(files).contains("autobot.png"));
    assertTrue(Arrays.toString(files).contains("decepticon.jpg"));
    assertTrue(Arrays.toString(files).contains("decepticon.png"));
  }

  @Test
  void createHTML() {
    String html = renderContentsAsHTML(getContentsOfDir("testroot"));
    System.out.println(html);
    int i = html.length();
    assertEquals("<ul>", html.substring(0, 4));
    assertEquals("</ul>\r\n", html.substring(i - 7));
    assertTrue(html.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(html.contains("<li><a href=\"/forms.html\">forms.html</a></li>"));
    assertTrue(html.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(html.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @AfterEach
  void teardown() {
    server.stop();
  }

  private static String readFile(String path){
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
