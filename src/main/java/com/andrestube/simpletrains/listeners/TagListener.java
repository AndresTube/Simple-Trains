// src/main/java/com/andrestube/simpletrains/listeners/TagListener.java

package com.andrestube.simpletrains.listeners;

import com.andrestube.simpletrains.SimpleTrains;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class TagListener implements Listener {

    private final SimpleTrains plugin;
    public static final String ST_ELIGIBLE_TAG = "ST_WARP_ELIGIBLE"; 
    private static final String PREFIX = ChatColor.GOLD + "[SimpleTrains] " + ChatColor.RESET;

    public TagListener(SimpleTrains plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMinecartTag(PlayerInteractEntityEvent event) {
        Entity clickedEntity = event.getRightClicked();
        
        // 1. Check if the entity is a Minecart
        if (!(clickedEntity instanceof Minecart)) {
            return;
        }
        
        Player p = event.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        
        // 2. Check if the player is holding a Name Tag
        if (item == null || item.getType() != Material.NAME_TAG) {
            return;
        }
        
        Minecart cart = (Minecart) clickedEntity;
        ItemMeta itemMeta = item.getItemMeta();
        
        // 3. Check if the Name Tag has a custom name (must be renamed in anvil)
        if (itemMeta == null || !itemMeta.hasDisplayName()) {
            p.sendMessage(PREFIX + ChatColor.RED + "The Name Tag must be renamed in an anvil to register the Minecart.");
            event.setCancelled(true); // Prevent consuming the name tag
            return;
        }

        // --- Core Tagging Logic ---
        String newName = itemMeta.getDisplayName();
        
        // 4. Apply Custom Name (Directly on Minecart/Entity)
        cart.setCustomName(newName);
        cart.setCustomNameVisible(true);
        
        // 5. Apply Persistent Eligibility Tag
        cart.setMetadata(ST_ELIGIBLE_TAG, new FixedMetadataValue(plugin, true));
        
        // 6. Consume the Name Tag
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            p.getInventory().setItemInMainHand(null);
        }

        p.sendMessage(PREFIX + ChatColor.GREEN + "Minecart registered as " + newName + ChatColor.GREEN + " and is now Warp-Eligible!");
        
        event.setCancelled(true); // Prevent default Name Tag behavior
    }
}