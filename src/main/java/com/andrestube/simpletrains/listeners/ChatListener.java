package com.andrestube.simpletrains.listeners;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.MessageInputHandler;
import com.andrestube.simpletrains.utils.Messages;
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

    public ChatListener(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.inputHandler = plugin.getMessageInputHandler();
    }

    private Messages msg() {
        return SimpleTrains.getMessages();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();

        if (inputHandler.isWaitingForInput(p.getUniqueId())) {

            event.setCancelled(true);
            String message = event.getMessage();
            String targetData = inputHandler.getTargetStation(p.getUniqueId());

            if (message.equalsIgnoreCase("cancel")) {
                p.sendMessage(msg().getWithPrefix("config-cancelled"));
                inputHandler.stopListening(p.getUniqueId());
                return;
            }

            if (targetData.startsWith("LINK_")) {

                String initiatingStation = targetData.substring("LINK_".length());
                String destinationStation = message;

                if (!manager.isStation(destinationStation)) {
                    p.sendMessage(msg().getWithPrefix("chat-station-not-exist", "STATION", destinationStation));
                } else if (manager.getStationOwnerId(destinationStation) == null) {
                    p.sendMessage(msg().getWithPrefix("chat-destination-no-owner"));
                } else if (manager.getStationLinksMap(initiatingStation).containsKey(destinationStation)) {
                    p.sendMessage(msg().getWithPrefix("chat-already-linked", "STATION_A", initiatingStation, "STATION_B", destinationStation));
                } else {

                    int cost = plugin.getLinkCreationXpCost();

                    if (!p.hasPermission("simpletrains.admin") && p.getLevel() < cost) {
                        p.sendMessage(msg().getWithPrefix("error-not-enough-xp", "COST", String.valueOf(cost)));
                        return;
                    }

                    if (!p.hasPermission("simpletrains.admin")) {
                        p.setLevel(p.getLevel() - cost);
                    }

                    if (plugin.requiresOwnerAcceptance()) {
                        manager.registerLinkRequest(p.getUniqueId(), initiatingStation, destinationStation);
                        p.sendMessage(msg().getWithPrefix("chat-link-request-sent", "RECEIVING_STATION", destinationStation));

                        UUID receiverId = manager.getStationOwnerId(destinationStation);
                        OfflinePlayer receiver = Bukkit.getOfflinePlayer(receiverId);
                        if (receiver.isOnline()) {
                            Player onlineReceiver = receiver.getPlayer();
                            onlineReceiver.sendMessage(msg().getWithPrefix("chat-link-request-received",
                                    "INITIATOR", p.getName(),
                                    "INITIATING_STATION", initiatingStation,
                                    "RECEIVING_STATION", destinationStation));

                            onlineReceiver.sendMessage(msg().getWithPrefix("chat-link-acceptance-hint",
                                    "INITIATING_STATION", initiatingStation));
                        }

                    } else {
                        manager.linkStations(initiatingStation, destinationStation);
                        p.sendMessage(msg().getWithPrefix("chat-instant-link-success",
                                "STATION_A", initiatingStation,
                                "STATION_B", destinationStation));
                    }
                }

            } else {
                String stationName = targetData;

                manager.setStationMessage(stationName, message);

                String stationWelcome = msg().get("station-welcome",
                        "STATION", stationName,
                        "MESSAGE", ChatColor.translateAlternateColorCodes('&', message));

                p.sendMessage(msg().getWithPrefix("chat-message-updated", "STATION", stationName));
                p.sendMessage(msg().get("chat-message-preview") + stationWelcome);
            }

            inputHandler.stopListening(p.getUniqueId());
        }
    }
}
