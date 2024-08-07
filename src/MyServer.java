import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
  public static void main(String[] args) throws IOException {
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
}
