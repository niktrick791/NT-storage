package de.niktr.storage.models;

import de.niktr.storage.util.XStorageVersion;
import org.bukkit.Material;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class StorageChest {

    private final String id;
    private final ChestRef ref;
    private final Set<Material> materials = new LinkedHashSet<Material>();

    public StorageChest(String id, ChestRef ref) {
        this.id = id;
        this.ref = ref;
    }

    public String getId() {
        return id;
    }

    public ChestRef getRef() {
        return ref;
    }

    public Set<Material> getMaterials() {
        return Collections.unmodifiableSet(materials);
    }

    public boolean addMaterial(Material material) {
        if (material == null || !XStorageVersion.isItem(material) || material == Material.AIR) return false;
        return materials.add(material);
    }

    public boolean removeMaterial(Material material) {
        return materials.remove(material);
    }

    public void clearMaterials() {
        materials.clear();
    }

    public void setMaterials(Set<Material> set) {
        materials.clear();
        if (set != null) {
            for (Material m : set) {
                if (m != null && XStorageVersion.isItem(m) && m != Material.AIR) {
                    materials.add(m);
                }
            }
        }
    }
}
