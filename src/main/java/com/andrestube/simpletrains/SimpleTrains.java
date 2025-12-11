package com.andrestube.simpletrains;

import com.andrestube.simpletrains.commands.TrainCommand;
import com.andrestube.simpletrains.listeners.ChatListener;
import com.andrestube.simpletrains.listeners.GuiListener;
import com.andrestube.simpletrains.listeners.TrainListener;
import com.andrestube.simpletrains.listeners.TagListener; 
import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleTrains extends JavaPlugin {
    
    private static SimpleTrains instance;
    private StationManager stationManager;
    private MessageInputHandler messageInputHandler;
    private String creationBlockType = "GOLD_BLOCK"; 
    
    // --- PROPERTIES FOR CONFIGURABLE MESSAGES ---
    private String warpConfirmMessage;
    private String stationWelcomeMessage;
    
    // --- NEW CONFIG PROPERTIES ---
    private int creationXpCost;
    private int linkCreationXpCost;
    private int linkAcceptanceXpCost;
    private boolean requiresOwnerAcceptance;
    // --------------------------------------------

    public static SimpleTrains getInstance() {
        return instance;
    }

    public StationManager getStationManager() {
        return stationManager;
    }
    
    public MessageInputHandler getMessageInputHandler() {
        return messageInputHandler;
    }

    public String getCreationBlockType() {
        return this.creationBlockType;
    }
    
    // --- NEW GETTERS FOR CONFIGURATION ---
    public int getCreationXpCost() {
        return creationXpCost;
    }
    
    public int getLinkCreationXpCost() {
        return linkCreationXpCost;
    }
    
    public int getLinkAcceptanceXpCost() {
        return linkAcceptanceXpCost;
    }
    
    public boolean requiresOwnerAcceptance() {
        return requiresOwnerAcceptance;
    }
    // ---------------------------------------------

    public void setCreationBlockType(String blockName) {
        this.creationBlockType = blockName;
        getConfig().set("settings.creation_block", blockName);
        saveConfig();
    }

    // --- GETTERS FOR CONFIGURABLE MESSAGES ---
    public String getWarpConfirmMessage() {
        return ChatColor.translateAlternateColorCodes('&', this.warpConfirmMessage);
    }

    public String getStationWelcomeMessage() {
        return ChatColor.translateAlternateColorCodes('&', this.stationWelcomeMessage);
    }
    // ---------------------------------------------

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        loadConfigSettings(); 

        this.stationManager = new StationManager(this);
        this.messageInputHandler = new MessageInputHandler();
        
        // Register all listeners
        getServer().getPluginManager().registerEvents(new TrainListener(this, stationManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, stationManager), this); 
        getServer().getPluginManager().registerEvents(new TagListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, stationManager), this);

        // Register command
        getCommand("train").setExecutor(new TrainCommand(this, stationManager)); 
        
        getLogger().info("SimpleTrains has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (this.stationManager != null) {
            this.stationManager.saveData(); 
        }
        saveConfig(); 
        getLogger().info("SimpleTrains has been disabled!");
    }

    private void loadConfigSettings() {
        FileConfiguration config = getConfig();

        this.creationBlockType = config.getString("settings.creation_block", "GOLD_BLOCK");
        
        // --- LOAD XP COSTS ---
        this.creationXpCost = config.getInt("settings.creation_xp_cost", 5);
        this.linkCreationXpCost = config.getInt("settings.link_creation_xp_cost", 3);
        this.linkAcceptanceXpCost = config.getInt("settings.link_acceptance_xp_cost", 2);
        
        // --- LOAD LINKING BEHAVIOR ---
        this.requiresOwnerAcceptance = config.getBoolean("settings.linking_requires_owner_acceptance", true);

        // --- LOAD MESSAGES ---
        this.warpConfirmMessage = config.getString("messages.warp_confirm", "&aWarping to %DESTINATION%...");
        this.stationWelcomeMessage = config.getString("messages.station_welcome", "&e>> Welcome to %STATION% station! &6%MESSAGE%");
        // ---------------------
        
        // Ensure creation cost is set for config file generation 
        if (!config.contains("settings.creation_xp_cost")) {
             config.set("settings.creation_xp_cost", 5);
             saveConfig();
        }
    }
}