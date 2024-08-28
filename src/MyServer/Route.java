package MyServer;

import java.io.OutputStream;
import java.util.Map;

public interface Route {
  void serve(Map<String, String> connData, OutputStream outputStream);
}
