package myservertests;

import myserver.routes.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class GameSessionTest {
  private GameSession gameSession;

  @BeforeEach
  void setup() {
    gameSession = new GameSession();
  }

  @Test
  void createSession() {
    String id = gameSession.createSession(42);
    assertNotNull(id);
    assertEquals(42, gameSession.getNumber(id));
    assertEquals(7, gameSession.getTriesLeft(id));
  }

  @Test
  void getNumberForUnknownSession() {
    assertNull(gameSession.getNumber("nonexistent"));
  }

  @Test
  void removeSession() {
    String id = gameSession.createSession(10);
    gameSession.removeSession(id);
    assertNull(gameSession.getNumber(id));
  }

  @Test
  void nullSessionId() {
    assertNull(gameSession.getNumber(null));
  }

  @Test
  void decrementTries() {
    String id = gameSession.createSession(42);
    gameSession.decrementTries(id);
    assertEquals(6, gameSession.getTriesLeft(id));
  }

  @Test
  void triesReachZero() {
    String id = gameSession.createSession(42);
    for (int i = 0; i < 7; i++) {
      gameSession.decrementTries(id);
    }
    assertEquals(0, gameSession.getTriesLeft(id));
  }

  @Test
  void concurrentAccess() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<String>> futures = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      final int num = i;
      futures.add(executor.submit(() -> gameSession.createSession(num)));
    }
    for (Future<String> future : futures) {
      String id = future.get();
      assertNotNull(id);
      assertNotNull(gameSession.getNumber(id));
    }
    executor.shutdown();
  }

  @Test
  void getSessionReturnsAtomicSnapshot() {
    String id = gameSession.createSession(42);
    GameSession.SessionData snapshot = gameSession.getSession(id);
    assertNotNull(snapshot);
    assertEquals(42, snapshot.number());
    assertEquals(7, snapshot.triesLeft());
  }
}
