package de.niktr.storage.gui;

import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.ChestRef;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.models.StorageHopperType;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.util.Menu;
import de.niktr.storage.util.XStorageVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class HopperConfigMenu extends Menu<Plugin> {

    private final StorageManager manager;
    private final StorageHopper hopper;

    public HopperConfigMenu(Plugin plugin, Player player, StorageManager manager, StorageHopper hopper, Menu<?> parent) {
        super(plugin, player, parent);
        this.manager = manager;
        this.hopper = hopper;
    }

    @Override
    public String title() {
        return "§3niktrick • Trichter verwalten";
    }

    @Override
    public int size() {
        return 36;
    }

    @Override
    protected void build() {
        buildFrame();

        // Slot 4: Header
        set(4, new ItemBuilder(Material.HOPPER)
                .name(hopper.getType().getFormattedName())
                .lore(Arrays.asList(
                        "§7Position: §f" + hopper.getRef().display(),
                        "§7Status: " + (hopper.isEnabled() ? "§a§lAKTIV" : "§c§lDEAKTIVIERT"),
                        "",
                        "§7Verbundene Kiste: " + (hopper.getAttachedChestRef() != null ? "§a" + hopper.getAttachedChestRef().display() : "§eKeine direkte Kiste"),
                        "§7Filter-Materialien: §e" + hopper.getFilterMaterials().size() + " definiert"
                ))
                .glow(true)
                .build());

        // Slot 10: Toggle Type (Input / Output / Fallback)
        ItemStack typeItem;
        if (hopper.getType() == StorageHopperType.INPUT) {
            typeItem = XStorageVersion.getDye(6, "CYAN_DYE");
        } else if (hopper.getType() == StorageHopperType.OUTPUT) {
            typeItem = XStorageVersion.getDye(10, "LIME_DYE");
        } else {
            typeItem = XStorageVersion.getDye(11, "YELLOW_DYE");
        }

        set(10, new ItemBuilder(typeItem)
                .name("§eModus: " + hopper.getType().getFormattedName())
                .lore(Arrays.asList(
                        "§7Aktuell: §f" + hopper.getType().getDescription(),
                        "",
                        "§b• Eingang: §7Saugt Items ein & verteilt sie",
                        "§a• Ausgang: §7Nimmt konfigurierte Filter auf",
                        "§e• Restekiste: §7Nimmt alle ungemappten Items",
                        "",
                        "§e▶ Klicke zum Wechseln des Modus"
                ))
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                StorageHopperType next;
                if (hopper.getType() == StorageHopperType.INPUT) {
                    next = StorageHopperType.OUTPUT;
                } else if (hopper.getType() == StorageHopperType.OUTPUT) {
                    next = StorageHopperType.FALLBACK;
                } else {
                    next = StorageHopperType.INPUT;
                }
                hopper.setType(next);
                manager.save();
                XStorageVersion.playClick(player);
                refresh();
            }
        });

        // Slot 12: Connected Chest
        boolean hasChest = hopper.getAttachedChestRef() != null;
        Material chestMat = hasChest ? Material.CHEST : (XStorageVersion.BARREL_MAT != null ? XStorageVersion.BARREL_MAT : Material.CHEST);
        set(12, new ItemBuilder(chestMat)
                .name(hasChest ? "§a§lVerbundene Kiste" : "§e§lKiste suchen / verbinden")
                .lore(Arrays.asList(
                        hasChest ? "§7Verbunden mit: §f" + hopper.getAttachedChestRef().display() : "§7Keine anliegende Kiste erkannt.",
                        "",
                        "§7Der Trichter sucht automatisch nach",
                        "§7Kisten darunter, dahinter oder daneben.",
                        "",
                        "§e▶ Klicke, um Verbindung neu zu scannen"
                ))
                .glow(hasChest)
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                Block block = hopper.getRef().block();
                ChestRef detected = manager.detectAttachedChest(block);
                hopper.setAttachedChestRef(detected);
                manager.save();
                XStorageVersion.playChest(player);
                refresh();
            }
        });

        // Slot 14: Filter-Items
        Material frameMat = Material.matchMaterial("ITEM_FRAME");
        if (frameMat == null) frameMat = Material.PAINTING;
        set(14, new ItemBuilder(frameMat)
                .name("§a§lFilter-Items (" + hopper.getFilterMaterials().size() + ")")
                .lore(Arrays.asList(
                        "§7Lege fest, welche Items in diesen",
                        "§7Trichter einsortiert werden dürfen.",
                        "",
                        "§7(Wichtig für §aAusgangstrichter§7!)",
                        "",
                        "§e▶ Klicke zum Bearbeiten der Filter"
                ))
                .glow(!hopper.getFilterMaterials().isEmpty())
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                XStorageVersion.playClick(player);
                openLater(new StorageFilterEditMenu(plugin, player, manager, hopper, HopperConfigMenu.this));
            }
        });

        // Slot 16: Toggle Enabled
        Material torchMat = Material.matchMaterial("REDSTONE_TORCH");
        if (torchMat == null) torchMat = Material.matchMaterial("REDSTONE_TORCH_ON");
        if (torchMat == null) torchMat = Material.TORCH;
        set(16, new ItemBuilder(hopper.isEnabled() ? torchMat : Material.LEVER)
                .name(hopper.isEnabled() ? "§a§lTrichter: AKTIV" : "§c§lTrichter: PAUSIERT")
                .lore(Arrays.asList(
                        "§7Klicke, um diesen spezifischen",
                        "§7Trichter zu aktivieren oder pausieren."
                ))
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                hopper.setEnabled(!hopper.isEnabled());
                manager.save();
                XStorageVersion.playPling(player, hopper.isEnabled() ? 1.5f : 0.8f);
                refresh();
            }
        });

        // Slot 22: Trichter entfernen
        set(22, new ItemBuilder(XStorageVersion.getConcreteOrWool(14, "RED_CONCRETE"))
                .name("§c§lTrichter entfernen")
                .lore(Arrays.asList(
                        "§7Entfernt den Trichter aus dem Netzwerk",
                        "§7und droppt das Wireless-Trichter-Item.",
                        "",
                        "§c▶ Klicke zum Entfernen"
                ))
                .build(), new ClickAction() {
            @Override
            public void execute(InventoryClickEvent event) {
                Block block = hopper.getRef().block();
                if (block != null && block.getType() == Material.HOPPER) {
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), StorageHopperItem.createHopper(hopper.getType(), 1));
                }
                manager.removeHopper(hopper.getRef());
                XStorageVersion.playItemBreak(player);
                player.sendMessage("§aTrichter erfolgreich entfernt!");
                back();
            }
        });

        // Slot 31: Back
        set(31, backItem(), new ClickAction() {
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
        for (int i = 27; i < 36; i++) if (i != 31) set(i, frame);
        set(9, frame); set(17, frame);
        set(18, frame); set(26, frame);
    }
}
