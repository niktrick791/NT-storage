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
    public Component title() {
        return Component.text("BorderMC • Trichter-Liste", NamedTextColor.DARK_AQUA);
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    protected void build() {
        buildFrame();

        List<StorageHopper> list = new ArrayList<>();
        boolean isAdmin = player.isOp() || player.hasPermission("bordermc.admin") || player.hasPermission("NiktrickStorage.admin");

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
            StorageHopper hopper = list.get(i);
            int slot = SLOTS[i - start];

            Material mat = switch (hopper.getType()) {
                case INPUT -> Material.CYAN_GLAZED_TERRACOTTA;
                case OUTPUT -> Material.LIME_GLAZED_TERRACOTTA;
                case FALLBACK -> Material.YELLOW_GLAZED_TERRACOTTA;
            };

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Position: §f" + hopper.getRef().display()));
            if (hopper.getAttachedChestRef() != null) {
                lore.add(Component.text("§7Verbundene Kiste: §a" + hopper.getAttachedChestRef().display()));
            } else {
                lore.add(Component.text("§7Verbundene Kiste: §eKeine (nur Trichter)"));
            }
            lore.add(Component.text("§7Status: " + (hopper.isEnabled() ? "§a§lAKTIV" : "§c§lDEAKTIVIERT")));

            if (hopper.getType() == StorageHopperType.OUTPUT) {
                lore.add(Component.empty());
                lore.add(Component.text("§6Filter-Items (" + hopper.getFilterMaterials().size() + "):"));
                if (hopper.getFilterMaterials().isEmpty()) {
                    lore.add(Component.text("§8• §cKeine Items zugewiesen (nimmt nichts an)"));
                } else {
                    int count = 0;
                    for (Material m : hopper.getFilterMaterials()) {
                        if (count++ >= 5) {
                            lore.add(Component.text("§8• §7... und " + (hopper.getFilterMaterials().size() - 5) + " weitere"));
                            break;
                        }
                        lore.add(Component.text("§8• §e" + m.name()));
                    }
                }
            }
            lore.add(Component.empty());
            lore.add(Component.text("§e▶ Klicke zum Konfigurieren"));

            set(slot, new ItemBuilder(mat)
                    .name(Component.text(hopper.getType().getFormattedName()))
                    .lore(lore)
                    .glow(hopper.isEnabled())
                    .build(), event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
                openLater(new HopperConfigMenu(plugin, player, manager, hopper, this));
            });
        }

        if (list.isEmpty()) {
            set(22, new ItemBuilder(Material.BARRIER)
                    .name(Component.text("§cKeine Trichter gefunden"))
                    .lore(List.of(
                            Component.text("§7Crafte einen Trichter mit einer"),
                            Component.text("§7Enderperle und platziere ihn!"),
                            Component.empty(),
                            Component.text("§7(Rezept: Trichter + Enderperle)")
                    ))
                    .build());
        }

        // Pagination buttons
        if (page > 0) {
            set(38, new ItemBuilder(Material.ARROW).name(Component.text("§e« Vorherige Seite (" + page + "/" + totalPages + ")")).build(), event -> {
                page--;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
                refresh();
            });
        }

        if (page < totalPages - 1) {
            set(42, new ItemBuilder(Material.ARROW).name(Component.text("§eNächste Seite (" + (page + 2) + "/" + totalPages + ") »")).build(), event -> {
                page++;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
                refresh();
            });
        }

        set(40, backItem(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            back();
        });

        fillEmpty();
    }

    private void buildFrame() {
        ItemStack frame = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(Component.empty()).build();
        for (int i = 0; i < 9; i++) set(i, frame);
        for (int i = 36; i < 45; i++) if (i != 38 && i != 40 && i != 42) set(i, frame);
        for (int row = 1; row < 4; row++) {
            set(row * 9, frame);
            set(row * 9 + 8, frame);
        }
    }
}
