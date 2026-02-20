package com.alexrootroatch.httpserver.routes;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {
  static final int MAX_TRIES = 7;
  private static final long SESSION_TTL = 30 * 60 * 1000L;
  private final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();

  public record SessionData(int number, int triesLeft, long createdAt) {}

  public String createSession(int number) {
    cleanExpired();
    String id = UUID.randomUUID().toString();
    sessions.put(id, new SessionData(number, MAX_TRIES, System.currentTimeMillis()));
    return id;
  }

  public SessionData getSession(String sessionId) {
    if (sessionId == null) return null;
    return sessions.get(sessionId);
  }

  public Integer getNumber(String sessionId) {
    if (sessionId == null) return null;
    SessionData data = sessions.get(sessionId);
    return data == null ? null : data.number();
  }

  public Integer getTriesLeft(String sessionId) {
    if (sessionId == null) return null;
    SessionData data = sessions.get(sessionId);
    return data == null ? null : data.triesLeft();
  }

  public SessionData decrementTries(String sessionId) {
    return sessions.computeIfPresent(sessionId, (key, data) ->
        new SessionData(data.number(), data.triesLeft() - 1, data.createdAt()));
  }

  public void removeSession(String sessionId) {
    if (sessionId != null) sessions.remove(sessionId);
  }

  public void clearAll() {
    sessions.clear();
  }

  private void cleanExpired() {
    long now = System.currentTimeMillis();
    sessions.entrySet().removeIf(e -> now - e.getValue().createdAt() > SESSION_TTL);
  }
}
