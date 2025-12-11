package com.andrestube.simpletrains.utils;

import com.andrestube.simpletrains.SimpleTrains;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;

public class StationManager {
    
    public enum LinkType {
        PUBLIC, 
        PRIVATE 
    }
    
    private final SimpleTrains plugin;
    
    private final Map<String, Location> stationLocations = new HashMap<>();
    private final Map<String, UUID> stationOwners = new HashMap<>();
    private final Map<String, String> stationMessages = new HashMap<>();
    
    private final Map<String, Map<String, LinkType>> stationLinks = new HashMap<>(); 
    
    private final Map<String, UUID> pendingRequests = new HashMap<>();
    private final Map<String, String> requestStations = new HashMap<>();
    
    private static final String STATION_PATH = "stations";
    private static final String REQUEST_PATH = "requests";

    public StationManager(SimpleTrains plugin) {
        this.plugin = plugin;
        loadAllData();
    }
    
    // --- Data Persistence ---
    
    public void loadAllData() {
        FileConfiguration config = plugin.getConfig();
        
        // 1. Load Stations
        ConfigurationSection stationsSection = config.getConfigurationSection(STATION_PATH);
        if (stationsSection != null) {
            for (String name : stationsSection.getKeys(false)) {
                String locString = stationsSection.getString(name + ".location");
                String ownerUuidString = stationsSection.getString(name + ".owner");
                String message = stationsSection.getString(name + ".message", "Welcome to station " + name + "!");
                
                ConfigurationSection linksSection = config.getConfigurationSection(STATION_PATH + "." + name + ".links");
                Map<String, LinkType> linksMap = new HashMap<>();
                if (linksSection != null) {
                    for (String destination : linksSection.getKeys(false)) {
                        String typeString = linksSection.getString(destination, LinkType.PUBLIC.name()); 
                        try {
                            linksMap.put(destination, LinkType.valueOf(typeString.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            linksMap.put(destination, LinkType.PUBLIC);
                        }
                    }
                } 
                
                if (locString != null) {
                    Location loc = LocationSerializer.deserialize(locString);
                    if (loc != null) {
                        stationLocations.put(name, loc);
                        stationMessages.put(name, message);
                        if (ownerUuidString != null) {
                            try {
                                stationOwners.put(name, UUID.fromString(ownerUuidString));
                            } catch (IllegalArgumentException ignored) {}
                        }
                        stationLinks.put(name, linksMap);
                    } else {
                        plugin.getLogger().warning("Skipping station " + name + ": Location failed to deserialize.");
                    }
                }
            }
        }
        
        // 2. Load Pending Requests
        ConfigurationSection requestSection = config.getConfigurationSection(REQUEST_PATH);
        if (requestSection != null) {
            for (String receivingStation : requestSection.getKeys(false)) {
                String requesterUuidString = requestSection.getString(receivingStation + ".requester_uuid");
                String initiatingStation = requestSection.getString(receivingStation + ".initiating_station");
                
                if (requesterUuidString != null && initiatingStation != null) {
                    try {
                        UUID requesterId = UUID.fromString(requesterUuidString);
                        pendingRequests.put(receivingStation, requesterId);
                        requestStations.put(receivingStation, initiatingStation);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        plugin.getLogger().info("Loaded " + stationLocations.size() + " stations and " + pendingRequests.size() + " pending link requests.");
    }

    public void saveData() {
        FileConfiguration config = plugin.getConfig();
        config.set(STATION_PATH, null);
        config.set(REQUEST_PATH, null);

        // 1. Save Stations
        for (Map.Entry<String, Location> entry : stationLocations.entrySet()) {
            String name = entry.getKey();
            String path = STATION_PATH + "." + name;
            
            // Use the dedicated serializer
            String serializedLoc = LocationSerializer.serialize(entry.getValue());
            if (serializedLoc != null) {
                config.set(path + ".location", serializedLoc);
            } else {
                plugin.getLogger().severe(ChatColor.RED + "Failed to serialize location for station: " + name);
                continue; // Skip saving this station's data if location fails
            }
            
            config.set(path + ".message", stationMessages.getOrDefault(name, "Welcome to station " + name + "!"));

            UUID ownerId = stationOwners.get(name);
            if (ownerId != null) {
                config.set(path + ".owner", ownerId.toString());
            }

            Map<String, LinkType> linksMap = stationLinks.getOrDefault(name, Collections.emptyMap());
            for (Map.Entry<String, LinkType> linkEntry : linksMap.entrySet()) {
                 config.set(path + ".links." + linkEntry.getKey(), linkEntry.getValue().name());
            }
        }

        // 2. Save Pending Requests
        for (Map.Entry<String, UUID> entry : pendingRequests.entrySet()) {
            String receivingStation = entry.getKey();
            String initiatingStation = requestStations.get(receivingStation);

            config.set(REQUEST_PATH + "." + receivingStation + ".requester_uuid", entry.getValue().toString());
            config.set(REQUEST_PATH + "." + receivingStation + ".initiating_station", initiatingStation);
        }

        plugin.saveConfig();
        plugin.getLogger().info("Saved " + stationLocations.size() + " stations and " + pendingRequests.size() + " pending link requests.");
    }
    
    // --- Station and Owner Management ---
    
    public void setStationOwner(String name, UUID newOwnerId) {
        if (stationOwners.containsKey(name)) {
            stationOwners.put(name, newOwnerId);
            saveData();
        }
    }
    
    public void addStation(String name, Location location, UUID ownerId) {
        stationLocations.put(name, location);
        stationOwners.put(name, ownerId);
        stationMessages.put(name, "Welcome to station " + name + "!");
        stationLinks.put(name, new HashMap<>()); 
        saveData();
    }
    
    public void removeStation(String name) {
        // Remove incoming and outgoing links referencing this station
        for (Map.Entry<String, Map<String, LinkType>> entry : stationLinks.entrySet()) {
            entry.getValue().remove(name);
        }

        stationLocations.remove(name);
        stationOwners.remove(name);
        stationMessages.remove(name);
        stationLinks.remove(name);
        pendingRequests.remove(name); 
        requestStations.remove(name);
        saveData();
    }
    
    // Core check if station exists
    public boolean stationExists(String name) {
        return stationLocations.containsKey(name);
    }
    
    // Redundant wrapper to satisfy GuiListener calls for isStation()
    public boolean isStation(String name) {
        return stationExists(name);
    }
    
    public Location getStationLocation(String name) {
        return stationLocations.get(name);
    }

    public String getStationName(Location loc) {
        for (Map.Entry<String, Location> entry : stationLocations.entrySet()) {
            Location stationLoc = entry.getValue();
            if (stationLoc.getWorld().equals(loc.getWorld()) && 
                stationLoc.getBlockX() == loc.getBlockX() && 
                stationLoc.getBlockY() == loc.getBlockY() && 
                stationLoc.getBlockZ() == loc.getBlockZ()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public UUID getStationOwnerId(String name) {
        return stationOwners.get(name);
    }

    public void setStationMessage(String name, String message) {
        stationMessages.put(name, message);
        saveData();
    }

    public String getStationMessage(String name) {
        return stationMessages.getOrDefault(name, "Welcome to station " + name + "!");
    }
    
    public Set<String> getAllStationNames() {
        return stationLocations.keySet();
    }

    // --- REQUIRED METHODS FOR GUI & COMMANDS ---
    
    public boolean isOwner(String stationName, UUID playerId) {
        UUID ownerId = stationOwners.get(stationName);
        return ownerId != null && ownerId.equals(playerId);
    }
    
    public List<String> getOwnedStations(UUID playerId) {
        return stationOwners.entrySet().stream()
                .filter(entry -> playerId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // --- Linking Methods (Fixed unlinkStations) ---

    public Map<String, LinkType> getStationLinksMap(String name) {
        return stationLinks.getOrDefault(name, Collections.emptyMap());
    }

    public List<String> getLinkedStations(String name) {
        return getStationLinksMap(name).keySet().stream().sorted().collect(Collectors.toList());
    }

    /**
     * Creates a bidirectional link between two stations.
     */
    public void linkStations(String nameA, String nameB, LinkType type) {
        stationLinks.computeIfAbsent(nameA, k -> new HashMap<>()).put(nameB, type);
        stationLinks.computeIfAbsent(nameB, k -> new HashMap<>()).put(nameA, type); 
        saveData();
    }
    
    public void linkStations(String nameA, String nameB) {
        linkStations(nameA, nameB, LinkType.PUBLIC);
    }

    /**
     * Removes the bidirectional link between two stations. (FIXED)
     */
    public void unlinkStations(String nameA, String nameB) {
        // Retrieve the map for nameA and remove the entry for nameB
        stationLinks.getOrDefault(nameA, new HashMap<>()).remove(nameB);
        
        // Retrieve the map for nameB and remove the entry for nameA
        stationLinks.getOrDefault(nameB, new HashMap<>()).remove(nameA);
        
        saveData();
    }

    // --- Link Request Methods ---

    public void registerLinkRequest(UUID requesterId, String fromStation, String toStation) {
        pendingRequests.put(toStation, requesterId);
        requestStations.put(toStation, fromStation);
        saveData();
    }

    public boolean isRequestPending(String receivingStation, String initiatingStation) {
        return pendingRequests.containsKey(receivingStation) && 
               initiatingStation.equals(requestStations.get(receivingStation));
    }

    public UUID getPendingRequesterId(String receivingStation) {
        return pendingRequests.get(receivingStation);
    }
    
    public String getPendingInitiatingStation(String receivingStation) {
        return requestStations.get(receivingStation);
    }

    public void clearLinkRequest(String receivingStation) {
        pendingRequests.remove(receivingStation);
        requestStations.remove(receivingStation);
        saveData();
    }
}