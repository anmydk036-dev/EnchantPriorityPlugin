package com.example.enchantpriority;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PriorityManager {
    private final JavaPlugin plugin;
    private final File dataFile;
    private final Map<String, List<UUID>> acquisitionOrder = new ConcurrentHashMap<>();

    public PriorityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "priority_data.yml");
        load();
    }

    public int registerAndGetRank(String enchantKey, UUID playerUuid) {
        List<UUID> list = acquisitionOrder.computeIfAbsent(
            enchantKey.toLowerCase(), k -> Collections.synchronizedList(new ArrayList<>())
        );

        synchronized (list) {
            int index = list.indexOf(playerUuid);
            if (index == -1) {
                list.add(playerUuid);
                saveAsync();
                return list.size();
            }
            return index + 1;
        }
    }

    public int getRank(String enchantKey, UUID playerUuid) {
        List<UUID> list = acquisitionOrder.get(enchantKey.toLowerCase());
        if (list == null) return -1;
        synchronized (list) {
            int index = list.indexOf(playerUuid);
            return index == -1 ? -1 : index + 1;
        }
    }

    public void setPlayerRank(String enchantKey, UUID playerUuid, int targetRank) {
        List<UUID> list = acquisitionOrder.computeIfAbsent(
            enchantKey.toLowerCase(), k -> Collections.synchronizedList(new ArrayList<>())
        );
        synchronized (list) {
            list.remove(playerUuid);
            int insertIndex = Math.max(0, Math.min(targetRank - 1, list.size()));
            list.add(insertIndex, playerUuid);
        }
        saveAsync();
    }

    public void resetEnchantment(String enchantKey) {
        acquisitionOrder.remove(enchantKey.toLowerCase());
        saveAsync();
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        if (!config.isConfigurationSection("enchantments")) return;

        for (String key : config.getConfigurationSection("enchantments").getKeys(false)) {
            List<String> rawUuids = config.getStringList("enchantments." + key + ".acquirers");
            List<UUID> uuids = Collections.synchronizedList(new ArrayList<>());
            for (String s : rawUuids) {
                try {
                    uuids.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {}
            }
            acquisitionOrder.put(key.toLowerCase(), uuids);
        }
    }

    public void saveAsync() {
        Map<String, List<String>> snapshot = new HashMap<>();
        acquisitionOrder.forEach((k, v) -> {
            synchronized (v) {
                snapshot.put(k, v.stream().map(UUID::toString).toList());
            }
        });

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            YamlConfiguration config = new YamlConfiguration();
            snapshot.forEach((k, list) -> config.set("enchantments." + k + ".acquirers", list));
            try {
                config.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save priority_data.yml: " + e.getMessage());
            }
        });
    }
}
