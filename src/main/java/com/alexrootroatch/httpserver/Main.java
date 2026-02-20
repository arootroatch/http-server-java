package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

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

    GameSession gameSession = new GameSession();
    Map<String, Route> routes = new HashMap<>();
    routes.put("/form", new Form());
    routes.put("/guess", new Guess(gameSession));
    routes.put("/hello", new Hello());
    routes.put("/listing", new Listing());
    routes.put("/ping", new Ping());

    MyServer server = new MyServer(port, rootDir, routes);
    server.start();
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
    String dir = (indexOfArg > -1 && indexOfArg + 1 < args.length) ? args[indexOfArg + 1] : "testroot";
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
