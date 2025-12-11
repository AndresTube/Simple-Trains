package com.andrestube.simpletrains.gui;

import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinkManagerGui {

    public static final String GUI_NAME = ChatColor.DARK_AQUA + "Link Manager";
    private static final int INVENTORY_SIZE = 54;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int REQUEST_NEW_SLOT = 47;
    private static final int PENDING_REQUESTS_SLOT = 51;

    /**
     * Creates the Link Management GUI for a specific station.
     * @param manager The StationManager.
     * @param stationName The station whose links are being managed.
     * @return The populated Inventory.
     */
    public static Inventory create(StationManager manager, String stationName) {
        String fullTitle = GUI_NAME + ChatColor.RESET + " - " + stationName; 
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, fullTitle);

        Map<String, StationManager.LinkType> linksMap = manager.getStationLinksMap(stationName);
        
        // 1. Add Current Linked Stations (Starting at slot 9, leaving top row for decoration)
        int slot = 9; 
        for (Map.Entry<String, StationManager.LinkType> entry : linksMap.entrySet()) {
            if (slot >= INVENTORY_SIZE - 9) break;

            String linkedStation = entry.getKey();
            StationManager.LinkType type = entry.getValue();
            
            ItemStack linkItem = new ItemStack(type == StationManager.LinkType.PUBLIC ? Material.CHAIN : Material.LEVER);
            ItemMeta meta = linkItem.getItemMeta();
            
            meta.setDisplayName(ChatColor.AQUA + linkedStation);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Link Type: " + (type == StationManager.LinkType.PUBLIC ? ChatColor.GREEN + "PUBLIC" : ChatColor.RED + "PRIVATE"));
            lore.add(ChatColor.YELLOW + "Left Click: " + ChatColor.RED + "UNLINK STATION");
            
            meta.setLore(lore);
            linkItem.setItemMeta(meta);
            
            inventory.setItem(slot++, linkItem);
        }
        
        // 2. Fill top row and main area with filler panes
        for (int i = 0; i < INVENTORY_SIZE - 9; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                // Use light blue for the header row (0-8) and gray for the main area
                Material fillerMat = (i < 9) ? Material.LIGHT_BLUE_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
                inventory.setItem(i, createFillerItem(fillerMat, " "));
            }
        }
        
        // 3. Add Control Items (Bottom row: 45-53)
        
        // Request New Link Button (Slot 47)
        inventory.setItem(REQUEST_NEW_SLOT, createControlItem(
            Material.PAPER, 
            ChatColor.GREEN + "Request New Link",
            ChatColor.GRAY + "Request a connection to another station."
        ));
        
        // View Pending Requests Button (Slot 51)
        inventory.setItem(PENDING_REQUESTS_SLOT, createControlItem(
            Material.CLOCK, 
            ChatColor.YELLOW + "View Pending Requests",
            ChatColor.GRAY + "Accept or reject incoming link requests."
        ));

        // Back to Config Button (Slot 49)
        inventory.setItem(BACK_BUTTON_SLOT, createControlItem(
            Material.SPECTRAL_ARROW, 
            ChatColor.RED + "Back to Configuration"
        ));
        
        // Fill remaining bottom row slots with dark filler
        for (int i = INVENTORY_SIZE - 9; i < INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                if (i != BACK_BUTTON_SLOT) {
                    inventory.setItem(i, createFillerItem(Material.BLUE_STAINED_GLASS_PANE, " "));
                }
            }
        }

        return inventory;
    }

    private static ItemStack createFillerItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(List.of(lore));
        } else {
            meta.setLore(List.of());
        }
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createControlItem(Material material, String name, String... lore) {
        return createFillerItem(material, name, lore);
    }
}