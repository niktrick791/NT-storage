package de.niktr.storage.gui;

import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.StorageHopper;
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
    public String title() {
        return "§3niktrick • Drahtloses Lager";
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
                .name("§b§lDrahtloses Lagersystem")
                .lore(Arrays.asList(
                        "§7Verbinde Kisten komplett ohne Trichter-Leitungen!",
                        "",
                        "§eStatus: " + (manager.isEnabled() ? "§a§lONLINE" : "§c§lPAUSIERT"),
                        "§eSortierte Items: §f" + sorted,
                        "§eDeine Trichter: §6" + playerHoppers,
                        "§eGesamt aktiv: §f" + totalHoppers + " §7(" + inputs + " In / " + outputs + " Out / " + fallbacks + " Rest)",
                        "",
                        "§8» Wähle unten eine Option aus"
                ))
                .glow(true)
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                XStorageVersion.playClick(player);
            }
        });

        // Slot 11: Meine Trichter verwalten
        set(11, new ItemBuilder(Material.HOPPER)
                .name("§e§lMeine Wireless-Trichter")
                .lore(Arrays.asList(
                        "§7Übersicht aller von dir platzierten",
                        "§7Eingangs- und Ausgangstrichter.",
                        "",
                        "§8• §7Einstellen von: §fModus, Filtern & Kisten",
                        "",
                        "§e▶ Klicke zum Öffnen der Liste"
                ))
                .glow(true)
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                XStorageVersion.playClick(player);
                openLater(new HopperListMenu(plugin, player, manager, StorageMainMenu.this));
            }
        });

        // Slot 13: Filter & Zuordnungen
        Material frameMat = Material.matchMaterial("ITEM_FRAME");
        if (frameMat == null) frameMat = Material.PAINTING;
        set(13, new ItemBuilder(frameMat)
                .name("§a§lFilter-Einstellungen")
                .lore(Arrays.asList(
                        "§7Bestimme, welche Items drahtlos in",
                        "§7welche Kisten transportiert werden.",
                        "",
                        "§8• §aMehrere Filter pro Trichter möglich",
                        "§8• §eVolle Kisten sperren nicht das System",
                        "",
                        "§e▶ Klicke zum Verwalten deiner Filter"
                ))
                .glow(true)
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                XStorageVersion.playClick(player);
                openLater(new HopperListMenu(plugin, player, manager, StorageMainMenu.this));
            }
        });

        // Slot 15: Crafting-Rezept Guide
        Material tableMat = Material.matchMaterial("CRAFTING_TABLE");
        if (tableMat == null) tableMat = Material.matchMaterial("WORKBENCH");
        if (tableMat == null) tableMat = Material.CHEST;
        set(15, new ItemBuilder(tableMat)
                .name("§d§lCrafting-Rezepte")
                .lore(Arrays.asList(
                        "§7So craftest du die Wireless-Trichter:",
                        "",
                        "§8• §bEnder-Trichter: §f1x Trichter + 1x Enderperle",
                        "§8• §7Funktioniert an jeder Kiste wireless!",
                        "",
                        "§e▶ Klicke für Rezept-Anzeige"
                ))
                .glow(true)
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                XStorageVersion.playClick(player);
                openLater(new StorageRecipeMenu(plugin, player, StorageMainMenu.this));
            }
        });

        // Slot 22: Anleitung & Kurzhilfe
        set(22, new ItemBuilder(Material.BOOK)
                .name("§6§lSo funktioniert's")
                .lore(Arrays.asList(
                        "§71. Crafte einen Trichter mit einer §5Enderperle§7.",
                        "§72. Platziere ihn an deiner Eingangskiste (§bInput§7).",
                        "§73. Platziere Trichter an deinen Zielkisten (§aOutput§7).",
                        "§74. Klicke auf den Ausgangstrichter und wähle",
                        "§7   die gewünschten Filter-Items aus.",
                        "§75. Fertig! Items werden §6völlig kabellos§7 einsortiert.",
                        "",
                        "§7Keine Trichter-Ketten oder Leitungen mehr nötig!"
                ))
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                XStorageVersion.playClick(player);
            }
        });

        // Admin Toggle in Slot 31
        if (player.isOp() || player.hasPermission("niktrick.storage.admin")) {
            ItemStack toggleItem = manager.isEnabled() ? XStorageVersion.getDye(10, "LIME_DYE") : XStorageVersion.getDye(8, "GRAY_DYE");
            set(31, new ItemBuilder(toggleItem)
                    .name(manager.isEnabled() ? "§a§lSortiersystem: AKTIV" : "§c§lSortiersystem: PAUSIERT")
                    .lore(Arrays.asList(
                            "§7Klicke, um das automatische",
                            "§7Sortieren global ein- oder auszuschalten."
                    ))
                    .build(), new ClickAction() {
                @Override
                public void execute(InventoryClickEvent event) {
                    manager.setEnabled(!manager.isEnabled());
                    XStorageVersion.playPling(player, manager.isEnabled() ? 1.5f : 0.8f);
                    refresh();
                }
            });
        }

        // Slot 40: Back / Close
        set(40, parent != null ? backItem() : closeItem(), new ClickAction() {
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
