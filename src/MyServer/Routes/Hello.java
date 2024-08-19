package MyServer.Routes;

import MyServer.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class Hello implements Route {
  String rootDir;
  OutputStream outputStream;

  public Hello(HashMap<String, String> connData, OutputStream outputStream){
    this.rootDir = connData.get("rootDir");
    this.outputStream = outputStream;
  }

  public void serve(){
    try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
      sendFile(file, "html", outputStream);
    } catch (IOException e) {
      send404(outputStream);
    }
  }
}
