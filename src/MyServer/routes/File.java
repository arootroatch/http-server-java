package MyServer.routes;

import MyServer.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import static MyServer.routes.Utils.send404;
import static MyServer.routes.Utils.sendFile;

public class File implements Route {
  String rootDir;
  String resource;
  OutputStream outputStream;

  public File() {
  }

  public void serve(HashMap<String, String> connData, OutputStream outputStream) {
    this.rootDir = connData.get("rootDir");
    this.resource = connData.get("resource");
    this.outputStream = outputStream;

    try (FileInputStream file = new FileInputStream(rootDir + resource)) {
      String filetype = resource.split("\\.")[1];
      sendFile(file, filetype, outputStream);
    } catch (IOException e) {
      send404(outputStream);
    }
  }
}
