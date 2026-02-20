package com.alexrootroatch.httpserver;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.alexrootroatch.httpserver.routes.DirectoryContents.getContentsOfDir;
import static com.alexrootroatch.httpserver.routes.DirectoryContents.renderAsHTML;
import static org.junit.jupiter.api.Assertions.*;

public class DirectoryContentsTest {
  @Test
  void listContents() {
    String[] files = getContentsOfDir("testroot");
    assertTrue(Arrays.toString(files).contains("forms.html"));
    assertTrue(Arrays.toString(files).contains("hello.pdf"));
    assertTrue(Arrays.toString(files).contains("index.html"));
    assertTrue(Arrays.toString(files).contains("img"));
  }

  @Test
  void listContentsImg() {
    String[] files = getContentsOfDir("testroot/img");
    assertTrue(Arrays.toString(files).contains("autobot.jpg"));
    assertTrue(Arrays.toString(files).contains("autobot.png"));
    assertTrue(Arrays.toString(files).contains("decepticon.jpg"));
    assertTrue(Arrays.toString(files).contains("decepticon.png"));
  }

  @Test
  void renderAsHTMLFiles() {
    String[] contents = {"file.txt"};
    String result = renderAsHTML(contents, item -> true,
        item -> "/files/" + item, item -> "/folders/" + item);
    assertTrue(result.contains("<a href=\"/files/file.txt\">file.txt</a>"));
  }

  @Test
  void renderAsHTMLFolders() {
    String[] contents = {"subdir"};
    String result = renderAsHTML(contents, item -> false,
        item -> "/files/" + item, item -> "/folders/" + item);
    assertTrue(result.contains("<a href=\"/folders/subdir\">subdir</a>"));
  }

  @Test
  void renderAsHTMLEscapesNames() {
    String[] contents = {"<script>alert(1)</script>"};
    String result = renderAsHTML(contents, item -> true,
        item -> "/files/" + item, item -> "/folders/" + item);
    assertTrue(result.contains(">&lt;script&gt;alert(1)&lt;/script&gt;</a>"));
  }
}
