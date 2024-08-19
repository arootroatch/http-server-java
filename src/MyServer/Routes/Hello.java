package MyServer.Routes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static MyServer.Response.send404;
import static MyServer.Response.sendFile;

public class Hello {
  String rootDir;
  OutputStream outputStream;

  public Hello(String rootDir, OutputStream outputStream){
    this.rootDir = rootDir;
    this.outputStream = outputStream;
  }

  public void serveHello(){
    try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
      sendFile(file, "html", outputStream);
    } catch (IOException e) {
      send404(outputStream);
    }
  }
}
