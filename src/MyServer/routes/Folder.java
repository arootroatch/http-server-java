package MyServer.routes;

import MyServer.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

import static MyServer.routes.DirectoryContents.getContentsOfDir;
import static MyServer.routes.Utils.*;

public class Folder implements Route {
  String rootDir;
  String resource;
  OutputStream outputStream;

  public Folder() {
  }

  public void serve(HashMap<String, String> connData, OutputStream outputStream) {
    this.rootDir = connData.get("rootDir");
    this.resource = connData.get("resource");
    this.outputStream = outputStream;

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
      sendHtmlString(renderContentsAsHTML(resource, contents), "html", outputStream);
    }
  }

  private String renderContentsAsHTML(String dir, Object[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (Object i : contents) {
      String li;
      if (i.toString().contains(".")) {
        li = String.format("<li><a href=\"%s/%s\">%s</a></li>", dir, i, i);
      } else {
        li = String.format("<li><a href=\"/%s/%s\">%s</a></li>", dir, i, i);
      }
      html.append(li);
    }
    html.append("</ul>");
    return html.toString();
  }
}
