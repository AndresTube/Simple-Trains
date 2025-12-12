package com.andrestube.simpletrains.gui;

import com.andrestube.simpletrains.SimpleTrains;
import com.andrestube.simpletrains.utils.Messages;
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

    private static Messages msg() {
        return SimpleTrains.getMessages();
    }

    public static Inventory create(SimpleTrains plugin, StationManager manager, String receivingStation) {
        String fullTitle = GUI_NAME + ChatColor.RESET + " - " + receivingStation;
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, fullTitle);

        int slot = 9;

        for (String initiatingStation : manager.getAllStationNames()) {
            if (manager.isRequestPending(receivingStation, initiatingStation)) {

                UUID requesterId = manager.getPendingRequesterId(receivingStation);
                OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterId);

                if (slot >= INVENTORY_SIZE - 9) break;

                int acceptanceCost = plugin.getLinkAcceptanceXpCost();

                ItemStack requestItem = new ItemStack(Material.BOOK);
                ItemMeta meta = requestItem.getItemMeta();

                meta.setDisplayName(ChatColor.YELLOW + "Request from " + initiatingStation);

                List<String> lore = new ArrayList<>();
                lore.add(msg().get("gui-lore-requester", "PLAYER", requester.getName()));
                lore.add(msg().get("gui-lore-station", "STATION", initiatingStation));
                lore.add(" ");
                lore.add(msg().get("gui-lore-accept-cost", "COST", String.valueOf(acceptanceCost)));
                lore.add(msg().get("gui-lore-reject"));

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
                msg().get("gui-item-back-link-manager")
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
