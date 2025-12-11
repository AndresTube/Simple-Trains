// src/main/java/com/andrestube/simpletrains/listeners/TrainListener.java

package com.andrestube.simpletrains.listeners;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.gui.DestinationGui;
import com.andrestube.simpletrains.utils.StationManager;
import com.andrestube.simpletrains.utils.StationManager.LinkType;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TrainListener implements Listener {

    private final SimpleTrains plugin;
    private final StationManager manager;
    private static final String PREFIX = ChatColor.GOLD + "[SimpleTrains] " + ChatColor.RESET;
    private static final double TELEPORT_IMPULSE = 0.1; 
    private static final long COOLDOWN_MS = 10000; // 10 seconds
    private static final int PLAYER_DETECTION_RADIUS = 20;
    
    public static final String START_STATION_META = "start_station"; 

    public TrainListener(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }
        
        Minecart minecart = (Minecart) event.getVehicle();
        
        // --- TAGGING CHECK MODIFIED: Use the persistent tag from TagListener ---
        if (!minecart.hasMetadata(TagListener.ST_ELIGIBLE_TAG)) { 
            return; 
        }
        // --- END TAGGING CHECK ---
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Ignore very small movements (no block change)
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        // Check teleport cooldown
        if (minecart.hasMetadata("isTeleporting")) {
            long teleportTime = minecart.getMetadata("isTeleporting").get(0).asLong();
            if (System.currentTimeMillis() - teleportTime < COOLDOWN_MS) { 
                 return; 
            } else {
                 minecart.removeMetadata("isTeleporting", plugin);
            }
        }
        
        String currentStationName = manager.getStationName(to);
        String previousStationName = manager.getStationName(from);
        
        // --- Fix for Message/GUI on ARRIVAL ONLY ---
        // Trigger only if moving from a non-station block (previousStationName == null) to a station block (currentStationName != null).
        if (currentStationName != null && previousStationName == null) {
            
            Player responsiblePlayer = findResponsiblePlayer(minecart);

            if (responsiblePlayer == null) {
                 minecart.setVelocity(new Vector(0, minecart.getVelocity().getY(), 0));
                 return; 
            }
            
            // Stop the minecart immediately upon station detection
            minecart.setVelocity(new Vector(0, minecart.getVelocity().getY(), 0)); 
            
            // --- Feature 6: Filter Destinations (Existing Logic) ---
            UUID passengerId = responsiblePlayer.getUniqueId();
            UUID ownerId = manager.getStationOwnerId(currentStationName);

            List<String> accessibleDestinations = manager.getStationLinksMap(currentStationName).entrySet().stream()
                .filter(entry -> {
                    String destinationName = entry.getKey();
                    LinkType linkType = entry.getValue();
                    
                    if (responsiblePlayer.hasPermission("simpletrains.admin") || 
                        (ownerId != null && ownerId.equals(passengerId))) {
                        return true;
                    }
                    
                    if (linkType == LinkType.PUBLIC) {
                        return true;
                    }
                    
                    UUID destOwnerId = manager.getStationOwnerId(destinationName);
                    return destOwnerId != null && destOwnerId.equals(passengerId);
                })
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());
            // --- End Feature 6 Filter ---

            if (accessibleDestinations.isEmpty()) {
                responsiblePlayer.sendMessage(PREFIX + ChatColor.YELLOW + "This station (" + currentStationName + ") has no accessible destinations for you.");
                return;
            }

            // 2. Open GUI or Auto-warp logic
            if (accessibleDestinations.size() == 1) {
                String destinationName = accessibleDestinations.get(0);
                teleportMinecart(minecart, responsiblePlayer, destinationName);
            } else {
                minecart.setMetadata(START_STATION_META, new FixedMetadataValue(plugin, currentStationName));
                
                responsiblePlayer.openInventory(
                    DestinationGui.create(manager, minecart, currentStationName, accessibleDestinations)
                );
            }
            
        } else {
            // Either moving within a station (current!=null, previous!=null) or moving normally/leaving a station. Do nothing.
            return;
        }
    }
    
    /**
     * Finds the nearest player who is controlling the train (passenger preferred).
     */
    private Player findResponsiblePlayer(Minecart minecart) {
        // 1. Check for passengers first (most reliable)
        for (Entity entity : minecart.getPassengers()) {
             if (entity instanceof Player) {
                return (Player) entity;
            }
        }
        // 2. Fallback: check for players nearby (within the increased radius)
        double radius = (double) PLAYER_DETECTION_RADIUS;
        for (Entity entity : minecart.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                return (Player) entity;
            }
        }
        return null;
    }
    
    /**
     * Teleports the minecart and the controlling player (if riding) to the destination station.
     */
    public static void teleportMinecart(Minecart minecart, Player player, String destinationName) {
        SimpleTrains plugin = SimpleTrains.getInstance();
        StationManager manager = plugin.getStationManager();
        Location destLoc = manager.getStationLocation(destinationName);
        
        if (destLoc == null) {
            player.sendMessage(ChatColor.RED + "Error: Destination station location not found!");
            return;
        }
        
        minecart.setMetadata("isTeleporting", new FixedMetadataValue(plugin, System.currentTimeMillis()));

        Location targetLoc = destLoc.clone().add(0.5, 0.1, 0.5); 
        
        boolean wasPassenger = minecart.getPassengers().contains(player);
        
        // 1. Temporarily remove the player if they are a passenger.
        if (wasPassenger) {
            minecart.removePassenger(player);
        }
        
        // 2. Teleport Minecart
        minecart.setVelocity(new Vector(0, 0, 0)); 
        if (!minecart.teleport(targetLoc)) {
            player.sendMessage(ChatColor.RED + "Minecart teleport failed on the server side.");
            if (wasPassenger) {
                minecart.addPassenger(player); 
            }
            return;
        }
        
        // 3. Teleport Player and Re-add as Passenger ONLY IF they were riding.
        if (wasPassenger) {
             player.teleport(targetLoc);
             minecart.addPassenger(player);
        }
        
        // 4. Give a slight push
        minecart.setVelocity(minecart.getLocation().getDirection().multiply(TELEPORT_IMPULSE)); 

        // 5. Send confirmation message
        String welcomeMessage = manager.getStationMessage(destinationName);
        player.sendMessage(PREFIX + ChatColor.GREEN + "Warping to " + destinationName + "...");
        player.sendMessage(ChatColor.GOLD + ">> " + ChatColor.translateAlternateColorCodes('&', welcomeMessage));
    }
}