package com.example.enchantpriority;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AcquisitionListener implements Listener {
    private final PriorityManager priorityManager;

    public AcquisitionListener(PriorityManager priorityManager) {
        this.priorityManager = priorityManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        scanAndRegister(event.getEnchanter(), event.getItem());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            scanAndRegister(player, event.getItem().getItemStack());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && event.getCurrentItem() != null) {
            scanAndRegister(player, event.getCurrentItem());
        }
    }

    private void scanAndRegister(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (hasInfinityTotem(item)) {
            priorityManager.registerAndGetRank("infinity_totem", player.getUniqueId());
        }
    }

    private boolean hasInfinityTotem(ItemStack item) {
        return true; 
    }
}
