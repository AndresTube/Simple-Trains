package com.andrestube.simpletrains.gui;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class StationConfigGui {

    public static final String GUI_NAME = ChatColor.DARK_RED + "Station Configuration";
    private final SimpleTrains plugin;
    private final StationManager manager;

    public StationConfigGui(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public Inventory create(String stationName) {
        // Use ChatColor.RESET to ensure the station name color isn't inherited from the GUI name
        String fullTitle = GUI_NAME + ChatColor.RESET + " - " + stationName; 
        Inventory inventory = Bukkit.createInventory(null, 27, fullTitle);

        // 1. Edit Welcome Message (Slot 10)
        inventory.setItem(10, createConfigItem(
                Material.WRITABLE_BOOK,
                ChatColor.YELLOW + "Edit Welcome Message",
                ChatColor.GRAY + "Current: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', manager.getStationMessage(stationName))
        ));

        // 2. Manage Links (Slot 13)
        inventory.setItem(13, createConfigItem(
                Material.CHAIN,
                ChatColor.AQUA + "Link Manager", // Renamed for clarity in the complex feature
                ChatColor.GRAY + "Linked Stations: " + manager.getLinkedStations(stationName).size(),
                ChatColor.GRAY + "Click to request or manage links."
        ));

        // 3. Delete Station (Slot 16)
        inventory.setItem(16, createConfigItem(
                Material.BARRIER,
                ChatColor.RED + "DELETE STATION",
                ChatColor.DARK_RED + "Warning: This action is permanent."
        ));
        
        // 4. Back button (Slot 22)
        inventory.setItem(22, createConfigItem(
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                ChatColor.WHITE + "Back to Main List"
        ));

        return inventory;
    }

    private ItemStack createConfigItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        } else {
            meta.setLore(Collections.emptyList());
        }
        item.setItemMeta(meta);
        return item;
    }
}