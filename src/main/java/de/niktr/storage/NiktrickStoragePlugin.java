package de.niktr.storage;

import de.niktr.storage.commands.LagerCommand;
import de.niktr.storage.filter.StrictStorageFilter;
import de.niktr.storage.listeners.StorageHopperListener;
import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.managers.StorageManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NiktrickStoragePlugin extends JavaPlugin {

    private static NiktrickStoragePlugin instance;
    private StorageManager storageManager;

    public static NiktrickStoragePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.storageManager = new StorageManager(this, getDataFolder());
        this.storageManager.start();

        // Install strict item filter protection against unauthorized chest insertions
        StrictStorageFilter.install(this, this.storageManager);

        // Register custom wireless hopper recipes
        StorageHopperItem.registerRecipes(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new StorageHopperListener(this, this.storageManager), this);

        // Register commands (/storage and /lager)
        LagerCommand cmd = new LagerCommand(this, this.storageManager);
        PluginCommand storageCmd = getCommand("storage");
        if (storageCmd != null) {
            storageCmd.setExecutor(cmd);
            storageCmd.setTabCompleter(cmd);
        }
        PluginCommand lagerCmd = getCommand("lager");
        if (lagerCmd != null) {
            lagerCmd.setExecutor(cmd);
            lagerCmd.setTabCompleter(cmd);
        }

        getLogger().info("=================================================");
        getLogger().info("niktrick-storage v" + getDescription().getVersion() + " erfolgreich aktiviert!");
        getLogger().info("Strikte Kistenfilter & Drahtloses Lagersystem aktiv.");
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        StorageHopperItem.unregisterRecipes(this);

        if (storageManager != null) {
            storageManager.shutdown();
        }

        getLogger().info("niktrick-storage deaktiviert.");
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
