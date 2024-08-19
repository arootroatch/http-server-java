package MyServer.Routes;

import MyServer.Route;

import java.io.OutputStream;

import static MyServer.DirectoryContents.getContentsOfDir;

public class Listing implements Route {
  String rootDir;
  OutputStream outputStream;
  String resource;

  public Listing(String rootDir, OutputStream outputStream, String resource) {
    this.rootDir = rootDir;
    this.outputStream = outputStream;
    this.resource = resource;
  }

  public void serve() {
    String[] split = resource.split("/");
    String dir;
    if (split.length > 2) {
      dir = resource.split("/")[2];
      Object[] contents = getContentsOfDir(rootDir + "/" + dir);
      sendHtmlString(renderContentsAsHTML("/" + dir, contents), "html", outputStream);
    } else sendHtmlString(renderContentsAsHTML("/", getContentsOfDir(rootDir)), "html", outputStream);
  }

  private String renderContentsAsHTML(String dir, Object[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (Object i : contents) {
      String li;
      if (i.toString().contains(".")) {
        li = setFileLi(dir, i);
      } else {
        li = setFolderLi(dir, i);
      }
      html.append(li);
    }
    html.append("</ul>");
    return html.toString();
  }

  private String setFileLi(String dir, Object i) {
    if (dir.equals("/")) return String.format("<li><a href=\"/%s\">%s</a></li>", i, i);
    else return String.format("<li><a href=\"%s/%s\">%s</a></li>", dir, i, i);
  }

  private String setFolderLi(String dir, Object i){
    if (dir.equals("/")) return String.format("<li><a href=\"/listing/%s\">%s</a></li>", i, i);
    else return String.format("<li><a href=\"/listing/%s/%s\">%s</a></li>", dir, i, i);
  }
}
