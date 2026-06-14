# Suffix Fix

LabyMod 3 addon for Minecraft 1.8.9 that scans a configurable cuboid zone, detects visible suffixes behind player names, and sends configurable chat commands through a rate-limited queue.

Default actions:

```text
/p kick {player}
/msg {player} einkauf mit Suffix ist hier nicht möglich
```

## Setup

1. Put the LabyMod 3 legacy addon API jar into `libs/`.
2. Run `gradlew setupDecompWorkspace`.
3. Run `gradlew build`.

The pure detection/queue logic is dependency-free and can be checked with `javac`.

## Placeholders

- `{player}` real account name
- `{display}` visible display name without color codes
- `{suffix}` detected suffix
- `{x}`, `{y}`, `{z}` integer player coordinates

## Notes

The addon uses the visible display name for suffix detection, not only the raw account name. This matters because rank prefixes and cosmetic suffixes are usually injected through display names or scoreboard formatting.
