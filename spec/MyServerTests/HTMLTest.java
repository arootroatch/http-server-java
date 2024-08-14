package MyServerTests;

import org.junit.jupiter.api.Test;

import static MyServer.HTML.getQueryParams;
import static MyServer.HTML.queryParamsToHTML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HTMLTest {
  @Test
  void noQueryParams(){
    String[] result = getQueryParams("/form");
    assertEquals(0, result.length);
  }

  @Test
  void oneQueryParam(){
    String[] result = getQueryParams("/form?foo=1");
    assertEquals(1, result.length);
    assertEquals("foo=1", result[0]);
  }

  @Test
  void twoQueryParams(){
    String[] result = getQueryParams("/form?foo=1&bar=2");
    assertEquals(2, result.length);
    assertEquals("foo=1", result[0]);
    assertEquals("bar=2", result[1]);
  }

  @Test
  void paramsToHTML(){
    String[] params = {"foo=1", "bar=2"};
    String result = queryParamsToHTML(params);
    int i = result.length();

    assertEquals("<ul>", result.substring(0, 4));
    assertTrue(result.contains("<li>foo: 1</li>"));
    assertTrue(result.contains("<li>bar: 2</li>"));
    assertTrue(result.contains("<li>bar: 2</li>"));
    assertEquals("</ul>\r\n", result.substring(i - 7));
  }
}
