package de.marlo.suffixfix;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;

public final class SuffixFixCommand extends CommandBase {
    private final SuffixFixAddon addon;

    public SuffixFixCommand(SuffixFixAddon addon) {
        this.addon = addon;
    }

    @Override
    public String getCommandName() {
        return "antisuffix";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("suffixfix");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/antisuffix <pos1|pos2>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        SuffixFixConfig config = addon.getSuffixFixConfig();
        String action = args[0].toLowerCase();

        if ("on".equals(action)) {
            config.setBoolean("enabled", true);
            saveAndReply(sender, "aktiviert");
        } else if ("off".equals(action)) {
            config.setBoolean("enabled", false);
            saveAndReply(sender, "deaktiviert");
        } else if ("debug".equals(action)) {
            boolean enabled = args.length > 1 && "on".equalsIgnoreCase(args[1]);
            config.setBoolean("debug", enabled);
            saveAndReply(sender, "Debug " + (enabled ? "aktiviert" : "deaktiviert"));
        } else if ("pos1".equals(action)) {
            setPosition(config, sender, "min", 1);
        } else if ("pos2".equals(action)) {
            setPosition(config, sender, "max", 2);
        } else if ("delay".equals(action) && args.length > 1) {
            config.setLong("chatDelayMillis", parseLong(args[1], 3000L));
            saveAndReply(sender, "Chat-Delay gesetzt");
        } else if ("scan".equals(action) && args.length > 1) {
            config.setLong("scanIntervalMillis", parseLong(args[1], 500L));
            saveAndReply(sender, "Scan-Intervall gesetzt");
        } else if ("command1".equals(action) && args.length > 1) {
            config.setCommand(0, join(args, 1));
            saveAndReply(sender, "Command 1 gesetzt");
        } else if ("command2".equals(action) && args.length > 1) {
            config.setCommand(1, join(args, 1));
            saveAndReply(sender, "Command 2 gesetzt");
        } else {
            sendHelp(sender);
        }
    }

    private void setPosition(SuffixFixConfig config, ICommandSender sender, String prefix, int corner) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            reply(sender, "Position konnte nicht gelesen werden");
            return;
        }

        config.setDouble(prefix + "X", floor(player.posX));
        config.setDouble(prefix + "Y", floor(player.posY));
        config.setDouble(prefix + "Z", floor(player.posZ));
        saveAndReply(sender, "Zone-Ecke " + corner + " gesetzt: " + formatPosition(player));
    }

    private void sendHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("\u00A76----------[SuffixFix]----------"));
        sender.addChatMessage(new ChatComponentText("/antisuffix pos1"));
        sender.addChatMessage(new ChatComponentText("/antisuffix pos2"));
        sender.addChatMessage(new ChatComponentText("\u00A76----------[SuffixFix]----------"));
    }

    private void saveAndReply(ICommandSender sender, String message) {
        addon.saveSuffixFixConfig();
        reply(sender, message);
    }

    private void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("[SuffixFix] " + message));
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String join(String[] values, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < values.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }

    private String formatPosition(EntityPlayer player) {
        return floor(player.posX) + " " + floor(player.posY) + " " + floor(player.posZ);
    }

    private int floor(double value) {
        return (int) Math.floor(value);
    }
}
