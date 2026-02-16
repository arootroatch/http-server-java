package myserver.routes;

import myserver.ConnectionData;
import myserver.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static myserver.routes.Utils.send404;
import static myserver.routes.Utils.sendFile;

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
