package com.andrestube.simpletrains.listeners;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.gui.DestinationGui;
import com.andrestube.simpletrains.gui.MainGui;
import com.andrestube.simpletrains.gui.StationConfigGui;
import com.andrestube.simpletrains.gui.LinkManagerGui;
import com.andrestube.simpletrains.gui.PendingRequestsGui;
import com.andrestube.simpletrains.utils.Messages;
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

    public GuiListener(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.configGui = new StationConfigGui(plugin, manager);
    }

    private Messages msg() {
        return SimpleTrains.getMessages();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        ItemStack clickedItem = event.getCurrentItem();
        String inventoryTitle = event.getView().getTitle();

        boolean isSimpleTrainsGui = inventoryTitle.equals(DestinationGui.GUI_TITLE) ||
                inventoryTitle.startsWith(MainGui.GUI_TITLE) ||
                inventoryTitle.startsWith(StationConfigGui.GUI_NAME) ||
                inventoryTitle.startsWith(LinkManagerGui.GUI_NAME) ||
                inventoryTitle.startsWith(PendingRequestsGui.GUI_NAME);

        if (!isSimpleTrainsGui) {
            return;
        }

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (event.getClickedInventory().equals(topInventory)) {
            event.setCancelled(true);
        } else {
            return;
        }

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

        if (inventoryTitle.startsWith(LinkManagerGui.GUI_NAME)) {
            handleLinkManagerGuiClick(event, p, clickedItem, inventoryTitle);
            return;
        }

        if (inventoryTitle.startsWith(PendingRequestsGui.GUI_NAME)) {
            handlePendingRequestsGuiClick(event, p, clickedItem, inventoryTitle);
            return;
        }
    }

    private void handleDestinationClick(InventoryClickEvent event, Player p, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String destinationName = ChatColor.stripColor(meta.getDisplayName());

        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 3) {
            p.sendMessage(msg().getWithPrefix("gui-missing-minecart-data"));
            p.closeInventory();
            return;
        }

        String minecartUuidString = ChatColor.stripColor(lore.get(lore.size() - 1));
        UUID minecartUuid;
        try {
            minecartUuid = UUID.fromString(minecartUuidString);
        } catch (IllegalArgumentException e) {
            p.sendMessage(msg().getWithPrefix("gui-invalid-minecart-id"));
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
            p.sendMessage(msg().getWithPrefix("gui-minecart-not-found"));
            p.closeInventory();
            return;
        }

        if (!targetMinecart.hasMetadata(TrainListener.START_STATION_META)) {
            p.sendMessage(msg().getWithPrefix("gui-start-station-missing"));
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

        if (clickedItem.getType() == Material.COMPASS) {
            String stationName = ChatColor.stripColor(meta.getDisplayName());

            if (manager.isStation(stationName) && (manager.isOwner(stationName, p.getUniqueId()) || p.hasPermission("simpletrains.admin"))) {
                p.openInventory(configGui.create(stationName));
            } else {
                p.sendMessage(msg().getWithPrefix("gui-no-permission-configure"));
            }
            return;
        }

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
            p.sendMessage(msg().getWithPrefix("gui-config-station-not-found"));
            p.closeInventory();
            return;
        }

        if (itemName.equals("Edit Welcome Message")) {
            if (plugin.getMessageInputHandler().isWaitingForInput(p.getUniqueId())) {
                p.sendMessage(msg().getWithPrefix("gui-already-setting-message"));
                p.closeInventory();
                return;
            }

            plugin.getMessageInputHandler().startListening(p.getUniqueId(), stationName);

            p.sendMessage(msg().getWithPrefix("gui-enter-message", "STATION", stationName));
            p.sendMessage(msg().getWithPrefix("gui-color-codes-hint"));
            p.closeInventory();
            return;

        } else if (itemName.equals("Link Manager")) {
            p.openInventory(LinkManagerGui.create(manager, stationName));
            return;

        } else if (itemName.equals("DELETE STATION")) {
            p.sendMessage(msg().get("gui-delete-confirm", "STATION", stationName));
            p.closeInventory();
        } else if (itemName.equals("Back to Main List")) {
            p.openInventory(MainGui.create(manager, p));
        }
    }

    private void handleLinkManagerGuiClick(InventoryClickEvent event, Player p, ItemStack clickedItem, String inventoryTitle) {

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String stationName = ChatColor.stripColor(inventoryTitle.replace(LinkManagerGui.GUI_NAME + ChatColor.RESET + " - ", ""));
        String itemName = ChatColor.stripColor(meta.getDisplayName());

        if (itemName.equals("Back to Configuration")) {
            p.openInventory(configGui.create(stationName));
            return;
        }

        if (itemName.equals("Request New Link")) {
            p.sendMessage(msg().getWithPrefix("gui-enter-link-station"));
            p.sendMessage(msg().getWithPrefix("gui-link-request-hint"));

            plugin.getMessageInputHandler().startListening(p.getUniqueId(), "LINK_" + stationName);
            p.closeInventory();
            return;
        }

        if (itemName.equals("View Pending Requests")) {
            p.openInventory(PendingRequestsGui.create(plugin, manager, stationName));
            return;
        }

        if (clickedItem.getType() == Material.CHAIN || clickedItem.getType() == Material.LEVER) {
            String linkedStation = itemName;

            manager.unlinkStations(stationName, linkedStation);
            p.sendMessage(msg().getWithPrefix("gui-unlinked", "STATION_A", stationName, "STATION_B", linkedStation));

            p.openInventory(LinkManagerGui.create(manager, stationName));
            return;
        }
    }

    private void handlePendingRequestsGuiClick(InventoryClickEvent event, Player p, ItemStack clickedItem, String inventoryTitle) {

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String receivingStation = ChatColor.stripColor(inventoryTitle.replace(PendingRequestsGui.GUI_NAME + ChatColor.RESET + " - ", ""));
        String itemName = ChatColor.stripColor(meta.getDisplayName());

        if (itemName.equals("Back to Link Manager")) {
            p.openInventory(LinkManagerGui.create(manager, receivingStation));
            return;
        }

        if (clickedItem.getType() == Material.BOOK) {

            String initiatingStation = ChatColor.stripColor(itemName.replace("Request from ", ""));
            String initiatingStationFromManager = manager.getPendingInitiatingStation(receivingStation);

            if (initiatingStationFromManager == null || !initiatingStationFromManager.equals(initiatingStation)) {
                p.sendMessage(msg().getWithPrefix("gui-request-expired"));
                manager.clearLinkRequest(receivingStation);
                p.openInventory(PendingRequestsGui.create(plugin, manager, receivingStation));
                return;
            }

            UUID requesterId = manager.getPendingRequesterId(receivingStation);

            if (requesterId == null) {
                p.sendMessage(msg().getWithPrefix("gui-requester-missing"));
                manager.clearLinkRequest(receivingStation);
                p.openInventory(PendingRequestsGui.create(plugin, manager, receivingStation));
                return;
            }

            if (event.isLeftClick()) {
                int cost = plugin.getLinkAcceptanceXpCost();

                if (!p.hasPermission("simpletrains.admin") && p.getLevel() < cost) {
                    p.sendMessage(msg().getWithPrefix("error-not-enough-xp", "COST", String.valueOf(cost)));
                    return;
                }

                if (!p.hasPermission("simpletrains.admin")) {
                    p.setLevel(p.getLevel() - cost);
                }

                manager.linkStations(receivingStation, initiatingStation, StationManager.LinkType.PUBLIC);
                manager.clearLinkRequest(receivingStation);

                p.sendMessage(msg().getWithPrefix("gui-link-accepted", "STATION", initiatingStation));

                OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterId);
                if (requester.isOnline()) {
                    Player onlineRequester = requester.getPlayer();
                    onlineRequester.sendMessage(msg().getWithPrefix("link-request-accepted", "STATION_A", initiatingStation, "STATION_B", receivingStation));
                }

            } else if (event.isRightClick()) {

                manager.clearLinkRequest(receivingStation);
                p.sendMessage(msg().getWithPrefix("gui-link-rejected", "STATION", initiatingStation));
            }

            p.openInventory(PendingRequestsGui.create(plugin, manager, receivingStation));
        }
    }
}
