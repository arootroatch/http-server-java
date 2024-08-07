import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyServerTest {
  static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  static final PrintStream originalOut = System.out;

  @BeforeAll
  static void setup() {
    System.setOut(new PrintStream(outContent));
  }

  @Test
  void start() throws IOException {
    String[] args = null;
    MyServer.main(null);
    assertTrue(outContent.toString().contains("MyServer"));
    assertTrue(outContent.toString().contains("Running on port: 80"));
    assertTrue(outContent.toString().contains("Serving files from:"));
  }

  @AfterAll
  static void teardown() {
    System.setOut(originalOut);
  }
}
