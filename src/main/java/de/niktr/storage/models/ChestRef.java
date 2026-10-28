package de.niktr.storage.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public final class ChestRef {

    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public ChestRef(String world, int x, int y, int z) {
        this.world = world != null ? world : "world";
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static ChestRef from(Block block) {
        if (block == null) return null;
        Location loc = block.getLocation();
        return new ChestRef(loc.getWorld() != null ? loc.getWorld().getName() : "world", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static ChestRef from(Location loc) {
        if (loc == null) return null;
        return new ChestRef(loc.getWorld() != null ? loc.getWorld().getName() : "world", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public String world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChestRef chestRef = (ChestRef) o;
        return x == chestRef.x &&
                y == chestRef.y &&
                z == chestRef.z &&
                Objects.equals(world, chestRef.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    @Override
    public String toString() {
        return "ChestRef{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
