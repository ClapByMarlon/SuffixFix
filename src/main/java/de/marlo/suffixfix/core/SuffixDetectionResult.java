package de.marlo.suffixfix.core;

public final class SuffixDetectionResult {
    private final boolean detected;
    private final String playerName;
    private final String displayName;
    private final String suffix;

    private SuffixDetectionResult(boolean detected, String playerName, String displayName, String suffix) {
        this.detected = detected;
        this.playerName = playerName;
        this.displayName = displayName;
        this.suffix = suffix;
    }

    public static SuffixDetectionResult detected(String playerName, String displayName, String suffix) {
        return new SuffixDetectionResult(true, playerName, displayName, suffix);
    }

    public static SuffixDetectionResult none(String playerName, String displayName) {
        return new SuffixDetectionResult(false, playerName, displayName, "");
    }

    public boolean isDetected() {
        return detected;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSuffix() {
        return suffix;
    }
}
