package com.andrestube.simpletrains;

import com.andrestube.simpletrains.commands.TrainCommand;
import com.andrestube.simpletrains.commands.TrainTabCompleter;
import com.andrestube.simpletrains.listeners.ChatListener;
import com.andrestube.simpletrains.listeners.GuiListener;
import com.andrestube.simpletrains.listeners.TrainListener;
import com.andrestube.simpletrains.listeners.TagListener;
import com.andrestube.simpletrains.utils.StationManager;
import com.andrestube.simpletrains.utils.Messages;
import com.andrestube.simpletrains.utils.SoundManager;
import com.andrestube.simpletrains.utils.TravelCostManager;
import com.andrestube.simpletrains.utils.CreationCostManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleTrains extends JavaPlugin {

    private static SimpleTrains instance;
    private static Messages messages;
    private StationManager stationManager;
    private MessageInputHandler messageInputHandler;
    private SoundManager soundManager;
    private TravelCostManager travelCostManager;
    private CreationCostManager creationCostManager;

    // --- CONFIG PROPERTIES ---
    private String creationBlockType = "GOLD_BLOCK";
    private int linkCreationXpCost;
    private int linkAcceptanceXpCost;
    private boolean requiresOwnerAcceptance;

    public static SimpleTrains getInstance() {
        return instance;
    }

    public static Messages getMessages() {
        return messages;
    }

    public StationManager getStationManager() {
        return stationManager;
    }

    public MessageInputHandler getMessageInputHandler() {
        return messageInputHandler;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public TravelCostManager getTravelCostManager() {
        return travelCostManager;
    }

    public CreationCostManager getCreationCostManager() {
        return creationCostManager;
    }

    public String getCreationBlockType() {
        return this.creationBlockType;
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

    public void setCreationBlockType(String blockName) {
        this.creationBlockType = blockName;
        getConfig().set("settings.creation_block", blockName);
        saveConfig();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize messages system
        messages = new Messages(this);

        loadConfigSettings();

        this.stationManager = new StationManager(this);
        this.messageInputHandler = new MessageInputHandler();
        this.soundManager = new SoundManager(this);
        this.travelCostManager = new TravelCostManager(this);
        this.creationCostManager = new CreationCostManager(this);

        // Register all listeners
        getServer().getPluginManager().registerEvents(new TrainListener(this, stationManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, stationManager), this);
        getServer().getPluginManager().registerEvents(new TagListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, stationManager), this);

        // Register command and tab completer
        getCommand("train").setExecutor(new TrainCommand(this, stationManager));
        getCommand("train").setTabCompleter(new TrainTabCompleter(this, stationManager));

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
        this.linkCreationXpCost = config.getInt("link_creation_xp_cost", 3);
        this.linkAcceptanceXpCost = config.getInt("link_acceptance_xp_cost", 2);
        this.requiresOwnerAcceptance = config.getBoolean("linking_requires_owner_acceptance", true);
    }
}