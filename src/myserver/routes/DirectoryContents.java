package myserver.routes;

import java.io.File;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public class DirectoryContents {
  public static String[] getContentsOfDir(String dir) {
    File d = new File(dir);
    File[] files = d.listFiles();
    if (files != null) return Arrays.stream(files).map(File::getName).toArray(String[]::new);
    else return new String[0];
  }

  public static String renderAsHTML(String[] contents,
                                     Predicate<String> isFilePredicate,
                                     Function<String, String> fileLinkFn,
                                     Function<String, String> folderLinkFn) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (String item : contents) {
      String safeItem = Utils.escapeHtml(item);
      String href;
      if (isFilePredicate.test(item)) {
        href = fileLinkFn.apply(item);
      } else {
        href = folderLinkFn.apply(item);
      }
      html.append(String.format("<li><a href=\"%s\">%s</a></li>", href, safeItem));
    }
    html.append("</ul>");
    return html.toString();
  }
}
