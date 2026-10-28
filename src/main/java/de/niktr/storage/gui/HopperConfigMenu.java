package de.niktr.storage.gui;

import de.niktr.storage.util.Menu;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.ChestRef;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.models.StorageHopperType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class HopperConfigMenu extends Menu<Plugin> {

    private final StorageManager manager;
    private final StorageHopper hopper;

    public HopperConfigMenu(Plugin plugin, Player player, StorageManager manager, StorageHopper hopper, Menu<?> parent) {
        super(plugin, player, parent);
        this.manager = manager;
        this.hopper = hopper;
    }

    @Override
    public Component title() {
        return Component.text("BorderMC • Trichter verwalten", NamedTextColor.DARK_AQUA);
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
                .name(Component.text(hopper.getType().getFormattedName()))
                .lore(List.of(
                        Component.text("§7Position: §f" + hopper.getRef().display()),
                        Component.text("§7Status: " + (hopper.isEnabled() ? "§a§lAKTIV" : "§c§lDEAKTIVIERT")),
                        Component.empty(),
                        Component.text("§7Verbundene Kiste: " + (hopper.getAttachedChestRef() != null ? "§a" + hopper.getAttachedChestRef().display() : "§eKeine direkte Kiste")),
                        Component.text("§7Filter-Materialien: §e" + hopper.getFilterMaterials().size() + " definiert")
                ))
                .glow(true)
                .build());

        // Slot 10: Toggle Type (Input / Output / Fallback)
        Material typeMat = switch (hopper.getType()) {
            case INPUT -> Material.CYAN_DYE;
            case OUTPUT -> Material.LIME_DYE;
            case FALLBACK -> Material.YELLOW_DYE;
        };
        set(10, new ItemBuilder(typeMat)
                .name(Component.text("§eModus: " + hopper.getType().getFormattedName()))
                .lore(List.of(
                        Component.text("§7Aktuell: §f" + hopper.getType().getDescription()),
                        Component.empty(),
                        Component.text("§b• Eingang: §7Saugt Items ein & verteilt sie"),
                        Component.text("§a• Ausgang: §7Nimmt konfigurierte Filter auf"),
                        Component.text("§e• Restekiste: §7Nimmt alle ungemappten Items"),
                        Component.empty(),
                        Component.text("§e▶ Klicke zum Wechseln des Modus")
                ))
                .build(), event -> {
            StorageHopperType next = switch (hopper.getType()) {
                case INPUT -> StorageHopperType.OUTPUT;
                case OUTPUT -> StorageHopperType.FALLBACK;
                case FALLBACK -> StorageHopperType.INPUT;
            };
            hopper.setType(next);
            manager.save();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.3f);
            refresh();
        });

        // Slot 12: Connected Chest
        boolean hasChest = hopper.getAttachedChestRef() != null;
        set(12, new ItemBuilder(hasChest ? Material.CHEST : Material.BARREL)
                .name(Component.text(hasChest ? "§a§lVerbundene Kiste" : "§e§lKiste suchen / verbinden"))
                .lore(List.of(
                        Component.text(hasChest ? "§7Verbunden mit: §f" + hopper.getAttachedChestRef().display() : "§7Keine anliegende Kiste erkannt."),
                        Component.empty(),
                        Component.text("§7Der Trichter sucht automatisch nach"),
                        Component.text("§7Kisten darunter, dahinter oder daneben."),
                        Component.empty(),
                        Component.text("§e▶ Klicke, um Verbindung neu zu scannen")
                ))
                .glow(hasChest)
                .build(), event -> {
            Block block = hopper.getRef().block();
            ChestRef detected = manager.detectAttachedChest(block);
            hopper.setAttachedChestRef(detected);
            manager.save();
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.4f);
            refresh();
        });

        // Slot 14: Filter-Items
        set(14, new ItemBuilder(Material.ITEM_FRAME)
                .name(Component.text("§a§lFilter-Items (" + hopper.getFilterMaterials().size() + ")"))
                .lore(List.of(
                        Component.text("§7Lege fest, welche Items in diesen"),
                        Component.text("§7Trichter einsortiert werden dürfen."),
                        Component.empty(),
                        Component.text("§7(Wichtig für §aAusgangstrichter§7!)"),
                        Component.empty(),
                        Component.text("§e▶ Klicke zum Bearbeiten der Filter")
                ))
                .glow(!hopper.getFilterMaterials().isEmpty())
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            openLater(new StorageFilterEditMenu(plugin, player, manager, hopper, this));
        });

        // Slot 16: Toggle Enabled
        set(16, new ItemBuilder(hopper.isEnabled() ? Material.REDSTONE_TORCH : Material.LEVER)
                .name(Component.text(hopper.isEnabled() ? "§a§lTrichter: AKTIV" : "§c§lTrichter: PAUSIERT"))
                .lore(List.of(
                        Component.text("§7Klicke, um diesen spezifischen"),
                        Component.text("§7Trichter zu aktivieren oder pausieren.")
                ))
                .build(), event -> {
            hopper.setEnabled(!hopper.isEnabled());
            manager.save();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, hopper.isEnabled() ? 1.5f : 0.8f);
            refresh();
        });

        // Slot 22: Trichter entfernen
        set(22, new ItemBuilder(Material.RED_CONCRETE)
                .name(Component.text("§c§lTrichter entfernen"))
                .lore(List.of(
                        Component.text("§7Entfernt den Trichter aus dem Netzwerk"),
                        Component.text("§7und droppt das Wireless-Trichter-Item."),
                        Component.empty(),
                        Component.text("§c▶ Klicke zum Entfernen")
                ))
                .build(), event -> {
            Block block = hopper.getRef().block();
            if (block != null && block.getType() == Material.HOPPER) {
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), StorageHopperItem.createHopper(hopper.getType(), 1));
            }
            manager.removeHopper(hopper.getRef());
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1.0f);
            player.sendMessage(Component.text("§aTrichter erfolgreich entfernt!"));
            back();
        });

        // Slot 31: Back
        set(31, backItem(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            back();
        });

        fillEmpty();
    }

    private void buildFrame() {
        ItemStack frame = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(Component.empty()).build();
        for (int i = 0; i < 9; i++) if (i != 4) set(i, frame);
        for (int i = 27; i < 36; i++) if (i != 31) set(i, frame);
        set(9, frame); set(17, frame);
        set(18, frame); set(26, frame);
    }
}
