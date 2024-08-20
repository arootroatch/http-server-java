package MyServer;

import java.io.OutputStream;
import java.util.HashMap;

public interface Route {
  void serve(HashMap<String, String> connData, OutputStream outputStream);
}
