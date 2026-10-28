package de.niktr.storage.gui;

import de.niktr.storage.util.Menu;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.StorageHopper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class StorageFilterEditMenu extends Menu<Plugin> {

    private final StorageManager manager;
    private final StorageHopper hopper;

    public StorageFilterEditMenu(Plugin plugin, Player player, StorageManager manager, StorageHopper hopper, Menu<?> parent) {
        super(plugin, player, parent);
        this.manager = manager;
        this.hopper = hopper;
    }

    @Override
    public Component title() {
        return Component.text("BorderMC • Filter bearbeiten", NamedTextColor.DARK_AQUA);
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        List<Material> materials = new ArrayList<>(hopper.getFilterMaterials());
        int maxSlots = 28; // 10..16, 19..25, 28..34

        int slotIdx = 0;
        int[] usableSlots = new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        for (Material mat : materials) {
            if (slotIdx >= usableSlots.length) break;
            int slot = usableSlots[slotIdx++];

            set(slot, new ItemBuilder(mat)
                    .name(Component.text("§a" + mat.name()))
                    .lore(List.of(
                            Component.text("§7Dieses Item ist für diese Kiste freigegeben."),
                            Component.empty(),
                            Component.text("§c▶ Klicke zum Entfernen des Filters")
                    ))
                    .build(), event -> {
                hopper.removeFilterMaterial(mat);
                manager.save();
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 1.4f);
                refresh();
            });
        }

        // Slot 4: Info instructions
        set(4, new ItemBuilder(Material.COMPASS)
                .name(Component.text("§b§lFilter hinzufügen & entfernen"))
                .lore(List.of(
                        Component.text("§7• Klicke ein Item in deinem §eSpieler-Inventar§7 an,"),
                        Component.text("§7  um es als Filter hinzuzufügen."),
                        Component.empty(),
                        Component.text("§7• Klicke ein Item hier §eoben§7 an,"),
                        Component.text("§7  um es wieder zu entfernen.")
                ))
                .glow(true)
                .build());

        // Slot 40: Back
        set(40, backItem(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            back();
        });

        fillEmpty();
    }

    @Override
    public void handle(InventoryClickEvent event) {
        if (event.getRawSlot() >= size()) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && !clicked.getType().isAir() && clicked.getType().isItem()) {
                Material mat = clicked.getType();
                if (hopper.addFilterMaterial(mat)) {
                    manager.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
                    refresh();
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                }
            }
            return;
        }
        super.handle(event);
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
