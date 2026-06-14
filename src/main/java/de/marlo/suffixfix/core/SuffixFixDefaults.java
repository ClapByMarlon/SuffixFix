package de.marlo.suffixfix.core;

import java.util.Arrays;
import java.util.List;

public final class SuffixFixDefaults {
    public static final long DEFAULT_CHAT_DELAY_MILLIS = 3000L;
    public static final long DEFAULT_PLAYER_COOLDOWN_MILLIS = 60000L;
    public static final long DEFAULT_SCAN_INTERVAL_MILLIS = 500L;

    private SuffixFixDefaults() {
    }

    public static List<String> defaultCommands() {
        return Arrays.asList(
            "/p kick {player}",
            "/msg {player} einkauf mit Suffix ist hier nicht möglich"
        );
    }
}
