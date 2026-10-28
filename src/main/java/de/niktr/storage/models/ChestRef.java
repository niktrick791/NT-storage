package de.niktr.storage.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public record ChestRef(String world, int x, int y, int z) {

    public static ChestRef from(Block block) {
        if (block == null) return null;
        Location loc = block.getLocation();
        return new ChestRef(loc.getWorld() != null ? loc.getWorld().getName() : "world", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static ChestRef from(Location loc) {
        if (loc == null) return null;
        return new ChestRef(loc.getWorld() != null ? loc.getWorld().getName() : "world", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public Block block() {
        World w = Bukkit.getWorld(world);
        return w != null ? w.getBlockAt(x, y, z) : null;
    }

    public Location location() {
        World w = Bukkit.getWorld(world);
        return w != null ? new Location(w, x, y, z) : null;
    }

    public boolean isChunkLoaded() {
        World w = Bukkit.getWorld(world);
        return w != null && w.isChunkLoaded(x >> 4, z >> 4);
    }

    public String display() {
        return world + " [" + x + ", " + y + ", " + z + "]";
    }
}
