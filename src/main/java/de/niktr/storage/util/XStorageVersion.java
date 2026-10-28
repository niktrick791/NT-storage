package de.niktr.storage.util;

import de.niktr.storage.models.StorageHopperType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XStorageVersion {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static final Material BARREL_MAT;
    private static final String WIRELESS_PREFIX = "§0§n§t§_§h§o§p§p§e§r§:";

    static {
        BARREL_MAT = Material.matchMaterial("BARREL");
    }

    private XStorageVersion() {}

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return "";
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static ItemStack createItem(Material mat, int amount, short durability) {
        try {
            return new ItemStack(mat, amount, durability);
        } catch (Throwable t) {
            return new ItemStack(mat, amount);
        }
    }

    public static ItemStack getGlassPane(int colorData, String modernName) {
        Material modern = Material.matchMaterial(modernName);
        if (modern != null) {
            return new ItemStack(modern, 1);
        }
        Material legacy = Material.matchMaterial("STAINED_GLASS_PANE");
        if (legacy != null) {
            return createItem(legacy, 1, (short) colorData);
        }
        Material glass = Material.matchMaterial("THIN_GLASS");
        if (glass != null) return new ItemStack(glass, 1);
        return new ItemStack(Material.GLASS, 1);
    }

    public static ItemStack getDye(int colorData, String modernName) {
        Material modern = Material.matchMaterial(modernName);
        if (modern != null) {
            return new ItemStack(modern, 1);
        }
        Material legacy = Material.matchMaterial("INK_SACK");
        if (legacy != null) {
            return createItem(legacy, 1, (short) colorData);
        }
        return new ItemStack(Material.PAPER, 1);
    }

    public static ItemStack getConcreteOrWool(int colorData, String modernName) {
        Material modern = Material.matchMaterial(modernName);
        if (modern != null) {
            return new ItemStack(modern, 1);
        }
        Material legacy = Material.matchMaterial("WOOL");
        if (legacy != null) {
            return createItem(legacy, 1, (short) colorData);
        }
        return new ItemStack(Material.STONE, 1);
    }

    public static ItemStack getGlazedTerracotta(int colorData, String modernName) {
        Material modern = Material.matchMaterial(modernName);
        if (modern != null) {
            return new ItemStack(modern, 1);
        }
        Material clay = Material.matchMaterial("STAINED_CLAY");
        if (clay != null) {
            return createItem(clay, 1, (short) colorData);
        }
        Material wool = Material.matchMaterial("WOOL");
        if (wool != null) {
            return createItem(wool, 1, (short) colorData);
        }
        return new ItemStack(Material.STONE, 1);
    }

    public static boolean isItem(Material mat) {
        if (mat == null) return false;
        try {
            Method m = Material.class.getMethod("isItem");
            return (Boolean) m.invoke(mat);
        } catch (Throwable ignored) {
            return mat != Material.AIR;
        }
    }

    public static boolean isChestBlock(Block block) {
        if (block == null) return false;
        Material mat = block.getType();
        if (mat == Material.CHEST || mat == Material.TRAPPED_CHEST) return true;
        if (BARREL_MAT != null && mat == BARREL_MAT) return true;
        return false;
    }

    public static void setGlint(ItemMeta meta) {
        if (meta == null) return;
        try {
            Method glintMethod = meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class);
            glintMethod.invoke(meta, Boolean.TRUE);
            return;
        } catch (Throwable ignored) {}

        try {
            Enchantment unbreaking = Enchantment.getByName("DURABILITY");
            if (unbreaking == null) unbreaking = Enchantment.getByName("UNBREAKING");
            if (unbreaking != null) {
                meta.addEnchant(unbreaking, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        } catch (Throwable ignored) {}
    }

    public static void markWirelessHopper(ItemStack item, StorageHopperType type) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Try PDC (1.14+)
        try {
            Method getPdc = meta.getClass().getMethod("getPersistentDataContainer");
            Object pdc = getPdc.invoke(meta);
            if (pdc != null) {
                Class<?> pdcClass = pdc.getClass();
                Class<?> pdtClass = Class.forName("org.bukkit.persistence.PersistentDataType");
                Object byteType = pdtClass.getField("BYTE").get(null);
                Object stringType = pdtClass.getField("STRING").get(null);

                NamespacedKey keyStorage = new NamespacedKey("niktrick", "wireless_storage_hopper");
                NamespacedKey keyType = new NamespacedKey("niktrick", "storage_hopper_type");

                Method setMethod = pdcClass.getMethod("set", NamespacedKey.class, pdtClass, Object.class);
                setMethod.invoke(pdc, keyStorage, byteType, (byte) 1);
                setMethod.invoke(pdc, keyType, stringType, type.name());
            }
        } catch (Throwable ignored) {}

        // Fallback / Dual Tag: Lore marker (works 1.8 - 1.21+)
        List<String> lore = meta.hasLore() ? new ArrayList<String>(meta.getLore()) : new ArrayList<String>();
        for (int i = lore.size() - 1; i >= 0; i--) {
            if (lore.get(i).startsWith(WIRELESS_PREFIX)) {
                lore.remove(i);
            }
        }
        lore.add(WIRELESS_PREFIX + type.name());
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static boolean isWirelessHopper(ItemStack item) {
        if (item == null || item.getType() != Material.HOPPER || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        // Try PDC first
        try {
            Method getPdc = meta.getClass().getMethod("getPersistentDataContainer");
            Object pdc = getPdc.invoke(meta);
            if (pdc != null) {
                Class<?> pdcClass = pdc.getClass();
                Class<?> pdtClass = Class.forName("org.bukkit.persistence.PersistentDataType");
                Object byteType = pdtClass.getField("BYTE").get(null);

                NamespacedKey keyStorage = new NamespacedKey("niktrick", "wireless_storage_hopper");
                Method hasMethod = pdcClass.getMethod("has", NamespacedKey.class, pdtClass);
                boolean has = (Boolean) hasMethod.invoke(pdc, keyStorage, byteType);
                if (has) return true;

                // Also check legacy bordermc key if previously set
                NamespacedKey oldKey = new NamespacedKey("bordermc", "wireless_storage_hopper");
                if ((Boolean) hasMethod.invoke(pdc, oldKey, byteType)) return true;
            }
        } catch (Throwable ignored) {}

        // Lore check fallback
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.startsWith(WIRELESS_PREFIX)) return true;
                if (line.contains("Drahtloses Lagersystem")) return true;
            }
        }
        return false;
    }

    public static StorageHopperType getHopperType(ItemStack item) {
        if (!isWirelessHopper(item)) return StorageHopperType.INPUT;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return StorageHopperType.INPUT;

        // Try PDC
        try {
            Method getPdc = meta.getClass().getMethod("getPersistentDataContainer");
            Object pdc = getPdc.invoke(meta);
            if (pdc != null) {
                Class<?> pdcClass = pdc.getClass();
                Class<?> pdtClass = Class.forName("org.bukkit.persistence.PersistentDataType");
                Object stringType = pdtClass.getField("STRING").get(null);

                NamespacedKey keyType = new NamespacedKey("niktrick", "storage_hopper_type");
                Method getMethod = pdcClass.getMethod("get", NamespacedKey.class, pdtClass);
                String typeStr = (String) getMethod.invoke(pdc, keyType, stringType);
                if (typeStr == null) {
                    NamespacedKey oldKey = new NamespacedKey("bordermc", "storage_hopper_type");
                    typeStr = (String) getMethod.invoke(pdc, oldKey, stringType);
                }
                if (typeStr != null) {
                    try {
                        return StorageHopperType.valueOf(typeStr);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        // Check lore marker
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.startsWith(WIRELESS_PREFIX)) {
                    String sub = line.substring(WIRELESS_PREFIX.length()).trim();
                    try {
                        return StorageHopperType.valueOf(sub);
                    } catch (IllegalArgumentException ignored) {}
                }
                if (line.contains("Ausgangstrichter")) return StorageHopperType.OUTPUT;
                if (line.contains("Restetrichter")) return StorageHopperType.FALLBACK;
            }
        }
        return StorageHopperType.INPUT;
    }

    public static void playClick(Player player) {
        playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 0.6f, 1.2f);
    }

    public static void playBreak(Location loc) {
        playSound(null, loc, "BLOCK_AMETHYST_BLOCK_BREAK", "ITEM_BREAK", 0.7f, 1.2f);
    }

    public static void playTeleport(Location loc) {
        playSound(null, loc, "ENTITY_ENDERMAN_TELEPORT", "ENDERMAN_TELEPORT", 0.7f, 1.2f);
    }

    public static void playPling(Player player, float pitch) {
        playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING", 0.7f, pitch);
    }

    public static void playChest(Player player) {
        playSound(player, player.getLocation(), "BLOCK_CHEST_OPEN", "CHEST_OPEN", 0.6f, 1.4f);
    }

    public static void playPickup(Location loc) {
        playSound(null, loc, "ENTITY_ITEM_PICKUP", "ITEM_PICKUP", 0.3f, 1.8f);
    }

    public static void playItemBreak(Player player) {
        playSound(player, player.getLocation(), "ENTITY_ITEM_BREAK", "ITEM_BREAK", 0.6f, 1.4f);
    }

    public static void playSuccess(Player player) {
        playSound(player, player.getLocation(), "ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP", 0.7f, 1.4f);
    }

    public static void playError(Player player) {
        playSound(player, player.getLocation(), "ENTITY_VILLAGER_NO", "VILLAGER_NO", 0.6f, 1.0f);
    }

    public static void playSound(Player target, Location loc, String modernSound, String legacySound, float volume, float pitch) {
        if (loc == null && target != null) loc = target.getLocation();
        if (loc == null) return;
        Sound sound = null;
        try {
            sound = Sound.valueOf(modernSound);
        } catch (Throwable t) {
            if (legacySound != null) {
                try {
                    sound = Sound.valueOf(legacySound);
                } catch (Throwable ignored) {}
            }
        }
        if (sound == null) return;
        try {
            if (target != null) {
                target.playSound(loc, sound, volume, pitch);
            } else if (loc.getWorld() != null) {
                loc.getWorld().playSound(loc, sound, volume, pitch);
            }
        } catch (Throwable ignored) {}
    }

    public static void spawnPortalParticle(Location loc, int count) {
        if (loc == null || loc.getWorld() == null) return;
        try {
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Object portal = particleClass.getField("PORTAL").get(null);
            Method spawn = loc.getWorld().getClass().getMethod("spawnParticle", particleClass, Location.class, int.class, double.class, double.class, double.class, double.class);
            spawn.invoke(loc.getWorld(), portal, loc, count, 0.2, 0.2, 0.2, 0.05);
            return;
        } catch (Throwable ignored) {}

        try {
            Class<?> effectClass = Class.forName("org.bukkit.Effect");
            Object portalEffect = effectClass.getField("PORTAL").get(null);
            Method playEffect = loc.getWorld().getClass().getMethod("playEffect", Location.class, effectClass, int.class);
            playEffect.invoke(loc.getWorld(), loc, portalEffect, 0);
        } catch (Throwable ignored) {}
    }

    public static void registerShapelessRecipe(Plugin plugin, String keyName, ItemStack result, Material... ingredients) {
        try {
            Constructor<NamespacedKey> nskCons = NamespacedKey.class.getConstructor(Plugin.class, String.class);
            NamespacedKey key = nskCons.newInstance(plugin, keyName);

            try {
                Bukkit.removeRecipe(key);
            } catch (Throwable ignored) {}

            Constructor<ShapelessRecipe> recCons = ShapelessRecipe.class.getConstructor(NamespacedKey.class, ItemStack.class);
            ShapelessRecipe recipe = recCons.newInstance(key, result);
            for (Material m : ingredients) {
                recipe.addIngredient(m);
            }
            Bukkit.addRecipe(recipe);
            return;
        } catch (Throwable t) {
            // 1.8 - 1.11 fallback without NamespacedKey
            try {
                Constructor<ShapelessRecipe> recCons = ShapelessRecipe.class.getConstructor(ItemStack.class);
                ShapelessRecipe recipe = recCons.newInstance(result);
                for (Material m : ingredients) {
                    recipe.addIngredient(m);
                }
                Bukkit.addRecipe(recipe);
            } catch (Throwable ex) {
                plugin.getLogger().warning("Konnte Rezept " + keyName + " nicht registrieren: " + ex.getMessage());
            }
        }
    }

    public static void unregisterShapelessRecipe(Plugin plugin, String keyName) {
        try {
            Constructor<NamespacedKey> nskCons = NamespacedKey.class.getConstructor(Plugin.class, String.class);
            NamespacedKey key = nskCons.newInstance(plugin, keyName);
            Bukkit.removeRecipe(key);
            return;
        } catch (Throwable ignored) {}

        try {
            Iterator<Recipe> it = Bukkit.recipeIterator();
            while (it.hasNext()) {
                Recipe r = it.next();
                if (r instanceof ShapelessRecipe) {
                    ItemStack res = r.getResult();
                    if (isWirelessHopper(res)) {
                        it.remove();
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
}
