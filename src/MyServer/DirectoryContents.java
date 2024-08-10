package MyServer;

import java.io.File;
import java.util.Arrays;

public class DirectoryContents {
  public static Object[] getContentsOfDir(String dir) {
    File d = new File(dir);
    File[] files = d.listFiles();
    Object[] empty = new Object[0];
    if (files != null) return Arrays.stream(files).map(File::getName).toArray();
    else return empty;
  }

  public static String renderContentsAsHTML(Object[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (Object i : contents) {
      String li;
      if (i.toString().contains(".")) {
        li = String.format("<li><a href=\"/%s\">%s</a></li>", i, i);
      } else {
        li = String.format("<li><a href=\"/listing/%s\">%s</a></li>", i, i);
      }
      html.append(li);
    }
    html.append("</ul>\r\n");
    return html.toString();
  }

  public static String renderContentsAsHTML(String dir, Object[] contents) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (Object i : contents) {
      String li;
      if (i.toString().contains(".")) {
        li = String.format("<li><a href=\"%s/%s\">%s</a></li>", dir, i, i);
      } else {
        li = String.format("<li><a href=\"/listing/%s/%s\">%s</a></li>", dir, i, i);
      }
      html.append(li);
    }
    html.append("</ul>");
    return html.toString();
  }
}
