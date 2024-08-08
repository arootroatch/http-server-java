import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

public class MyServer {
  public static void main(String[] args) throws IOException {
    String port = setPort(args);
    String rootDir = setRootDir(args);

    if (contains(args, "-h") > -1){
      printHelp();
      return;
    }

    if (contains(args, "-x") > -1){
      printConfig(port, rootDir);
      return;
    }

    try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port))) {
        printConfig(port, rootDir);
//      while (true){
//        try (Socket client = serverSocket.accept()){
//
//        }
//      }
    } catch (IOException e) {
      System.err.println("Error handling client request: " + e.getMessage());
    }
  }

  private static String setPort(String[] args) {
    int indexOfArg = contains(args, "-p");
    if (indexOfArg > -1) return args[indexOfArg + 1];
    else return "80";
  }

  private static String setRootDir(String[] args) {
    int indexOfArg = contains(args, "-r");
    if (indexOfArg > -1) return args[indexOfArg + 1];
    else return "root";
  }

  private static void printHelp() {
    System.out.println("  -p     Specify the port.  Default is 80.");
    System.out.println("  -r     Specify the root directory.  Default is the current working directory.");
    System.out.println("  -h     Print this help message");
    System.out.println("  -x     Print the startup configuration without starting the server");
  }

  private static int contains(String[] args, String s) {
    if (args == null) return -1;

    for (int i = 0; i < args.length; i++) {
      if (Objects.equals(args[i], s)) return i;
    }
    return -1;
  }

  private static void printConfig(String port, String rootDir){
    System.out.println("MyServer");
    System.out.println("Running on port: " + port);
    System.out.println("Serving files from: " + rootDir);
  }
}
