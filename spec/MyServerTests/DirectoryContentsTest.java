package MyServerTests;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static MyServer.DirectoryContents.getContentsOfDir;
import static MyServer.DirectoryContents.renderContentsAsHTML;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Test
  void createHTML() {
    String html = renderContentsAsHTML(getContentsOfDir("testroot"));
    int i = html.length();
    assertEquals("<ul>", html.substring(0, 4));
    assertEquals("</ul>\r\n", html.substring(i - 7));
    assertTrue(html.contains("<li><a href=\"/index.html\">index.html</a></li>"));
    assertTrue(html.contains("<li><a href=\"/forms.html\">forms.html</a></li>"));
    assertTrue(html.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
    assertTrue(html.contains("<li><a href=\"/listing/img\">img</a></li>"));
  }
}
