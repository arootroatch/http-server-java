package MyServer.Routes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static MyServer.Response.*;

public class Folder {
  String rootDir;
  String resource;
  OutputStream outputStream;

  public Folder(String rootDir, String resource, OutputStream outputStream){
    this.rootDir = rootDir;
    this.resource = resource;
    this.outputStream = outputStream;
  }

  public void serve(){
    Object[] contents = getContentsOfDir(rootDir + resource);
    if (Arrays.asList(contents).contains("index.html")) {
      try (FileInputStream file = new FileInputStream(rootDir + resource + "/index.html")) {
        sendFile(file, "html", outputStream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else if (contents.length == 0) {
      send404(outputStream);
    } else {
      sendHTMLString(renderContentsAsHTML(resource, contents), outputStream);
    }
  }
}
