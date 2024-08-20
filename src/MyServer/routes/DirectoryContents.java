package MyServer.routes;

import java.io.File;
import java.util.Arrays;

public class DirectoryContents {
  public static Object[] getContentsOfDir(String dir) {
    File d = new File(dir);
    File[] files = d.listFiles();
    Object[] empty = new Object[0];
    if (files != null) return Arrays.stream(files).map(File::getName).toArray();
    else return empty;
  }
}
