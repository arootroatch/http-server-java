package com.alexrootroatch.httpserver;

import com.alexrootroatch.httpserver.routes.Ping;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class PingTest {

  @Test
  void rendersHTML() {
    String response = TestHelper.serve(new Ping(), TestHelper.get("/ping"));
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h2>Ping</h2>"));
    assertTrue(response.contains("<li>start time:"));
    assertTrue(response.contains("<li>end time:"));
  }

  @Test
  void invalidDelay() {
    String response = TestHelper.serve(new Ping(), TestHelper.get("/ping/abc"));
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h2>Ping</h2>"));
    TestHelper.assertNoLeakedErrors(response);
  }

  @Test
  void negativeDelay() {
    String response = TestHelper.serve(new Ping(), TestHelper.get("/ping/-5"));
    assertTrue(response.contains("200 OK"));
    assertTrue(response.contains("<h2>Ping</h2>"));
    TestHelper.assertNoLeakedErrors(response);
  }

}
