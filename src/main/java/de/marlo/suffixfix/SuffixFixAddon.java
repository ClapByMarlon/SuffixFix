package de.marlo.suffixfix;

import de.marlo.suffixfix.core.CommandQueue;
import de.marlo.suffixfix.core.PlayerContext;
import de.marlo.suffixfix.core.ProcessedPlayerCache;
import de.marlo.suffixfix.core.SuffixDetectionResult;
import de.marlo.suffixfix.core.SuffixDetector;
import de.marlo.suffixfix.core.SuffixFixDefaults;
import de.marlo.suffixfix.core.ZoneBounds;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SuffixFixAddon extends LabyModAddon {
    private final SuffixDetector suffixDetector = new SuffixDetector();
    private CommandQueue commandQueue = new CommandQueue(SuffixFixDefaults.DEFAULT_CHAT_DELAY_MILLIS);
    private ProcessedPlayerCache processedPlayers = new ProcessedPlayerCache(SuffixFixDefaults.DEFAULT_PLAYER_COOLDOWN_MILLIS);
    private SuffixFixConfig config;
    private long lastScanMillis;

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new SuffixFixCommand(this));
    }

    @Override
    public void loadConfig() {
        this.config = new SuffixFixConfig(getConfig());
        this.commandQueue = new CommandQueue(config.getChatDelayMillis());
        this.processedPlayers = new ProcessedPlayerCache(config.getPlayerCooldownMillis());
    }

    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {
        SuffixFixSettings.fill(this, subSettings);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || config == null || !config.isEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        sendQueuedCommand(now);

        if (now - lastScanMillis < config.getScanIntervalMillis()) {
            return;
        }

        lastScanMillis = now;
        scanPlayers(now);
    }

    public SuffixFixConfig getSuffixFixConfig() {
        if (config == null) {
            config = new SuffixFixConfig(getConfig());
        }
        return config;
    }

    public void saveSuffixFixConfig() {
        saveConfig();
        this.commandQueue.setDelayMillis(config.getChatDelayMillis());
        this.processedPlayers.setCooldownMillis(config.getPlayerCooldownMillis());
    }

    private void scanPlayers(long now) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null || minecraft.thePlayer == null) {
            return;
        }

        ZoneBounds zone = config.getZoneBounds();
        String ownName = minecraft.thePlayer.getName();
        Set<String> playerNamesInZone = new HashSet<String>();

        for (Object object : minecraft.theWorld.playerEntities) {
            if (!(object instanceof EntityPlayer)) {
                continue;
            }

            EntityPlayer player = (EntityPlayer) object;
            if (player.getName().equalsIgnoreCase(ownName)) {
                continue;
            }

            if (!zone.contains(player.posX, player.posY, player.posZ)) {
                continue;
            }

            playerNamesInZone.add(player.getName());
            handlePlayer(player, now);
        }

        processedPlayers.removePlayersNotIn(playerNamesInZone);
        processedPlayers.prune(now);
    }

    private void handlePlayer(EntityPlayer player, long now) {
        String playerName = player.getName();
        if (!processedPlayers.canProcess(playerName, now)) {
            return;
        }

        SuffixDetectionResult detection = detectSuffix(player);
        if (!detection.isDetected()) {
            if (config.isDebugEnabled()) {
                debug("In Zone ohne Suffix: " + playerName + " sichtbar=" + buildVisibleNames(player));
            }
            return;
        }

        PlayerContext context = new PlayerContext(
            detection.getPlayerName(),
            detection.getDisplayName(),
            detection.getSuffix(),
            (int) Math.floor(player.posX),
            (int) Math.floor(player.posY),
            (int) Math.floor(player.posZ)
        );
        commandQueue.enqueue(config.getCommands(), context);
        processedPlayers.markProcessed(playerName, now);

        if (config.isDebugEnabled()) {
            debug("Suffix erkannt bei " + detection.getDisplayName() + " -> " + detection.getSuffix());
        }
    }

    private SuffixDetectionResult detectSuffix(EntityPlayer player) {
        String playerName = player.getName();
        List<String> visibleNames = buildVisibleNames(player);

        for (String visibleName : visibleNames) {
            SuffixDetectionResult detection = suffixDetector.detect(playerName, visibleName);
            if (detection.isDetected()) {
                return detection;
            }
        }

        return SuffixDetectionResult.none(playerName, visibleNames.isEmpty() ? playerName : visibleNames.get(0));
    }

    private List<String> buildVisibleNames(EntityPlayer player) {
        List<String> names = new ArrayList<String>();
        String playerName = player.getName();

        addName(names, player.getDisplayName());
        addName(names, player.getDisplayNameString());
        for (IChatComponent suffix : player.getSuffixes()) {
            if (suffix != null) {
                addName(names, playerName + " " + suffix.getFormattedText());
                addName(names, playerName + " " + suffix.getUnformattedText());
            }
        }

        Team entityTeam = player.getTeam();
        if (entityTeam != null) {
            addName(names, entityTeam.formatString(playerName));
            if (entityTeam instanceof ScorePlayerTeam) {
                ScorePlayerTeam scoreTeam = (ScorePlayerTeam) entityTeam;
                addName(names, scoreTeam.getColorPrefix() + playerName + scoreTeam.getColorSuffix());
            }
        }

        NetworkPlayerInfo tabInfo = getTabInfo(player);
        if (tabInfo != null) {
            addName(names, tabInfo.getDisplayName());
            ScorePlayerTeam tabTeam = tabInfo.getPlayerTeam();
            if (tabTeam != null) {
                addName(names, tabTeam.formatString(playerName));
                addName(names, tabTeam.getColorPrefix() + playerName + tabTeam.getColorSuffix());
            }
        }

        addName(names, playerName);
        return names;
    }

    private NetworkPlayerInfo getTabInfo(EntityPlayer player) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.getNetHandler() == null || player.getGameProfile() == null) {
            return null;
        }

        return minecraft.getNetHandler().getPlayerInfo(player.getGameProfile().getId());
    }

    private void addName(List<String> names, IChatComponent component) {
        if (component != null) {
            addName(names, component.getFormattedText());
            addName(names, component.getUnformattedText());
        }
    }

    private void addName(List<String> names, String name) {
        if (name != null && !name.trim().isEmpty() && !names.contains(name)) {
            names.add(name);
        }
    }

    private void sendQueuedCommand(long now) {
        String command = commandQueue.pollReady(now);
        if (command == null) {
            return;
        }

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            player.sendChatMessage(command);
        }
    }

    private void debug(String message) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            player.addChatMessage(new ChatComponentText("[SuffixFix] " + message));
        }
    }
}
