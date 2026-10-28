package de.niktr.storage.gui;

import de.niktr.storage.util.Menu;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.models.StorageHopperType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class StorageRecipeMenu extends Menu<Plugin> {

    public StorageRecipeMenu(Plugin plugin, Player player, Menu<?> parent) {
        super(plugin, player, parent);
    }

    @Override
    public Component title() {
        return Component.text("BorderMC • Trichter-Rezepte", NamedTextColor.DARK_AQUA);
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        // Slot 4: Info
        set(4, new ItemBuilder(Material.CRAFTING_TABLE)
                .name(Component.text("§b§lRezept: Drahtloser Trichter"))
                .lore(List.of(
                        Component.text("§7Lege einen normalen Trichter und"),
                        Component.text("§7eine §5Enderperle§7 in die Werkbank."),
                        Component.empty(),
                        Component.text("§8• §bEingangstrichter: §fTrichter + Enderperle"),
                        Component.text("§8• §aAusgangstrichter: §fTrichter + Enderperle + Truhe"),
                        Component.text("§8• §eRestetrichter: §fTrichter + Enderperle + Redstone"),
                        Component.empty(),
                        Component.text("§7Tipp: Der Modus kann nach dem Platzieren"),
                        Component.text("§7auch jederzeit im Menü gewechselt werden!")
                ))
                .glow(true)
                .build());

        // 3x3 Crafting Grid slots:
        // Row 1: 10, 11, 12
        // Row 2: 19, 20, 21
        // Row 3: 28, 29, 30

        set(11, new ItemBuilder(Material.ENDER_PEARL)
                .name(Component.text("§5Enderperle"))
                .lore(Component.text("§7Gibt dem Trichter drahtlose Magie"))
                .build());

        set(20, new ItemBuilder(Material.HOPPER)
                .name(Component.text("§fNormaler Trichter"))
                .build());

        // Arrow
        set(23, new ItemBuilder(Material.ARROW)
                .name(Component.text("§e= Ergibt ="))
                .build());

        // Result slot 25
        set(25, StorageHopperItem.createHopper(StorageHopperType.INPUT, 1));

        // Slot 40: Back
        set(40, backItem(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            back();
        });

        fillEmpty();
    }

    private void buildFrame() {
        ItemStack frame = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(Component.empty()).build();
        for (int i = 0; i < 9; i++) if (i != 4) set(i, frame);
        for (int i = 36; i < 45; i++) if (i != 40) set(i, frame);
        for (int row = 1; row < 4; row++) {
            set(row * 9, frame);
            set(row * 9 + 8, frame);
        }
    }
}
