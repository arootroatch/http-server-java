package MyServer.routes;

import MyServer.Route;

import java.io.OutputStream;
import java.util.HashMap;

public class RouteMap {
  public static HashMap<String, Route> getRoutes(HashMap<String, String> connData, OutputStream outputStream) {
    HashMap<String, Route> routes = new HashMap<>();
    routes.put("/form", new Form(connData, outputStream));
    routes.put("/hello", new Hello(connData, outputStream));
    routes.put("/listing", new Listing(connData, outputStream));
    routes.put("/ping", new Ping(connData, outputStream));
    return routes;
  }
}
