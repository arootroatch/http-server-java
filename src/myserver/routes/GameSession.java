package myserver.routes;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {
  private static final long SESSION_TTL = 30 * 60 * 1000L;
  private static final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();

  private record SessionData(int number, int triesLeft, long createdAt) {}

  public static String createSession(int number) {
    cleanExpired();
    String id = UUID.randomUUID().toString();
    sessions.put(id, new SessionData(number, 7, System.currentTimeMillis()));
    return id;
  }

  public static Integer getNumber(String sessionId) {
    if (sessionId == null) return null;
    SessionData data = sessions.get(sessionId);
    return data == null ? null : data.number();
  }

  public static Integer getTriesLeft(String sessionId) {
    if (sessionId == null) return null;
    SessionData data = sessions.get(sessionId);
    return data == null ? null : data.triesLeft();
  }

  public static void decrementTries(String sessionId) {
    sessions.computeIfPresent(sessionId, (key, data) ->
        new SessionData(data.number(), data.triesLeft() - 1, data.createdAt()));
  }

  public static void removeSession(String sessionId) {
    if (sessionId != null) sessions.remove(sessionId);
  }

  public static void clearAll() {
    sessions.clear();
  }

  private static void cleanExpired() {
    long now = System.currentTimeMillis();
    sessions.entrySet().removeIf(e -> now - e.getValue().createdAt() > SESSION_TTL);
  }
}
