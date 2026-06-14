package de.marlo.suffixfix.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ProcessedPlayerCache {
    private final Map<String, Long> processedPlayers = new HashMap<String, Long>();
    private long cooldownMillis;

    public ProcessedPlayerCache(long cooldownMillis) {
        setCooldownMillis(cooldownMillis);
    }

    public void setCooldownMillis(long cooldownMillis) {
        this.cooldownMillis = Math.max(0L, cooldownMillis);
    }

    public boolean canProcess(String playerName, long nowMillis) {
        return !processedPlayers.containsKey(playerName);
    }

    public void markProcessed(String playerName, long nowMillis) {
        processedPlayers.put(playerName, nowMillis);
    }

    public void prune(long nowMillis) {
        // Entries are cleared when the player leaves the configured zone.
    }

    public void removePlayersNotIn(Set<String> playerNamesInZone) {
        java.util.Iterator<String> iterator = processedPlayers.keySet().iterator();
        while (iterator.hasNext()) {
            String playerName = iterator.next();
            if (!playerNamesInZone.contains(playerName)) {
                iterator.remove();
            }
        }
    }

    public void clear() {
        processedPlayers.clear();
    }
}
