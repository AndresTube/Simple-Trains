// src/main/java/com/andrestube/simpletrains/gui/DestinationGui.java

package com.andrestube.simpletrains.gui;

import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DestinationGui {
    
    public static final String GUI_TITLE = ChatColor.DARK_AQUA + "Select Destination";
    public static final Material DESTINATION_ITEM_MATERIAL = Material.COMPASS;

    /**
     * Creates the destination selection GUI.
     */
    public static Inventory create(StationManager manager, Minecart minecart, String currentStationName, List<String> destinations) {
        int size = (destinations.size() / 9 + 1) * 9;
        if (size > 54) size = 54;
        
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (int i = 0; i < destinations.size(); i++) {
            String destName = destinations.get(i);
            
            // Get the link type for display purposes
            String linkType = "";
            StationManager.LinkType type = manager.getStationLinksMap(currentStationName).get(destName);
            if (type != null) {
                linkType = (type == StationManager.LinkType.PRIVATE) ? ChatColor.RED + "[PRIVATE]" : ChatColor.GREEN + "[PUBLIC]";
            }
            
            // The last lore line is used as a hidden marker for the Minecart UUID
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "From: " + ChatColor.YELLOW + currentStationName);
            lore.add(linkType);
            lore.add(ChatColor.DARK_GRAY + minecart.getUniqueId().toString()); // Hidden Minecart UUID

            ItemStack item = createGuiItem(DESTINATION_ITEM_MATERIAL, ChatColor.AQUA + destName, lore);
            gui.setItem(i, item);
        }
        
        return gui;
    }
    
    private static ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}