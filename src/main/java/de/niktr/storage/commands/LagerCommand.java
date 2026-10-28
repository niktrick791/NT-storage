package de.niktr.storage.commands;

import de.niktr.storage.gui.StorageMainMenu;
import de.niktr.storage.gui.StorageRecipeMenu;
import de.niktr.storage.managers.StorageHopperItem;
import de.niktr.storage.managers.StorageManager;
import de.niktr.storage.models.ChestRef;
import de.niktr.storage.models.StorageHopper;
import de.niktr.storage.models.StorageHopperType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class LagerCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "gui", "help", "status", "list", "recipe", "givehopper",
            "register", "unregister", "input", "fallback", "map", "unmap", "reload"
    );

    private final Plugin plugin;
    private final StorageManager manager;

    public LagerCommand(Plugin plugin, StorageManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("§cNur Spieler können das Menü öffnen."));
                return true;
            }
            new StorageMainMenu(plugin, player, manager).open();
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> {
                msg(sender, "§6=== BorderMC Drahtloses Lagersystem ===");
                msg(sender, "§e/lager gui §7- Öffnet das Hauptmenü");
                msg(sender, "§e/lager recipe §7- Zeigt das Crafting-Rezept");
                msg(sender, "§e/lager status §7- Zeigt Systemstatistiken");
                msg(sender, "§e/lager list §7- Listet alle registrierten Kisten auf");
                if (sender.hasPermission("NiktrickStorage.admin") || sender.isOp()) {
                    msg(sender, "§6Admin-Befehle:");
                    msg(sender, "§e/lager givehopper <spieler> [input|output|fallback]");
                    msg(sender, "§e/lager input §7- Registriert Kiste als Eingang");
                    msg(sender, "§e/lager register §7- Registriert Kiste als Lagerkiste");
                    msg(sender, "§e/lager unregister §7- Entfernt Kiste aus dem System");
                    msg(sender, "§e/lager map <MATERIAL> §7- Weist Kiste ein Material zu");
                    msg(sender, "§e/lager unmap <MATERIAL> §7- Entfernt Zuweisung");
                    msg(sender, "§e/lager fallback §7- Setzt Kiste als Restekiste");
                    msg(sender, "§e/lager reload §7- Lädt Konfiguration neu");
                }
                return true;
            }
            case "recipe" -> {
                if (sender instanceof Player player) {
                    new StorageRecipeMenu(plugin, player, null).open();
                } else {
                    msg(sender, "§7Rezept: 1x Trichter + 1x Enderperle in der Werkbank.");
                }
                return true;
            }
            case "status" -> {
                long sorted = manager.getTotalItemsSorted();
                int hoppers = manager.getHoppers().size();
                int storages = manager.getStorages().size();
                int inputs = manager.getInputs().size();
                msg(sender, "§6Status: §a" + (manager.isEnabled() ? "AKTIV" : "PAUSIERT"));
                msg(sender, "§7Drahtlose Trichter: §f" + hoppers);
                msg(sender, "§7Lagerkisten: §f" + storages + " §7| Eingangskisten: §f" + inputs);
                msg(sender, "§7Sortierte Items gesamt: §e" + sorted);
                return true;
            }
            case "list" -> {
                msg(sender, "§6=== Registrierte Lagerkisten & Trichter ===");
                for (StorageHopper h : manager.getHoppers()) {
                    msg(sender, "§8• " + h.getType().getFormattedName() + " §7bei §f" + h.getRef().display() + " §7(Filter: §e" + h.getFilterMaterials().size() + "§7)");
                }
                return true;
            }
            case "givehopper" -> {
                if (!checkAdmin(sender)) return true;
                if (args.length < 2) {
                    msg(sender, "§cVerwendung: /lager givehopper <spieler> [input|output|fallback]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    msg(sender, "§cSpieler nicht gefunden.");
                    return true;
                }
                StorageHopperType type = StorageHopperType.INPUT;
                if (args.length >= 3) {
                    try {
                        type = StorageHopperType.valueOf(args[2].toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ignored) {}
                }
                target.getInventory().addItem(StorageHopperItem.createHopper(type, 1));
                msg(sender, "§a" + type.getFormattedName() + " §aan §f" + target.getName() + " §agegeben.");
                return true;
            }
            case "reload" -> {
                if (!checkAdmin(sender)) return true;
                plugin.reloadConfig();
                manager.loadConfig();
                manager.load();
                manager.startSortingTask();
                msg(sender, "§aBorderMC-Lager Konfiguration neu geladen!");
                return true;
            }
            case "input", "register", "unregister", "fallback", "map", "unmap" -> {
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player player)) {
                    msg(sender, "§cNur Spieler können Kisten anvisieren.");
                    return true;
                }
                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null || !StorageManager.isChestBlock(targetBlock)) {
                    msg(player, "§cBitte schaue eine Kiste an (max. 5 Blöcke Entfernung).");
                    return true;
                }
                ChestRef ref = manager.canonical(targetBlock);

                switch (sub) {
                    case "input" -> {
                        if (manager.addInput(ref)) {
                            msg(player, "§aEingangskiste erfolgreich registriert!");
                        } else {
                            msg(player, "§eDiese Kiste ist bereits registriert.");
                        }
                    }
                    case "register" -> {
                        String id = manager.addStorage(ref);
                        if (id != null) {
                            msg(player, "§aLagerkiste erfolgreich registriert mit ID: §f" + id);
                        } else {
                            msg(player, "§eDiese Kiste ist bereits registriert.");
                        }
                    }
                    case "unregister" -> {
                        if (manager.remove(ref)) {
                            msg(player, "§eKiste erfolgreich aus dem System entfernt.");
                        } else {
                            msg(player, "§cDiese Kiste war nicht registriert.");
                        }
                    }
                    case "fallback" -> {
                        if (manager.setFallback(ref)) {
                            msg(player, "§aRestekiste (Fallback) erfolgreich gesetzt!");
                        } else {
                            msg(player, "§eKonnte Restekiste nicht setzen (bereits anderweitig registriert).");
                        }
                    }
                    case "map" -> {
                        if (args.length < 2) {
                            msg(player, "§cVerwendung: /lager map <MATERIAL>");
                            return true;
                        }
                        Material mat = Material.matchMaterial(args[1]);
                        if (mat == null || !mat.isItem()) {
                            msg(player, "§cUnbekanntes Minecraft-Item: " + args[1]);
                            return true;
                        }
                        if (manager.map(ref, mat)) {
                            msg(player, "§aMaterial §f" + mat.name() + " §azugewiesen!");
                        } else {
                            msg(player, "§cKiste ist nicht als Lagerkiste registriert.");
                        }
                    }
                    case "unmap" -> {
                        if (args.length < 2) {
                            msg(player, "§cVerwendung: /lager unmap <MATERIAL>");
                            return true;
                        }
                        Material mat = Material.matchMaterial(args[1]);
                        if (mat == null) {
                            msg(player, "§cUnbekanntes Minecraft-Item: " + args[1]);
                            return true;
                        }
                        if (manager.unmap(ref, mat)) {
                            msg(player, "§eMaterial §f" + mat.name() + " §eentfernt!");
                        } else {
                            msg(player, "§cMaterial war nicht zugewiesen.");
                        }
                    }
                }
                return true;
            }
            default -> {
                msg(sender, "§cUnbekannter Befehl. Nutze §e/lager help");
                return true;
            }
        }
    }

    private boolean checkAdmin(CommandSender sender) {
        if (sender.hasPermission("NiktrickStorage.admin") || sender.isOp()) return true;
        msg(sender, "§cDazu hast du keine Berechtigung.");
        return false;
    }

    private void msg(CommandSender sender, String text) {
        sender.sendMessage(Component.text("§6[BorderMC-Lager] " + text));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (String s : SUBCOMMANDS) {
                if (s.startsWith(prefix)) matches.add(s);
            }
            return matches;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("unmap"))) {
            String prefix = args[1].toUpperCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (Material m : Material.values()) {
                if (m.isItem() && m.name().startsWith(prefix)) {
                    matches.add(m.name());
                    if (matches.size() >= 40) break;
                }
            }
            return matches;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("givehopper")) {
            return List.of("input", "output", "fallback");
        }
        return Collections.emptyList();
    }
}
