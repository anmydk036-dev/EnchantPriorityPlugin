package com.example.enchantpriority;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RestrictionTracker {
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<String, Integer> charges = new ConcurrentHashMap<>();

    private String key(String enchantKey, UUID uuid) {
        return enchantKey.toLowerCase() + ":" + uuid.toString();
    }

    public boolean isOnCooldown(String enchantKey, UUID uuid) {
        Long expiry = cooldowns.get(key(enchantKey, uuid));
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            cooldowns.remove(key(enchantKey, uuid));
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(String enchantKey, UUID uuid) {
        Long expiry = cooldowns.get(key(enchantKey, uuid));
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis() + 999) / 1000);
    }

    public void applyCooldown(String enchantKey, UUID uuid, int seconds) {
        if (seconds <= 0) return;
        cooldowns.put(key(enchantKey, uuid), System.currentTimeMillis() + (seconds * 1000L));
    }
}
