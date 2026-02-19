package myserver.routes;

import myserver.ConnectionData;
import myserver.Route;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static myserver.routes.DirectoryContents.getContentsOfDir;
import static myserver.routes.DirectoryContents.renderAsHTML;
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
      String safeDir = Utils.escapeHtml(path);
      String html = renderAsHTML(contents,
          item -> new File(rootDir + path + "/" + item).isFile(),
          item -> safeDir + "/" + Utils.escapeHtml(item),
          item -> "/" + safeDir + "/" + Utils.escapeHtml(item));
      sendString(html, "html", outputStream);
    }
  }
}
