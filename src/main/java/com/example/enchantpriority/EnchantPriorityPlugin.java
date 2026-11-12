package com.example.enchantpriority;

import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantPriorityPlugin extends JavaPlugin {
    private PriorityManager priorityManager;
    private RestrictionTracker restrictionTracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.priorityManager = new PriorityManager(this);
        this.restrictionTracker = new RestrictionTracker();

        getServer().getPluginManager().registerEvents(new AcquisitionListener(priorityManager), this);
        getServer().getPluginManager().registerEvents(new InfinityTotemListener(this, priorityManager, restrictionTracker), this);

        getLogger().info("EnchantPriorityPlugin has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (priorityManager != null) {
            priorityManager.saveAsync();
        }
        getLogger().info("EnchantPriorityPlugin has been disabled.");
    }
}
