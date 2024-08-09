package MyServer;

import java.util.Objects;

public class Print {
  public static void printHelp() {
    System.out.println("  -p     Specify the port.  Default is 80.");
    System.out.println("  -r     Specify the root directory.  Default is the current working directory.");
    System.out.println("  -h     Print this help message");
    System.out.println("  -x     Print the startup configuration without starting the server");
  }

  public static void printConfig(int port, String rootDir) {
    System.out.println("MyServer");
    System.out.println("Running on port: " + port);
    System.out.println("Serving files from: " + rootDir);
  }
}
