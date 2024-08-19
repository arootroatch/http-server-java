package MyServer.Routes;

import MyServer.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class File implements Route {
  String rootDir;
  String resource;
  OutputStream outputStream;

  public File(String rootDir, String resource, OutputStream outputStream){
    this.rootDir = rootDir;
    this.resource = resource;
    this.outputStream = outputStream;
  }

  public void serve(){
    try (FileInputStream file = new FileInputStream(rootDir + resource)) {
      String filetype = resource.split("\\.")[1];
      sendFile(file, filetype, outputStream);
    } catch (IOException e) {
      send404(outputStream);
    }
  }
}
