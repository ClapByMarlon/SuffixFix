package de.marlo.suffixfix;

import de.marlo.suffixfix.core.ZoneBounds;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ListContainerElement;
import net.labymod.settings.elements.NumberElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;

import java.util.List;

public final class SuffixFixSettings {
    private SuffixFixSettings() {
    }

    public static void fill(final SuffixFixAddon addon, List<SettingsElement> settings) {
        final SuffixFixConfig config = addon.getSuffixFixConfig();

        ListContainerElement general = new ListContainerElement("Allgemein", icon(Material.REDSTONE_COMPARATOR_ON));
        settings.add(general);
        general.getSubSettings().add(new BooleanElement("Aktiviert", icon(Material.LEVER), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean value) {
                config.setBoolean("enabled", value.booleanValue());
                addon.saveSuffixFixConfig();
            }
        }, config.isEnabled()));
        general.getSubSettings().add(new BooleanElement("Debug", icon(Material.LEVER), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean value) {
                config.setBoolean("debug", value.booleanValue());
                addon.saveSuffixFixConfig();
            }
        }, config.isDebugEnabled()));
        general.getSubSettings().add(new NumberElement("Scan-Intervall ms", icon(Material.WATCH), (int) config.getScanIntervalMillis()).setMinValue(50).setSteps(50).addCallback(new Consumer<Integer>() {
            @Override
            public void accept(Integer value) {
                config.setLong("scanIntervalMillis", value.longValue());
                addon.saveSuffixFixConfig();
            }
        }));

        ListContainerElement zone = new ListContainerElement("Zone", icon(Material.COMPASS));
        settings.add(zone);
        ZoneBounds bounds = config.getZoneBounds();
        addCoordinate(zone, addon, config, "Pos1 X", "minX", bounds.getMinX());
        addCoordinate(zone, addon, config, "Pos1 Y", "minY", bounds.getMinY());
        addCoordinate(zone, addon, config, "Pos1 Z", "minZ", bounds.getMinZ());
        addCoordinate(zone, addon, config, "Pos2 X", "maxX", bounds.getMaxX());
        addCoordinate(zone, addon, config, "Pos2 Y", "maxY", bounds.getMaxY());
        addCoordinate(zone, addon, config, "Pos2 Z", "maxZ", bounds.getMaxZ());

        ListContainerElement actions = new ListContainerElement("Aktionen", icon(Material.PAPER));
        settings.add(actions);
        actions.getSubSettings().add(new StringElement("Command 1", icon(Material.PAPER), config.getCommands().get(0), new Consumer<String>() {
            @Override
            public void accept(String value) {
                config.setCommand(0, value);
                addon.saveSuffixFixConfig();
            }
        }));
        actions.getSubSettings().add(new StringElement("Command 2", icon(Material.PAPER), config.getCommands().size() > 1 ? config.getCommands().get(1) : "", new Consumer<String>() {
            @Override
            public void accept(String value) {
                config.setCommand(1, value);
                addon.saveSuffixFixConfig();
            }
        }));
        actions.getSubSettings().add(new NumberElement("Chat-Delay ms", icon(Material.WATCH), (int) config.getChatDelayMillis()).setMinValue(250).setSteps(250).addCallback(new Consumer<Integer>() {
            @Override
            public void accept(Integer value) {
                config.setLong("chatDelayMillis", value.longValue());
                addon.saveSuffixFixConfig();
            }
        }));
    }

    private static void addCoordinate(ListContainerElement parent, final SuffixFixAddon addon, final SuffixFixConfig config, String label, final String key, double value) {
        parent.getSubSettings().add(new StringElement(label, icon(Material.PAPER), format(value), new Consumer<String>() {
            @Override
            public void accept(String value) {
                Integer parsed = parseCoordinate(value);
                if (parsed == null) {
                    return;
                }

                config.setDouble(key, parsed.intValue());
                addon.saveSuffixFixConfig();
            }
        }));
    }

    private static Integer parseCoordinate(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String format(double value) {
        return String.valueOf((int) Math.floor(value));
    }

    private static ControlElement.IconData icon(Material material) {
        return new ControlElement.IconData(material);
    }
}
