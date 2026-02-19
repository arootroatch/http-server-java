package myserver.routes;

import myserver.ConnectionData;
import myserver.RequestDispatcher;
import myserver.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static myserver.routes.Utils.send404;
import static myserver.routes.Utils.sendFile;

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
