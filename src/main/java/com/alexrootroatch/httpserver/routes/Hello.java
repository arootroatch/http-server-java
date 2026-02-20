package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.alexrootroatch.httpserver.routes.http.HttpResponse.send404;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.sendFile;

public class Hello implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String rootDir = connData.rootDir();

    try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
      sendFile(file, "html", outputStream);
    } catch (IOException e) {
      send404(outputStream);
    }
  }
}
