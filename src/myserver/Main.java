package myserver;

import myserver.routes.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {
  private static final Map<String, Route> routes = new HashMap<>();

  public static void main(String[] args) {
    int port = setPort(args);
    String rootDir = setRootDir(args);

    if (indexOf(args, "-h") > -1) {
      Print.printHelp();
      return;
    }

    if (indexOf(args, "-x") > -1) {
      Print.printConfig(port, rootDir);
      return;
    }

    setRoute("/form", new Form());
    setRoute("/guess", new Guess());
    setRoute("/hello", new Hello());
    setRoute("/listing", new Listing());
    setRoute("/ping", new Ping());

    MyServer server = new MyServer(port, rootDir, routes);
    server.start();
  }

  public static void setRoute(String route, Route handler) {
    routes.put(route, handler);
  }

  private static int setPort(String[] args) {
    int indexOfArg = indexOf(args, "-p");
    if (indexOfArg > -1 && indexOfArg + 1 < args.length) {
      try {
        int port = Integer.parseInt(args[indexOfArg + 1]);
        if (port < 0 || port > 65535) return 80;
        return port;
      } catch (NumberFormatException e) {
        return 80;
      }
    }
    return 80;
  }

  private static String setRootDir(String[] args) {
    int indexOfArg = indexOf(args, "-r");
    String dir = indexOfArg > -1 ? args[indexOfArg + 1] : "testroot";
    try {
      return new File(dir).getCanonicalPath();
    } catch (IOException e) {
      return dir;
    }
  }

  private static int indexOf(String[] args, String s) {
    if (args == null) return -1;

    for (int i = 0; i < args.length; i++) {
      if (Objects.equals(args[i], s)) return i;
    }
    return -1;
  }
}
