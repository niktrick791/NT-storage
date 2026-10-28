package de.niktr.storage.gui;

import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.models.StorageHopperType;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.util.Menu;
import de.niktr.storage.util.XStorageVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class StorageRecipeMenu extends Menu<Plugin> {

    public StorageRecipeMenu(Plugin plugin, Player player, Menu<?> parent) {
        super(plugin, player, parent);
    }

    @Override
    public String title() {
        return "§3niktrick • Trichter-Rezepte";
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        // Slot 4: Info
        Material tableMat = Material.matchMaterial("CRAFTING_TABLE");
        if (tableMat == null) tableMat = Material.matchMaterial("WORKBENCH");
        if (tableMat == null) tableMat = Material.CHEST;

        set(4, new ItemBuilder(tableMat)
                .name("§b§lRezept: Drahtloser Trichter")
                .lore(Arrays.asList(
                        "§7Lege einen normalen Trichter und",
                        "§7eine §5Enderperle§7 in die Werkbank.",
                        "",
                        "§8• §bEingangstrichter: §fTrichter + Enderperle",
                        "§8• §aAusgangstrichter: §fTrichter + Enderperle + Truhe",
                        "§8• §eRestetrichter: §fTrichter + Enderperle + Redstone",
                        "",
                        "§7Tipp: Der Modus kann nach dem Platzieren",
                        "§7auch jederzeit im Menü gewechselt werden!"
                ))
                .glow(true)
                .build());

        // 3x3 Crafting Grid slots:
        set(11, new ItemBuilder(Material.ENDER_PEARL)
                .name("§5Enderperle")
                .lore("§7Gibt dem Trichter drahtlose Magie")
                .build());

        set(20, new ItemBuilder(Material.HOPPER)
                .name("§fNormaler Trichter")
                .build());

        // Arrow
        set(23, new ItemBuilder(Material.ARROW)
                .name("§e= Ergibt =")
                .build());

        // Result slot 25
        set(25, StorageHopperItem.createHopper(StorageHopperType.INPUT, 1));

        // Slot 40: Back
        set(40, backItem(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                XStorageVersion.playClick(player);
                back();
            }
        });

        fillEmpty();
    }

    private void buildFrame() {
        ItemStack frame = new ItemBuilder(XStorageVersion.getGlassPane(9, "CYAN_STAINED_GLASS_PANE")).name(" ").build();
        for (int i = 0; i < 9; i++) if (i != 4) set(i, frame);
        for (int i = 36; i < 45; i++) if (i != 40) set(i, frame);
        for (int row = 1; row < 4; row++) {
            set(row * 9, frame);
            set(row * 9 + 8, frame);
        }
    }
}
