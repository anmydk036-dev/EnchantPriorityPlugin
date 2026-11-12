package com.example.enchantpriority;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InfinityTotemListener implements Listener {
    private static final String ENCHANT_KEY = "infinity_totem";
    private final JavaPlugin plugin;
    private final PriorityManager priorityManager;
    private final RestrictionTracker tracker;

    public InfinityTotemListener(JavaPlugin plugin, PriorityManager pm, RestrictionTracker tracker) {
        this.plugin = plugin;
        this.priorityManager = pm;
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTotemPop(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        ItemStack totem = player.getInventory().getItem(hand);
        if (totem == null || totem.getType() != Material.TOTEM_OF_UNDYING) return;
        if (!hasInfinityEnchant(totem)) return;

        int rank = priorityManager.registerAndGetRank(ENCHANT_KEY, player.getUniqueId());

        ConfigurationSection tier = getTierConfig(ENCHANT_KEY, rank);
        boolean unlimited = tier.getBoolean("unlimited-usage", false);
        int cooldownSeconds = tier.getInt("cooldown-seconds", 60);

        if (!unlimited && tracker.isOnCooldown(ENCHANT_KEY, player.getUniqueId())) {
            long remaining = tracker.getRemainingSeconds(ENCHANT_KEY, player.getUniqueId());
            player.sendActionBar(Component.text(
                "Infinity Totem on cooldown (" + remaining + "s) [Priority #" + rank + "]",
                NamedTextColor.RED
            ));
            event.setCancelled(true);
            return;
        }

        final ItemStack preservedTotem = totem.clone();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.getInventory().setItem(hand, preservedTotem);
        });

        if (!unlimited && cooldownSeconds > 0) {
            tracker.applyCooldown(ENCHANT_KEY, player.getUniqueId(), cooldownSeconds);
            player.sendActionBar(Component.text(
                "Infinity Totem triggered! Cooldown: " + cooldownSeconds + "s [Priority #" + rank + "]",
                NamedTextColor.GOLD
            ));
        }
    }

    private ConfigurationSection getTierConfig(String enchantKey, int rank) {
        String base = "enchantments." + enchantKey;
        int maxPos = plugin.getConfig().getInt(base + ".max-priority-positions", 4);
        if (rank <= maxPos && plugin.getConfig().isConfigurationSection(base + ".tiers." + rank)) {
            return plugin.getConfig().getConfigurationSection(base + ".tiers." + rank);
        }
        return plugin.getConfig().getConfigurationSection(base + ".overflow");
    }

    private boolean hasInfinityEnchant(ItemStack item) {
        return true;
    }
}
