package com.andrestube.simpletrains.utils;

import com.andrestube.simpletrains.SimpleTrains;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Messages {

    private final SimpleTrains plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public Messages(SimpleTrains plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Loads the messages.yml file, creating it if it doesn't exist.
     */
    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Load defaults from jar to ensure new keys are available
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            messagesConfig.setDefaults(defaultConfig);
        }
    }

    /**
     * Reloads the messages configuration from disk.
     */
    public void reload() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            messagesConfig.setDefaults(defaultConfig);
        }
    }

    /**
     * Gets a message by key with color codes translated.
     * @param key The message key in messages.yml
     * @return The translated message, or an error message if key not found
     */
    public String get(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "[Missing message: " + key + "]";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Gets a message by key without the prefix.
     * @param key The message key in messages.yml
     * @return The translated message without prefix
     */
    public String getRaw(String key) {
        return get(key);
    }

    /**
     * Gets a message with the plugin prefix prepended.
     * @param key The message key in messages.yml
     * @return The translated message with prefix
     */
    public String getWithPrefix(String key) {
        return get("prefix") + get(key);
    }

    /**
     * Gets a message by key and replaces placeholders.
     * Placeholders should be passed in pairs: placeholder name (without %), value
     * Example: get("station-created", "STATION", "MyStation", "COST", "5")
     *
     * @param key The message key in messages.yml
     * @param replacements Pairs of placeholder names and their values
     * @return The translated message with placeholders replaced
     */
    public String get(String key, String... replacements) {
        String message = get(key);

        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = "%" + replacements[i] + "%";
            String value = replacements[i + 1];
            message = message.replace(placeholder, value);
        }

        return message;
    }

    /**
     * Gets a message with prefix and replaces placeholders.
     * @param key The message key in messages.yml
     * @param replacements Pairs of placeholder names and their values
     * @return The translated message with prefix and placeholders replaced
     */
    public String getWithPrefix(String key, String... replacements) {
        return get("prefix") + get(key, replacements);
    }

    /**
     * Gets the plugin prefix.
     * @return The translated prefix
     */
    public String getPrefix() {
        return get("prefix");
    }

    /**
     * Checks if a message key exists in the configuration.
     * @param key The message key to check
     * @return true if the key exists
     */
    public boolean has(String key) {
        return messagesConfig.contains(key);
    }

    /**
     * Gets the raw FileConfiguration for advanced usage.
     * @return The messages FileConfiguration
     */
    public FileConfiguration getConfig() {
        return messagesConfig;
    }
}
