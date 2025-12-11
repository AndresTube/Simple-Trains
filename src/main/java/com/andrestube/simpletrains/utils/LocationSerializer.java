// src/main/java/com/andrestube/simpletrains/utils/LocationSerializer.java

package com.andrestube.simpletrains.utils;

import com.andrestube.simpletrains.SimpleTrains;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSerializer {

    // Format: worldName,x,y,z,yaw,pitch
    private static final String SEPARATOR = ",";

    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location.getWorld().getName() + SEPARATOR +
               location.getX() + SEPARATOR +
               location.getY() + SEPARATOR +
               location.getZ() + SEPARATOR +
               location.getYaw() + SEPARATOR +
               location.getPitch();
    }

    public static Location deserialize(String serializedLocation) {
        if (serializedLocation == null) {
            return null;
        }
        
        try {
            String[] parts = serializedLocation.split(SEPARATOR);
            if (parts.length < 6) {
                SimpleTrains.getInstance().getLogger().warning("Invalid serialized location format: " + serializedLocation);
                return null;
            }
            
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                // If world is null, the location is invalid/world unloaded
                SimpleTrains.getInstance().getLogger().warning("World not found for location: " + parts[0]);
                return null;
            }

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(world, x, y, z, yaw, pitch);

        } catch (NumberFormatException e) {
            SimpleTrains.getInstance().getLogger().severe("Error parsing location coordinates: " + e.getMessage());
            return null;
        }
    }
}