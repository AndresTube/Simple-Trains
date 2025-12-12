package com.andrestube.simpletrains.listeners;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.gui.DestinationGui;
import com.andrestube.simpletrains.utils.Messages;
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
    private static final double TELEPORT_IMPULSE = 0.1;
    private static final long COOLDOWN_MS = 10000; // 10 seconds
    private static final int PLAYER_DETECTION_RADIUS = 20;

    public static final String START_STATION_META = "start_station";

    public TrainListener(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private static Messages msg() {
        return SimpleTrains.getMessages();
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }

        Minecart minecart = (Minecart) event.getVehicle();

        if (!minecart.hasMetadata(TagListener.ST_ELIGIBLE_TAG)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

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

        if (currentStationName != null && previousStationName == null) {

            Player responsiblePlayer = findResponsiblePlayer(minecart);

            if (responsiblePlayer == null) {
                minecart.setVelocity(new Vector(0, minecart.getVelocity().getY(), 0));
                return;
            }

            minecart.setVelocity(new Vector(0, minecart.getVelocity().getY(), 0));

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

            if (accessibleDestinations.isEmpty()) {
                responsiblePlayer.sendMessage(msg().getWithPrefix("no-accessible-destinations", "STATION", currentStationName));
                return;
            }

            if (accessibleDestinations.size() == 1) {
                String destinationName = accessibleDestinations.get(0);
                teleportMinecart(minecart, responsiblePlayer, destinationName);
            } else {
                minecart.setMetadata(START_STATION_META, new FixedMetadataValue(plugin, currentStationName));

                responsiblePlayer.openInventory(
                        DestinationGui.create(manager, minecart, currentStationName, accessibleDestinations)
                );
            }

        }
    }

    private Player findResponsiblePlayer(Minecart minecart) {
        for (Entity entity : minecart.getPassengers()) {
            if (entity instanceof Player) {
                return (Player) entity;
            }
        }
        double radius = (double) PLAYER_DETECTION_RADIUS;
        for (Entity entity : minecart.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                return (Player) entity;
            }
        }
        return null;
    }

    public static void teleportMinecart(Minecart minecart, Player player, String destinationName) {
        SimpleTrains plugin = SimpleTrains.getInstance();
        StationManager manager = plugin.getStationManager();
        Location destLoc = manager.getStationLocation(destinationName);

        if (destLoc == null) {
            player.sendMessage(msg().getWithPrefix("destination-not-found"));
            return;
        }

        minecart.setMetadata("isTeleporting", new FixedMetadataValue(plugin, System.currentTimeMillis()));

        Location targetLoc = destLoc.clone().add(0.5, 0.1, 0.5);

        boolean wasPassenger = minecart.getPassengers().contains(player);

        if (wasPassenger) {
            minecart.removePassenger(player);
        }

        minecart.setVelocity(new Vector(0, 0, 0));
        if (!minecart.teleport(targetLoc)) {
            player.sendMessage(msg().getWithPrefix("teleport-failed"));
            if (wasPassenger) {
                minecart.addPassenger(player);
            }
            return;
        }

        if (wasPassenger) {
            player.teleport(targetLoc);
            minecart.addPassenger(player);
        }

        minecart.setVelocity(minecart.getLocation().getDirection().multiply(TELEPORT_IMPULSE));

        String welcomeMessage = manager.getStationMessage(destinationName);
        player.sendMessage(msg().getWithPrefix("warp-confirm", "DESTINATION", destinationName));

        String stationWelcome = msg().get("station-welcome", "STATION", destinationName, "MESSAGE", ChatColor.translateAlternateColorCodes('&', welcomeMessage));
        player.sendMessage(stationWelcome);
    }
}
