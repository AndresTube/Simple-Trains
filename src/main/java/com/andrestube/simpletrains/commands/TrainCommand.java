package com.andrestube.simpletrains.commands;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.gui.MainGui;
import com.andrestube.simpletrains.utils.Messages;
import com.andrestube.simpletrains.utils.StationManager;
import com.andrestube.simpletrains.utils.StationManager.LinkType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.Map;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TrainCommand implements CommandExecutor {

    private final SimpleTrains plugin;
    private final StationManager manager;
    private static final String ADMIN_PERM = "simpletrains.admin";

    public TrainCommand(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private Messages msg() {
        return SimpleTrains.getMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg().getWithPrefix("console-only-players"));
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(p);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                handleSetStation(p, args);
                break;
            case "delete":
                handleDeleteStation(p, args);
                break;
            case "link":
                handleLinkStations(p, args);
                break;
            case "unlink":
                handleUnlinkStations(p, args);
                break;
            case "transfer":
                handleTransferOwner(p, args);
                break;
            case "near":
                handleNearStations(p);
                break;
            case "block":
                handleSetBlock(p, args);
                break;
            case "message":
                handleSetMessage(p, args);
                break;
            case "accept":
                handleLinkAccept(p, args);
                break;
            case "reject":
                handleLinkReject(p, args);
                break;
            case "gui":
                p.openInventory(MainGui.create(manager, p));
                break;
            case "reload":
                handleReload(p);
                break;
            case "help":
                sendHelpMessage(p);
                break;
            default:
                p.sendMessage(msg().getWithPrefix("unknown-command"));
        }

        return true;
    }

    private void handleSetStation(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(msg().getWithPrefix("set-usage"));
            return;
        }
        String name = args[1];

        if (manager.stationExists(name)) {
            p.sendMessage(msg().getWithPrefix("station-already-exists", "STATION", name));
            return;
        }

        // Check creation cost
        var creationCostManager = plugin.getCreationCostManager();
        if (creationCostManager.isEnabled() && !creationCostManager.canAfford(p)) {
            creationCostManager.sendNotEnoughMessage(p);
            return;
        }

        Block railBlock = p.getLocation().getBlock();
        Block baseBlock = railBlock.getRelative(0, -1, 0);

        Material requiredBlock = Material.matchMaterial(plugin.getCreationBlockType());
        if (requiredBlock == null) {
            p.sendMessage(msg().getWithPrefix("config-invalid-block"));
            return;
        }

        if (railBlock.getType().name().contains("RAIL") && baseBlock.getType() == requiredBlock) {
            // Charge creation cost
            if (creationCostManager.isEnabled()) {
                creationCostManager.charge(p);
            }
            manager.addStation(name, railBlock.getLocation(), p.getUniqueId());
            p.sendMessage(msg().getWithPrefix("station-created", "STATION", name, "COST", creationCostManager.getCostDisplay()));
        } else {
            p.sendMessage(msg().getWithPrefix("wrong-block-position", "BLOCK", requiredBlock.name()));
        }
    }

    private void handleDeleteStation(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(msg().getWithPrefix("delete-usage"));
            return;
        }
        String name = args[1];

        if (!manager.stationExists(name)) {
            p.sendMessage(msg().getWithPrefix("station-not-found", "STATION", name));
            return;
        }

        UUID ownerId = manager.getStationOwnerId(name);

        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
            p.sendMessage(msg().getWithPrefix("not-owner-delete"));
            return;
        }

        manager.removeStation(name);
        p.sendMessage(msg().getWithPrefix("station-deleted", "STATION", name));
    }

    private void handleLinkStations(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(msg().getWithPrefix("link-usage"));
            return;
        }
        String nameA = args[1];
        String nameB = args[2];
        LinkType type = LinkType.PUBLIC;

        if (args.length > 3) {
            try {
                type = LinkType.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                p.sendMessage(msg().getWithPrefix("invalid-link-type"));
            }
        }

        if (!manager.stationExists(nameA) || !manager.stationExists(nameB)) {
            p.sendMessage(msg().getWithPrefix("link-stations-not-exist"));
            return;
        }

        UUID ownerAId = manager.getStationOwnerId(nameA);
        if (!p.hasPermission(ADMIN_PERM) && (ownerAId == null || !ownerAId.equals(p.getUniqueId()))) {
            p.sendMessage(msg().getWithPrefix("not-owner-link", "STATION", nameA));
            return;
        }

        if (manager.getLinkedStations(nameA).contains(nameB)) {
            p.sendMessage(msg().getWithPrefix("stations-already-linked", "STATION_A", nameA, "STATION_B", nameB));
            return;
        }

        UUID ownerBId = manager.getStationOwnerId(nameB);

        if (ownerBId != null && !ownerBId.equals(p.getUniqueId())) {
            manager.registerLinkRequest(p.getUniqueId(), nameA, nameB);

            p.sendMessage(msg().getWithPrefix("link-request-sent", "STATION_A", nameA, "STATION_B", nameB, "TYPE", type.name()));

            Player ownerB = Bukkit.getPlayer(ownerBId);
            if (ownerB != null && ownerB.isOnline()) {
                ownerB.sendMessage(msg().getWithPrefix("link-request-received", "PLAYER", p.getName(), "STATION_B", nameB, "STATION_A", nameA));
            }

        } else {
            manager.linkStations(nameA, nameB, type);
            p.sendMessage(msg().getWithPrefix("stations-linked", "STATION_A", nameA, "STATION_B", nameB, "TYPE", type.name()));
        }
    }

    private void handleUnlinkStations(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(msg().getWithPrefix("unlink-usage"));
            return;
        }
        String nameA = args[1];
        String nameB = args[2];

        if (!manager.stationExists(nameA) || !manager.stationExists(nameB)) {
            p.sendMessage(msg().getWithPrefix("unlink-stations-not-exist"));
            return;
        }

        UUID ownerAId = manager.getStationOwnerId(nameA);
        UUID ownerBId = manager.getStationOwnerId(nameB);

        boolean isOwner = (ownerAId != null && ownerAId.equals(p.getUniqueId())) || (ownerBId != null && ownerBId.equals(p.getUniqueId()));

        if (!p.hasPermission(ADMIN_PERM) && !isOwner) {
            p.sendMessage(msg().getWithPrefix("not-owner-unlink"));
            return;
        }

        if (!manager.getLinkedStations(nameA).contains(nameB)) {
            p.sendMessage(msg().getWithPrefix("stations-not-linked", "STATION_A", nameA, "STATION_B", nameB));
            return;
        }

        manager.unlinkStations(nameA, nameB);
        p.sendMessage(msg().getWithPrefix("stations-unlinked", "STATION_A", nameA, "STATION_B", nameB));
    }

    private void handleTransferOwner(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(msg().getWithPrefix("transfer-usage"));
            return;
        }
        String stationName = args[1];
        String targetName = args[2];

        if (!manager.stationExists(stationName)) {
            p.sendMessage(msg().getWithPrefix("station-not-found", "STATION", stationName));
            return;
        }

        UUID ownerId = manager.getStationOwnerId(stationName);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
            p.sendMessage(msg().getWithPrefix("not-owner-transfer"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            p.sendMessage(msg().getWithPrefix("target-not-online"));
            return;
        }

        manager.setStationOwner(stationName, target.getUniqueId());
        p.sendMessage(msg().getWithPrefix("ownership-transferred", "STATION", stationName, "PLAYER", target.getName()));
        target.sendMessage(msg().getWithPrefix("ownership-received", "STATION", stationName));
    }

    private void handleNearStations(Player p) {
        final int MAX_DISTANCE = 500;

        Map<String, Location> allStations = manager.getAllStationNames().stream()
                .filter(manager::stationExists)
                .collect(Collectors.toMap(name -> name, manager::getStationLocation));

        Map<String, AtomicInteger> stationDistances = allStations.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().getWorld().equals(p.getWorld()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new AtomicInteger((int) entry.getValue().distance(p.getLocation()))
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().get() <= MAX_DISTANCE)
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(AtomicInteger::get)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        if (stationDistances.isEmpty()) {
            p.sendMessage(msg().getWithPrefix("no-stations-nearby", "DISTANCE", String.valueOf(MAX_DISTANCE)));
            return;
        }

        p.sendMessage(msg().get("nearby-header", "DISTANCE", String.valueOf(MAX_DISTANCE)));
        stationDistances.forEach((name, distance) -> {
            ChatColor color = distance.get() < 50 ? ChatColor.GREEN : (distance.get() < 200 ? ChatColor.YELLOW : ChatColor.WHITE);
            p.sendMessage(color + name + ChatColor.GRAY + " (" + distance.get() + "m)");
        });
    }

    private void handleLinkAccept(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(msg().getWithPrefix("accept-usage"));
            return;
        }
        String nameA = args[1];
        String nameB = args[2];

        UUID ownerId = manager.getStationOwnerId(nameB);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
            p.sendMessage(msg().getWithPrefix("not-owner-accept", "STATION", nameB));
            return;
        }

        UUID requesterId = manager.getPendingRequesterId(nameB);
        String initiatingStation = manager.getPendingInitiatingStation(nameB);

        if (requesterId == null || !initiatingStation.equals(nameA)) {
            p.sendMessage(msg().getWithPrefix("no-pending-request", "STATION_A", nameA, "STATION_B", nameB));
            return;
        }

        manager.linkStations(nameA, nameB, LinkType.PUBLIC);
        manager.clearLinkRequest(nameB);
        p.sendMessage(msg().getWithPrefix("link-accepted", "STATION", nameA));

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(msg().getWithPrefix("link-request-accepted", "STATION_A", nameA, "STATION_B", nameB));
        }
    }

    private void handleLinkReject(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(msg().getWithPrefix("reject-usage"));
            return;
        }
        String nameA = args[1];
        String nameB = args[2];

        UUID ownerId = manager.getStationOwnerId(nameB);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
            p.sendMessage(msg().getWithPrefix("not-owner-reject", "STATION", nameB));
            return;
        }

        UUID requesterId = manager.getPendingRequesterId(nameB);
        String initiatingStation = manager.getPendingInitiatingStation(nameB);

        if (requesterId == null || !initiatingStation.equals(nameA)) {
            p.sendMessage(msg().getWithPrefix("no-pending-request", "STATION_A", nameA, "STATION_B", nameB));
            return;
        }

        manager.clearLinkRequest(nameB);
        p.sendMessage(msg().getWithPrefix("link-rejected", "STATION", nameA));

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(msg().getWithPrefix("link-request-rejected", "STATION_A", nameA, "STATION_B", nameB));
        }
    }

    private void handleSetBlock(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERM)) {
            p.sendMessage(msg().getWithPrefix("no-permission"));
            return;
        }

        if (args.length < 2) {
            p.sendMessage(msg().getWithPrefix("block-usage"));
            return;
        }
        String blockName = args[1].toUpperCase();

        if (Material.matchMaterial(blockName) == null) {
            p.sendMessage(msg().getWithPrefix("invalid-material", "MATERIAL", blockName));
            return;
        }

        plugin.setCreationBlockType(blockName);
        p.sendMessage(msg().getWithPrefix("block-set", "BLOCK", blockName));
    }

    private void handleSetMessage(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(msg().getWithPrefix("message-usage"));
            return;
        }
        String name = args[1];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        if (!manager.stationExists(name)) {
            p.sendMessage(msg().getWithPrefix("message-station-not-found", "STATION", name));
            return;
        }

        UUID ownerId = manager.getStationOwnerId(name);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
            p.sendMessage(msg().getWithPrefix("not-owner-message"));
            return;
        }

        manager.setStationMessage(name, message);
        p.sendMessage(msg().getWithPrefix("message-updated", "STATION", name, "MESSAGE", ChatColor.translateAlternateColorCodes('&', message)));
    }

    private void handleReload(Player p) {
        if (!p.hasPermission(ADMIN_PERM)) {
            p.sendMessage(msg().getWithPrefix("no-permission"));
            return;
        }

        // Reload config
        plugin.reloadConfig();

        // Reload messages
        SimpleTrains.getMessages().reload();

        // Reload sound manager
        plugin.getSoundManager().loadConfig();

        // Reload travel cost manager
        plugin.getTravelCostManager().loadConfig();

        // Reload creation cost manager
        plugin.getCreationCostManager().loadConfig();

        p.sendMessage(msg().getWithPrefix("config-reloaded"));
    }

    private void sendHelpMessage(Player p) {
        String creationCost = plugin.getCreationCostManager().getCostDisplay();
        p.sendMessage(msg().get("help-header"));
        p.sendMessage(msg().get("help-set", "COST", creationCost));
        p.sendMessage(msg().get("help-delete"));
        p.sendMessage(msg().get("help-link"));
        p.sendMessage(msg().get("help-transfer"));
        p.sendMessage(msg().get("help-near"));
        p.sendMessage(msg().get("help-gui"));
        p.sendMessage(msg().get("help-accept-reject"));
        p.sendMessage(msg().get("help-message"));
        p.sendMessage(msg().get("help-block"));
        if (p.hasPermission(ADMIN_PERM)) {
            p.sendMessage(msg().get("help-reload"));
        }
    }
}
