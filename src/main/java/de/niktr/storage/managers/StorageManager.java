package de.niktr.storage.managers;

import de.niktr.storage.filter.StrictStorageFilter;
import de.niktr.storage.models.*;
import de.niktr.storage.util.XStorageVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StorageManager {

    private final Plugin plugin;
    private final File dataFile;

    private final Map<ChestRef, StorageHopper> hoppers = new ConcurrentHashMap<ChestRef, StorageHopper>();
    private final Map<ChestRef, StorageChest> storages = new ConcurrentHashMap<ChestRef, StorageChest>();
    private final Set<ChestRef> inputs = ConcurrentHashMap.newKeySet();
    private ChestRef fallback;

    private int nextStorageId = 1;
    private BukkitTask sortingTask;
    private final AtomicLong totalItemsSorted = new AtomicLong(0);

    // Config options
    private boolean enabled = true;
    private int scanIntervalTicks = 10;
    private int maxItemsPerCycle = 128;
    private boolean loadChunks = false;
    private boolean particlesEnabled = true;
    private boolean soundsEnabled = true;

    public StorageManager(Plugin plugin, File dataFolder) {
        this.plugin = plugin;
        this.dataFile = new File(dataFolder, "storage_data.yml");
    }

    public void start() {
        loadConfig();
        load();
        startSortingTask();
    }

    public void shutdown() {
        if (sortingTask != null) {
            sortingTask.cancel();
            sortingTask = null;
        }
        save();
    }

    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        this.enabled = cfg.getBoolean("enabled", true);
        this.scanIntervalTicks = Math.max(2, cfg.getInt("sorting.scan-interval-ticks", 10));
        this.maxItemsPerCycle = Math.max(1, cfg.getInt("sorting.max-items-per-cycle", 128));
        this.loadChunks = cfg.getBoolean("storage.load-chunks", false);
        this.particlesEnabled = cfg.getBoolean("effects.particles", true);
        this.soundsEnabled = cfg.getBoolean("effects.sounds", true);
    }

    public void startSortingTask() {
        if (sortingTask != null) {
            sortingTask.cancel();
            sortingTask = null;
        }
        if (!enabled) return;

        sortingTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    processSortingCycle(maxItemsPerCycle);
                } catch (Throwable t) {
                    plugin.getLogger().warning("Fehler im Sortierzyklus: " + t.getMessage());
                }
            }
        }, scanIntervalTicks, scanIntervalTicks);
    }

    public void processSortingCycle(int maxItems) {
        int remainingQuota = maxItems;

        // 1. Process wireless input hoppers
        for (StorageHopper hopper : hoppers.values()) {
            if (remainingQuota <= 0) break;
            if (!hopper.isEnabled() || hopper.getType() != StorageHopperType.INPUT) continue;

            Inventory sourceInv = getSourceInventory(hopper);
            if (sourceInv == null) continue;

            remainingQuota -= sortFromInventory(sourceInv, hopper.getRef(), remainingQuota);
        }

        // 2. Process legacy input chests
        for (ChestRef inputRef : new ArrayList<ChestRef>(inputs)) {
            if (remainingQuota <= 0) break;
            Inventory sourceInv = getInventoryAt(inputRef);
            if (sourceInv == null) continue;

            remainingQuota -= sortFromInventory(sourceInv, inputRef, remainingQuota);
        }
    }

    private Inventory getSourceInventory(StorageHopper hopper) {
        // First priority: chest attached to the input hopper
        if (hopper.getAttachedChestRef() != null) {
            Inventory inv = getInventoryAt(hopper.getAttachedChestRef());
            if (inv != null && hasAnyItem(inv)) return inv;
        }
        // Second priority: items directly in the hopper itself
        return getInventoryAt(hopper.getRef());
    }

    private boolean hasAnyItem(Inventory inv) {
        for (ItemStack it : inv.getContents()) {
            if (it != null && it.getType() != Material.AIR && it.getAmount() > 0) return true;
        }
        return false;
    }

    private int sortFromInventory(Inventory sourceInv, ChestRef sourceRef, int quota) {
        int moved = 0;
        ItemStack[] contents = sourceInv.getContents();

        for (int slot = 0; slot < contents.length; slot++) {
            if (moved >= quota) break;
            ItemStack item = contents[slot];
            if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) continue;

            int toMove = Math.min(item.getAmount(), quota - moved);
            Material mat = item.getType();

            // Find target destination
            Destination target = findBestDestination(mat);
            if (target == null) continue; // No match and no fallback, leave item

            ItemStack transferStack = item.clone();
            transferStack.setAmount(toMove);

            HashMap<Integer, ItemStack> leftover = StrictStorageFilter.addChecked(target.inventory(), new ItemStack[]{transferStack}, this);
            int leftoverAmount = 0;
            for (ItemStack left : leftover.values()) {
                if (left != null && left.getType() != Material.AIR) leftoverAmount += left.getAmount();
            }

            int successfullyMoved = toMove - leftoverAmount;
            if (successfullyMoved > 0) {
                moved += successfullyMoved;
                totalItemsSorted.addAndGet(successfullyMoved);

                int remainingInSlot = item.getAmount() - successfullyMoved;
                if (remainingInSlot <= 0) {
                    sourceInv.setItem(slot, null);
                } else {
                    item.setAmount(remainingInSlot);
                    sourceInv.setItem(slot, item);
                }

                // Visual & audio feedback
                playTransferEffect(sourceRef, target.ref());
            }
        }
        return moved;
    }

    private static final class Destination {
        private final Inventory inventory;
        private final ChestRef ref;

        public Destination(Inventory inventory, ChestRef ref) {
            this.inventory = inventory;
            this.ref = ref;
        }

        public Inventory inventory() {
            return inventory;
        }

        public ChestRef ref() {
            return ref;
        }
    }

    private Destination findBestDestination(Material material) {
        // Priority 1: Wireless output hoppers matching material
        List<StorageHopper> matchingHoppers = new ArrayList<StorageHopper>();
        for (StorageHopper hopper : hoppers.values()) {
            if (hopper.isEnabled() && hopper.getType() == StorageHopperType.OUTPUT) {
                if (hopper.getFilterMaterials().contains(material)) {
                    matchingHoppers.add(hopper);
                }
            }
        }

        // Sort by occupied slots of attached chest
        Collections.sort(matchingHoppers, new Comparator<StorageHopper>() {
            @Override
            public int compare(StorageHopper o1, StorageHopper o2) {
                return Integer.compare(getHopperFillLevel(o1), getHopperFillLevel(o2));
            }
        });

        for (StorageHopper hopper : matchingHoppers) {
            Inventory inv = getTargetInventory(hopper);
            if (inv != null && getFreeSpaceFor(inv, material) > 0) {
                return new Destination(inv, hopper.getRef());
            }
        }

        // Priority 2: Legacy mapped storage chests
        List<StorageChest> matchingChests = new ArrayList<StorageChest>();
        for (StorageChest sc : storages.values()) {
            if (sc.getMaterials().contains(material)) {
                matchingChests.add(sc);
            }
        }
        Collections.sort(matchingChests, new Comparator<StorageChest>() {
            @Override
            public int compare(StorageChest o1, StorageChest o2) {
                return Integer.compare(getOccupiedSlots(o1.getRef()), getOccupiedSlots(o2.getRef()));
            }
        });

        for (StorageChest sc : matchingChests) {
            Inventory inv = getInventoryAt(sc.getRef());
            if (inv != null && getFreeSpaceFor(inv, material) > 0) {
                return new Destination(inv, sc.getRef());
            }
        }

        // Priority 3: Fallback / Restekiste
        // Check wireless fallback hoppers
        for (StorageHopper hopper : hoppers.values()) {
            if (hopper.isEnabled() && hopper.getType() == StorageHopperType.FALLBACK) {
                Inventory inv = getTargetInventory(hopper);
                if (inv != null && getFreeSpaceFor(inv, material) > 0) {
                    return new Destination(inv, hopper.getRef());
                }
            }
        }

        // Check legacy fallback chest
        if (fallback != null) {
            Inventory inv = getInventoryAt(fallback);
            if (inv != null && getFreeSpaceFor(inv, material) > 0) {
                return new Destination(inv, fallback);
            }
        }

        return null;
    }

    private Inventory getTargetInventory(StorageHopper hopper) {
        if (hopper.getAttachedChestRef() != null) {
            Inventory inv = getInventoryAt(hopper.getAttachedChestRef());
            if (inv != null) return inv;
        }
        return getInventoryAt(hopper.getRef());
    }

    private int getHopperFillLevel(StorageHopper hopper) {
        if (hopper.getAttachedChestRef() != null) {
            return getOccupiedSlots(hopper.getAttachedChestRef());
        }
        return getOccupiedSlots(hopper.getRef());
    }

    public int getFreeSpaceFor(Inventory inv, Material material) {
        int free = 0;
        int maxStack = material.getMaxStackSize();
        for (ItemStack it : inv.getContents()) {
            if (it == null || it.getType() == Material.AIR) {
                free += maxStack;
            } else if (it.getType() == material) {
                free += Math.max(0, maxStack - it.getAmount());
            }
        }
        return free;
    }

    public int getOccupiedSlots(ChestRef ref) {
        Inventory inv = getInventoryAt(ref);
        if (inv == null) return Integer.MAX_VALUE;
        int occupied = 0;
        for (ItemStack it : inv.getContents()) {
            if (it != null && it.getType() != Material.AIR && it.getAmount() > 0) {
                occupied++;
            }
        }
        return occupied;
    }

    public Inventory getInventoryAt(ChestRef ref) {
        if (ref == null) return null;
        if (!ref.isChunkLoaded() && !loadChunks) return null;

        Block block = ref.block();
        if (block == null) return null;

        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) block.getState();
            return holder.getInventory();
        }
        return null;
    }

    public ChestRef detectAttachedChest(Block hopperBlock) {
        if (hopperBlock == null) return null;

        // 1. Try modern BlockData facing (1.13+)
        try {
            Method getBlockData = Block.class.getMethod("getBlockData");
            Object bd = getBlockData.invoke(hopperBlock);
            if (bd != null) {
                Method getFacing = bd.getClass().getMethod("getFacing");
                BlockFace facing = (BlockFace) getFacing.invoke(bd);
                if (facing != null) {
                    Block targetBlock = hopperBlock.getRelative(facing);
                    if (isChestBlock(targetBlock)) {
                        return canonical(targetBlock);
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 1b. Try legacy byte data facing (1.8 - 1.12)
        try {
            Method getData = Block.class.getMethod("getData");
            byte data = (Byte) getData.invoke(hopperBlock);
            int facingId = data & 0x7;
            BlockFace facing = null;
            if (facingId == 0) facing = BlockFace.DOWN;
            else if (facingId == 2) facing = BlockFace.NORTH;
            else if (facingId == 3) facing = BlockFace.SOUTH;
            else if (facingId == 4) facing = BlockFace.WEST;
            else if (facingId == 5) facing = BlockFace.EAST;
            if (facing != null) {
                Block targetBlock = hopperBlock.getRelative(facing);
                if (isChestBlock(targetBlock)) {
                    return canonical(targetBlock);
                }
            }
        } catch (Throwable ignored) {}

        // 2. Check adjacent faces (down, north, south, east, west, up)
        BlockFace[] faces = new BlockFace[]{
                BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST, BlockFace.UP
        };
        for (BlockFace face : faces) {
            Block adjacent = hopperBlock.getRelative(face);
            if (isChestBlock(adjacent)) {
                return canonical(adjacent);
            }
        }
        return null;
    }

    public static boolean isChestBlock(Block block) {
        return XStorageVersion.isChestBlock(block);
    }

    public ChestRef canonical(Block block) {
        if (block == null) return null;
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            Inventory inv = chest.getInventory();
            if (inv.getHolder() instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) inv.getHolder();
                if (doubleChest.getLeftSide() instanceof Chest) {
                    Chest left = (Chest) doubleChest.getLeftSide();
                    return ChestRef.from(left.getBlock());
                }
            }
        }
        return ChestRef.from(block);
    }

    private void playTransferEffect(ChestRef from, ChestRef to) {
        if (from == null || to == null) return;
        if (!particlesEnabled && !soundsEnabled) return;

        Location fromLoc = from.location();
        Location toLoc = to.location();

        if (fromLoc != null && from.isChunkLoaded()) {
            Location center = fromLoc.clone().add(0.5, 0.6, 0.5);
            if (particlesEnabled) {
                XStorageVersion.spawnPortalParticle(center, 8);
            }
            if (soundsEnabled) {
                XStorageVersion.playPickup(center);
            }
        }

        if (toLoc != null && to.isChunkLoaded()) {
            Location center = toLoc.clone().add(0.5, 0.6, 0.5);
            if (particlesEnabled) {
                XStorageVersion.spawnPortalParticle(center, 8);
            }
            if (soundsEnabled) {
                XStorageVersion.playSound(null, center, "BLOCK_AMETHYST_BLOCK_CHIME", "ORB_PICKUP", 0.4f, 1.6f);
            }
        }
    }

    // --- Hopper Management ---

    public boolean addHopper(StorageHopper hopper) {
        if (hopper == null || hopper.getRef() == null) return false;
        hoppers.put(hopper.getRef(), hopper);
        save();
        return true;
    }

    public StorageHopper getHopperAt(ChestRef ref) {
        return ref != null ? hoppers.get(ref) : null;
    }

    public boolean removeHopper(ChestRef ref) {
        if (ref == null) return false;
        StorageHopper removed = hoppers.remove(ref);
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    public Collection<StorageHopper> getHoppers() {
        return Collections.unmodifiableCollection(hoppers.values());
    }

    public List<StorageHopper> getPlayerHoppers(UUID playerId) {
        if (playerId == null) return Collections.emptyList();
        List<StorageHopper> list = new ArrayList<StorageHopper>();
        for (StorageHopper h : hoppers.values()) {
            if (playerId.equals(h.getOwner())) {
                list.add(h);
            }
        }
        return list;
    }

    public List<StorageHopper> getHoppersAttachedTo(ChestRef chestRef) {
        if (chestRef == null) return Collections.emptyList();
        List<StorageHopper> list = new ArrayList<StorageHopper>();
        for (StorageHopper h : hoppers.values()) {
            if (chestRef.equals(h.getAttachedChestRef()) || chestRef.equals(h.getRef())) {
                list.add(h);
            }
        }
        return list;
    }

    // --- Legacy / Direct Chest Management ---

    public boolean addInput(ChestRef ref) {
        if (ref == null || isRegistered(ref)) return false;
        boolean ok = inputs.add(ref);
        if (ok) save();
        return ok;
    }

    public String addStorage(ChestRef ref) {
        if (ref == null || isRegistered(ref)) return null;
        String id = "truhe-" + String.format("%03d", nextStorageId++);
        storages.put(ref, new StorageChest(id, ref));
        save();
        return id;
    }

    public boolean remove(ChestRef ref) {
        if (ref == null) return false;
        boolean changed = inputs.remove(ref);
        if (storages.remove(ref) != null) changed = true;
        if (ref.equals(fallback)) {
            fallback = null;
            changed = true;
        }
        if (hoppers.remove(ref) != null) changed = true;
        if (changed) save();
        return changed;
    }

    public boolean setFallback(ChestRef ref) {
        if (ref == null || isRegistered(ref)) return false;
        this.fallback = ref;
        save();
        return true;
    }

    public boolean map(ChestRef ref, Material mat) {
        StorageChest chest = storages.get(ref);
        if (chest != null) {
            boolean ok = chest.addMaterial(mat);
            if (ok) save();
            return ok;
        }
        StorageHopper hopper = hoppers.get(ref);
        if (hopper != null) {
            boolean ok = hopper.addFilterMaterial(mat);
            if (ok) save();
            return ok;
        }
        return false;
    }

    public boolean unmap(ChestRef ref, Material mat) {
        StorageChest chest = storages.get(ref);
        if (chest != null) {
            boolean ok = chest.removeMaterial(mat);
            if (ok) save();
            return ok;
        }
        StorageHopper hopper = hoppers.get(ref);
        if (hopper != null) {
            boolean ok = hopper.removeFilterMaterial(mat);
            if (ok) save();
            return ok;
        }
        return false;
    }

    public boolean isRegistered(ChestRef ref) {
        return ref != null && (inputs.contains(ref) || storages.containsKey(ref) || ref.equals(fallback) || hoppers.containsKey(ref));
    }

    public boolean isInput(ChestRef ref) {
        return ref != null && inputs.contains(ref);
    }

    public boolean isFallback(ChestRef ref) {
        return ref != null && ref.equals(fallback);
    }

    public boolean isProtected(ChestRef ref) {
        if (ref == null) return false;
        if (storages.containsKey(ref) || ref.equals(fallback)) return true;
        for (StorageHopper h : hoppers.values()) {
            if (ref.equals(h.getRef()) || ref.equals(h.getAttachedChestRef())) return true;
        }
        return false;
    }

    public StorageChest getStorageAt(ChestRef ref) {
        return ref != null ? storages.get(ref) : null;
    }

    public boolean isGloballyMapped(Material mat) {
        for (StorageChest sc : storages.values()) {
            if (sc.getMaterials().contains(mat)) return true;
        }
        for (StorageHopper sh : hoppers.values()) {
            if (sh.getType() == StorageHopperType.OUTPUT && sh.getFilterMaterials().contains(mat)) return true;
        }
        return false;
    }

    public Collection<StorageChest> getStorages() {
        return Collections.unmodifiableCollection(storages.values());
    }

    public Set<ChestRef> getInputs() {
        return Collections.unmodifiableSet(inputs);
    }

    public ChestRef getFallback() {
        return fallback;
    }

    public long getTotalItemsSorted() {
        return totalItemsSorted.get();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        startSortingTask();
    }

    // --- Persistence ---

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);

        hoppers.clear();
        storages.clear();
        inputs.clear();
        fallback = null;

        this.nextStorageId = yaml.getInt("next-id", 1);
        this.totalItemsSorted.set(yaml.getLong("total-sorted", 0));

        // Load wireless hoppers
        ConfigurationSection hoppersSec = yaml.getConfigurationSection("hoppers");
        if (hoppersSec != null) {
            for (String key : hoppersSec.getKeys(false)) {
                ConfigurationSection hSec = hoppersSec.getConfigurationSection(key);
                if (hSec == null) continue;

                ChestRef ref = readRef(hSec.getConfigurationSection("ref"));
                if (ref == null) continue;

                ChestRef attached = readRef(hSec.getConfigurationSection("attached"));
                String typeStr = hSec.getString("type", "INPUT");
                StorageHopperType type;
                try {
                    type = StorageHopperType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    type = StorageHopperType.INPUT;
                }

                String ownerStr = hSec.getString("owner");
                UUID owner = ownerStr != null ? UUID.fromString(ownerStr) : null;
                boolean enabled = hSec.getBoolean("enabled", true);

                StorageHopper hopper = new StorageHopper(UUID.fromString(key), ref, attached, type, owner, enabled);
                List<String> mats = hSec.getStringList("materials");
                for (String m : mats) {
                    Material mat = Material.matchMaterial(m);
                    if (mat != null) hopper.addFilterMaterial(mat);
                }
                hoppers.put(ref, hopper);
            }
        }

        // Load legacy inputs
        List<Map<?, ?>> inputList = yaml.getMapList("inputs");
        for (Map<?, ?> map : inputList) {
            ChestRef ref = readRef(map);
            if (ref != null) inputs.add(ref);
        }

        // Load legacy fallback
        if (yaml.isConfigurationSection("fallback")) {
            this.fallback = readRef(yaml.getConfigurationSection("fallback"));
        }

        // Load legacy storages
        ConfigurationSection storagesSec = yaml.getConfigurationSection("storages");
        if (storagesSec != null) {
            for (String key : storagesSec.getKeys(false)) {
                ConfigurationSection sec = storagesSec.getConfigurationSection(key);
                if (sec == null) continue;
                ChestRef ref = readRef(sec.getConfigurationSection("ref"));
                if (ref == null) continue;
                String id = sec.getString("id", key);
                StorageChest chest = new StorageChest(id, ref);
                List<String> mats = sec.getStringList("materials");
                for (String m : mats) {
                    Material mat = Material.matchMaterial(m);
                    if (mat != null) chest.addMaterial(mat);
                }
                storages.put(ref, chest);
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("next-id", nextStorageId);
        yaml.set("total-sorted", totalItemsSorted.get());

        // Save hoppers
        for (StorageHopper hopper : hoppers.values()) {
            String path = "hoppers." + hopper.getId().toString();
            writeRef(yaml, path + ".ref", hopper.getRef());
            if (hopper.getAttachedChestRef() != null) {
                writeRef(yaml, path + ".attached", hopper.getAttachedChestRef());
            }
            yaml.set(path + ".type", hopper.getType().name());
            if (hopper.getOwner() != null) {
                yaml.set(path + ".owner", hopper.getOwner().toString());
            }
            yaml.set(path + ".enabled", hopper.isEnabled());
            List<String> mats = new ArrayList<String>();
            for (Material m : hopper.getFilterMaterials()) mats.add(m.name());
            yaml.set(path + ".materials", mats);
        }

        // Save legacy inputs
        List<Map<String, Object>> inputList = new ArrayList<Map<String, Object>>();
        for (ChestRef ref : inputs) inputList.add(writeRefMap(ref));
        yaml.set("inputs", inputList);

        // Save legacy fallback
        if (fallback != null) {
            writeRef(yaml, "fallback", fallback);
        }

        // Save legacy storages
        for (StorageChest sc : storages.values()) {
            String path = "storages." + sc.getId();
            yaml.set(path + ".id", sc.getId());
            writeRef(yaml, path + ".ref", sc.getRef());
            List<String> mats = new ArrayList<String>();
            for (Material m : sc.getMaterials()) mats.add(m.name());
            yaml.set(path + ".materials", mats);
        }

        try {
            if (!dataFile.getParentFile().exists()) dataFile.getParentFile().mkdirs();
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte storage_data.yml nicht speichern: " + e.getMessage());
        }
    }

    private void writeRef(YamlConfiguration yaml, String path, ChestRef ref) {
        if (ref == null) return;
        yaml.set(path + ".world", ref.world());
        yaml.set(path + ".x", ref.x());
        yaml.set(path + ".y", ref.y());
        yaml.set(path + ".z", ref.z());
    }

    private Map<String, Object> writeRefMap(ChestRef ref) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("world", ref.world());
        map.put("x", ref.x());
        map.put("y", ref.y());
        map.put("z", ref.z());
        return map;
    }

    private ChestRef readRef(ConfigurationSection sec) {
        if (sec == null) return null;
        String world = sec.getString("world");
        int x = sec.getInt("x");
        int y = sec.getInt("y");
        int z = sec.getInt("z");
        return world != null ? new ChestRef(world, x, y, z) : null;
    }

    private ChestRef readRef(Map<?, ?> map) {
        if (map == null) return null;
        Object w = map.get("world");
        Object x = map.get("x");
        Object y = map.get("y");
        Object z = map.get("z");
        if (w instanceof String && x instanceof Number && y instanceof Number && z instanceof Number) {
            String world = (String) w;
            Number nx = (Number) x;
            Number ny = (Number) y;
            Number nz = (Number) z;
            return new ChestRef(world, nx.intValue(), ny.intValue(), nz.intValue());
        }
        return null;
    }
}
