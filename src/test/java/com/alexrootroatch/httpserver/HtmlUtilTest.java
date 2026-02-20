package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.HtmlUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HtmlUtilTest {

  @Test
  void escapeHtmlNull() {
    assertEquals("", HtmlUtil.escapeHtml(null));
  }

  @Test
  void escapeHtmlNoSpecialChars() {
    assertEquals("hello world", HtmlUtil.escapeHtml("hello world"));
  }

  @Test
  void escapeHtmlAngleBrackets() {
    assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;",
        HtmlUtil.escapeHtml("<script>alert(1)</script>"));
  }

  @Test
  void escapeHtmlAmpersand() {
    assertEquals("foo &amp; bar", HtmlUtil.escapeHtml("foo & bar"));
  }

  @Test
  void escapeHtmlQuotes() {
    assertEquals("&quot;hello&quot; &#x27;world&#x27;",
        HtmlUtil.escapeHtml("\"hello\" 'world'"));
  }

  @Test
  void escapeHtmlMixed() {
    assertEquals("&lt;a href=&quot;x&quot;&gt;foo &amp; bar&lt;/a&gt;",
        HtmlUtil.escapeHtml("<a href=\"x\">foo & bar</a>"));
  }
}
