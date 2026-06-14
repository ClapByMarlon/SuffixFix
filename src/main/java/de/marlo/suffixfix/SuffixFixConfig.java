package de.marlo.suffixfix;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.marlo.suffixfix.core.SuffixFixDefaults;
import de.marlo.suffixfix.core.ZoneBounds;

import java.util.ArrayList;
import java.util.List;

public final class SuffixFixConfig {
    private final JsonObject json;

    public SuffixFixConfig(JsonObject json) {
        this.json = json == null ? new JsonObject() : json;
        applyDefaults();
    }

    public boolean isEnabled() {
        return getBoolean("enabled", true);
    }

    public boolean isDebugEnabled() {
        return getBoolean("debug", false);
    }

    public long getScanIntervalMillis() {
        return getLong("scanIntervalMillis", SuffixFixDefaults.DEFAULT_SCAN_INTERVAL_MILLIS);
    }

    public long getChatDelayMillis() {
        return getLong("chatDelayMillis", SuffixFixDefaults.DEFAULT_CHAT_DELAY_MILLIS);
    }

    public long getPlayerCooldownMillis() {
        return getLong("playerCooldownMillis", SuffixFixDefaults.DEFAULT_PLAYER_COOLDOWN_MILLIS);
    }

    public ZoneBounds getZoneBounds() {
        return new ZoneBounds(
            getDouble("minX", 0D),
            getDouble("minY", 0D),
            getDouble("minZ", 0D),
            getDouble("maxX", 0D),
            getDouble("maxY", 255D),
            getDouble("maxZ", 0D)
        );
    }

    public List<String> getCommands() {
        List<String> commands = new ArrayList<String>();
        JsonArray array = json.getAsJsonArray("commands");
        if (array != null) {
            for (JsonElement element : array) {
                if (element != null && !element.isJsonNull()) {
                    commands.add(element.getAsString());
                }
            }
        }

        if (commands.isEmpty()) {
            commands.addAll(SuffixFixDefaults.defaultCommands());
        }

        return commands;
    }

    public void setDouble(String key, double value) {
        json.addProperty(key, value);
    }

    public void setLong(String key, long value) {
        json.addProperty(key, value);
    }

    public void setBoolean(String key, boolean value) {
        json.addProperty(key, value);
    }

    public void setCommand(int index, String command) {
        JsonArray currentCommands = json.getAsJsonArray("commands");
        JsonArray updatedCommands = new JsonArray();
        int size = Math.max(currentCommands.size(), index + 1);
        for (int i = 0; i < size; i++) {
            if (i == index) {
                updatedCommands.add(new JsonPrimitive(command));
            } else if (i < currentCommands.size()) {
                updatedCommands.add(currentCommands.get(i));
            } else {
                updatedCommands.add(new JsonPrimitive(""));
            }
        }
        json.add("commands", updatedCommands);
    }

    private void applyDefaults() {
        addDefault("enabled", true);
        addDefault("debug", false);
        addDefault("scanIntervalMillis", SuffixFixDefaults.DEFAULT_SCAN_INTERVAL_MILLIS);
        addDefault("chatDelayMillis", SuffixFixDefaults.DEFAULT_CHAT_DELAY_MILLIS);
        addDefault("playerCooldownMillis", SuffixFixDefaults.DEFAULT_PLAYER_COOLDOWN_MILLIS);
        addDefault("minX", 0D);
        addDefault("minY", 0D);
        addDefault("minZ", 0D);
        addDefault("maxX", 0D);
        addDefault("maxY", 255D);
        addDefault("maxZ", 0D);

        if (!json.has("commands") || !json.get("commands").isJsonArray()) {
            JsonArray commands = new JsonArray();
            for (String command : SuffixFixDefaults.defaultCommands()) {
                commands.add(new JsonPrimitive(command));
            }
            json.add("commands", commands);
        }
    }

    private void addDefault(String key, boolean value) {
        if (!json.has(key)) {
            json.addProperty(key, value);
        }
    }

    private void addDefault(String key, long value) {
        if (!json.has(key)) {
            json.addProperty(key, value);
        }
    }

    private void addDefault(String key, double value) {
        if (!json.has(key)) {
            json.addProperty(key, value);
        }
    }

    private boolean getBoolean(String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    private long getLong(String key, long fallback) {
        return json.has(key) ? json.get(key).getAsLong() : fallback;
    }

    private double getDouble(String key, double fallback) {
        return json.has(key) ? json.get(key).getAsDouble() : fallback;
    }
}
