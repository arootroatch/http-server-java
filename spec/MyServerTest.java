import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class MyServerTest {
  static ByteArrayOutputStream outContent;
  static final PrintStream originalOut = System.out;

  @BeforeEach
  void setup() {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @Test
  void start() throws IOException {
    String[] args = null;
    MyServer.main(null);
    assertTrue(outContent.toString().contains("MyServer"));
    assertTrue(outContent.toString().contains("Running on port: 80"));
    assertTrue(outContent.toString().contains("Serving files from: root"));
  }

  @Test
  void specifyPort() throws IOException {
    String[] args = {"-p", "1234"};
    MyServer.main(args);
    assertTrue(outContent.toString().contains("Running on port: 1234"));
  }

  @Test
  void specifyRootDir() throws IOException{
    String[] args = {"-r", "testroot"};
    MyServer.main(args);
    assertTrue(outContent.toString().contains("Serving files from: testroot"));
  }

  @Test
  void printHelp() throws IOException {
    String[] args = {"-h"};
    MyServer.main(args);
    assertTrue(outContent.toString().contains("  -p     Specify the port.  Default is 80."));
    assertTrue(outContent.toString()
        .contains("  -r     Specify the root directory.  Default is the current working directory."));
    assertTrue(outContent.toString().contains("  -h     Print this help message"));
    assertTrue(outContent.toString().contains("  -x     Print the startup configuration without starting the server"));
  }

  @Test
  void printHelpDoesNotStartServer() throws IOException {
    String[] args = {"-h"};
    MyServer.main(args);
    assertFalse(outContent.toString().contains("MyServer"));
    assertFalse(outContent.toString().contains("Running on port: 80"));
    assertFalse(outContent.toString().contains("Serving files from: root"));
  }

  @Test
  void printConfig() throws IOException {
    String[] args = {"-x"};
    MyServer.main(args);
    assertEquals("MyServer\n" + "Running on port: 80\n" + "Serving files from: root\n",
        outContent.toString());
  }

  @Test
  void printConfigWithDirAndPort() throws IOException {
    String[] args = {"-r", "testroot", "-p", "1234", "-x"};
    MyServer.main(args);
    assertEquals("MyServer\n" + "Running on port: 1234\n" + "Serving files from: testroot\n",
        outContent.toString());
  }

  @AfterAll
  static void teardown() {
    System.setOut(originalOut);
  }
}
