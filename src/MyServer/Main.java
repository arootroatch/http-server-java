package MyServer;

import MyServer.routes.Form;
import MyServer.routes.Hello;
import MyServer.routes.Listing;
import MyServer.routes.Ping;

import java.util.HashMap;
import java.util.Objects;

public class Main {
  private static final HashMap<String, Route> routes = new HashMap<>();

  public static void main(String[] args) {
    int port = setPort(args);
    String rootDir = setRootDir(args);

    if (contains(args, "-h") > -1) {
      Print.printHelp();
      return;
    }

    if (contains(args, "-x") > -1) {
      Print.printConfig(port, rootDir);
      return;
    }

    setRoute("/form", new Form());
    setRoute("/hello", new Hello());
    setRoute("/listing", new Listing());
    setRoute("/ping", new Ping());

    MyServer server = new MyServer(port, rootDir, routes);
    server.start();
  }

  public static void setRoute(String route, Route classname){
      routes.put(route, classname);
  }

  public static void removeRoute(String route){
    routes.remove(route);
  }

  private static Integer setPort(String[] args) {
    int indexOfArg = contains(args, "-p");
    if (indexOfArg > -1) return Integer.parseInt(args[indexOfArg + 1]) ;
    else return 80;
  }

  private static String setRootDir(String[] args) {
    String path = "/Users/AlexRoot-Roatch/current-projects/http-server-java/";
    int indexOfArg = contains(args, "-r");
    if (indexOfArg > -1) return path + args[indexOfArg + 1];
    else return path + "testroot";
  }

  private static int contains(String[] args, String s) {
    if (args == null) return -1;

    for (int i = 0; i < args.length; i++) {
      if (Objects.equals(args[i], s)) return i;
    }
    return -1;
  }

}
