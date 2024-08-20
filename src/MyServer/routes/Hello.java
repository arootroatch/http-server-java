package MyServer.routes;

import MyServer.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import static MyServer.routes.Utils.send404;
import static MyServer.routes.Utils.sendFile;

public class Hello implements Route {
  String rootDir;
  OutputStream outputStream;

  public Hello() {
  }

  public void serve(HashMap<String, String> connData, OutputStream outputStream) {
    this.rootDir = connData.get("rootDir");
    this.outputStream = outputStream;

    try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
      sendFile(file, "html", outputStream);
    } catch (IOException e) {
      send404(outputStream);
    }
  }
}
