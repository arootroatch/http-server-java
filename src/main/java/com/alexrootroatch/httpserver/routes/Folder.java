package com.alexrootroatch.httpserver.routes;

import com.alexrootroatch.httpserver.ConnectionData;
import com.alexrootroatch.httpserver.Route;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alexrootroatch.httpserver.routes.http.HtmlUtil;

import static com.alexrootroatch.httpserver.routes.DirectoryContents.getContentsOfDir;
import static com.alexrootroatch.httpserver.routes.DirectoryContents.renderAsHTML;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.send404;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.sendFile;
import static com.alexrootroatch.httpserver.routes.http.HttpResponse.sendString;

public class Folder implements Route {
  private static final Logger logger = Logger.getLogger(Folder.class.getName());

  public void serve(ConnectionData connData, OutputStream outputStream) {
    String rootDir = connData.rootDir();
    String path = connData.request().path();

    String[] contents = getContentsOfDir(rootDir + path);
    if (Arrays.asList(contents).contains("index.html")) {
      try (FileInputStream file = new FileInputStream(rootDir + path + "/index.html")) {
        sendFile(file, "html", outputStream);
      } catch (IOException e) {
        logger.log(Level.WARNING, "Failed to read index.html in " + rootDir + path, e);
        send404(outputStream);
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
