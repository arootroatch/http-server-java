package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.MultipartParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MultipartParserTest {

  private byte[] buildMultipartBody(String filename, String contentType, byte[] fileBytes) throws IOException {
    ByteArrayOutputStream bodyBuilder = new ByteArrayOutputStream();
    bodyBuilder.write("------boundary\r\n".getBytes());
    bodyBuilder.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes());
    bodyBuilder.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes());
    bodyBuilder.write(fileBytes);
    bodyBuilder.write("\r\n------boundary--\r\n".getBytes());
    return bodyBuilder.toByteArray();
  }

  @Test
  void extractsFileName() throws IOException {
    byte[] body = buildMultipartBody("test.jpg", "image/jpeg", new byte[]{1, 2, 3});
    assertEquals("test.jpg", MultipartParser.getFileName(body));
  }

  @Test
  void extractsContentType() throws IOException {
    byte[] body = buildMultipartBody("test.jpg", "image/jpeg", new byte[]{1, 2, 3});
    assertEquals("image/jpeg", MultipartParser.getContentType(body));
  }

  @Test
  void calculatesFileSize() throws IOException {
    byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};
    byte[] body = buildMultipartBody("test.jpg", "image/jpeg", fileBytes);
    assertEquals(5, MultipartParser.getFileSize(body));
  }

  @Test
  void handlesEmptyBody() {
    byte[] body = new byte[0];
    assertEquals("(unknown)", MultipartParser.getFileName(body));
    assertEquals("(unknown)", MultipartParser.getContentType(body));
    assertEquals(0, MultipartParser.getFileSize(body));
  }

  @Test
  void handlesMalformedBody() {
    byte[] body = "not a valid multipart body".getBytes();
    assertEquals("(unknown)", MultipartParser.getFileName(body));
    assertEquals("(unknown)", MultipartParser.getContentType(body));
    assertEquals(0, MultipartParser.getFileSize(body));
  }
}
