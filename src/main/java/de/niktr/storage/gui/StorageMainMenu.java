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
        return manager.isDe() ? "§3NT • Drahtloses Lager" : "§3NT • Wireless Storage";
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        boolean de = manager.isDe();
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
                .name(de ? "§b§lDrahtloses Lagersystem" : "§b§lWireless Storage System")
                .lore(de ? Arrays.asList(
                        "§7Verbinde Kisten komplett ohne Trichter-Leitungen!",
                        "",
                        "§eStatus: " + (manager.isEnabled() ? "§a§lONLINE" : "§c§lPAUSIERT"),
                        "§eSortierte Items: §f" + sorted,
                        "§eDeine Trichter: §6" + playerHoppers,
                        "§eGesamt aktiv: §f" + totalHoppers + " §7(" + inputs + " In / " + outputs + " Out / " + fallbacks + " Rest)",
                        "",
                        "§8» Wähle unten eine Option aus"
                ) : Arrays.asList(
                        "§7Connect chests without any hopper pipe lines!",
                        "",
                        "§eStatus: " + (manager.isEnabled() ? "§a§lONLINE" : "§c§lPAUSED"),
                        "§eSorted Items: §f" + sorted,
                        "§eYour Hoppers: §6" + playerHoppers,
                        "§eTotal Active: §f" + totalHoppers + " §7(" + inputs + " In / " + outputs + " Out / " + fallbacks + " Fallback)",
                        "",
                        "§8» Select an option below"
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
                .name(de ? "§e§lMeine Wireless-Trichter" : "§e§lMy Wireless Hoppers")
                .lore(de ? Arrays.asList(
                        "§7Übersicht aller von dir platzierten",
                        "§7Eingangs- und Ausgangstrichter.",
                        "",
                        "§8• §7Einstellen von: §fModus, Filtern & Kisten",
                        "",
                        "§e▶ Klicke zum Öffnen der Liste"
                ) : Arrays.asList(
                        "§7Overview of all your placed input",
                        "§7and output hoppers.",
                        "",
                        "§8• §7Configure: §fModes, filters & chests",
                        "",
                        "§e▶ Click to open hopper list"
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
                .name(de ? "§a§lFilter-Einstellungen" : "§a§lFilter Settings")
                .lore(de ? Arrays.asList(
                        "§7Bestimme, welche Items drahtlos in",
                        "§7welche Kisten transportiert werden.",
                        "",
                        "§8• §aMehrere Filter pro Trichter möglich",
                        "§8• §eVolle Kisten sperren nicht das System",
                        "",
                        "§e▶ Klicke zum Verwalten deiner Filter"
                ) : Arrays.asList(
                        "§7Define which items are sorted wirelessly",
                        "§7into which designated chests.",
                        "",
                        "§8• §aMultiple filters per hopper supported",
                        "§8• §eFull chests do not block the pipeline",
                        "",
                        "§e▶ Click to manage filters"
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
                .name(de ? "§d§lCrafting-Rezepte" : "§d§lCrafting Recipes")
                .lore(de ? Arrays.asList(
                        "§7So craftest du die Wireless-Trichter:",
                        "",
                        "§8• §bEnder-Trichter: §f1x Trichter + 1x Enderperle",
                        "§8• §7Funktioniert an jeder Kiste wireless!",
                        "",
                        "§e▶ Klicke für Rezept-Anzeige"
                ) : Arrays.asList(
                        "§7How to craft wireless hoppers:",
                        "",
                        "§8• §bEnder Hopper: §f1x Hopper + 1x Ender Pearl",
                        "§8• §7Works on any chest completely wireless!",
                        "",
                        "§e▶ Click to view recipe diagram"
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
                .name(de ? "§6§lSo funktioniert's" : "§6§lHow It Works")
                .lore(de ? Arrays.asList(
                        "§71. Crafte einen Trichter mit einer §5Enderperle§7.",
                        "§72. Platziere ihn an deiner Eingangskiste (§bInput§7).",
                        "§73. Platziere Trichter an deinen Zielkisten (§aOutput§7).",
                        "§74. Klicke auf den Ausgangstrichter und wähle",
                        "§7   die gewünschten Filter-Items aus.",
                        "§75. Fertig! Items werden §6völlig kabellos§7 einsortiert.",
                        "",
                        "§7Keine Trichter-Ketten oder Leitungen mehr nötig!"
                ) : Arrays.asList(
                        "§71. Craft a hopper with an §5Ender Pearl§7.",
                        "§72. Attach it to your intake chest (§bInput§7).",
                        "§73. Attach hoppers to destination chests (§aOutput§7).",
                        "§74. Right click output hoppers to choose",
                        "§7   your designated filter items.",
                        "§75. Done! Items are sorted §6completely wirelessly§7.",
                        "",
                        "§7No complex hopper pipes or redstone lines needed!"
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
                    .name(manager.isEnabled()
                            ? (de ? "§a§lSortiersystem: AKTIV" : "§a§lSorting System: ACTIVE")
                            : (de ? "§c§lSortiersystem: PAUSIERT" : "§c§lSorting System: PAUSED"))
                    .lore(de ? Arrays.asList(
                            "§7Klicke, um das automatische",
                            "§7Sortieren global ein- oder auszuschalten."
                    ) : Arrays.asList(
                            "§7Click to toggle automatic wireless",
                            "§7sorting system globally on or off."
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
