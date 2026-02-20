package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.Form;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FormsTest {

  @Test
  void form() {
    String response = TestHelper.serve(new Form(), TestHelper.get("/form"));
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
    String response = TestHelper.serve(new Form(), TestHelper.getWithQuery("/form", "foo=1"));
    String body = TestHelper.responseBody(response);

    assertTrue(body.startsWith("<html>"));
    assertTrue(response.contains("<h2>GET Form</h2>"));
    assertTrue(response.contains("<li>foo: 1</li>"));
    assertTrue(response.endsWith("</html>"));
  }

  @Test
  void formTwoParams() {
    String response = TestHelper.serve(new Form(), TestHelper.getWithQuery("/form", "foo=1&bar=2"));
    assertTrue(response.contains("<h2>GET Form</h2>"));
    assertTrue(response.contains("<li>foo: 1</li>"));
    assertTrue(response.contains("<li>bar: 2</li>"));
  }

  @Test
  void post() throws IOException {
    byte[] fileBytes = Files.readAllBytes(Path.of("testroot/img/autobot.jpg"));

    ByteArrayOutputStream bodyBuilder = new ByteArrayOutputStream();
    bodyBuilder.write("------WebKitFormBoundaryz3skuKJCdTzwsajI\r\n".getBytes());
    bodyBuilder.write("Content-Disposition: form-data; name=\"file\"; filename=\"autobot.jpg\"\r\n".getBytes());
    bodyBuilder.write("Content-Type: image/jpeg\r\n\r\n".getBytes());
    bodyBuilder.write(fileBytes);
    bodyBuilder.write("\r\n------WebKitFormBoundaryz3skuKJCdTzwsajI--\r\n".getBytes());
    byte[] body = bodyBuilder.toByteArray();

    String response = TestHelper.serve(new Form(), TestHelper.post("/form", body));

    assertTrue(response.contains("<h2>POST Form</h2>"));
    assertTrue(response.contains("<li>file name: autobot.jpg</li>"));
    assertTrue(response.contains("<li>content type: image/jpeg</li>"));
    assertTrue(response.contains("<li>file size: " + fileBytes.length + "</li>"));
    assertTrue(response.endsWith("</html>"));
  }

  @Test
  void formQueryParamXSS() {
    String response = TestHelper.serve(new Form(),
        TestHelper.getWithQuery("/form", "x=<script>alert(1)</script>"));
    assertTrue(response.contains("&lt;script&gt;"));
    assertFalse(response.contains("<script>"));
  }

  @Test
  void postMalformedMultipart() {
    byte[] body = "garbage data not a valid multipart body".getBytes();
    String response = TestHelper.serve(new Form(), TestHelper.post("/form", body));
    assertTrue(response.contains("200 OK") || response.contains("Invalid upload"));
  }

  @Test
  void postEmptyBody() {
    String response = TestHelper.serve(new Form(), TestHelper.post("/form", new byte[0]));
    assertTrue(response.contains("200 OK") || response.contains("Invalid upload"));
  }
}
