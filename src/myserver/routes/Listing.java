package myserver.routes;

import myserver.ConnectionData;
import myserver.RequestDispatcher;
import myserver.Route;

import java.io.File;
import java.io.OutputStream;

import static myserver.routes.DirectoryContents.getContentsOfDir;
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
      sendString(renderContentsAsHTML("/" + dir, rootDir, contents), "html", outputStream);
    } else {
      sendString(renderContentsAsHTML(rootDir, rootDir, getContentsOfDir(rootDir)), "html", outputStream);
    }
  }

  private String renderContentsAsHTML(String dir, String rootDir, String[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (String item : contents) {
      String li;
      String fullPath = dir.equals(rootDir) ? rootDir + "/" + item : rootDir + dir + "/" + item;
      if (new File(fullPath).isFile()) {
        li = setFileLi(dir, rootDir, item);
      } else {
        li = setFolderLi(dir, rootDir, item);
      }
      html.append(li);
    }
    html.append("</ul>");
    return html.toString();
  }

  private String setFileLi(String dir, String rootDir, String item) {
    String safeItem = Utils.escapeHtml(item);
    if (dir.equals(rootDir)) return String.format("<li><a href=\"/%s\">%s</a></li>", safeItem, safeItem);
    String safeDir = Utils.escapeHtml(dir);
    return String.format("<li><a href=\"%s/%s\">%s</a></li>", safeDir, safeItem, safeItem);
  }

  private String setFolderLi(String dir, String rootDir, String item) {
    String safeItem = Utils.escapeHtml(item);
    if (dir.equals(rootDir)) return String.format("<li><a href=\"/listing/%s\">%s</a></li>", safeItem, safeItem);
    String safeDir = Utils.escapeHtml(dir);
    return String.format("<li><a href=\"/listing/%s/%s\">%s</a></li>", safeDir, safeItem, safeItem);
  }
}
