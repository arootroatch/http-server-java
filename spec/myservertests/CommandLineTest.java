package myservertests;

import myserver.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class CommandLineTest {
  ByteArrayOutputStream outContent;
  final PrintStream originalOut = System.out;

  @BeforeEach
  void setup() {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void teardown() {
    System.setOut(originalOut);
  }

  @Test
  void start() {
    String[] args = {"-x"};
    Main.main(args);
    assertTrue(outContent.toString().contains("Example Server"));
    assertTrue(outContent.toString().contains("Running on port: 80"));
    assertTrue(outContent.toString().contains("Serving files from: "));
    assertTrue(outContent.toString().contains("testroot"));
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
    assertTrue(outContent.toString().contains("Serving files from:"));
    assertTrue(outContent.toString().contains("testroot"));
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
    assertTrue(outContent.toString().contains("Example Server"));
    assertTrue(outContent.toString().contains("Running on port: 80\n"));
    assertTrue(outContent.toString().contains("Serving files from:"));
    assertTrue(outContent.toString().contains("testroot"));
  }

  @Test
  void printConfigWithDirAndPort() {
    String[] args = {"-r", "root", "-p", "1234", "-x"};
    Main.main(args);
    assertTrue(outContent.toString().contains("Example Server"));
    assertTrue(outContent.toString().contains("Running on port: 1234\n"));
    assertTrue(outContent.toString().contains("Serving files from:"));
    assertTrue(outContent.toString().contains("root"));
  }

  @Test
  void invalidPort() {
    String[] args = {"-x", "-p", "abc"};
    Main.main(args);
    assertTrue(outContent.toString().contains("Running on port: 80"));
  }

  @Test
  void portOutOfRange() {
    String[] args = {"-x", "-p", "99999"};
    Main.main(args);
    assertTrue(outContent.toString().contains("Running on port: 80"));
  }

  @Test
  void negativePort() {
    String[] args = {"-x", "-p", "-1"};
    Main.main(args);
    assertTrue(outContent.toString().contains("Running on port: 80"));
  }
}
