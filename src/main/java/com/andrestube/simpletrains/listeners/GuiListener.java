package com.andrestube.simpletrains.listeners;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.gui.DestinationGui;
import com.andrestube.simpletrains.gui.MainGui; 
import com.andrestube.simpletrains.gui.StationConfigGui; 
import com.andrestube.simpletrains.gui.LinkManagerGui; 
import com.andrestube.simpletrains.gui.PendingRequestsGui; 
import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;


import java.util.List;
import java.util.UUID;
import java.util.Optional;

public class GuiListener implements Listener {

    private final SimpleTrains plugin;
    private final StationManager manager;
    private final StationConfigGui configGui; 
    private static final String PREFIX = ChatColor.GOLD + "[SimpleTrains] " + ChatColor.RESET;

    public GuiListener(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.configGui = new StationConfigGui(plugin, manager); 
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        ItemStack clickedItem = event.getCurrentItem();
        String inventoryTitle = event.getView().getTitle();

        // 1. Check if the inventory is one of ours
        boolean isSimpleTrainsGui = inventoryTitle.equals(DestinationGui.GUI_TITLE) || 
                                    inventoryTitle.startsWith(MainGui.GUI_TITLE) ||
                                    inventoryTitle.startsWith(StationConfigGui.GUI_NAME) ||
                                    inventoryTitle.startsWith(LinkManagerGui.GUI_NAME) ||
                                    inventoryTitle.startsWith(PendingRequestsGui.GUI_NAME); 

        if (!isSimpleTrainsGui) {
            return; // Not our GUI, ignore
        }
        
        // 2. Ignore clicks on air/null items (safety check)
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // 3. ONLY CANCEL CLICKS IF THEY ARE IN THE TOP (CUSTOM) INVENTORY.
        if (event.getClickedInventory().equals(topInventory)) {
            event.setCancelled(true); 
        } else {
            return; // Clicked player inventory, do nothing (allow the click)
        }
        
        // --- 4. Handle Clicks inside the Custom GUI ---
        
        if (inventoryTitle.equals(DestinationGui.GUI_TITLE)) {
            handleDestinationClick(event, p, clickedItem);
            return;
        }
        
        if (inventoryTitle.startsWith(MainGui.GUI_TITLE)) {
            handleMainGuiClick(event, p, clickedItem, inventoryTitle);
            return;
        }

        if (inventoryTitle.startsWith(StationConfigGui.GUI_NAME)) {
            handleConfigGuiClick(event, p, clickedItem, inventoryTitle);
            return;
        }
        
        // --- Link Manager GUI ---
        if (inventoryTitle.startsWith(LinkManagerGui.GUI_NAME)) {
            handleLinkManagerGuiClick(event, p, clickedItem, inventoryTitle);
            return;
        }

        // --- Pending Requests GUI ---
        if (inventoryTitle.startsWith(PendingRequestsGui.GUI_NAME)) { 
            handlePendingRequestsGuiClick(event, p, clickedItem, inventoryTitle);
            return;
        }
    }

    // ====================================================================
    // --- HANDLERS (Destination/Main/Config) ---
    // ====================================================================

    private void handleDestinationClick(InventoryClickEvent event, Player p, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String destinationName = ChatColor.stripColor(meta.getDisplayName());
        
        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 3) {
            p.sendMessage(PREFIX + ChatColor.RED + "Error: Missing minecart data in item lore.");
            p.closeInventory();
            return;
        }
        
        String minecartUuidString = ChatColor.stripColor(lore.get(lore.size() - 1));
        UUID minecartUuid;
        try {
            minecartUuid = UUID.fromString(minecartUuidString);
        } catch (IllegalArgumentException e) {
            p.sendMessage(PREFIX + ChatColor.RED + "Error: Invalid minecart ID in item lore.");
            p.closeInventory();
            return;
        }
        
        Minecart targetMinecart = null;
        for (Entity entity : p.getWorld().getEntities()) {
            if (entity instanceof Minecart && entity.getUniqueId().equals(minecartUuid)) {
                targetMinecart = (Minecart) entity;
                break;
            }
        }

        if (targetMinecart == null) {
             p.sendMessage(PREFIX + ChatColor.RED + "Minecart not found. Please retry.");
             p.closeInventory();
             return;
        }
        
        if (!targetMinecart.hasMetadata(TrainListener.START_STATION_META)) {
            p.sendMessage(PREFIX + ChatColor.RED + "Error: Starting station data is missing.");
            p.closeInventory();
            return;
        }

        p.closeInventory();
        TrainListener.teleportMinecart(targetMinecart, p, destinationName);
        targetMinecart.removeMetadata(TrainListener.START_STATION_META, plugin);
    }
    
    private void handleMainGuiClick(InventoryClickEvent event, Player p, ItemStack clickedItem, String inventoryTitle) {
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        List<String> lore = meta.getLore();
        if (lore == null) lore = List.of(); 

        // --- STATION ITEM CLICK (Opens Config) ---
        if (clickedItem.getType() == Material.COMPASS) {
            String stationName = ChatColor.stripColor(meta.getDisplayName());

            if (manager.isStation(stationName) && (manager.isOwner(stationName, p.getUniqueId()) || p.hasPermission("simpletrains.admin"))) {
                p.openInventory(configGui.create(stationName));
            } else {
                p.sendMessage(ChatColor.RED + "You do not have permission to configure this station.");
            }
            return;
        }
        
        // --- PAGINATION / TOGGLE LOGIC (Unchanged) ---
        
        if (event.getSlot() == MainGui.PREVIOUS_PAGE_SLOT || event.getSlot() == MainGui.NEXT_PAGE_SLOT) {
            
            Optional<String> pageLore = lore.stream().filter(s -> s.startsWith(ChatColor.DARK_GRAY + "Page:")).findFirst();
            Optional<String> modeLore = lore.stream().filter(s -> s.startsWith(ChatColor.DARK_GRAY + "Mode:")).findFirst();

            if (pageLore.isEmpty() || modeLore.isEmpty()) return;

            int nextPage;
            try {
                nextPage = Integer.parseInt(pageLore.get().substring((ChatColor.DARK_GRAY + "Page:").length()));
            } catch (NumberFormatException e) {
                return; 
            }
            
            String nextMode = modeLore.get().substring((ChatColor.DARK_GRAY + "Mode:").length());
            
            p.openInventory(MainGui.create(manager, p, nextPage, nextMode));
            return;
        }

        if (event.getSlot() == MainGui.TOGGLE_VIEW_SLOT) {
             Optional<String> nextModeLore = lore.stream()
                                   .filter(s -> s.startsWith(ChatColor.DARK_GRAY + "NextMode:"))
                                   .findFirst();

            if (nextModeLore.isEmpty()) return;
            
            String nextMode = nextModeLore.get().substring((ChatColor.DARK_GRAY + "NextMode:").length());
            p.openInventory(MainGui.create(manager, p, 0, nextMode));
            return;
        }
    }

    private void handleConfigGuiClick(InventoryClickEvent event, Player p, ItemStack clickedItem, String inventoryTitle) {
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String stationName = ChatColor.stripColor(inventoryTitle.replace(StationConfigGui.GUI_NAME + ChatColor.RESET + " - ", ""));
        String itemName = ChatColor.stripColor(meta.getDisplayName());

        if (!manager.isStation(stationName)) {
            p.sendMessage(ChatColor.RED + "Error: Configuration station not found.");
            p.closeInventory();
            return;
        }

        // --- CONFIGURATION ACTIONS ---
        if (itemName.equals("Edit Welcome Message")) {
             if (plugin.getMessageInputHandler().isWaitingForInput(p.getUniqueId())) {
                p.sendMessage(PREFIX + ChatColor.RED + "You are already setting a message/link. Please finish that first.");
                p.closeInventory();
                return;
            }
            
            plugin.getMessageInputHandler().startListening(p.getUniqueId(), stationName);
            
            p.sendMessage(PREFIX + ChatColor.GREEN + "Enter new welcome message for station " + 
                          ChatColor.AQUA + stationName + ChatColor.GREEN + " in chat.");
            p.sendMessage(PREFIX + ChatColor.YELLOW + "Use '&' for color codes. Type 'cancel' to stop.");
            p.closeInventory();
            return;
            
        } else if (itemName.equals("Link Manager")) {
            p.openInventory(LinkManagerGui.create(manager, stationName));
            return;
            
        } else if (itemName.equals("DELETE STATION")) {
            p.sendMessage(ChatColor.RED + "Are you sure you want to delete " + stationName + "? Use /train delete " + stationName + " to confirm.");
            p.closeInventory();
        } else if (itemName.equals("Back to Main List")) {
            p.openInventory(MainGui.create(manager, p));
        }
        // --- END CONFIGURATION ACTIONS ---
    }
    
    // --- HANDLER FOR LINK MANAGER GUI ---
    private void handleLinkManagerGuiClick(InventoryClickEvent event, Player p, ItemStack clickedItem, String inventoryTitle) {
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        // Extract the station name from the inventory title
        String stationName = ChatColor.stripColor(inventoryTitle.replace(LinkManagerGui.GUI_NAME + ChatColor.RESET + " - ", ""));
        String itemName = ChatColor.stripColor(meta.getDisplayName());

        // Handle Back Button
        if (itemName.equals("Back to Configuration")) {
            p.openInventory(configGui.create(stationName));
            return;
        }
        
        // Handle Request New Link
        if (itemName.equals("Request New Link")) {
             p.sendMessage(PREFIX + ChatColor.GREEN + "Enter the name of the station you wish to link to in chat.");
             p.sendMessage(PREFIX + ChatColor.YELLOW + "This sends a request to the other station's owner. Type 'cancel' to stop.");
             
             // Starts listening for the destination station name
             plugin.getMessageInputHandler().startListening(p.getUniqueId(), "LINK_" + stationName);
             p.closeInventory();
             return;
        }
        
        // Handle View Pending Requests (Opens new GUI)
        if (itemName.equals("View Pending Requests")) {
            p.openInventory(PendingRequestsGui.create(plugin, manager, stationName));
            return;
        }
        
        // Handle clicking on an existing link (Link management/Unlink)
        if (clickedItem.getType() == Material.CHAIN || clickedItem.getType() == Material.LEVER) {
            String linkedStation = itemName;
            
            // Unlink Logic (Direct Unlink)
            manager.unlinkStations(stationName, linkedStation);
            p.sendMessage(PREFIX + ChatColor.GREEN + "Successfully unlinked " + ChatColor.AQUA + stationName + ChatColor.GREEN + " and " + ChatColor.AQUA + linkedStation + ChatColor.GREEN + ".");
            
            // Re-open the GUI to refresh the list
            p.openInventory(LinkManagerGui.create(manager, stationName));
            return;
        }
    }

    // --- NEW HANDLER FOR PENDING REQUESTS GUI ---
    private void handlePendingRequestsGuiClick(InventoryClickEvent event, Player p, ItemStack clickedItem, String inventoryTitle) {
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Extract the station name receiving the requests
        String receivingStation = ChatColor.stripColor(inventoryTitle.replace(PendingRequestsGui.GUI_NAME + ChatColor.RESET + " - ", ""));
        String itemName = ChatColor.stripColor(meta.getDisplayName());
        
        // Handle Back Button
        if (itemName.equals("Back to Link Manager")) {
            p.openInventory(LinkManagerGui.create(manager, receivingStation));
            return;
        }
        
        // Handle Request Item Click
        if (clickedItem.getType() == Material.BOOK) {
            
            String initiatingStation = ChatColor.stripColor(itemName.replace("Request from ", ""));
            String initiatingStationFromManager = manager.getPendingInitiatingStation(receivingStation);
            
            if (initiatingStationFromManager == null || !initiatingStationFromManager.equals(initiatingStation)) {
                 p.sendMessage(PREFIX + ChatColor.RED + "Error: Request data expired or corrupted.");
                 manager.clearLinkRequest(receivingStation);
                 p.openInventory(PendingRequestsGui.create(plugin, manager, receivingStation));
                 return;
            }

            UUID requesterId = manager.getPendingRequesterId(receivingStation);
            
            if (requesterId == null) {
                p.sendMessage(PREFIX + ChatColor.RED + "Error: Requester data missing.");
                manager.clearLinkRequest(receivingStation);
                p.openInventory(PendingRequestsGui.create(plugin, manager, receivingStation));
                return;
            }
            
            // --- ACCEPT LOGIC (Left Click) ---
            if (event.isLeftClick()) {
                int cost = plugin.getLinkAcceptanceXpCost();

                if (!p.hasPermission("simpletrains.admin") && p.getLevel() < cost) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.error_not_enough_xp"))
                                    .replace("%COST%", String.valueOf(cost)));
                    return;
                }
                
                // Deduct XP
                if (!p.hasPermission("simpletrains.admin")) {
                    p.setLevel(p.getLevel() - cost);
                }
                
                // Create Link
                manager.linkStations(receivingStation, initiatingStation, StationManager.LinkType.PUBLIC);
                manager.clearLinkRequest(receivingStation);
                
                p.sendMessage(PREFIX + ChatColor.GREEN + "Link to " + ChatColor.AQUA + initiatingStation + ChatColor.GREEN + " accepted!");
                
                // Inform the requester (if they are online)
                OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterId);
                if (requester.isOnline()) {
                    Player onlineRequester = requester.getPlayer();
                    onlineRequester.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.link_request_accepted"))
                                                    .replace("%RECEIVING_STATION%", receivingStation));
                }

            // --- REJECT LOGIC (Right Click) ---
            } else if (event.isRightClick()) {
                
                manager.clearLinkRequest(receivingStation);
                p.sendMessage(PREFIX + ChatColor.RED + "Link request from " + ChatColor.AQUA + initiatingStation + ChatColor.RED + " rejected.");
            }
            
            // Re-open the GUI to refresh the list
            p.openInventory(PendingRequestsGui.create(plugin, manager, receivingStation));
        }
    }
}