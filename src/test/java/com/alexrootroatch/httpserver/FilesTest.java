package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.HttpRequest;
import com.alexrootroatch.httpserver.routes.Folder;
import com.alexrootroatch.httpserver.routes.Listing;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FilesTest {

  private String serveAndGetBody(String method, String path, String rootDir, com.alexrootroatch.httpserver.Route route) {
    HttpRequest request = new HttpRequest(method, path, "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, rootDir);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    route.serve(connData, out);
    return out.toString();
  }

  @Test
  void listing() {
    String response = serveAndGetBody("GET", "/listing", "testroot", new Listing());
    String body = response.split("\r\n\r\n")[1];

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(body.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(body.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingSlash() {
    String response = serveAndGetBody("GET", "/listing/", "testroot", new Listing());
    String body = response.split("\r\n\r\n")[1];

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(body.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(body.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingImg() {
    String response = serveAndGetBody("GET", "/listing/img", "testroot", new Listing());
    String body = response.split("\r\n\r\n")[1];

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void img() {
    String response = serveAndGetBody("GET", "/img", "testroot", new Folder());
    String body = response.split("\r\n\r\n")[1];

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void dirIndex() {
    String response = serveAndGetBody("GET", "/test-dir", "root", new Folder());

    assertTrue(response.contains("<h1>Hello, World!</h1>"));
    assertTrue(response.contains(
        "<p>You have reached the index.html file in root/test-dir of the http-spec project.</p>"));
  }

  @Test
  void servesHTML() throws IOException {
    HttpRequest request = new HttpRequest("GET", "/index.html", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new com.alexrootroatch.httpserver.routes.StaticFile().serve(connData, out);

    String response = out.toString();
    String file = Files.readString(Path.of("testroot/index.html"));
    assertTrue(response.contains(file));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void servesJPG() {
    HttpRequest request = new HttpRequest("GET", "/img/autobot.jpg", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new com.alexrootroatch.httpserver.routes.StaticFile().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("Content-Type: image/jpeg"));
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void servesPNG() {
    HttpRequest request = new HttpRequest("GET", "/img/decepticon.png", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new com.alexrootroatch.httpserver.routes.StaticFile().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("Content-Type: image/png"));
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void listingPathTraversal() {
    String response = serveAndGetBody("GET", "/listing/../../etc", "testroot", new Listing());
    assertTrue(response.contains("404 Not Found"));
  }

  @Test
  void servesPDF() {
    HttpRequest request = new HttpRequest("GET", "/hello.pdf", "", Map.of(), new byte[0]);
    ConnectionData connData = new ConnectionData(request, "testroot");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    new com.alexrootroatch.httpserver.routes.StaticFile().serve(connData, out);

    String response = out.toString();
    assertTrue(response.contains("Content-Type: application/pdf"));
    assertTrue(response.contains("200 OK"));
  }
}
