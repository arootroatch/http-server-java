package com.alexrootroatch.httpserver;

public record ConnectionData(HttpRequest request, String rootDir) {
}
