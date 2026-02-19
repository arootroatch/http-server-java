package myservertests;

import org.junit.jupiter.api.Test;

import static myserver.routes.Form.getQueryParams;
import static myserver.routes.Form.queryParamsToHTML;
import static org.junit.jupiter.api.Assertions.*;

public class HTMLTest {
  @Test
  void noQueryParams() {
    String[] result = getQueryParams("");
    assertEquals(0, result.length);
  }

  @Test
  void oneQueryParam() {
    String[] result = getQueryParams("foo=1");
    assertEquals(1, result.length);
    assertEquals("foo=1", result[0]);
  }

  @Test
  void twoQueryParams() {
    String[] result = getQueryParams("foo=1&bar=2");
    assertEquals(2, result.length);
    assertEquals("foo=1", result[0]);
    assertEquals("bar=2", result[1]);
  }

  @Test
  void paramsToHTML() {
    String[] params = {"foo=1", "bar=2"};
    String result = queryParamsToHTML(params);

    assertEquals("<ul>", result.substring(0, 4));
    assertTrue(result.contains("<li>foo: 1</li>"));
    assertTrue(result.contains("<li>bar: 2</li>"));
    assertTrue(result.endsWith("</ul>\r\n"));
  }

  @Test
  void paramsToHTMLMissingEquals() {
    String[] params = {"keyonly"};
    String result = queryParamsToHTML(params);
    assertTrue(result.contains("<li>keyonly: </li>"));
  }

  @Test
  void paramsToHTMLValueContainsEquals() {
    String[] params = {"data=a=b=c"};
    String result = queryParamsToHTML(params);
    assertTrue(result.contains("<li>data: a=b=c</li>"));
  }

  @Test
  void paramsToHTMLWithXSS() {
    String[] params = {"name=<script>alert(1)</script>"};
    String result = queryParamsToHTML(params);
    assertTrue(result.contains("&lt;script&gt;"));
    assertFalse(result.contains("<script>"));
  }
}
