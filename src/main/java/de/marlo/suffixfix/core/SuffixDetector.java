package de.marlo.suffixfix.core;

import java.util.regex.Pattern;

public final class SuffixDetector {
    private static final Pattern COLOR_CODES = Pattern.compile("(?i)\u00A7[0-9A-FK-OR]");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    public SuffixDetectionResult detect(String playerName, String visibleName) {
        String safePlayer = normalize(playerName);
        String safeVisible = normalize(visibleName);

        if (safePlayer.isEmpty() || safeVisible.isEmpty()) {
            return SuffixDetectionResult.none(safePlayer, safeVisible);
        }

        int nameIndex = safeVisible.indexOf(safePlayer);
        if (nameIndex < 0) {
            return SuffixDetectionResult.none(safePlayer, safeVisible);
        }

        int suffixStart = nameIndex + safePlayer.length();
        if (suffixStart >= safeVisible.length()) {
            return SuffixDetectionResult.none(safePlayer, safeVisible);
        }

        String suffix = safeVisible.substring(suffixStart).trim();
        if (suffix.isEmpty()) {
            return SuffixDetectionResult.none(safePlayer, safeVisible);
        }

        return SuffixDetectionResult.detected(safePlayer, safeVisible, suffix);
    }

    public String normalize(String value) {
        if (value == null) {
            return "";
        }

        String withoutColors = COLOR_CODES.matcher(value).replaceAll("");
        String withoutControl = CONTROL_CHARS.matcher(withoutColors).replaceAll("");
        return withoutControl.trim();
    }
}
