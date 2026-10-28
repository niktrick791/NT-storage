package de.niktr.storage.filter;

import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.ChestRef;
import de.niktr.storage.models.StorageChest;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.models.StorageHopperType;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class StrictStorageFilter implements Listener {

    private final StorageManager manager;
    private final Map<HumanEntity, Long> lastWarning = new WeakHashMap<>();

    public StrictStorageFilter(StorageManager manager) {
        this.manager = manager;
    }

    public static void install(Plugin plugin, StorageManager manager) {
        plugin.getServer().getPluginManager().registerEvents(new StrictStorageFilter(manager), plugin);
    }

    public enum Decision {
        ALLOW, WRONG_ITEM, UNASSIGNED, CONFLICT
    }

    public static boolean empty(ItemStack item) {
        return item == null || item.getAmount() <= 0 || item.getType().isAir();
    }

    public static Set<ChestRef> references(Inventory inventory) {
        if (inventory == null) return Set.of();
        Set<ChestRef> refs = new LinkedHashSet<>(2);
        InventoryHolder holder = inventory.getHolder();
        addHolder(refs, holder);
        if (holder instanceof Chest chest) {
            Inventory full = chest.getInventory();
            if (full != null) addHolder(refs, full.getHolder());
        }
        return refs;
    }

    private static void addHolder(Set<ChestRef> refs, InventoryHolder holder) {
        if (holder instanceof Chest chest) {
            Location l = chest.getLocation();
            if (l.getWorld() != null)
                refs.add(new ChestRef(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));
        } else if (holder instanceof DoubleChest doubleChest) {
            addHolder(refs, doubleChest.getLeftSide());
            addHolder(refs, doubleChest.getRightSide());
        }
    }

    public static Decision decision(StorageManager manager, Inventory destination, ItemStack item) {
        if (empty(item)) return Decision.ALLOW;
        Set<ChestRef> refs = references(destination);
        if (refs.isEmpty()) return Decision.ALLOW;
        Material material = item.getType();

        // Check wireless hoppers attached to this inventory
        boolean isInput = false;
        boolean isFallback = false;
        int outputCount = 0;
        boolean outputUnassigned = false;
        boolean outputForbidden = false;
        boolean outputMatch = false;

        for (ChestRef ref : refs) {
            if (manager.isInput(ref)) isInput = true;
            if (manager.isFallback(ref)) isFallback = true;

            StorageChest storage = manager.getStorageAt(ref);
            if (storage != null) {
                outputCount++;
                if (storage.getMaterials().isEmpty()) {
                    outputUnassigned = true;
                } else if (storage.getMaterials().contains(material)) {
                    outputMatch = true;
                } else {
                    outputForbidden = true;
                }
            }

            // Check if there are wireless hoppers attached to this chest
            for (StorageHopper hopper : manager.getHoppersAttachedTo(ref)) {
                if (!hopper.isEnabled()) continue;
                if (hopper.getType() == StorageHopperType.INPUT) isInput = true;
                if (hopper.getType() == StorageHopperType.FALLBACK) isFallback = true;
                if (hopper.getType() == StorageHopperType.OUTPUT) {
                    outputCount++;
                    if (hopper.getFilterMaterials().isEmpty()) {
                        outputUnassigned = true;
                    } else if (hopper.getFilterMaterials().contains(material)) {
                        outputMatch = true;
                    } else {
                        outputForbidden = true;
                    }
                }
            }
        }

        if ((isInput && (isFallback || outputCount > 0)) || (isFallback && outputCount > 0)) {
            return Decision.CONFLICT;
        }

        if (outputCount > 0) {
            if (outputMatch) return Decision.ALLOW;
            if (outputUnassigned) return Decision.UNASSIGNED;
            if (outputForbidden) return Decision.WRONG_ITEM;
        }

        if (isFallback) {
            // Fallback receives items that have no specific target
            boolean globallyMapped = manager.isGloballyMapped(material);
            if (globallyMapped) return Decision.WRONG_ITEM;
        }

        return Decision.ALLOW;
    }

    public static boolean protectedInventory(StorageManager manager, Inventory inventory) {
        Set<ChestRef> refs = references(inventory);
        if (refs.isEmpty()) return false;
        for (ChestRef ref : refs) {
            if (manager.isProtected(ref)) return true;
        }
        return false;
    }

    public static HashMap<Integer, ItemStack> addChecked(Inventory destination, ItemStack[] items, StorageManager manager) {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(items, "items");
        HashMap<Integer, ItemStack> leftovers = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack original = items[i];
            if (empty(original)) continue;
            if (decision(manager, destination, original) != Decision.ALLOW) {
                leftovers.put(i, original.clone());
                continue;
            }
            HashMap<Integer, ItemStack> remaining = destination.addItem(original.clone());
            int amount = 0;
            for (ItemStack rest : remaining.values()) {
                if (!empty(rest)) amount += rest.getAmount();
            }
            if (amount > 0) {
                ItemStack rest = original.clone();
                rest.setAmount(amount);
                leftovers.put(i, rest);
            }
        }
        return leftovers;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || event.getRawSlot() < 0) return;
        Inventory top = event.getView().getTopInventory();
        boolean inTop = event.getRawSlot() < top.getSize();
        Inventory destination = clicked;
        ItemStack incoming = null;
        String action = event.getAction().name();

        if (event instanceof InventoryCreativeEvent) {
            incoming = event.getCursor();
        } else if (action.equals("MOVE_TO_OTHER_INVENTORY")) {
            destination = inTop ? event.getView().getBottomInventory() : top;
            incoming = event.getCurrentItem();
        } else if (action.equals("HOTBAR_SWAP") || action.equals("HOTBAR_MOVE_AND_READD")) {
            int key = event.getHotbarButton();
            if (key >= 0 && key <= 8) incoming = event.getWhoClicked().getInventory().getItem(key);
            else if (event.getClick() == ClickType.SWAP_OFFHAND)
                incoming = event.getWhoClicked().getInventory().getItemInOffHand();
            else if (protectedInventory(manager, destination)) {
                reject(event, Decision.WRONG_ITEM);
                return;
            }
        } else if (action.equals("PLACE_ALL") || action.equals("PLACE_SOME") ||
                action.equals("PLACE_ONE") || action.equals("SWAP_WITH_CURSOR") ||
                action.equals("PLACE_ALL_INTO_BUNDLE") || action.equals("PLACE_SOME_INTO_BUNDLE")) {
            incoming = event.getCursor();
        } else if (action.equals("PLACE_FROM_BUNDLE")) {
            if (protectedInventory(manager, destination)) {
                event.setCancelled(true);
                warn(event.getWhoClicked(), "§cBitte das Item zuerst im Spielerinventar aus dem Bündel nehmen.");
            }
            return;
        } else if (Set.of("NOTHING", "PICKUP_ALL", "PICKUP_SOME", "PICKUP_HALF", "PICKUP_ONE",
                "PICKUP_FROM_BUNDLE", "PICKUP_ALL_INTO_BUNDLE", "PICKUP_SOME_INTO_BUNDLE",
                "COLLECT_TO_CURSOR", "CLONE_STACK", "DROP_ALL_SLOT", "DROP_ONE_SLOT",
                "DROP_ALL_CURSOR", "DROP_ONE_CURSOR").contains(action)) {
            return;
        } else {
            if (protectedInventory(manager, destination) ||
                    (event.isShiftClick() && protectedInventory(manager, top))) {
                reject(event, Decision.WRONG_ITEM);
            }
            return;
        }
        Decision result = decision(manager, destination, incoming);
        if (result != Decision.ALLOW) reject(event, result);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.isCancelled()) return;
        for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
            if (entry.getKey() < 0) continue;
            Inventory target = event.getView().getInventory(entry.getKey());
            Decision result = decision(manager, target, entry.getValue());
            if (result != Decision.ALLOW) {
                event.setCancelled(true);
                warn(event.getWhoClicked(), message(result));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(InventoryMoveItemEvent event) {
        if (!event.isCancelled() && decision(manager, event.getDestination(), event.getItem()) != Decision.ALLOW) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(InventoryPickupItemEvent event) {
        if (!event.isCancelled() && decision(manager, event.getInventory(), event.getItem().getItemStack()) != Decision.ALLOW) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || !(event.getBlockPlaced().getState() instanceof Chest chest)) return;
        Inventory inventory = chest.getInventory();
        Set<ChestRef> refs = references(inventory);
        Block placed = event.getBlockPlaced();
        Location location = placed.getLocation();
        ChestRef self = new ChestRef(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (refs.size() > 1) {
            for (ChestRef ref : refs) {
                if (!ref.equals(self) && manager.isProtected(ref)) {
                    event.setCancelled(true);
                    warn(event.getPlayer(), "§cDoppelkiste zuerst bauen, danach registrieren.");
                    return;
                }
            }
        }
        for (ItemStack item : inventory.getContents()) {
            Decision result = decision(manager, inventory, item);
            if (result != Decision.ALLOW) {
                event.setCancelled(true);
                warn(event.getPlayer(), message(result));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoot(LootGenerateEvent event) {
        if (event.isCancelled() || event.getInventoryHolder() == null) return;
        Inventory destination = event.getInventoryHolder().getInventory();
        for (ItemStack item : event.getLoot()) {
            if (decision(manager, destination, item) != Decision.ALLOW) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void reject(InventoryClickEvent event, Decision decision) {
        event.setCancelled(true);
        warn(event.getWhoClicked(), message(decision));
    }

    private static String message(Decision decision) {
        return switch (decision) {
            case UNASSIGNED -> "§cDieser Lagerkiste ist noch kein Item zugewiesen. Nutze /lager map <MATERIAL> oder das Trichter-Menü.";
            case CONFLICT -> "§cDiese Doppelkiste hat widersprüchliche Registrierungen. Bitte die Zuordnung korrigieren.";
            default -> "§cDieses Item ist für diese Kiste nicht freigegeben.";
        };
    }

    private void warn(HumanEntity player, String text) {
        long now = System.nanoTime();
        Long previous = lastWarning.get(player);
        if (previous == null || now - previous >= 1_000_000_000L) {
            lastWarning.put(player, now);
            player.sendMessage(Component.text("§6[Lager] " + text));
        }
    }
}
