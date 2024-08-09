package MyServerTests;

import MyServer.Main;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class CommandLineTest {
  static ByteArrayOutputStream outContent;
  static final PrintStream originalOut = System.out;

  @BeforeEach
  void setup() {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @Test
  void start() {
    String[] args = {"-x"};
    Main.main(args);
    assertTrue(outContent.toString().contains("MyServer"));
    assertTrue(outContent.toString().contains("Running on port: 80"));
    assertTrue(outContent.toString().contains("Serving files from: root"));
  }

  @Test
  void specifyPort() {
    String[] args = {"-x", "-p", "1234"};
    Main.main(args);
    assertTrue(outContent.toString().contains("Running on port: 1234"));
  }

  @Test
  void specifyRootDir() {
    String[] args = {"-x", "-r", "testroot"};
    Main.main(args);
    assertTrue(outContent.toString().contains("Serving files from: testroot"));
  }

  @Test
  void printHelp() {
    String[] args = {"-h"};
    Main.main(args);
    assertTrue(outContent.toString().contains("  -p     Specify the port.  Default is 80."));
    assertTrue(outContent.toString()
        .contains("  -r     Specify the root directory.  Default is the current working directory."));
    assertTrue(outContent.toString().contains("  -h     Print this help message"));
    assertTrue(outContent.toString().contains("  -x     Print the startup configuration without starting the server"));
  }

  @Test
  void printHelpDoesNotStartServer() {
    String[] args = {"-h"};
    Main.main(args);
    assertFalse(outContent.toString().contains("MyServer"));
    assertFalse(outContent.toString().contains("Running on port: 80"));
    assertFalse(outContent.toString().contains("Serving files from: root"));
  }

  @Test
  void printConfig() {
    String[] args = {"-x"};
    Main.main(args);
    assertEquals("MyServer\n" + "Running on port: 80\n" + "Serving files from: root\n",
        outContent.toString());
  }

  @Test
  void printConfigWithDirAndPort() {
    String[] args = {"-r", "testroot", "-p", "1234", "-x"};
    Main.main(args);
    assertEquals("MyServer\n" + "Running on port: 1234\n" + "Serving files from: testroot\n",
        outContent.toString());
  }

  @AfterAll
  static void teardown() {
    System.setOut(originalOut);
  }
}
