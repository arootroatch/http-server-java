package myserver;

import java.io.OutputStream;

public interface Route {
  void serve(ConnectionData connData, OutputStream outputStream);
}
