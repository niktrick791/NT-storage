package de.niktr.storage.managers;

import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.models.StorageHopperType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class StorageHopperItem {

    public static final NamespacedKey KEY_STORAGE_HOPPER = new NamespacedKey("bordermc", "wireless_storage_hopper");
    public static final NamespacedKey KEY_HOPPER_TYPE = new NamespacedKey("bordermc", "storage_hopper_type");

    private static final NamespacedKey RECIPE_INPUT = new NamespacedKey("bordermc", "wireless_hopper_input");
    private static final NamespacedKey RECIPE_OUTPUT = new NamespacedKey("bordermc", "wireless_hopper_output");
    private static final NamespacedKey RECIPE_FALLBACK = new NamespacedKey("bordermc", "wireless_hopper_fallback");

    private StorageHopperItem() {}

    public static ItemStack createHopper(StorageHopperType type, int amount) {
        if (type == null) type = StorageHopperType.INPUT;
        ItemStack item = new ItemBuilder(Material.HOPPER, amount)
                .name(Component.text(type.getColor() + "§lDrahtloser " + type.getDisplayName()))
                .lore(List.of(
                        Component.text("§7Drahtloses Lagersystem von BorderMC"),
                        Component.text("§8• §7Typ: " + type.getFormattedName()),
                        Component.text("§8• §7Funktion: §f" + type.getDescription()),
                        Component.empty(),
                        Component.text("§e▶ Platziere an einer Kiste (hinten/unten/oben/neben)"),
                        Component.text("§6▶ Rechtsklick: Modus & Filter im Menü anpassen"),
                        Component.text("§7(Rezept: Trichter + Enderperle)")
                ))
                .glow(true)
                .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(KEY_STORAGE_HOPPER, PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(KEY_HOPPER_TYPE, PersistentDataType.STRING, type.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isWirelessHopper(ItemStack item) {
        if (item == null || item.getType() != Material.HOPPER || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(KEY_STORAGE_HOPPER, PersistentDataType.BYTE);
    }

    public static StorageHopperType getHopperType(ItemStack item) {
        if (!isWirelessHopper(item)) return StorageHopperType.INPUT;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return StorageHopperType.INPUT;
        String typeStr = meta.getPersistentDataContainer().get(KEY_HOPPER_TYPE, PersistentDataType.STRING);
        if (typeStr == null) return StorageHopperType.INPUT;
        try {
            return StorageHopperType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return StorageHopperType.INPUT;
        }
    }

    public static void registerRecipes(Plugin plugin) {
        try {
            Bukkit.removeRecipe(RECIPE_INPUT);
            Bukkit.removeRecipe(RECIPE_OUTPUT);
            Bukkit.removeRecipe(RECIPE_FALLBACK);
        } catch (Throwable ignored) {}

        try {
            // Recipe 1: Trichter + Enderperle = Eingangstrichter (Input)
            ShapelessRecipe inputRecipe = new ShapelessRecipe(RECIPE_INPUT, createHopper(StorageHopperType.INPUT, 1));
            inputRecipe.addIngredient(Material.HOPPER);
            inputRecipe.addIngredient(Material.ENDER_PEARL);
            Bukkit.addRecipe(inputRecipe);

            // Recipe 2: Trichter + Enderperle + Papier/Truhe = Ausgangstrichter (Output)
            ShapelessRecipe outputRecipe = new ShapelessRecipe(RECIPE_OUTPUT, createHopper(StorageHopperType.OUTPUT, 1));
            outputRecipe.addIngredient(Material.HOPPER);
            outputRecipe.addIngredient(Material.ENDER_PEARL);
            outputRecipe.addIngredient(Material.CHEST);
            Bukkit.addRecipe(outputRecipe);

            // Recipe 3: Trichter + Enderperle + Redstone = Restetrichter (Fallback)
            ShapelessRecipe fallbackRecipe = new ShapelessRecipe(RECIPE_FALLBACK, createHopper(StorageHopperType.FALLBACK, 1));
            fallbackRecipe.addIngredient(Material.HOPPER);
            fallbackRecipe.addIngredient(Material.ENDER_PEARL);
            fallbackRecipe.addIngredient(Material.REDSTONE);
            Bukkit.addRecipe(fallbackRecipe);
        } catch (Throwable t) {
            plugin.getLogger().warning("Fehler beim Registrieren der Trichter-Rezepte: " + t.getMessage());
        }
    }

    public static void unregisterRecipes() {
        try {
            Bukkit.removeRecipe(RECIPE_INPUT);
            Bukkit.removeRecipe(RECIPE_OUTPUT);
            Bukkit.removeRecipe(RECIPE_FALLBACK);
        } catch (Throwable ignored) {}
    }
}
