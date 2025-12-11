// src/main/java/com/andrestube/simpletrains/gui/MainGui.java

package com.andrestube.simpletrains.gui;

import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Set;

public class MainGui {

    public static final String GUI_TITLE = ChatColor.DARK_AQUA + "Station List";
    private static final int ITEMS_PER_PAGE = 45; 

    // --- Pagination Item Slots ---
    public static final int PREVIOUS_PAGE_SLOT = 45; 
    public static final int TOGGLE_VIEW_SLOT = 47; 
    public static final int CURRENT_PAGE_INFO_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 53; 

    // --- View Mode Identifier ---
    public static final String ALL_STATIONS_MODE = "ALL";
    public static final String OWNER_STATIONS_MODE = "OWNER";
    
    /**
     * Creates a new MainGui inventory for the specified page and view mode.
     */
    public static Inventory create(StationManager manager, Player player, int page, String viewMode) {
        
        // 1. Determine which stations to display based on viewMode
        List<String> rawStations;
        String titleSuffix;
        
        // Use a non-final variable to store the actual mode being used, for consistency
        String actualViewMode = viewMode;

        if (actualViewMode.equals(OWNER_STATIONS_MODE)) {
            rawStations = manager.getOwnedStations(player.getUniqueId());
            titleSuffix = " (" + ChatColor.YELLOW + "My Stations" + ChatColor.DARK_AQUA + ")";
        } else {
            rawStations = new ArrayList<>(manager.getAllStationNames());
            titleSuffix = " (" + ChatColor.YELLOW + "All Stations" + ChatColor.DARK_AQUA + ")";
            actualViewMode = ALL_STATIONS_MODE; 
        }
        
        Inventory inventory = Bukkit.createInventory(null, 54, GUI_TITLE + titleSuffix);
        
        List<String> sortedStations = rawStations;
        Collections.sort(sortedStations);
        
        int totalStations = sortedStations.size();
        int totalPages = (int) Math.ceil((double) totalStations / ITEMS_PER_PAGE);
        
        // Ensure the requested page is valid
        if (totalPages > 0) {
            if (page < 0) page = 0;
            if (page >= totalPages) page = totalPages - 1;
        } else {
            page = 0; // No stations, page is 0
        }

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalStations);

        // 2. Add Station Items (from slot 0 to 44)
        for (int i = start; i < end; i++) {
            String stationName = sortedStations.get(i);
            ItemStack item = createStationItem(stationName, manager);
            inventory.setItem(i - start, item); 
        }

        // 3. Add Pagination Controls and Toggle Button (bottom row)
        
        // Previous Page
        if (page > 0) {
            inventory.setItem(PREVIOUS_PAGE_SLOT, createPaginationItem(Material.ARROW, ChatColor.YELLOW + "<< Previous Page", page - 1, actualViewMode));
        } else {
            inventory.setItem(PREVIOUS_PAGE_SLOT, createFillerItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        }
        
        // Toggle Button
        inventory.setItem(TOGGLE_VIEW_SLOT, createToggleItem(actualViewMode));
        
        // Page Info
        inventory.setItem(CURRENT_PAGE_INFO_SLOT, createPageInfoItem(page + 1, totalPages, totalStations));
        
        // Next Page
        if (page < totalPages - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, createPaginationItem(Material.ARROW, ChatColor.YELLOW + "Next Page >>", page + 1, actualViewMode));
        } else {
            inventory.setItem(NEXT_PAGE_SLOT, createFillerItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        }

        return inventory;
    }
    
    public static Inventory create(StationManager manager, Player player) {
        // Default to My Stations view
        return create(manager, player, 0, OWNER_STATIONS_MODE); 
    }

    private static ItemStack createStationItem(String name, StationManager manager) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.AQUA + name);
        
        List<String> lore = new ArrayList<>();
        UUID ownerId = manager.getStationOwnerId(name);
        String ownerName = ownerId != null ? Bukkit.getOfflinePlayer(ownerId).getName() : "Server";

        lore.add(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + ownerName); 
        lore.add(ChatColor.GRAY + "Linked Stations: " + ChatColor.WHITE + manager.getLinkedStations(name).size());
        
        if (manager.getStationLocation(name) != null) {
            lore.add(ChatColor.GRAY + "Location: " + ChatColor.WHITE + manager.getStationLocation(name).getBlockX() + ", " + manager.getStationLocation(name).getBlockZ());
        }
        
        lore.add(ChatColor.DARK_PURPLE + "Click to open configuration (if owned)");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPaginationItem(Material material, String name, int pageNumber, String viewMode) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = new ArrayList<>();
        // These lore lines are used by GuiListener to determine the next action
        lore.add(ChatColor.DARK_GRAY + "Page:" + pageNumber); 
        lore.add(ChatColor.DARK_GRAY + "Mode:" + viewMode);
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createPageInfoItem(int currentPage, int totalPages, int totalStations) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Page " + currentPage + ChatColor.GRAY + " / " + totalPages);
        meta.setLore(Collections.singletonList(ChatColor.DARK_GRAY + "Total Stations: " + totalStations));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createToggleItem(String currentMode) {
        String nextMode = currentMode.equals(ALL_STATIONS_MODE) ? OWNER_STATIONS_MODE : ALL_STATIONS_MODE;
        Material material = nextMode.equals(OWNER_STATIONS_MODE) ? Material.ENDER_PEARL : Material.NETHER_STAR;
        String name = nextMode.equals(OWNER_STATIONS_MODE) ? ChatColor.LIGHT_PURPLE + "Switch to My Stations" : ChatColor.LIGHT_PURPLE + "Switch to All Stations";
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Current View: " + currentMode);
        // This lore line is used by GuiListener to determine the next action
        lore.add(ChatColor.DARK_GRAY + "NextMode:" + nextMode); 
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createFillerItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}