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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinkManagerGui {

    public static final String GUI_NAME = ChatColor.DARK_AQUA + "Link Manager";
    private static final int INVENTORY_SIZE = 54;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int REQUEST_NEW_SLOT = 47;
    private static final int PENDING_REQUESTS_SLOT = 51;

    private static Messages msg() {
        return SimpleTrains.getMessages();
    }

    public static Inventory create(StationManager manager, String stationName) {
        String fullTitle = GUI_NAME + ChatColor.RESET + " - " + stationName;
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, fullTitle);

        Map<String, StationManager.LinkType> linksMap = manager.getStationLinksMap(stationName);

        // 1. Add Current Linked Stations (Starting at slot 9)
        int slot = 9;
        for (Map.Entry<String, StationManager.LinkType> entry : linksMap.entrySet()) {
            if (slot >= INVENTORY_SIZE - 9) break;

            String linkedStation = entry.getKey();
            StationManager.LinkType type = entry.getValue();

            ItemStack linkItem = new ItemStack(type == StationManager.LinkType.PUBLIC ? Material.CHAIN : Material.LEVER);
            ItemMeta meta = linkItem.getItemMeta();

            meta.setDisplayName(ChatColor.AQUA + linkedStation);

            List<String> lore = new ArrayList<>();
            lore.add(type == StationManager.LinkType.PUBLIC ? msg().get("gui-lore-link-public") : msg().get("gui-lore-link-private"));
            lore.add(msg().get("gui-lore-left-click-unlink"));

            meta.setLore(lore);
            linkItem.setItemMeta(meta);

            inventory.setItem(slot++, linkItem);
        }

        // 2. Fill top row and main area with filler panes
        for (int i = 0; i < INVENTORY_SIZE - 9; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                Material fillerMat = (i < 9) ? Material.LIGHT_BLUE_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
                inventory.setItem(i, createFillerItem(fillerMat, " "));
            }
        }

        // 3. Add Control Items (Bottom row)

        // Request New Link Button (Slot 47)
        inventory.setItem(REQUEST_NEW_SLOT, createControlItem(
                Material.PAPER,
                msg().get("gui-item-request-new-link"),
                msg().get("gui-lore-request-link-desc")
        ));

        // View Pending Requests Button (Slot 51)
        inventory.setItem(PENDING_REQUESTS_SLOT, createControlItem(
                Material.CLOCK,
                msg().get("gui-item-view-pending"),
                msg().get("gui-lore-pending-desc")
        ));

        // Back to Config Button (Slot 49)
        inventory.setItem(BACK_BUTTON_SLOT, createControlItem(
                Material.SPECTRAL_ARROW,
                msg().get("gui-item-back-config")
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
