package MyServer;

import java.util.Objects;

public class Main {
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

    MyServer server = new MyServer(port, "testroot");
    server.start();
  }

  private static Integer setPort(String[] args) {
    int indexOfArg = contains(args, "-p");
    if (indexOfArg > -1) return Integer.parseInt(args[indexOfArg + 1]) ;
    else return 80;
  }

  private static String setRootDir(String[] args) {
    int indexOfArg = contains(args, "-r");
    if (indexOfArg > -1) return args[indexOfArg + 1];
    else return "root";
  }

  private static int contains(String[] args, String s) {
    if (args == null) return -1;

    for (int i = 0; i < args.length; i++) {
      if (Objects.equals(args[i], s)) return i;
    }
    return -1;
  }

}
