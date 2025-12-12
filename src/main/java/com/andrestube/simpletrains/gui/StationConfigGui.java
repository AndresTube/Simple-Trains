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

import java.util.Arrays;
import java.util.Collections;

public class StationConfigGui {

    public static final String GUI_NAME = ChatColor.DARK_RED + "Station Configuration";
    private final SimpleTrains plugin;
    private final StationManager manager;

    public StationConfigGui(SimpleTrains plugin, StationManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private Messages msg() {
        return SimpleTrains.getMessages();
    }

    public Inventory create(String stationName) {
        String fullTitle = GUI_NAME + ChatColor.RESET + " - " + stationName;
        Inventory inventory = Bukkit.createInventory(null, 27, fullTitle);

        // 1. Edit Welcome Message (Slot 10)
        inventory.setItem(10, createConfigItem(
                Material.WRITABLE_BOOK,
                msg().get("gui-item-edit-message"),
                msg().get("gui-lore-current-message", "MESSAGE", ChatColor.translateAlternateColorCodes('&', manager.getStationMessage(stationName)))
        ));

        // 2. Manage Links (Slot 13)
        inventory.setItem(13, createConfigItem(
                Material.CHAIN,
                msg().get("gui-item-link-manager"),
                msg().get("gui-lore-linked-count", "COUNT", String.valueOf(manager.getLinkedStations(stationName).size())),
                msg().get("gui-lore-click-manage-links")
        ));

        // 3. Delete Station (Slot 16)
        inventory.setItem(16, createConfigItem(
                Material.BARRIER,
                msg().get("gui-item-delete-station"),
                msg().get("gui-lore-delete-warning")
        ));

        // 4. Back button (Slot 22)
        inventory.setItem(22, createConfigItem(
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                msg().get("gui-item-back-main")
        ));

        return inventory;
    }

    private ItemStack createConfigItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        } else {
            meta.setLore(Collections.emptyList());
        }
        item.setItemMeta(meta);
        return item;
    }
}
