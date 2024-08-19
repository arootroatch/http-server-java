package MyServerTests;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static MyServer.DirectoryContents.getContentsOfDir;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryContentsTest {
  @Test
  void listContents() {
    Object[] files = getContentsOfDir("testroot");
    assertTrue(Arrays.toString(files).contains("forms.html"));
    assertTrue(Arrays.toString(files).contains("hello.pdf"));
    assertTrue(Arrays.toString(files).contains("index.html"));
    assertTrue(Arrays.toString(files).contains("img"));
  }

  @Test
  void listContentsImg() {
    Object[] files = getContentsOfDir("testroot/img");
    assertTrue(Arrays.toString(files).contains("autobot.jpg"));
    assertTrue(Arrays.toString(files).contains("autobot.png"));
    assertTrue(Arrays.toString(files).contains("decepticon.jpg"));
    assertTrue(Arrays.toString(files).contains("decepticon.png"));
  }
}
