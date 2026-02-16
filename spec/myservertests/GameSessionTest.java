package myservertests;

import myserver.routes.GameSession;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class GameSessionTest {

  @Test
  void createSession() {
    String id = GameSession.createSession(42);
    assertNotNull(id);
    assertEquals(42, GameSession.getNumber(id));
  }

  @Test
  void getNumberForUnknownSession() {
    assertNull(GameSession.getNumber("nonexistent"));
  }

  @Test
  void removeSession() {
    String id = GameSession.createSession(10);
    GameSession.removeSession(id);
    assertNull(GameSession.getNumber(id));
  }

  @Test
  void nullSessionId() {
    assertNull(GameSession.getNumber(null));
  }

  @Test
  void concurrentAccess() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<String>> futures = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      final int num = i;
      futures.add(executor.submit(() -> GameSession.createSession(num)));
    }
    for (Future<String> future : futures) {
      String id = future.get();
      assertNotNull(id);
      assertNotNull(GameSession.getNumber(id));
    }
    executor.shutdown();
  }
}
