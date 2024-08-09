package MyServerTests;
import MyServer.MyServer;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;

import static org.junit.jupiter.api.Assertions.*;

public class BasicsTest {
  static MyServer server;

  @BeforeEach
  void setup() {
    server  = new MyServer(1234, "testroot");
  }

  @Test
  void start(){
    assertFalse(server.isRunning());
    assertNull(server.getThread());
    server.start();
    assertTrue(server.isRunning());
    assertNotNull(server.getThread());
    assertTrue(server.getThread().isAlive());
  }

  @Test
  void servesIndex() throws IOException {
    server.start();
    HttpURLConnection connection = connectToURL("http://localhost:1234");
    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertEquals(200, responseCode);
  }

  @Test
  void servesIndexSlash() throws IOException {
    server.start();
    HttpURLConnection connection = connectToURL("http://localhost:1234/");
    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertEquals(200, responseCode);
  }

  @Test
  void servesIndexHTML() throws IOException {
    server.start();
    HttpURLConnection connection = connectToURL("http://localhost:1234/index.html");
    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertEquals(200, responseCode);
  }

  @Test
  void status404() throws IOException {
    server.start();
    HttpURLConnection connection = connectToURL("http://localhost:1234/blah");
//    StringBuilder response = parseResponse(connection.getInputStream());
    int responseCode = connection.getResponseCode();

//    assertTrue(response.toString().contains("<h1>Hello, World!</h1>"));
    assertEquals(404, responseCode);
  }

  @Test
  void serverHeader() throws IOException {
    server.start();
    HttpURLConnection connection = connectToURL("http://localhost:1234/");
    String header = connection.getHeaderField("Server");
    assertNotNull(header);
    assertEquals("My MacBook Pro", header);
  }

  @AfterEach
  void teardown(){
    server.stop();
  }


  private HttpURLConnection connectToURL(String s) throws IOException {
    URL url = URI.create(s).toURL();
    return (HttpURLConnection) url.openConnection();
  }

  private StringBuilder parseResponse(InputStream inputStream) throws IOException {
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder request = new StringBuilder();
    String line = br.readLine();

    while (!line.isBlank()) {
      request.append(line).append("\r\n");
      line = br.readLine();
    }
    return request;
  }

}
