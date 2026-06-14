package de.marlo.suffixfix.core;

public final class PlayerContext {
    private final String playerName;
    private final String displayName;
    private final String suffix;
    private final int x;
    private final int y;
    private final int z;

    public PlayerContext(String playerName, String displayName, String suffix, int x, int y, int z) {
        this.playerName = playerName;
        this.displayName = displayName;
        this.suffix = suffix;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String render(String template) {
        return template
            .replace("{player}", playerName)
            .replace("{display}", displayName)
            .replace("{suffix}", suffix)
            .replace("{x}", String.valueOf(x))
            .replace("{y}", String.valueOf(y))
            .replace("{z}", String.valueOf(z));
    }
}
