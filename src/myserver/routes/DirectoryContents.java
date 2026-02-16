package myserver.routes;

import java.io.File;
import java.util.Arrays;

public class DirectoryContents {
  public static String[] getContentsOfDir(String dir) {
    File d = new File(dir);
    File[] files = d.listFiles();
    if (files != null) return Arrays.stream(files).map(File::getName).toArray(String[]::new);
    else return new String[0];
  }
}
