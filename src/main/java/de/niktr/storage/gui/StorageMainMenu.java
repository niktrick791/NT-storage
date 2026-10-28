package de.niktr.storage.gui;

import de.niktr.storage.util.Menu;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.models.StorageHopperType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class StorageMainMenu extends Menu<Plugin> {

    private final StorageManager manager;

    public StorageMainMenu(Plugin plugin, Player player, StorageManager manager) {
        this(plugin, player, manager, null);
    }

    public StorageMainMenu(Plugin plugin, Player player, StorageManager manager, Menu<?> parent) {
        super(plugin, player, parent);
        this.manager = manager;
    }

    @Override
    public Component title() {
        return Component.text("BorderMC • Drahtloses Lager", NamedTextColor.DARK_AQUA);
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        long sorted = manager.getTotalItemsSorted();
        int totalHoppers = manager.getHoppers().size();
        int playerHoppers = manager.getPlayerHoppers(player.getUniqueId()).size();
        int inputs = 0, outputs = 0, fallbacks = 0;
        for (StorageHopper h : manager.getHoppers()) {
            if (h.getType() == StorageHopperType.INPUT) inputs++;
            else if (h.getType() == StorageHopperType.OUTPUT) outputs++;
            else if (h.getType() == StorageHopperType.FALLBACK) fallbacks++;
        }

        // Slot 4: Header Overview
        set(4, new ItemBuilder(Material.NETHER_STAR)
                .name(Component.text("§b§lDrahtloses Lagersystem"))
                .lore(List.of(
                        Component.text("§7Verbinde Kisten komplett ohne Trichter-Leitungen!"),
                        Component.empty(),
                        Component.text("§eStatus: " + (manager.isEnabled() ? "§a§lONLINE" : "§c§lPAUSIERT")),
                        Component.text("§eSortierte Items: §f" + sorted),
                        Component.text("§eDeine Trichter: §6" + playerHoppers),
                        Component.text("§eGesamt aktiv: §f" + totalHoppers + " §7(" + inputs + " In / " + outputs + " Out / " + fallbacks + " Rest)"),
                        Component.empty(),
                        Component.text("§8» Wähle unten eine Option aus")
                ))
                .glow(true)
                .build(), event -> player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f));

        // Slot 11: Meine Trichter verwalten
        set(11, new ItemBuilder(Material.HOPPER)
                .name(Component.text("§e§lMeine Wireless-Trichter"))
                .lore(List.of(
                        Component.text("§7Übersicht aller von dir platzierten"),
                        Component.text("§7Eingangs- und Ausgangstrichter."),
                        Component.empty(),
                        Component.text("§8• §7Einstellen von: §fModus, Filtern & Kisten"),
                        Component.empty(),
                        Component.text("§e▶ Klicke zum Öffnen der Liste")
                ))
                .glow(true)
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            openLater(new HopperListMenu(plugin, player, manager, this));
        });

        // Slot 13: Filter & Zuordnungen
        set(13, new ItemBuilder(Material.ITEM_FRAME)
                .name(Component.text("§a§lFilter-Einstellungen"))
                .lore(List.of(
                        Component.text("§7Bestimme, welche Items drahtlos in"),
                        Component.text("§7welche Kisten transportiert werden."),
                        Component.empty(),
                        Component.text("§8• §aMehrere Filter pro Trichter möglich"),
                        Component.text("§8• §eVolle Kisten sperren nicht das System"),
                        Component.empty(),
                        Component.text("§e▶ Klicke zum Verwalten deiner Filter")
                ))
                .glow(true)
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            openLater(new HopperListMenu(plugin, player, manager, this));
        });

        // Slot 15: Crafting-Rezept Guide
        set(15, new ItemBuilder(Material.CRAFTING_TABLE)
                .name(Component.text("§d§lCrafting-Rezepte"))
                .lore(List.of(
                        Component.text("§7So craftest du die Wireless-Trichter:"),
                        Component.empty(),
                        Component.text("§8• §bEnder-Trichter: §f1x Trichter + 1x Enderperle"),
                        Component.text("§8• §7Funktioniert an jeder Kiste wireless!"),
                        Component.empty(),
                        Component.text("§e▶ Klicke für Rezept-Anzeige")
                ))
                .glow(true)
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            openLater(new StorageRecipeMenu(plugin, player, this));
        });

        // Slot 22: Anleitung & Kurzhilfe
        set(22, new ItemBuilder(Material.BOOK)
                .name(Component.text("§6§lSo funktioniert's"))
                .lore(List.of(
                        Component.text("§71. Crafte einen Trichter mit einer §5Enderperle§7."),
                        Component.text("§72. Platziere ihn an deiner Eingangskiste (§bInput§7)."),
                        Component.text("§73. Platziere Trichter an deinen Zielkisten (§aOutput§7)."),
                        Component.text("§74. Klicke auf den Ausgangstrichter und wähle"),
                        Component.text("§7   die gewünschten Filter-Items aus."),
                        Component.text("§75. Fertig! Items werden §6völlig kabellos§7 einsortiert."),
                        Component.empty(),
                        Component.text("§7Keine Trichter-Ketten oder Leitungen mehr nötig!")
                ))
                .build(), event -> player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f));

        // Admin Toggle in Slot 31
        if (player.isOp() || player.hasPermission("bordermc.admin") || player.hasPermission("NiktrickStorage.admin")) {
            set(31, new ItemBuilder(manager.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name(Component.text(manager.isEnabled() ? "§a§lSortiersystem: AKTIV" : "§c§lSortiersystem: PAUSIERT"))
                    .lore(List.of(
                            Component.text("§7Klicke, um das automatische"),
                            Component.text("§7Sortieren global ein- oder auszuschalten.")
                    ))
                    .build(), event -> {
                manager.setEnabled(!manager.isEnabled());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, manager.isEnabled() ? 1.5f : 0.8f);
                refresh();
            });
        }

        // Slot 40: Back / Close
        set(40, parent != null ? backItem() : closeItem(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            back();
        });

        fillEmpty();
    }

    private void buildFrame() {
        ItemStack frame = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(Component.empty()).build();
        for (int i = 0; i < 9; i++) {
            if (i != 4) set(i, frame);
            if (i != 4) set(36 + i, frame);
        }
        for (int row = 1; row < 4; row++) {
            set(row * 9, frame);
            set(row * 9 + 8, frame);
        }
    }
}
