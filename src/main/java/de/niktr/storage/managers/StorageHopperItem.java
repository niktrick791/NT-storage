package de.niktr.storage.managers;

import de.niktr.storage.models.StorageHopperType;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.util.XStorageVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public final class StorageHopperItem {

    public static final String KEY_INPUT = "wireless_hopper_input";
    public static final String KEY_OUTPUT = "wireless_hopper_output";
    public static final String KEY_FALLBACK = "wireless_hopper_fallback";

    private StorageHopperItem() {}

    public static ItemStack createHopper(StorageHopperType type, int amount) {
        return createHopper(type, amount, false);
    }

    public static ItemStack createHopper(StorageHopperType type, int amount, boolean isDe) {
        if (type == null) type = StorageHopperType.INPUT;
        String name = isDe
                ? type.getColor() + "§lDrahtloser " + type.getDisplayName(true)
                : type.getColor() + "§lWireless " + type.getDisplayName(false);

        java.util.List<String> lore = isDe ? Arrays.asList(
                "§7Drahtloses Lagersystem von niktrick",
                "§8• §7Typ: " + type.getFormattedName(true),
                "§8• §7Funktion: §f" + type.getDescription(true),
                "",
                "§e▶ Platziere an einer Kiste (hinten/unten/oben/neben)",
                "§6▶ Rechtsklick: Modus & Filter im Menü anpassen",
                "§7(Rezept: Trichter + Enderperle)"
        ) : Arrays.asList(
                "§7Wireless Storage System by niktrick",
                "§8• §7Type: " + type.getFormattedName(false),
                "§8• §7Function: §f" + type.getDescription(false),
                "",
                "§e▶ Place next to a chest (back/bottom/top/side)",
                "§6▶ Right click: Configure mode & filters in GUI",
                "§7(Recipe: Hopper + Ender Pearl)"
        );

        ItemStack item = new ItemBuilder(Material.HOPPER, amount)
                .name(name)
                .lore(lore)
                .glow(true)
                .build();

        XStorageVersion.markWirelessHopper(item, type);
        return item;
    }

    public static boolean isWirelessHopper(ItemStack item) {
        return XStorageVersion.isWirelessHopper(item);
    }

    public static StorageHopperType getHopperType(ItemStack item) {
        return XStorageVersion.getHopperType(item);
    }

    public static void registerRecipes(Plugin plugin) {
        registerRecipes(plugin, false);
    }

    public static void registerRecipes(Plugin plugin, boolean isDe) {
        try {
            unregisterRecipes(plugin);

            // Recipe 1: Trichter + Enderperle = Eingangstrichter (Input)
            XStorageVersion.registerShapelessRecipe(
                    plugin, KEY_INPUT,
                    createHopper(StorageHopperType.INPUT, 1, isDe),
                    Material.HOPPER, Material.ENDER_PEARL
            );

            // Recipe 2: Trichter + Enderperle + Kiste = Ausgangstrichter (Output)
            XStorageVersion.registerShapelessRecipe(
                    plugin, KEY_OUTPUT,
                    createHopper(StorageHopperType.OUTPUT, 1, isDe),
                    Material.HOPPER, Material.ENDER_PEARL, Material.CHEST
            );

            // Recipe 3: Trichter + Enderperle + Redstone = Restetrichter (Fallback)
            XStorageVersion.registerShapelessRecipe(
                    plugin, KEY_FALLBACK,
                    createHopper(StorageHopperType.FALLBACK, 1, isDe),
                    Material.HOPPER, Material.ENDER_PEARL, Material.REDSTONE
            );
        } catch (Throwable t) {
            plugin.getLogger().warning("Fehler beim Registrieren der Trichter-Rezepte: " + t.getMessage());
        }
    }

    public static void unregisterRecipes(Plugin plugin) {
        try {
            XStorageVersion.unregisterShapelessRecipe(plugin, KEY_INPUT);
            XStorageVersion.unregisterShapelessRecipe(plugin, KEY_OUTPUT);
            XStorageVersion.unregisterShapelessRecipe(plugin, KEY_FALLBACK);
        } catch (Throwable ignored) {}
    }
}
