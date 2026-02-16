package myserver.routes;

import myserver.ConnectionData;
import myserver.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static myserver.routes.DirectoryContents.getContentsOfDir;
import static myserver.routes.Utils.*;

public class Folder implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String rootDir = connData.rootDir();
    String path = connData.request().path();

    String[] contents = getContentsOfDir(rootDir + path);
    if (Arrays.asList(contents).contains("index.html")) {
      try (FileInputStream file = new FileInputStream(rootDir + path + "/index.html")) {
        sendFile(file, "html", outputStream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else if (contents.length == 0) {
      send404(outputStream);
    } else {
      sendHtmlString(renderContentsAsHTML(path, contents), "html", outputStream);
    }
  }

  private String renderContentsAsHTML(String dir, String[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (String item : contents) {
      String li;
      if (item.contains(".")) {
        li = String.format("<li><a href=\"%s/%s\">%s</a></li>", dir, item, item);
      } else {
        li = String.format("<li><a href=\"/%s/%s\">%s</a></li>", dir, item, item);
      }
      html.append(li);
    }
    html.append("</ul>");
    return html.toString();
  }
}
