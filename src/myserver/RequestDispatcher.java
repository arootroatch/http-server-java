package myserver;

import myserver.routes.Folder;
import myserver.routes.StaticFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static myserver.routes.Utils.send404;

public class RequestDispatcher {

  public static void dispatch(HttpRequest request, String rootDir,
                              Map<String, Route> routes, OutputStream outputStream) {
    String path = request.path();

    if (!isPathSafe(rootDir, path)) {
      send404(outputStream);
      return;
    }

    String routeKey = extractRouteKey(path);
    ConnectionData connData = new ConnectionData(request, rootDir);

    if (routes.containsKey(routeKey)) {
      routes.get(routeKey).serve(connData, outputStream);
    } else {
      File file = new File(rootDir + path);
      if (file.isDirectory()) {
        new Folder().serve(connData, outputStream);
      } else if (file.isFile()) {
        new StaticFile().serve(connData, outputStream);
      } else {
        send404(outputStream);
      }
    }
  }

  public static String extractRouteKey(String path) {
    if (path.isEmpty() || path.equals("/")) return "/";
    int secondSlash = path.indexOf('/', 1);
    if (secondSlash > 0) {
      return path.substring(0, secondSlash);
    }
    return path;
  }

  public static boolean isPathSafe(String rootDir, String path) {
    try {
      File root = new File(rootDir).getCanonicalFile();
      File target = new File(rootDir + path).getCanonicalFile();
      return target.getPath().startsWith(root.getPath());
    } catch (IOException e) {
      return false;
    }
  }
}
