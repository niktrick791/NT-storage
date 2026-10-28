package de.niktr.storage.commands;

import de.niktr.storage.gui.StorageMainMenu;
import de.niktr.storage.gui.StorageRecipeMenu;
import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.ChestRef;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.models.StorageHopperType;
import de.niktr.storage.util.XStorageVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

public class LagerCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "gui", "help", "status", "list", "recipe", "givehopper",
            "register", "unregister", "input", "fallback", "map", "unmap", "reload", "lang"
    );

    private final Plugin plugin;
    private final StorageManager manager;

    public LagerCommand(Plugin plugin, StorageManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private boolean isDe() {
        return manager.isDe();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(isDe() ? "§cNur Spieler können das Menü öffnen." : "§cOnly players can open the storage GUI.");
                return true;
            }
            Player player = (Player) sender;
            new StorageMainMenu(plugin, player, manager).open();
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if ("help".equals(sub)) {
            if (isDe()) {
                msg(sender, "§6=== niktrick Drahtloses Lagersystem ===");
                msg(sender, "§e/lager gui §7- Öffnet das Hauptmenü");
                msg(sender, "§e/lager recipe §7- Zeigt das Crafting-Rezept");
                msg(sender, "§e/lager status §7- Zeigt Systemstatistiken");
                msg(sender, "§e/lager list §7- Listet alle registrierten Kisten auf");
                if (sender.hasPermission("niktrick.storage.admin") || sender.isOp()) {
                    msg(sender, "§6Admin-Befehle:");
                    msg(sender, "§e/lager givehopper <spieler> [input|output|fallback]");
                    msg(sender, "§e/lager input §7- Registriert Kiste als Eingang");
                    msg(sender, "§e/lager register §7- Registriert Kiste als Lagerkiste");
                    msg(sender, "§e/lager unregister §7- Entfernt Kiste aus dem System");
                    msg(sender, "§e/lager map <MATERIAL> §7- Weist Kiste ein Material zu");
                    msg(sender, "§e/lager unmap <MATERIAL> §7- Entfernt Zuweisung");
                    msg(sender, "§e/lager fallback §7- Setzt Kiste als Restekiste");
                    msg(sender, "§e/lager reload §7- Lädt Konfiguration neu");
                    msg(sender, "§e/lager lang <de|en> §7- Ändert die Sprache (Global)");
                }
            } else {
                msg(sender, "§6=== niktrick Wireless Storage System ===");
                msg(sender, "§e/lager gui §7- Opens main storage GUI");
                msg(sender, "§e/lager recipe §7- Shows crafting recipe");
                msg(sender, "§e/lager status §7- Shows system statistics");
                msg(sender, "§e/lager list §7- Lists all registered hoppers/chests");
                if (sender.hasPermission("niktrick.storage.admin") || sender.isOp()) {
                    msg(sender, "§6Admin Commands:");
                    msg(sender, "§e/lager givehopper <player> [input|output|fallback]");
                    msg(sender, "§e/lager input §7- Registers chest as input");
                    msg(sender, "§e/lager register §7- Registers chest as storage chest");
                    msg(sender, "§e/lager unregister §7- Removes chest from system");
                    msg(sender, "§e/lager map <MATERIAL> §7- Maps a material to chest");
                    msg(sender, "§e/lager unmap <MATERIAL> §7- Removes material mapping");
                    msg(sender, "§e/lager fallback §7- Sets chest as fallback chest");
                    msg(sender, "§e/lager reload §7- Reloads configuration");
                    msg(sender, "§e/lager lang <de|en> §7- Changes language (Global)");
                }
            }
            return true;
        }

        if ("lang".equals(sub) || "language".equals(sub)) {
            if (!checkAdmin(sender)) return true;
            if (args.length >= 2) {
                String target = args[1].toLowerCase(Locale.ROOT);
                if (target.equals("de") || target.equals("en")) {
                    manager.setLanguage(target);
                    if ("de".equals(target)) {
                        msg(sender, "§aSprache wurde auf §eDeutsch §ageändert. Rezepte aktualisiert!");
                    } else {
                        msg(sender, "§aLanguage set to §eEnglish§a. Recipes updated!");
                    }
                    return true;
                }
            }
            msg(sender, isDe()
                    ? "§7Aktuelle Sprache: §e" + manager.getLanguage() + " §7(Verfügbar: §bde§7, §ben§7)"
                    : "§7Current language: §e" + manager.getLanguage() + " §7(Available: §bde§7, §ben§7)");
            return true;
        }

        if ("recipe".equals(sub)) {
            if (sender instanceof Player) {
                new StorageRecipeMenu(plugin, (Player) sender, null).open();
            } else {
                msg(sender, isDe() ? "§7Rezept: 1x Trichter + 1x Enderperle in der Werkbank." : "§7Recipe: 1x Hopper + 1x Ender Pearl in crafting table.");
            }
            return true;
        }

        if ("status".equals(sub)) {
            long sorted = manager.getTotalItemsSorted();
            int hoppers = manager.getHoppers().size();
            int storages = manager.getStorages().size();
            int inputs = manager.getInputs().size();
            if (isDe()) {
                msg(sender, "§6Status: §a" + (manager.isEnabled() ? "AKTIV" : "PAUSIERT"));
                msg(sender, "§7Drahtlose Trichter: §f" + hoppers);
                msg(sender, "§7Lagerkisten: §f" + storages + " §7| Eingangskisten: §f" + inputs);
                msg(sender, "§7Sortierte Items gesamt: §e" + sorted);
            } else {
                msg(sender, "§6Status: §a" + (manager.isEnabled() ? "ACTIVE" : "PAUSED"));
                msg(sender, "§7Wireless Hoppers: §f" + hoppers);
                msg(sender, "§7Storage Chests: §f" + storages + " §7| Input Chests: §f" + inputs);
                msg(sender, "§7Total Items Sorted: §e" + sorted);
            }
            return true;
        }

        if ("list".equals(sub)) {
            msg(sender, isDe() ? "§6=== Registrierte Lagerkisten & Trichter ===" : "§6=== Registered Storage Chests & Hoppers ===");
            for (StorageHopper h : manager.getHoppers()) {
                msg(sender, "§8• " + h.getType().getFormattedName(isDe()) + " §7@ §f" + h.getRef().display() + " §7(Filter: §e" + h.getFilterMaterials().size() + "§7)");
            }
            return true;
        }

        if ("givehopper".equals(sub)) {
            if (!checkAdmin(sender)) return true;
            if (args.length < 2) {
                msg(sender, isDe() ? "§cVerwendung: /lager givehopper <spieler> [input|output|fallback]" : "§cUsage: /lager givehopper <player> [input|output|fallback]");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                msg(sender, isDe() ? "§cSpieler nicht gefunden." : "§cPlayer not found.");
                return true;
            }
            StorageHopperType type = StorageHopperType.INPUT;
            if (args.length >= 3) {
                try {
                    type = StorageHopperType.valueOf(args[2].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {}
            }
            target.getInventory().addItem(StorageHopperItem.createHopper(type, 1, isDe()));
            msg(sender, isDe() ? "§a" + type.getFormattedName(true) + " §aan §f" + target.getName() + " §agegeben." : "§aGave " + type.getFormattedName(false) + " §ato §f" + target.getName() + "§a.");
            return true;
        }

        if ("reload".equals(sub)) {
            if (!checkAdmin(sender)) return true;
            plugin.reloadConfig();
            manager.loadConfig();
            manager.load();
            manager.startSortingTask();
            msg(sender, isDe() ? "§aNT-storage Konfiguration neu geladen!" : "§aNT-storage configuration reloaded!");
            return true;
        }

        if ("input".equals(sub) || "register".equals(sub) || "unregister".equals(sub)
                || "fallback".equals(sub) || "map".equals(sub) || "unmap".equals(sub)) {
            if (!checkAdmin(sender)) return true;
            if (!(sender instanceof Player)) {
                msg(sender, isDe() ? "§cNur Spieler können Kisten anvisieren." : "§cOnly players can target chests.");
                return true;
            }
            Player player = (Player) sender;
            Block targetBlock = getTargetBlock(player, 5);
            if (targetBlock == null || !StorageManager.isChestBlock(targetBlock)) {
                msg(player, isDe() ? "§cBitte schaue eine Kiste an (max. 5 Blöcke Entfernung)." : "§cPlease target a chest block (max. 5 blocks away).");
                return true;
            }
            ChestRef ref = manager.canonical(targetBlock);

            if ("input".equals(sub)) {
                if (manager.addInput(ref)) {
                    msg(player, isDe() ? "§aEingangskiste erfolgreich registriert!" : "§aInput chest registered successfully!");
                } else {
                    msg(player, isDe() ? "§eDiese Kiste ist bereits registriert." : "§eThis chest is already registered.");
                }
            } else if ("register".equals(sub)) {
                String id = manager.addStorage(ref);
                if (id != null) {
                    msg(player, isDe() ? "§aLagerkiste erfolgreich registriert mit ID: §f" + id : "§aStorage chest registered with ID: §f" + id);
                } else {
                    msg(player, isDe() ? "§eDiese Kiste ist bereits registriert." : "§eThis chest is already registered.");
                }
            } else if ("unregister".equals(sub)) {
                if (manager.remove(ref)) {
                    msg(player, isDe() ? "§eKiste erfolgreich aus dem System entfernt." : "§eChest removed from the storage system.");
                } else {
                    msg(player, isDe() ? "§cDiese Kiste war nicht registriert." : "§cThis chest was not registered.");
                }
            } else if ("fallback".equals(sub)) {
                if (manager.setFallback(ref)) {
                    msg(player, isDe() ? "§aRestekiste (Fallback) erfolgreich gesetzt!" : "§aFallback chest set successfully!");
                } else {
                    msg(player, isDe() ? "§eKonnte Restekiste nicht setzen (bereits anderweitig registriert)." : "§eCould not set fallback chest (already registered otherwise).");
                }
            } else if ("map".equals(sub)) {
                if (args.length < 2) {
                    msg(player, isDe() ? "§cVerwendung: /lager map <MATERIAL>" : "§cUsage: /lager map <MATERIAL>");
                    return true;
                }
                Material mat = Material.matchMaterial(args[1]);
                if (mat == null || !XStorageVersion.isItem(mat)) {
                    msg(player, isDe() ? "§cUnbekanntes Minecraft-Item: " + args[1] : "§cUnknown Minecraft item: " + args[1]);
                    return true;
                }
                if (manager.map(ref, mat)) {
                    msg(player, isDe() ? "§aMaterial §f" + mat.name() + " §azugewiesen!" : "§aMaterial §f" + mat.name() + " §amapped!");
                } else {
                    msg(player, isDe() ? "§cKiste ist nicht als Lagerkiste registriert." : "§cChest is not registered as a storage chest.");
                }
            } else if ("unmap".equals(sub)) {
                if (args.length < 2) {
                    msg(player, isDe() ? "§cVerwendung: /lager unmap <MATERIAL>" : "§cUsage: /lager unmap <MATERIAL>");
                    return true;
                }
                Material mat = Material.matchMaterial(args[1]);
                if (mat == null) {
                    msg(player, isDe() ? "§cUnbekanntes Minecraft-Item: " + args[1] : "§cUnknown Minecraft item: " + args[1]);
                    return true;
                }
                if (manager.unmap(ref, mat)) {
                    msg(player, isDe() ? "§eMaterial §f" + mat.name() + " §eentfernt!" : "§eMaterial §f" + mat.name() + " §eremoved!");
                } else {
                    msg(player, isDe() ? "§cMaterial war nicht zugewiesen." : "§cMaterial was not mapped.");
                }
            }
            return true;
        }

        msg(sender, isDe() ? "§cUnbekannter Befehl. Nutze §e/lager help" : "§cUnknown command. Use §e/lager help");
        return true;
    }

    private Block getTargetBlock(Player player, int range) {
        try {
            Method m = player.getClass().getMethod("getTargetBlockExact", int.class);
            Block b = (Block) m.invoke(player, range);
            if (b != null) return b;
        } catch (Throwable ignored) {}

        try {
            return player.getTargetBlock((Set<Material>) null, range);
        } catch (Throwable ignored) {}

        return null;
    }

    private boolean checkAdmin(CommandSender sender) {
        if (sender.hasPermission("niktrick.storage.admin") || sender.isOp()) return true;
        msg(sender, isDe() ? "§cDazu hast du keine Berechtigung." : "§cYou do not have permission.");
        return false;
    }

    private void msg(CommandSender sender, String text) {
        sender.sendMessage("§6[NT-storage] " + text);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<String>();
            for (String s : SUBCOMMANDS) {
                if (s.startsWith(prefix)) matches.add(s);
            }
            return matches;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language")) && (sender.hasPermission("niktrick.storage.admin") || sender.isOp())) {
            return Arrays.asList("de", "en");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("unmap"))) {
            String prefix = args[1].toUpperCase(Locale.ROOT);
            List<String> matches = new ArrayList<String>();
            for (Material m : Material.values()) {
                if (XStorageVersion.isItem(m) && m.name().startsWith(prefix)) {
                    matches.add(m.name());
                    if (matches.size() >= 40) break;
                }
            }
            return matches;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("givehopper")) {
            return Arrays.asList("input", "output", "fallback");
        }
        return Collections.emptyList();
    }
}
