package MyServerTests;

import MyServer.MyServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;

import static MyServer.HTML.getQueryParams;
import static MyServer.HTML.queryParamsToHTML;
import static MyServerTests.URLConnection.connectToURL;
import static MyServerTests.URLConnection.parseInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormsTest {
  static MyServer server;

  @BeforeAll
  static void setup() {
    server = new MyServer(1237, "testroot");
    server.start();
  }

  @Test
  void form() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1237/form");
    StringBuilder response = parseInputStream(connection.getInputStream());
    assertTrue(response.toString().contains("<h2>GET Form</h2>"));
    assertTrue(response.toString().contains("<form method=\"get\" action=\"/form\">"));
    assertTrue(response.toString().contains("<label for=\"foo\">Foo:</label>"));
    assertTrue(response.toString().contains("<input type=\"text\" name=\"foo\" id=\"foo\"/>"));
    assertTrue(response.toString().contains("<input type=\"submit\" value=\"Submit\"/>"));
    assertTrue(response.toString().contains("</form>"));

    assertTrue(response.toString().contains("<h2>POST Form</h2>"));
    assertTrue(response.toString().contains("<form method=\"post\" action=\"/form\" enctype=\"multipart/form-data\">"));
    assertTrue(response.toString().contains("<label>File:</label>"));
    assertTrue(response.toString().contains("<input type=\"file\" name=\"file\"/>"));
    assertTrue(response.toString().contains("<input type=\"submit\" value=\"Submit\"/>"));
    assertTrue(response.toString().contains("</form>"));
  }

  @Test
  void formOneParam() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1237/form?foo=1");
    StringBuilder response = parseInputStream(connection.getInputStream());
    int i = response.length();
    assertEquals("<html>", response.substring(0, 6));
    assertTrue(response.toString().contains("<h2>GET Form</h2>"));
    assertTrue(response.toString().contains("<li>foo: 1</li>"));
    assertEquals("</html>\r\n", response.substring(i - 9));
  }

  @Test
  void formTwoParams() throws IOException {
    HttpURLConnection connection = connectToURL("http://localhost:1237/form?foo=1&bar=2");
    StringBuilder response = parseInputStream(connection.getInputStream());
    int i = response.length();
    assertEquals("<html>", response.substring(0, 6));
    assertTrue(response.toString().contains("<h2>GET Form</h2>"));
    assertTrue(response.toString().contains("<li>foo: 1</li>"));
    assertTrue(response.toString().contains("<li>bar: 2</li>"));
    assertEquals("</html>\r\n", response.substring(i - 9));
  }

  @Test
  void noQueryParams(){
    String[] result = getQueryParams("/form");
    assertEquals(0, result.length);
  }

  @Test
  void oneQueryParam(){
    String[] result = getQueryParams("/form?foo=1");
    assertEquals(1, result.length);
    assertEquals("foo=1", result[0]);
  }

  @Test
  void twoQueryParams(){
    String[] result = getQueryParams("/form?foo=1&bar=2");
    assertEquals(2, result.length);
    assertEquals("foo=1", result[0]);
    assertEquals("bar=2", result[1]);
  }

//  @Test
//  void formPost() throws IOException {
//    HttpURLConnection connection = connectToURL("http://localhost:1237/form");
//    connection.setRequestMethod("POST");
//    connection.setDoOutput(true);
//    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=test-boundary");
//    OutputStream outputStream = connection.getOutputStream();
//    outputStream.write(("--test-boundary\r\n").getBytes());
//    outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" +
//        (new File("testroot/img/autobot.jpg").getName()) + "\"\r\n").getBytes());
//    outputStream.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
//
//    FileInputStream file = new FileInputStream("testroot/img/autobot.jpg");
//    outputStream.write(file.readAllBytes());
//    outputStream.write(("\r\n").getBytes());
//    outputStream.write(("--test-boundary--\r\n").getBytes());
//    outputStream.flush();
//
//    StringBuilder response = parseInputStream(connection.getInputStream());
//    int i = response.length();
//    assertEquals("<html>", response.substring(0, 6));
//    assertTrue(response.toString().contains("<h2>POST Form</h2>"));
//    assertTrue(response.toString().contains("<li>file name: autobot.jpg</li>"));
//    assertTrue(response.toString().contains("<li>content type: image/jpeg</li>"));
//    assertTrue(response.toString().contains("<li>file size: 58588</li>"));
//    assertEquals("</html>\r\n", response.substring(i - 9));
//  }

  @Test
  void paramsToHTML(){
    String[] params = {"foo=1", "bar=2"};
    String result = queryParamsToHTML(params);
    int i = result.length();

    assertEquals("<ul>", result.substring(0, 4));
    assertTrue(result.contains("<li>foo: 1</li>"));
    assertTrue(result.contains("<li>bar: 2</li>"));
    assertTrue(result.contains("<li>bar: 2</li>"));
    assertEquals("</ul>\r\n", result.substring(i - 7));
  }

  @AfterAll
  static void teardown() {
    server.stop();
  }
}
