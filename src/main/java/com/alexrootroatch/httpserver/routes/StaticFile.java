package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.RequestDispatcher;
import com.alexrootroatch.httpserver.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.alexrootroatch.httpserver.routes.http.HttpResponse.send404;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.sendFile;

public class StaticFile implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String rootDir = connData.rootDir();
    String path = connData.request().path();

    if (!RequestDispatcher.isPathSafe(rootDir, path)) {
      send404(outputStream);
      return;
    }

    try (FileInputStream file = new FileInputStream(rootDir + path)) {
      int dotIndex = path.lastIndexOf('.');
      String filetype = dotIndex >= 0 ? path.substring(dotIndex + 1) : "";
      sendFile(file, filetype, outputStream);
    } catch (IOException e) {
      send404(outputStream);
    }
  }
}
