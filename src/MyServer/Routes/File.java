package MyServer.Routes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static MyServer.Response.send404;
import static MyServer.Response.sendFile;

public class File {
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
