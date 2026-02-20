package com.alexrootroatch.httpserver.routes;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MultipartParser {
  private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\"([^\"]*)\"");
  private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("Content-Type:\\s*(.+)");

  private MultipartParser() {}

  public static String getFileName(byte[] body) {
    String metadata = extractMetadata(body);
    if (metadata.isEmpty()) return "(unknown)";
    Matcher matcher = FILENAME_PATTERN.matcher(metadata);
    return matcher.find() ? matcher.group(1) : "(unknown)";
  }

  public static String getContentType(byte[] body) {
    String metadata = extractMetadata(body);
    if (metadata.isEmpty()) return "(unknown)";
    Matcher matcher = CONTENT_TYPE_PATTERN.matcher(metadata);
    return matcher.find() ? matcher.group(1).trim() : "(unknown)";
  }

  public static int getFileSize(byte[] body) {
    int headerEnd = findDoubleCRLF(body);
    if (headerEnd == -1) return 0;
    int fileStart = headerEnd + 4;

    int fileEnd = body.length;
    for (int i = body.length - 4; i >= fileStart; i--) {
      if (body[i] == '\r' && body[i + 1] == '\n' && body[i + 2] == '-' && body[i + 3] == '-') {
        fileEnd = i;
        break;
      }
    }
    return fileEnd - fileStart;
  }

  static String extractMetadata(byte[] body) {
    int pos = findDoubleCRLF(body);
    if (pos == -1) return "";
    return new String(body, 0, pos, StandardCharsets.US_ASCII);
  }

  static int findDoubleCRLF(byte[] data) {
    if (data.length < 4) return -1;
    for (int i = 0; i < data.length - 3; i++) {
      if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
        return i;
      }
    }
    return -1;
  }
}
