package myserver.routes;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {
  private static final ConcurrentHashMap<String, Integer> sessions = new ConcurrentHashMap<>();

  public static String createSession(int number) {
    String id = UUID.randomUUID().toString();
    sessions.put(id, number);
    return id;
  }

  public static Integer getNumber(String sessionId) {
    if (sessionId == null) return null;
    return sessions.get(sessionId);
  }

  public static void removeSession(String sessionId) {
    if (sessionId != null) sessions.remove(sessionId);
  }

  public static void clearAll() {
    sessions.clear();
  }
}
