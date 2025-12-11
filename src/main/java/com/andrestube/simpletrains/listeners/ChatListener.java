package com.andrestube.simpletrains.listeners;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.MessageInputHandler;
import com.andrestube.simpletrains.utils.StationManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.UUID;


public class ChatListener implements Listener {

    private final SimpleTrains plugin;
    private final StationManager manager;
    private final MessageInputHandler inputHandler;
    private static final String PREFIX = ChatColor.GOLD + "[SimpleTrains] " + ChatColor.RESET;

    public ChatListener(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.inputHandler = plugin.getMessageInputHandler();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        
        // Check if the player is currently expected to set a station message/link
        if (inputHandler.isWaitingForInput(p.getUniqueId())) {
            
            event.setCancelled(true); // Prevent the message from being sent to chat
            String message = event.getMessage();
            String targetData = inputHandler.getTargetStation(p.getUniqueId());
            
            // Check for cancel command first
            if (message.equalsIgnoreCase("cancel")) {
                p.sendMessage(PREFIX + ChatColor.YELLOW + "Configuration cancelled.");
                inputHandler.stopListening(p.getUniqueId());
                return;
            }

            // --- LINK CONFIGURATION HANDLER ---
            if (targetData.startsWith("LINK_")) {
                
                String initiatingStation = targetData.substring("LINK_".length());
                String destinationStation = message;

                if (!manager.isStation(destinationStation)) {
                    p.sendMessage(PREFIX + ChatColor.RED + "Error: Station " + destinationStation + " does not exist.");
                } else if (manager.getStationOwnerId(destinationStation) == null) {
                    p.sendMessage(PREFIX + ChatColor.RED + "Error: Destination station must have an owner to receive a request.");
                } else if (manager.getStationLinksMap(initiatingStation).containsKey(destinationStation)) {
                    p.sendMessage(PREFIX + ChatColor.RED + "Error: Stations " + initiatingStation + " and " + destinationStation + " are already linked.");
                } else {
                    
                    int cost = plugin.getLinkCreationXpCost();
                    
                    if (!p.hasPermission("simpletrains.admin") && p.getLevel() < cost) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.error_not_enough_xp"))
                                    .replace("%COST%", String.valueOf(cost)));
                        return;
                    }
                    
                    // Deduct XP
                    if (!p.hasPermission("simpletrains.admin")) {
                        p.setLevel(p.getLevel() - cost);
                    }

                    if (plugin.requiresOwnerAcceptance()) {
                        // Advanced: Register a pending request
                        manager.registerLinkRequest(p.getUniqueId(), initiatingStation, destinationStation);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.link_request_sent"))
                                .replace("%RECEIVING_STATION%", destinationStation));
                        
                        // Notify the receiving station's owner
                        UUID receiverId = manager.getStationOwnerId(destinationStation);
                        OfflinePlayer receiver = Bukkit.getOfflinePlayer(receiverId);
                        if (receiver.isOnline()) {
                             Player onlineReceiver = receiver.getPlayer();
                             onlineReceiver.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.link_request_received"))
                                .replace("%INITIATOR%", p.getName())
                                .replace("%INITIATING_STATION%", initiatingStation)
                                .replace("%RECEIVING_STATION%", destinationStation));
                            
                             onlineReceiver.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.link_acceptance_hint"))
                                .replace("%INITIATING_STATION%", initiatingStation));
                        }
                        
                    } else {
                        // Simple: Link instantly
                        manager.linkStations(initiatingStation, destinationStation);
                        p.sendMessage(PREFIX + ChatColor.GREEN + "Station " + ChatColor.AQUA + initiatingStation + 
                                  ChatColor.GREEN + " successfully linked to " + ChatColor.AQUA + destinationStation + ChatColor.GREEN + " (Public Link).");
                    }
                }
                
            } else {
                // --- MESSAGE CONFIGURATION HANDLER (Default) ---
                String stationName = targetData; 

                // 1. Save the raw message (manager is responsible for saving the raw '&' codes)
                manager.setStationMessage(stationName, message);
                
                // 2. Build and send the confirmation/preview
                
                // Get the translated wrapper from SimpleTrains.java
                String wrapper = plugin.getStationWelcomeMessage(); 
                
                // Translate the user's input (message) and then insert it into the wrapper
                String finalMessage = wrapper
                    .replace("%STATION%", stationName)
                    .replace("%MESSAGE%", ChatColor.translateAlternateColorCodes('&', message));
                
                // Confirmation message
                p.sendMessage(PREFIX + ChatColor.GREEN + "Welcome message for " + 
                              ChatColor.AQUA + stationName + ChatColor.GREEN + " successfully updated.");
                
                // Send the preview. finalMessage already contains all necessary color translations.
                p.sendMessage(ChatColor.YELLOW + "Preview: " + finalMessage);
            }
            
            // Stop listening
            inputHandler.stopListening(p.getUniqueId());
        }
    }
}