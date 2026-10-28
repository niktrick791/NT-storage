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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HopperListMenu extends Menu<Plugin> {

    private final StorageManager manager;
    private int page = 0;

    private static final int[] SLOTS = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public HopperListMenu(Plugin plugin, Player player, StorageManager manager, Menu<?> parent) {
        super(plugin, player, parent);
        this.manager = manager;
    }

    @Override
    public String title() {
        return "§3niktrick • Trichter-Liste";
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        List<StorageHopper> list = new ArrayList<StorageHopper>();
        boolean isAdmin = player.isOp() || player.hasPermission("niktrick.storage.admin");

        if (isAdmin) {
            list.addAll(manager.getHoppers());
        } else {
            list.addAll(manager.getPlayerHoppers(player.getUniqueId()));
        }

        int pageSize = SLOTS.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) list.size() / pageSize));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        int start = page * pageSize;
        int end = Math.min(start + pageSize, list.size());

        for (int i = start; i < end; i++) {
            final StorageHopper hopper = list.get(i);
            int slot = SLOTS[i - start];

            ItemStack matItem;
            if (hopper.getType() == StorageHopperType.INPUT) {
                matItem = XStorageVersion.getGlazedTerracotta(9, "CYAN_GLAZED_TERRACOTTA");
            } else if (hopper.getType() == StorageHopperType.OUTPUT) {
                matItem = XStorageVersion.getGlazedTerracotta(5, "LIME_GLAZED_TERRACOTTA");
            } else {
                matItem = XStorageVersion.getGlazedTerracotta(4, "YELLOW_GLAZED_TERRACOTTA");
            }

            List<String> lore = new ArrayList<String>();
            lore.add("§7Position: §f" + hopper.getRef().display());
            if (hopper.getAttachedChestRef() != null) {
                lore.add("§7Verbundene Kiste: §a" + hopper.getAttachedChestRef().display());
            } else {
                lore.add("§7Verbundene Kiste: §eKeine (nur Trichter)");
            }
            lore.add("§7Status: " + (hopper.isEnabled() ? "§a§lAKTIV" : "§c§lDEAKTIVIERT"));

            if (hopper.getType() == StorageHopperType.OUTPUT) {
                lore.add("");
                lore.add("§6Filter-Items (" + hopper.getFilterMaterials().size() + "):");
                if (hopper.getFilterMaterials().isEmpty()) {
                    lore.add("§8• §cKeine Items zugewiesen (nimmt nichts an)");
                } else {
                    int count = 0;
                    for (Material m : hopper.getFilterMaterials()) {
                        if (count++ >= 5) {
                            lore.add("§8• §7... und " + (hopper.getFilterMaterials().size() - 5) + " weitere");
                            break;
                        }
                        lore.add("§8• §e" + m.name());
                    }
                }
            }

            lore.add("");
            lore.add("§e▶ Klicke zum Konfigurieren");

            set(slot, new ItemBuilder(matItem)
                    .name(hopper.getType().getFormattedName())
                    .lore(lore)
                    .glow(hopper.isEnabled())
                    .build(), new ClickAction() {
                @Override
                public void execute(InventoryClickEvent event) {
                    XStorageVersion.playClick(player);
                    openLater(new HopperConfigMenu(plugin, player, manager, hopper, HopperListMenu.this));
                }
            });
        }

        if (list.isEmpty()) {
            set(22, new ItemBuilder(Material.BARRIER)
                    .name("§cKeine Trichter gefunden")
                    .lore(Arrays.asList(
                            "§7Crafte einen Trichter mit einer",
                            "§7Enderperle und platziere ihn!",
                            "",
                            "§7(Rezept: Trichter + Enderperle)"
                    ))
                    .build());
        }

        // Pagination buttons
        if (page > 0) {
            set(38, new ItemBuilder(Material.ARROW).name("§e« Vorherige Seite (" + page + "/" + totalPages + ")").build(), new ClickAction() {
                @Override
                public void execute(InventoryClickEvent event) {
                    page--;
                    XStorageVersion.playClick(player);
                    refresh();
                }
            });
        }

        if (page < totalPages - 1) {
            set(42, new ItemBuilder(Material.ARROW).name("§eNächste Seite (" + (page + 2) + "/" + totalPages + ") »").build(), new ClickAction() {
                @Override
                public void execute(InventoryClickEvent event) {
                    page++;
                    XStorageVersion.playClick(player);
                    refresh();
                }
            });
        }

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
        for (int i = 0; i < 9; i++) set(i, frame);
        for (int i = 36; i < 45; i++) if (i != 38 && i != 40 && i != 42) set(i, frame);
        for (int row = 1; row < 4; row++) {
            set(row * 9, frame);
            set(row * 9 + 8, frame);
        }
    }
}
