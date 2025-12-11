package com.andrestube.simpletrains.gui;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PendingRequestsGui {

    public static final String GUI_NAME = ChatColor.GOLD + "Pending Link Requests";
    private static final int INVENTORY_SIZE = 54;
    private static final int BACK_BUTTON_SLOT = 49;

    /**
     * Creates the Pending Link Requests GUI for a specific station.
     * @param plugin The SimpleTrains plugin instance.
     * @param manager The StationManager.
     * @param receivingStation The station that received the requests.
     * @return The populated Inventory.
     */
    public static Inventory create(SimpleTrains plugin, StationManager manager, String receivingStation) {
        String fullTitle = GUI_NAME + ChatColor.RESET + " - " + receivingStation; 
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, fullTitle);

        int slot = 9; 
        
        // Iterate through all stations to find pending requests targeting this receivingStation
        for (String initiatingStation : manager.getAllStationNames()) {
            // Check if there is a request pending for this pair (receivingStation, initiatingStation)
            if (manager.isRequestPending(receivingStation, initiatingStation)) {
                
                UUID requesterId = manager.getPendingRequesterId(receivingStation);
                // We use getOfflinePlayer here as the requester might not be online
                OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterId); 
                
                if (slot >= INVENTORY_SIZE - 9) break;

                // Use the configured cost from the main plugin instance
                int acceptanceCost = plugin.getLinkAcceptanceXpCost();
                
                // Create Request Item
                ItemStack requestItem = new ItemStack(Material.BOOK);
                ItemMeta meta = requestItem.getItemMeta();
                
                meta.setDisplayName(ChatColor.YELLOW + "Request from " + initiatingStation);
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Requester: " + ChatColor.AQUA + requester.getName());
                lore.add(ChatColor.GRAY + "Station: " + ChatColor.AQUA + initiatingStation);
                lore.add(" ");
                lore.add(ChatColor.GREEN + "Left Click: " + ChatColor.DARK_GREEN + "ACCEPT (Cost: " + acceptanceCost + " XP)");
                lore.add(ChatColor.RED + "Right Click: " + ChatColor.DARK_RED + "REJECT");
                
                meta.setLore(lore);
                requestItem.setItemMeta(meta);
                
                inventory.setItem(slot++, requestItem);
            }
        }
        
        // Fill header and main area with filler panes
        for (int i = 0; i < INVENTORY_SIZE - 9; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                Material fillerMat = (i < 9) ? Material.BLUE_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
                inventory.setItem(i, createFillerItem(fillerMat, " "));
            }
        }
        
        // Back Button (Slot 49)
        inventory.setItem(BACK_BUTTON_SLOT, createFillerItem(
            Material.SPECTRAL_ARROW, 
            ChatColor.RED + "Back to Link Manager"
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
}