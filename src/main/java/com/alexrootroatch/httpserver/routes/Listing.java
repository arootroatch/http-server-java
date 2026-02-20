package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.RequestDispatcher;
import com.alexrootroatch.httpserver.Route;

import com.alexrootroatch.httpserver.routes.http.HtmlUtil;

import java.io.File;
import java.io.OutputStream;

import static com.alexrootroatch.httpserver.routes.DirectoryContents.getContentsOfDir;
import static com.alexrootroatch.httpserver.routes.DirectoryContents.renderAsHTML;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.send404;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.sendString;

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
      String safeDir = HtmlUtil.escapeHtml("/" + dir);
      String html = renderAsHTML(contents,
          item -> new File(rootDir + "/" + dir + "/" + item).isFile(),
          item -> safeDir + "/" + HtmlUtil.escapeHtml(item),
          item -> "/listing" + safeDir + "/" + HtmlUtil.escapeHtml(item));
      sendString(html, "html", outputStream);
    } else {
      String[] contents = getContentsOfDir(rootDir);
      String html = renderAsHTML(contents,
          item -> new File(rootDir + "/" + item).isFile(),
          item -> "/" + HtmlUtil.escapeHtml(item),
          item -> "/listing/" + HtmlUtil.escapeHtml(item));
      sendString(html, "html", outputStream);
    }
  }
}
