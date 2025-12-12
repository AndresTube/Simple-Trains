package com.andrestube.simpletrains.utils;

import com.andrestube.simpletrains.SimpleTrains;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Manages sound effects for train travel.
 */
public class SoundManager {

    private final SimpleTrains plugin;

    private boolean enabled;
    private Sound departureSound;
    private float departureVolume;
    private float departurePitch;
    private Sound arrivalSound;
    private float arrivalVolume;
    private float arrivalPitch;

    public SoundManager(SimpleTrains plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("sounds.enabled", true);

        // Load departure sound
        String departureSoundName = config.getString("sounds.departure.sound", "ENTITY_ENDERMAN_TELEPORT");
        this.departureSound = parseSound(departureSoundName, Sound.ENTITY_ENDERMAN_TELEPORT);
        this.departureVolume = (float) config.getDouble("sounds.departure.volume", 1.0);
        this.departurePitch = (float) config.getDouble("sounds.departure.pitch", 1.0);

        // Load arrival sound
        String arrivalSoundName = config.getString("sounds.arrival.sound", "BLOCK_NOTE_BLOCK_CHIME");
        this.arrivalSound = parseSound(arrivalSoundName, Sound.BLOCK_NOTE_BLOCK_CHIME);
        this.arrivalVolume = (float) config.getDouble("sounds.arrival.volume", 1.0);
        this.arrivalPitch = (float) config.getDouble("sounds.arrival.pitch", 1.2);
    }

    private Sound parseSound(String soundName, Sound defaultSound) {
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound '" + soundName + "', using default.");
            return defaultSound;
        }
    }

    /**
     * Play the departure sound at the player's location.
     *
     * @param player The player departing
     */
    public void playDepartureSound(Player player) {
        if (!enabled || departureSound == null) return;
        player.playSound(player.getLocation(), departureSound, departureVolume, departurePitch);
    }

    /**
     * Play the departure sound at a specific location.
     *
     * @param player   The player to play the sound for
     * @param location The location to play the sound at
     */
    public void playDepartureSound(Player player, Location location) {
        if (!enabled || departureSound == null) return;
        player.playSound(location, departureSound, departureVolume, departurePitch);
    }

    /**
     * Play the arrival sound at the player's location.
     *
     * @param player The player arriving
     */
    public void playArrivalSound(Player player) {
        if (!enabled || arrivalSound == null) return;
        player.playSound(player.getLocation(), arrivalSound, arrivalVolume, arrivalPitch);
    }

    /**
     * Play the arrival sound at a specific location.
     *
     * @param player   The player to play the sound for
     * @param location The location to play the sound at
     */
    public void playArrivalSound(Player player, Location location) {
        if (!enabled || arrivalSound == null) return;
        player.playSound(location, arrivalSound, arrivalVolume, arrivalPitch);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
