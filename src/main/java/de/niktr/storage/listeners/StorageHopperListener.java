package de.niktr.storage.listeners;

import de.niktr.storage.gui.HopperConfigMenu;
import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.ChestRef;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.models.StorageHopperType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class StorageHopperListener implements Listener {

    private final Plugin plugin;
    private final StorageManager manager;

    public StorageHopperListener(Plugin plugin, StorageManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack inHand = event.getItemInHand();
        if (!StorageHopperItem.isWirelessHopper(inHand)) return;

        Block placed = event.getBlockPlaced();
        Player player = event.getPlayer();
        StorageHopperType type = StorageHopperItem.getHopperType(inHand);

        ChestRef hopperRef = ChestRef.from(placed);
        ChestRef attachedChest = manager.detectAttachedChest(placed);

        StorageHopper hopper = new StorageHopper(
                UUID.randomUUID(),
                hopperRef,
                attachedChest,
                type,
                player.getUniqueId(),
                true
        );

        manager.addHopper(hopper);

        // Visual & audio feedback
        placed.getWorld().spawnParticle(Particle.PORTAL, placed.getLocation().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.1);
        placed.getWorld().playSound(placed.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.2f);

        player.sendMessage(Component.text("§6[BorderMC-Lager] " + type.getFormattedName() + " §aerfolgreich platziert!"));
        if (attachedChest != null) {
            player.sendMessage(Component.text("§7Verbundene Kiste erkannt: §f" + attachedChest.display()));
        } else {
            player.sendMessage(Component.text("§eHinweis: Keine Kiste direkt anliegend. Items im Trichter werden genutzt."));
        }
        player.sendMessage(Component.text("§6Tipp: §eRechtsklick auf den Trichter öffnet das Konfigurationsmenü!"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.HOPPER) return;

        ChestRef ref = ChestRef.from(block);
        StorageHopper hopper = manager.getHopperAt(ref);
        if (hopper == null) return;

        event.setDropItems(false);
        manager.removeHopper(ref);

        ItemStack customDrop = StorageHopperItem.createHopper(hopper.getType(), 1);
        block.getWorld().dropItemNaturally(block.getLocation(), customDrop);

        block.getWorld().spawnParticle(Particle.PORTAL, block.getLocation().add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, 0.05);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.7f, 1.2f);

        event.getPlayer().sendMessage(Component.text("§6[BorderMC-Lager] §eDrahtloser Trichter abgebaut!"));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.HOPPER) return;

        ChestRef ref = ChestRef.from(clicked);
        StorageHopper hopper = manager.getHopperAt(ref);
        if (hopper == null) return;

        Player player = event.getPlayer();

        // If player is sneaking and holding a block/hopper, let vanilla place if possible, otherwise open GUI
        event.setCancelled(true);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
        new HopperConfigMenu(plugin, player, manager, hopper, null).open();
    }
}
