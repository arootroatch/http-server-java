package myserver.routes;

import myserver.ConnectionData;
import myserver.Route;

import java.io.OutputStream;

import static myserver.routes.DirectoryContents.getContentsOfDir;
import static myserver.routes.Utils.sendHtmlString;

public class Listing implements Route {

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String rootDir = connData.rootDir();
    String path = connData.request().path();

    String[] split = path.split("/");
    if (split.length > 2) {
      String dir = split[2];
      String[] contents = getContentsOfDir(rootDir + "/" + dir);
      sendHtmlString(renderContentsAsHTML("/" + dir, rootDir, contents), "html", outputStream);
    } else {
      sendHtmlString(renderContentsAsHTML(rootDir, rootDir, getContentsOfDir(rootDir)), "html", outputStream);
    }
  }

  private String renderContentsAsHTML(String dir, String rootDir, String[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (String item : contents) {
      String li;
      if (item.contains(".")) {
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
    if (dir.equals(rootDir)) return String.format("<li><a href=\"/%s\">%s</a></li>", item, item);
    else return String.format("<li><a href=\"%s/%s\">%s</a></li>", dir, item, item);
  }

  private String setFolderLi(String dir, String rootDir, String item) {
    if (dir.equals(rootDir)) return String.format("<li><a href=\"/listing/%s\">%s</a></li>", item, item);
    else return String.format("<li><a href=\"/listing/%s/%s\">%s</a></li>", dir, item, item);
  }
}
