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

    } else if (resource.contains("/form")) {
      try (FileInputStream file = new FileInputStream(rootDir + "/forms.html")) {
        if (request.toString().contains("POST")) {
          String html = parseHTML(file);
//          headersToHTML(request.toString());
//          String addHTML = queryParamsToHTML
        }
        if (resource.contains("?")) {
          String html = parseHTML(file);
          String addHTML = queryParamsToHTML(getQueryParams(resource));
          sendFile(html, "html", outputStream, addHTML);
        } else sendFile(file, "html", outputStream);
      } catch (IOException e) {
        send404(outputStream);
//        throw new RuntimeException(e);
      }

    } else if (!resource.contains(".")) {
      Object[] contents = getContentsOfDir(rootDir + resource);
      if (Arrays.asList(contents).contains("index.html")) {
        try (FileInputStream file = new FileInputStream(rootDir + resource + "/index.html")) {
          sendFile(file, "html", outputStream);
        } catch (IOException e) {
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

  public static String[] getQueryParams(String resource) {
    String[] split = resource.split("[?&]");
    String[] params = new String[split.length - 1];
    System.arraycopy(split, 1, params, 0, params.length);
    return params;
  }

  public static String queryParamsToHTML(String[] params) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (String s : params) {
      String name = s.split("=")[0];
      String value = s.split("=")[1];
      String li = String.format("<li>%s: %s</li>", name, value);
      html.append(li);
    }
    html.append("</ul>\r\n");
    return html.toString();
  }

//  public static String headersToHTML(String request){
//    String fileName;
//    String contentType;
//    String fileSize;
//    String[] requestSplit = request.split("\r\n");
//    StringBuilder html = new StringBuilder();
//
//    html.append("<ul>");
////    for(String i : requestSplit){
////      if (i.contains(""))
////    }
//
//    System.out.println(Arrays.toString(requestSplit));
//    return "";
//  }

  public static String parseHTML(InputStream inputStream) throws IOException {
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder html = new StringBuilder();
    String line = br.readLine();

    while (line != null) {
      html.append(line).append("\r\n");
      line = br.readLine();
    }
    return html.toString();
  }
}
