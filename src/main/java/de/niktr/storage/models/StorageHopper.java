package de.niktr.storage.models;

import org.bukkit.Material;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class StorageHopper {

    private final UUID id;
    private final ChestRef ref;
    private ChestRef attachedChestRef;
    private StorageHopperType type;
    private final Set<Material> filterMaterials = new LinkedHashSet<>();
    private final UUID owner;
    private boolean enabled;

    public StorageHopper(UUID id, ChestRef ref, ChestRef attachedChestRef, StorageHopperType type, UUID owner, boolean enabled) {
        this.id = id != null ? id : UUID.randomUUID();
        this.ref = ref;
        this.attachedChestRef = attachedChestRef;
        this.type = type != null ? type : StorageHopperType.INPUT;
        this.owner = owner;
        this.enabled = enabled;
    }

    public UUID getId() {
        return id;
    }

    public ChestRef getRef() {
        return ref;
    }

    public ChestRef getAttachedChestRef() {
        return attachedChestRef;
    }

    public void setAttachedChestRef(ChestRef attachedChestRef) {
        this.attachedChestRef = attachedChestRef;
    }

    public StorageHopperType getType() {
        return type;
    }

    public void setType(StorageHopperType type) {
        this.type = type;
    }

    public Set<Material> getFilterMaterials() {
        return Collections.unmodifiableSet(filterMaterials);
    }

    public boolean addFilterMaterial(Material material) {
        if (material == null || !material.isItem() || material.isAir()) return false;
        return filterMaterials.add(material);
    }

    public boolean removeFilterMaterial(Material material) {
        return filterMaterials.remove(material);
    }

    public void clearFilterMaterials() {
        filterMaterials.clear();
    }

    public void setFilterMaterials(Set<Material> materials) {
        filterMaterials.clear();
        if (materials != null) {
            for (Material m : materials) {
                if (m != null && m.isItem() && !m.isAir()) {
                    filterMaterials.add(m);
                }
            }
        }
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
