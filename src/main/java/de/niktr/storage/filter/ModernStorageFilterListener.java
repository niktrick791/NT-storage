package de.niktr.storage.filter;

import de.niktr.storage.managers.StorageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ModernStorageFilterListener implements Listener {

    private final StorageManager manager;

    public ModernStorageFilterListener(StorageManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoot(LootGenerateEvent event) {
        if (event.isCancelled() || event.getInventoryHolder() == null) return;
        Inventory destination = event.getInventoryHolder().getInventory();
        for (ItemStack item : event.getLoot()) {
            if (StrictStorageFilter.decision(manager, destination, item) != StrictStorageFilter.Decision.ALLOW) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
