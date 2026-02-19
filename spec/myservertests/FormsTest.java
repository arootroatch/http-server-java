package myservertests;

import myserver.ConnectionData;
import myserver.HttpRequest;
import myserver.routes.Form;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FormsTest {

  @Test
  void form() {
    HttpRequest request = new HttpRequest("GET", "/form", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Form().serve(connData, out);

    String response = out.toString();
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
    assertTrue(response.contains("</form>"));
  }

  @Test
  void formOneParam() {
    HttpRequest request = new HttpRequest("GET", "/form", "foo=1", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Form().serve(connData, out);

    String response = out.toString();
    String body = response.split("\r\n\r\n")[1];

    assertTrue(body.startsWith("<html>"));
    assertTrue(response.contains("<h2>GET Form</h2>"));
    assertTrue(response.contains("<li>foo: 1</li>"));
    assertTrue(response.endsWith("</html>"));
  }

  @Test
  void formTwoParams() {
    HttpRequest request = new HttpRequest("GET", "/form", "foo=1&bar=2", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Form().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("<h2>GET Form</h2>"));
    assertTrue(response.contains("<li>foo: 1</li>"));
    assertTrue(response.contains("<li>bar: 2</li>"));
  }

  @Test
  void post() throws IOException {
    FileInputStream file = new FileInputStream("testroot/img/autobot.jpg");
    byte[] fileBytes = file.readAllBytes();
    file.close();

    ByteArrayOutputStream bodyBuilder = new ByteArrayOutputStream();
    bodyBuilder.write("------WebKitFormBoundaryz3skuKJCdTzwsajI\r\n".getBytes());
    bodyBuilder.write("Content-Disposition: form-data; name=\"file\"; filename=\"autobot.jpg\"\r\n".getBytes());
    bodyBuilder.write("Content-Type: image/jpeg\r\n\r\n".getBytes());
    bodyBuilder.write(fileBytes);
    bodyBuilder.write("\r\n------WebKitFormBoundaryz3skuKJCdTzwsajI--\r\n".getBytes());
    byte[] body = bodyBuilder.toByteArray();

    HttpRequest request = new HttpRequest("POST", "/form", "",
        Map.of("Content-Length", String.valueOf(body.length)), body);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new Form().serve(connData, out);

    String response = out.toString();

    assertTrue(response.contains("<h2>POST Form</h2>"));
    assertTrue(response.contains("<li>file name: autobot.jpg</li>"));
    assertTrue(response.contains("<li>content type: image/jpeg</li>"));
    assertTrue(response.contains("<li>file size: " + fileBytes.length + "</li>"));
    assertTrue(response.endsWith("</html>"));
  }
}
