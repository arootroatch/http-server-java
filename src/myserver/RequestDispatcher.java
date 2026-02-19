package myserver;

import myserver.routes.Folder;
import myserver.routes.StaticFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import static myserver.routes.Utils.send404;
import static myserver.routes.Utils.send405;

public class RequestDispatcher {
  private final Route folderHandler = new Folder();
  private final Route staticFileHandler = new StaticFile();
  private final Map<String, Route> routes;
  private final String rootDir;

  public RequestDispatcher(String rootDir, Map<String, Route> routes) {
    this.rootDir = rootDir;
    this.routes = routes;
  }

  private static final Set<String> RECOGNIZED_METHODS = Set.of(
      "GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

  public void dispatch(HttpRequest request, OutputStream outputStream) {
    String method = request.method();
    if (!method.isEmpty() && !RECOGNIZED_METHODS.contains(method)) {
      send405(outputStream);
      return;
    }

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
        folderHandler.serve(connData, outputStream);
      } else if (file.isFile()) {
        staticFileHandler.serve(connData, outputStream);
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
