package com.andrestube.simpletrains.commands;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TrainTabCompleter implements TabCompleter {

    private final SimpleTrains plugin;
    private final StationManager manager;

    private static final String ADMIN_PERM = "simpletrains.admin";

    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "set", "delete", "link", "unlink", "transfer", "near",
        "block", "message", "accept", "reject", "gui", "help", "reload"
    );

    private static final List<String> LINK_TYPES = Arrays.asList("public", "private");

    public TrainTabCompleter(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return completions;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            // First argument: subcommand
            String partial = args[0].toLowerCase();
            for (String sub : SUBCOMMANDS) {
                // Hide admin commands from non-admins
                if ((sub.equals("block") || sub.equals("reload")) && !player.hasPermission(ADMIN_PERM)) {
                    continue;
                }
                if (sub.startsWith(partial)) {
                    completions.add(sub);
                }
            }
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            String partial = args[args.length - 1].toLowerCase();

            switch (subCommand) {
                case "set":
                    // No completions for station name (user creates it)
                    break;

                case "delete":
                case "message":
                    if (args.length == 2) {
                        // Station names (owned by player or all if admin)
                        completions.addAll(getAccessibleStations(player, partial));
                    }
                    break;

                case "link":
                    if (args.length == 2) {
                        // First station: owned by player
                        completions.addAll(getOwnedStations(player, partial));
                    } else if (args.length == 3) {
                        // Second station: all stations except first
                        String firstStation = args[1];
                        completions.addAll(getAllStationsExcept(partial, firstStation));
                    } else if (args.length == 4) {
                        // Link type
                        completions.addAll(filterStartsWith(LINK_TYPES, partial));
                    }
                    break;

                case "unlink":
                    if (args.length == 2) {
                        // First station: accessible to player
                        completions.addAll(getAccessibleStations(player, partial));
                    } else if (args.length == 3) {
                        // Second station: linked to first
                        String firstStation = args[1];
                        completions.addAll(getLinkedStations(firstStation, partial));
                    }
                    break;

                case "transfer":
                    if (args.length == 2) {
                        // Station names (owned by player)
                        completions.addAll(getOwnedStations(player, partial));
                    } else if (args.length == 3) {
                        // Online player names
                        completions.addAll(getOnlinePlayers(partial));
                    }
                    break;

                case "accept":
                case "reject":
                    if (args.length == 2) {
                        // Initiating station (from pending requests)
                        completions.addAll(getPendingInitiatingStations(player, partial));
                    } else if (args.length == 3) {
                        // Receiving station (owned by player with pending request)
                        completions.addAll(getPendingReceivingStations(player, partial));
                    }
                    break;

                case "block":
                    if (args.length == 2 && player.hasPermission(ADMIN_PERM)) {
                        // Block materials
                        completions.addAll(getBlockMaterials(partial));
                    }
                    break;

                case "reload":
                case "near":
                case "gui":
                case "help":
                    // No additional arguments
                    break;
            }
        }

        return completions;
    }

    private List<String> getOwnedStations(Player player, String partial) {
        Set<String> allStations = manager.getAllStationNames();
        return allStations.stream()
            .filter(name -> {
                if (player.hasPermission(ADMIN_PERM)) return true;
                var ownerId = manager.getStationOwnerId(name);
                return ownerId != null && ownerId.equals(player.getUniqueId());
            })
            .filter(name -> name.toLowerCase().startsWith(partial))
            .collect(Collectors.toList());
    }

    private List<String> getAccessibleStations(Player player, String partial) {
        Set<String> allStations = manager.getAllStationNames();
        return allStations.stream()
            .filter(name -> {
                if (player.hasPermission(ADMIN_PERM)) return true;
                var ownerId = manager.getStationOwnerId(name);
                return ownerId != null && ownerId.equals(player.getUniqueId());
            })
            .filter(name -> name.toLowerCase().startsWith(partial))
            .collect(Collectors.toList());
    }

    private List<String> getAllStationsExcept(String partial, String except) {
        Set<String> allStations = manager.getAllStationNames();
        return allStations.stream()
            .filter(name -> !name.equalsIgnoreCase(except))
            .filter(name -> name.toLowerCase().startsWith(partial))
            .collect(Collectors.toList());
    }

    private List<String> getLinkedStations(String station, String partial) {
        List<String> linked = manager.getLinkedStations(station);
        return linked.stream()
            .filter(name -> name.toLowerCase().startsWith(partial))
            .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayers(String partial) {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(partial))
            .collect(Collectors.toList());
    }

    private List<String> getPendingInitiatingStations(Player player, String partial) {
        // Get stations that have pending requests where player owns the receiving station
        Set<String> allStations = manager.getAllStationNames();
        List<String> result = new ArrayList<>();

        for (String station : allStations) {
            var ownerId = manager.getStationOwnerId(station);
            if (ownerId != null && ownerId.equals(player.getUniqueId())) {
                String initiating = manager.getPendingInitiatingStation(station);
                if (initiating != null && initiating.toLowerCase().startsWith(partial)) {
                    result.add(initiating);
                }
            }
        }

        return result.stream().distinct().collect(Collectors.toList());
    }

    private List<String> getPendingReceivingStations(Player player, String partial) {
        // Get stations owned by player that have pending requests
        Set<String> allStations = manager.getAllStationNames();
        return allStations.stream()
            .filter(name -> {
                var ownerId = manager.getStationOwnerId(name);
                if (ownerId == null || !ownerId.equals(player.getUniqueId())) return false;
                return manager.getPendingRequesterId(name) != null;
            })
            .filter(name -> name.toLowerCase().startsWith(partial))
            .collect(Collectors.toList());
    }

    private List<String> getBlockMaterials(String partial) {
        return Arrays.stream(Material.values())
            .filter(Material::isBlock)
            .filter(m -> !m.isAir())
            .map(Material::name)
            .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
            .limit(30) // Limit results to avoid lag
            .collect(Collectors.toList());
    }

    private List<String> filterStartsWith(List<String> list, String partial) {
        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(partial))
            .collect(Collectors.toList());
    }
}
