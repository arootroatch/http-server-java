package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.Route;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static com.alexrootroatch.httpserver.routes.DirectoryContents.getContentsOfDir;
import static com.alexrootroatch.httpserver.routes.DirectoryContents.renderAsHTML;
import static com.alexrootroatch.httpserver.routes.HttpResponse.send404;
import static com.alexrootroatch.httpserver.routes.HttpResponse.sendFile;
import static com.alexrootroatch.httpserver.routes.HttpResponse.sendString;

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
      String safeDir = HtmlUtil.escapeHtml(path);
      String html = renderAsHTML(contents,
          item -> new File(rootDir + path + "/" + item).isFile(),
          item -> safeDir + "/" + HtmlUtil.escapeHtml(item),
          item -> "/" + safeDir + "/" + HtmlUtil.escapeHtml(item));
      sendString(html, "html", outputStream);
    }
  }
}
