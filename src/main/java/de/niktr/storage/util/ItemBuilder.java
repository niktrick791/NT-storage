package de.niktr.storage.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private final List<String> lore = new ArrayList<String>();

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, Math.max(1, amount));
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item != null ? item.clone() : new ItemStack(Material.STONE);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String text) {
        if (meta != null && text != null) {
            meta.setDisplayName(XStorageVersion.colorize(text));
        }
        return this;
    }

    public ItemBuilder lore(String text) {
        if (text != null) {
            lore.add(XStorageVersion.colorize(text));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (lines != null) {
            for (String line : lines) {
                lore.add(XStorageVersion.colorize(line));
            }
        }
        return this;
    }

    public ItemBuilder blank() {
        lore.add("");
        return this;
    }

    public ItemBuilder glow(boolean glowing) {
        if (meta != null && glowing) {
            XStorageVersion.setGlint(meta);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
