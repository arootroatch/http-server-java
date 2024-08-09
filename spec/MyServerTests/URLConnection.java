package MyServerTests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class URLConnection {
  public static HttpURLConnection connectToURL(String s) throws IOException {
    URL url = URI.create(s).toURL();
    return (HttpURLConnection) url.openConnection();
  }

  public static StringBuilder parseResponse(InputStream inputStream) throws IOException {
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder request = new StringBuilder();
    String line = br.readLine();

    while (line != null) {
      if (line.isBlank()) break;
      request.append(line).append("\r\n");
      line = br.readLine();
    }
    return request;
  }
}
