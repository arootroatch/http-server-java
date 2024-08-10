package MyServer;

import java.io.*;
import java.util.Arrays;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static MyServer.Response.*;

public class Request {
  public static StringBuilder parseRequest(InputStream inputStream) throws IOException {
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

  public static String parseResource(StringBuilder request) {
    String firstLine = request.toString().split("\r\n")[0];
    String resource = "";
    if (!firstLine.isBlank()) resource = firstLine.split(" ")[1];
    return resource;
  }

  public static void handleRequest(StringBuilder request, OutputStream outputStream, String rootDir) {
    String resource = parseResource(request);

    if (resource.equals("/") || resource.equals("/hello")) {
      try (FileInputStream file = new FileInputStream(rootDir + "/index.html")) {
        sendFile(file, "html", outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }
    } else if (resource.equals("/listing")) {
      sendHTMLString(renderContentsAsHTML(getContentsOfDir(rootDir)), outputStream);
    } else if (resource.contains("/listing/")) {
      String[] split = resource.split("/");
      String dir = "";
      if (split.length > 2) {
        dir = resource.split("/")[2];
        Object[] contents = getContentsOfDir(rootDir + "/" + dir);
        sendHTMLString(renderContentsAsHTML("/" + dir, contents), outputStream);
      } else sendHTMLString(renderContentsAsHTML(getContentsOfDir(rootDir)), outputStream);

    } else if (!resource.contains(".")) {
      Object[] contents = getContentsOfDir(rootDir + resource);
      if (Arrays.asList(contents).contains("index.html")){
        try (FileInputStream file = new FileInputStream(rootDir + resource + "/index.html")){
          sendFile(file, "html", outputStream);
        } catch (IOException e){
          throw new RuntimeException(e);
        }
      } else if (contents.length == 0) {
        send404(outputStream);
      } else {
        sendHTMLString(renderContentsAsHTML(resource, contents), outputStream);
      }
    } else {
      try (FileInputStream file = new FileInputStream(rootDir + resource)) {
        String filetype = resource.split("\\.")[1];
        sendFile(file, filetype, outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }
    }
  }
}
