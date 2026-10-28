package de.niktr.storage.gui;

import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.util.ItemBuilder;
import de.niktr.storage.util.Menu;
import de.niktr.storage.util.XStorageVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
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
    public String title() {
        return "§3niktrick • Filter bearbeiten";
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        List<Material> materials = new ArrayList<Material>(hopper.getFilterMaterials());
        int slotIdx = 0;
        int[] usableSlots = new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        for (final Material mat : materials) {
            if (slotIdx >= usableSlots.length) break;
            int slot = usableSlots[slotIdx++];

            set(slot, new ItemBuilder(mat)
                    .name("§a" + mat.name())
                    .lore(Arrays.asList(
                            "§7Dieses Item ist für diese Kiste freigegeben.",
                            "",
                            "§c▶ Klicke zum Entfernen des Filters"
                    ))
                    .build(), new ClickAction() {
                @Override
                public void execute(InventoryClickEvent event) {
                    hopper.removeFilterMaterial(mat);
                    manager.save();
                    XStorageVersion.playItemBreak(player);
                    refresh();
                }
            });
        }

        // Slot 4: Info instructions
        set(4, new ItemBuilder(Material.COMPASS)
                .name("§b§lFilter hinzufügen & entfernen")
                .lore(Arrays.asList(
                        "§7• Klicke ein Item in deinem §eSpieler-Inventar§7 an,",
                        "§7  um es als Filter hinzuzufügen.",
                        "",
                        "§7• Klicke ein Item hier §eoben§7 an,",
                        "§7  um es wieder zu entfernen."
                ))
                .glow(true)
                .build());

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

    @Override
    public void handle(InventoryClickEvent event) {
        if (event.getRawSlot() >= size()) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR && XStorageVersion.isItem(clicked.getType())) {
                Material mat = clicked.getType();
                if (hopper.addFilterMaterial(mat)) {
                    manager.save();
                    XStorageVersion.playSuccess(player);
                    refresh();
                } else {
                    XStorageVersion.playError(player);
                }
            }
            return;
        }
        super.handle(event);
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
