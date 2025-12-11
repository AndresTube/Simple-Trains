// src/main/java/com/andrestube/simpletrains/commands/TrainCommand.java

package com.andrestube.simpletrains.commands;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.gui.MainGui;
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
import java.util.List;

public class TrainCommand implements CommandExecutor {

    private final SimpleTrains plugin;
    private final StationManager manager;
    private static final String PREFIX = ChatColor.GOLD + "[SimpleTrains] " + ChatColor.RESET;
    private static final String ADMIN_PERM = "simpletrains.admin";

    public TrainCommand(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + "Only players can use train commands.");
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
            case "help":
                sendHelpMessage(p);
                break;
            default:
                p.sendMessage(PREFIX + ChatColor.RED + "Unknown command. Use /train help.");
        }

        return true;
    }

    private void handleSetStation(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train set <stationName>");
            return;
        }
        String name = args[1];
        
        if (manager.stationExists(name)) { 
            p.sendMessage(PREFIX + ChatColor.RED + String.format("Station '%s' already exists!", name));
            return;
        }
        
        // --- XP COST CHECK ---
        int cost = plugin.getConfig().getInt("settings.creation_xp_cost", 5);
        if (p.getLevel() < cost) {
            p.sendMessage(PREFIX + ChatColor.RED + String.format("You need %d XP levels to create a station. You currently have %d.", cost, p.getLevel()));
            return;
        }
        
        Block railBlock = p.getLocation().getBlock(); 
        Block baseBlock = railBlock.getRelative(0, -1, 0);

        Material requiredBlock = Material.matchMaterial(plugin.getCreationBlockType());
        if (requiredBlock == null) {
            p.sendMessage(PREFIX + ChatColor.RED + "Configuration Error: Invalid creation block type.");
            return;
        }
        
        if (railBlock.getType().name().contains("RAIL") && baseBlock.getType() == requiredBlock) {
            
            // --- XP DEDUCTION ---
            p.setLevel(p.getLevel() - cost);

            manager.addStation(name, railBlock.getLocation(), p.getUniqueId()); 
            p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Station '%s' created successfully! (-%d XP)", name, cost));
        } else {
            p.sendMessage(PREFIX + ChatColor.RED + String.format("You must be standing on a RAIL block, with a %s block directly beneath it.", requiredBlock.name()));
        }
    }
    
    private void handleDeleteStation(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train delete <stationName>");
            return;
        }
        String name = args[1];
        
        if (!manager.stationExists(name)) { 
            p.sendMessage(PREFIX + ChatColor.RED + String.format("Station '%s' does not exist.", name));
            return;
        }
        
        UUID ownerId = manager.getStationOwnerId(name);
        
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
             p.sendMessage(PREFIX + ChatColor.RED + "You must be the owner of this station or an admin to delete it.");
             return;
        }

        manager.removeStation(name);
        p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Station '%s' and all associated links deleted.", name));
    }

    private void handleLinkStations(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train link <stationA> <stationB> [private/public]");
            return;
        }
        String nameA = args[1];
        String nameB = args[2];
        LinkType type = LinkType.PUBLIC; 

        if (args.length > 3) {
            try {
                type = LinkType.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                p.sendMessage(PREFIX + ChatColor.RED + "Invalid link type. Use 'private' or 'public'. Defaulting to Public.");
            }
        }
        
        if (!manager.stationExists(nameA) || !manager.stationExists(nameB)) {
             p.sendMessage(PREFIX + ChatColor.RED + "Both stations must exist to create a link.");
             return;
        }
        
        UUID ownerAId = manager.getStationOwnerId(nameA);
        if (!p.hasPermission(ADMIN_PERM) && (ownerAId == null || !ownerAId.equals(p.getUniqueId()))) {
             p.sendMessage(PREFIX + ChatColor.RED + String.format("You must own station '%s' or be an admin to initiate a link.", nameA));
             return;
        }

        if (manager.getLinkedStations(nameA).contains(nameB)) {
             p.sendMessage(PREFIX + ChatColor.YELLOW + String.format("Stations '%s' and '%s' are already linked.", nameA, nameB));
             return;
        }
        
        UUID ownerBId = manager.getStationOwnerId(nameB);
        
        if (ownerBId != null && !ownerBId.equals(p.getUniqueId())) {
            manager.registerLinkRequest(p.getUniqueId(), nameA, nameB);
            
            p.sendMessage(PREFIX + ChatColor.YELLOW + String.format("Link request sent for '%s' to owner of '%s'. Link Type: %s.", nameA, nameB, type.name()));
            
            Player ownerB = Bukkit.getPlayer(ownerBId);
            if (ownerB != null && ownerB.isOnline()) {
                 ownerB.sendMessage(PREFIX + ChatColor.AQUA + String.format("Link request received from %s to link your station '%s' with '%s'. Use /train accept %s %s.", p.getName(), nameB, nameA, nameA, nameB));
            }
            
        } else {
            manager.linkStations(nameA, nameB, type);
            p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Stations '%s' and '%s' linked successfully as %s!", nameA, nameB, type.name()));
        }
    }
    
    private void handleUnlinkStations(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train unlink <stationA> <stationB>");
            return;
        }
        String nameA = args[1];
        String nameB = args[2];
        
        if (!manager.stationExists(nameA) || !manager.stationExists(nameB)) {
             p.sendMessage(PREFIX + ChatColor.RED + "Both stations must exist.");
             return;
        }

        UUID ownerAId = manager.getStationOwnerId(nameA);
        UUID ownerBId = manager.getStationOwnerId(nameB);

        boolean isOwner = (ownerAId != null && ownerAId.equals(p.getUniqueId())) || (ownerBId != null && ownerBId.equals(p.getUniqueId()));
        
        if (!p.hasPermission(ADMIN_PERM) && !isOwner) {
             p.sendMessage(PREFIX + ChatColor.RED + "You must own one of the stations or be an admin to unlink them.");
             return;
        }

        if (!manager.getLinkedStations(nameA).contains(nameB)) {
             p.sendMessage(PREFIX + ChatColor.YELLOW + String.format("Stations '%s' and '%s' are not linked.", nameA, nameB));
             return;
        }

        manager.unlinkStations(nameA, nameB);
        p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Stations '%s' and '%s' unlinked.", nameA, nameB));
    }
    
    private void handleTransferOwner(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train transfer <stationName> <targetPlayer>");
            return;
        }
        String stationName = args[1];
        String targetName = args[2];

        if (!manager.stationExists(stationName)) {
            p.sendMessage(PREFIX + ChatColor.RED + String.format("Station '%s' does not exist.", stationName));
            return;
        }
        
        UUID ownerId = manager.getStationOwnerId(stationName);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
             p.sendMessage(PREFIX + ChatColor.RED + "You must be the station owner or an admin to transfer ownership.");
             return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            p.sendMessage(PREFIX + ChatColor.RED + "Target player must be online.");
            return;
        }

        manager.setStationOwner(stationName, target.getUniqueId());
        p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Ownership of '%s' transferred to %s.", stationName, target.getName()));
        target.sendMessage(PREFIX + ChatColor.AQUA + String.format("You are now the owner of station '%s'.", stationName));
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
            p.sendMessage(PREFIX + ChatColor.YELLOW + "No stations found within " + MAX_DISTANCE + " blocks in this world.");
            return;
        }
        
        p.sendMessage(ChatColor.DARK_GRAY + "--- Stations Near You (Max " + MAX_DISTANCE + "m) ---");
        stationDistances.forEach((name, distance) -> {
            ChatColor color = distance.get() < 50 ? ChatColor.GREEN : (distance.get() < 200 ? ChatColor.YELLOW : ChatColor.WHITE);
            p.sendMessage(color + name + ChatColor.GRAY + " (" + distance.get() + "m)");
        });
    }

    private void handleLinkAccept(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train accept <initiatingStation> <receivingStation>");
            return;
        }
        String nameA = args[1]; // Initiating Station (Requester's)
        String nameB = args[2]; // Receiving Station (Your Station)

        UUID ownerId = manager.getStationOwnerId(nameB);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
             p.sendMessage(PREFIX + ChatColor.RED + "You must own station " + nameB + " or be an admin to accept links.");
             return;
        }
        
        UUID requesterId = manager.getPendingRequesterId(nameB);
        String initiatingStation = manager.getPendingInitiatingStation(nameB);

        if (requesterId == null || !initiatingStation.equals(nameA)) {
            p.sendMessage(PREFIX + ChatColor.RED + String.format("No pending link request found from %s to %s.", nameA, nameB));
            return;
        }
        
        manager.linkStations(nameA, nameB, LinkType.PUBLIC); 
        manager.clearLinkRequest(nameB);
        p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Link to '%s' accepted! Stations are now linked as PUBLIC.", nameA));
        
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null && requester.isOnline()) {
             requester.sendMessage(PREFIX + ChatColor.AQUA + String.format("Your link request from '%s' to '%s' has been accepted!", nameA, nameB));
        }
    }
    
    private void handleLinkReject(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train reject <initiatingStation> <receivingStation>");
            return;
        }
        String nameA = args[1]; // Initiating Station (Requester's)
        String nameB = args[2]; // Receiving Station (Your Station)

        UUID ownerId = manager.getStationOwnerId(nameB);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
             p.sendMessage(PREFIX + ChatColor.RED + "You must own station " + nameB + " or be an admin to reject links.");
             return;
        }
        
        UUID requesterId = manager.getPendingRequesterId(nameB);
        String initiatingStation = manager.getPendingInitiatingStation(nameB);

        if (requesterId == null || !initiatingStation.equals(nameA)) {
            p.sendMessage(PREFIX + ChatColor.RED + String.format("No pending link request found from %s to %s.", nameA, nameB));
            return;
        }

        manager.clearLinkRequest(nameB);
        p.sendMessage(PREFIX + ChatColor.RED + String.format("Link request from '%s' rejected.", nameA));
        
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null && requester.isOnline()) {
             requester.sendMessage(PREFIX + ChatColor.RED + String.format("Your link request from '%s' to '%s' has been rejected.", nameA, nameB));
        }
    }

    private void handleSetBlock(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERM)) {
             p.sendMessage(PREFIX + ChatColor.RED + "You do not have permission for this command.");
             return;
        }

        if (args.length < 2) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train block <MaterialName>");
            return;
        }
        String blockName = args[1].toUpperCase();
        
        if (Material.matchMaterial(blockName) == null) {
            p.sendMessage(PREFIX + ChatColor.RED + String.format("Material '%s' is not recognized.", blockName));
            return;
        }

        plugin.setCreationBlockType(blockName);
        p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Station creation block set to %s.", blockName));
    }
    
    private void handleSetMessage(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PREFIX + ChatColor.RED + "Usage: /train message <stationName> <message>");
            return;
        }
        String name = args[1];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        
        if (!manager.stationExists(name)) { 
            p.sendMessage(PREFIX + ChatColor.RED + String.format("Station '%s' does not exist!", name));
            return;
        }

        UUID ownerId = manager.getStationOwnerId(name);
        if (!p.hasPermission(ADMIN_PERM) && (ownerId == null || !ownerId.equals(p.getUniqueId()))) {
             p.sendMessage(PREFIX + ChatColor.RED + "You must be the station owner or an admin to change the message.");
             return;
        }

        manager.setStationMessage(name, message);
        p.sendMessage(PREFIX + ChatColor.GREEN + String.format("Message for '%s' updated. Preview: %s", name, ChatColor.translateAlternateColorCodes('&', message)));
    }

    private void sendHelpMessage(Player p) {
        int xpCost = plugin.getConfig().getInt("settings.creation_xp_cost", 5);
        p.sendMessage(ChatColor.DARK_GRAY + "--- SimpleTrains Help ---");
        p.sendMessage(ChatColor.YELLOW + "/train set <name> " + ChatColor.GRAY + "-> Creates a station (-" + xpCost + " XP).");
        p.sendMessage(ChatColor.YELLOW + "/train delete <name> " + ChatColor.GRAY + "-> Deletes a station.");
        p.sendMessage(ChatColor.YELLOW + "/train link <A> <B> [type] " + ChatColor.GRAY + "-> Sends link request (Type: public/private).");
        p.sendMessage(ChatColor.YELLOW + "/train transfer <st> <player> " + ChatColor.GRAY + "-> Transfers ownership (Owner/Admin).");
        p.sendMessage(ChatColor.YELLOW + "/train near " + ChatColor.GRAY + "-> Lists nearby stations.");
        p.sendMessage(ChatColor.YELLOW + "/train gui " + ChatColor.GRAY + "-> Opens the station list GUI.");
        p.sendMessage(ChatColor.YELLOW + "/train accept/reject <A> <B> " + ChatColor.GRAY + "-> Accept/Reject a link request.");
        p.sendMessage(ChatColor.YELLOW + "/train message <st> <&msg> " + ChatColor.GRAY + "-> Sets station welcome message.");
        p.sendMessage(ChatColor.YELLOW + "/train block <mat> " + ChatColor.GRAY + "-> (Admin) Sets creation block.");
    }
}