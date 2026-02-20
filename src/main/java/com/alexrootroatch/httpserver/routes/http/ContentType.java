package com.alexrootroatch.httpserver.routes.http;

public final class ContentType {
  private ContentType() {
  }

  public static String contentTypeHeader(String filetype) {
    return switch (filetype) {
      case "jpg", "jpeg" -> "Content-Type: image/jpeg\r\n";
      case "png" -> "Content-Type: image/png\r\n";
      case "gif" -> "Content-Type: image/gif\r\n";
      case "svg" -> "Content-Type: image/svg+xml\r\n";
      case "ico" -> "Content-Type: image/x-icon\r\n";
      case "txt" -> "Content-Type: text/plain\r\n";
      case "html", "htm" -> "Content-Type: text/html\r\n";
      case "css" -> "Content-Type: text/css\r\n";
      case "js" -> "Content-Type: application/javascript\r\n";
      case "json" -> "Content-Type: application/json\r\n";
      case "xml" -> "Content-Type: application/xml\r\n";
      case "pdf" -> "Content-Type: application/pdf\r\n";
      default -> "Content-Type: application/octet-stream\r\n";
    };
  }
}
