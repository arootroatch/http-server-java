package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.Folder;
import com.alexrootroatch.httpserver.routes.Listing;
import com.alexrootroatch.httpserver.routes.StaticFile;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FilesTest {

  @Test
  void listing() {
    String response = TestHelper.serve(new Listing(), TestHelper.get("/listing"));
    String body = TestHelper.responseBody(response);

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(body.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(body.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingSlash() {
    String response = TestHelper.serve(new Listing(), TestHelper.get("/listing/"));
    String body = TestHelper.responseBody(response);

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(body.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(body.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }

  @Test
  void listingImg() {
    String response = TestHelper.serve(new Listing(), TestHelper.get("/listing/img"));
    String body = TestHelper.responseBody(response);

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void img() {
    String response = TestHelper.serve(new Folder(), TestHelper.get("/img"));
    String body = TestHelper.responseBody(response);

    assertTrue(body.startsWith("<ul>"));
    assertTrue(body.endsWith("</ul>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/autobot.png\">autobot.png</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    assertTrue(body.contains("<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>"));
  }

  @Test
  void dirIndex() {
    String response = TestHelper.serve(new Folder(), TestHelper.get("/test-dir"), "root");

    assertTrue(response.contains("<h1>Hello, World!</h1>"));
    assertTrue(response.contains(
        "<p>You have reached the index.html file in root/test-dir of the http-spec project.</p>"));
  }

  @Test
  void servesHTML() throws IOException {
    String response = TestHelper.serve(new StaticFile(), TestHelper.get("/index.html"));
    String file = Files.readString(Path.of("testroot/index.html"));
    assertTrue(response.contains(file));
    assertTrue(response.contains("Content-Type: text/html"));
  }

  @Test
  void servesJPG() {
    String response = TestHelper.serve(new StaticFile(), TestHelper.get("/img/autobot.jpg"));
    assertTrue(response.contains("Content-Type: image/jpeg"));
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void servesPNG() {
    String response = TestHelper.serve(new StaticFile(), TestHelper.get("/img/decepticon.png"));
    assertTrue(response.contains("Content-Type: image/png"));
    assertTrue(response.contains("200 OK"));
  }

  @Test
  void listingPathTraversal() {
    String response = TestHelper.serve(new Listing(), TestHelper.get("/listing/../../etc"));
    assertTrue(response.contains("404 Not Found"));
  }

  @Test
  void servesPDF() {
    String response = TestHelper.serve(new StaticFile(), TestHelper.get("/hello.pdf"));
    assertTrue(response.contains("Content-Type: application/pdf"));
    assertTrue(response.contains("200 OK"));
  }
}
