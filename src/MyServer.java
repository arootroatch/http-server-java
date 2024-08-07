import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

public class MyServer {
  public static void main(String[] args) throws IOException {
    String port;
    setPort(args);

    try (ServerSocket serverSocket = new ServerSocket(80)){
      System.out.println("MyServer");
      System.out.println("Running on port: 80");
      System.out.println("Serving files from:");
//      while (true){
//        try (Socket client = serverSocket.accept()){
//
//        }
//      }
    } catch (IOException e){
      System.err.println("Error handling client request: " + e.getMessage());
    }
  }

  private static void setPort(String[] args, String port){
    boolean specifiesPort = false;
    int indexOfArg;

    for (int i = 0; i < args.length; i++){
      if (Objects.equals(args[i], "-p")){
        specifiesPort = true;
        indexOfArg = i;
      }
    }

    if (specifiesPort){
      port = args[indexOfArg + 1];
    }
  }
}
