package MyServer.Routes;

import java.io.OutputStream;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static MyServer.Response.sendHTMLString;

public class Listing {
  String rootDir;
  OutputStream outputStream;
  String resource;

  public Listing(String rootDir, OutputStream outputStream, String resource) {
    this.rootDir = rootDir;
    this.outputStream = outputStream;
    this.resource = resource;
  }

  public void serveListing() {
    String[] split = resource.split("/");
    String dir;
    if (split.length > 2) {
      dir = resource.split("/")[2];
      Object[] contents = getContentsOfDir(rootDir + "/" + dir);
      sendHTMLString(renderContentsAsHTML("/" + dir, contents), outputStream);
    } else sendHTMLString(renderContentsAsHTML(getContentsOfDir(rootDir)), outputStream);
  }
}
