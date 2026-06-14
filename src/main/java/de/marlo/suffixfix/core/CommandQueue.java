package de.marlo.suffixfix.core;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

public final class CommandQueue {
    private final Queue<String> pendingCommands = new ArrayDeque<String>();
    private long delayMillis;
    private long lastSendMillis;

    public CommandQueue(long delayMillis) {
        setDelayMillis(delayMillis);
        this.lastSendMillis = 0L;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = Math.max(250L, delayMillis);
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void enqueue(Collection<String> commands, PlayerContext context) {
        for (String command : commands) {
            if (command == null || command.trim().isEmpty()) {
                continue;
            }

            pendingCommands.add(context.render(command.trim()));
        }
    }

    public String pollReady(long nowMillis) {
        if (pendingCommands.isEmpty()) {
            return null;
        }

        if (lastSendMillis > 0L && nowMillis - lastSendMillis < delayMillis) {
            return null;
        }

        lastSendMillis = nowMillis;
        return pendingCommands.poll();
    }

    public int size() {
        return pendingCommands.size();
    }

    public void clear() {
        pendingCommands.clear();
    }
}
