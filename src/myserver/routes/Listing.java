package myserver.routes;

import myserver.ConnectionData;
import myserver.RequestDispatcher;
import myserver.Route;

import java.io.File;
import java.io.OutputStream;

import static myserver.routes.DirectoryContents.getContentsOfDir;
import static myserver.routes.DirectoryContents.renderAsHTML;
import static myserver.routes.Utils.send404;
import static myserver.routes.Utils.sendString;

public class Listing implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String rootDir = connData.rootDir();
    String path = connData.request().path();

    String[] split = path.split("/");
    if (split.length > 2) {
      String dir = split[2];
      if (!RequestDispatcher.isPathSafe(rootDir, "/" + dir)) {
        send404(outputStream);
        return;
      }
      String[] contents = getContentsOfDir(rootDir + "/" + dir);
      String safeDir = Utils.escapeHtml("/" + dir);
      String html = renderAsHTML(contents,
          item -> new File(rootDir + "/" + dir + "/" + item).isFile(),
          item -> safeDir + "/" + Utils.escapeHtml(item),
          item -> "/listing" + safeDir + "/" + Utils.escapeHtml(item));
      sendString(html, "html", outputStream);
    } else {
      String[] contents = getContentsOfDir(rootDir);
      String html = renderAsHTML(contents,
          item -> new File(rootDir + "/" + item).isFile(),
          item -> "/" + Utils.escapeHtml(item),
          item -> "/listing/" + Utils.escapeHtml(item));
      sendString(html, "html", outputStream);
    }
  }
}
