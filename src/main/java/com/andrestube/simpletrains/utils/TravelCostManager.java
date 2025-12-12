package com.andrestube.simpletrains.utils;

import com.andrestube.simpletrains.SimpleTrains;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages travel costs for train stations.
 * Supports XP, Vault economy, and item costs.
 */
public class TravelCostManager {

    public enum CostType {
        XP, VAULT, ITEM
    }

    private final SimpleTrains plugin;

    private boolean enabled;
    private CostType costType;
    private int amount;
    private Material itemMaterial;
    private int itemAmount;
    private String itemName;

    // Vault economy (loaded dynamically)
    private Economy economy;
    private boolean vaultAvailable;

    public TravelCostManager(SimpleTrains plugin) {
        this.plugin = plugin;
        loadConfig();
        setupVault();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("travel_cost.enabled", false);

        // Parse cost type
        String typeStr = config.getString("travel_cost.type", "XP").toUpperCase();
        try {
            this.costType = CostType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid travel cost type '" + typeStr + "', defaulting to XP.");
            this.costType = CostType.XP;
        }

        this.amount = config.getInt("travel_cost.amount", 1);

        // Item configuration
        String materialStr = config.getString("travel_cost.item.material", "GOLD_INGOT");
        try {
            this.itemMaterial = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid item material '" + materialStr + "', defaulting to GOLD_INGOT.");
            this.itemMaterial = Material.GOLD_INGOT;
        }

        this.itemAmount = config.getInt("travel_cost.item.amount", 1);
        this.itemName = config.getString("travel_cost.item.name", "");
    }

    private void setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            this.vaultAvailable = false;
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.vaultAvailable = false;
            return;
        }

        this.economy = rsp.getProvider();
        this.vaultAvailable = true;
    }

    /**
     * Check if the player can afford the travel cost.
     *
     * @param player The player to check
     * @return true if the player can afford, false otherwise
     */
    public boolean canAfford(Player player) {
        if (!enabled) return true;

        // Admins bypass travel cost
        if (player.hasPermission("simpletrains.admin")) {
            return true;
        }

        switch (costType) {
            case XP:
                return player.getLevel() >= amount;

            case VAULT:
                if (!vaultAvailable || economy == null) {
                    return true; // No economy, allow travel
                }
                return economy.has(player, amount);

            case ITEM:
                return hasRequiredItems(player);

            default:
                return true;
        }
    }

    /**
     * Charge the player the travel cost.
     *
     * @param player The player to charge
     * @return true if successfully charged, false otherwise
     */
    public boolean charge(Player player) {
        if (!enabled) return true;

        // Admins bypass travel cost
        if (player.hasPermission("simpletrains.admin")) {
            player.sendMessage(SimpleTrains.getMessages().getWithPrefix("travel-cost-bypass"));
            return true;
        }

        switch (costType) {
            case XP:
                if (player.getLevel() >= amount) {
                    player.setLevel(player.getLevel() - amount);
                    player.sendMessage(SimpleTrains.getMessages().get("travel-cost-xp", "COST", String.valueOf(amount)));
                    return true;
                }
                return false;

            case VAULT:
                if (!vaultAvailable || economy == null) {
                    player.sendMessage(SimpleTrains.getMessages().getWithPrefix("vault-not-found"));
                    return true; // Allow travel if Vault not available
                }
                if (economy.has(player, amount)) {
                    economy.withdrawPlayer(player, amount);
                    player.sendMessage(SimpleTrains.getMessages().get("travel-cost-money", "COST", String.valueOf(amount)));
                    return true;
                }
                return false;

            case ITEM:
                if (removeRequiredItems(player)) {
                    String itemDisplayName = itemName.isEmpty() ?
                        formatMaterialName(itemMaterial) :
                        ChatColor.translateAlternateColorCodes('&', itemName);
                    player.sendMessage(SimpleTrains.getMessages().get("travel-cost-item",
                        "AMOUNT", String.valueOf(itemAmount),
                        "ITEM", itemDisplayName));
                    return true;
                }
                return false;

            default:
                return true;
        }
    }

    /**
     * Send the appropriate "not enough" message to the player.
     *
     * @param player The player to send the message to
     */
    public void sendNotEnoughMessage(Player player) {
        Messages msg = SimpleTrains.getMessages();

        switch (costType) {
            case XP:
                player.sendMessage(msg.getWithPrefix("travel-not-enough-xp",
                    "COST", String.valueOf(amount),
                    "CURRENT", String.valueOf(player.getLevel())));
                break;

            case VAULT:
                if (vaultAvailable && economy != null) {
                    double balance = economy.getBalance(player);
                    player.sendMessage(msg.getWithPrefix("travel-not-enough-money",
                        "COST", String.valueOf(amount),
                        "CURRENT", String.format("%.2f", balance)));
                }
                break;

            case ITEM:
                String itemDisplayName = itemName.isEmpty() ?
                    formatMaterialName(itemMaterial) :
                    ChatColor.translateAlternateColorCodes('&', itemName);
                player.sendMessage(msg.getWithPrefix("travel-not-enough-items",
                    "AMOUNT", String.valueOf(itemAmount),
                    "ITEM", itemDisplayName));
                break;
        }
    }

    private boolean hasRequiredItems(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == itemMaterial) {
                if (matchesItemName(item)) {
                    count += item.getAmount();
                }
            }
        }
        return count >= itemAmount;
    }

    private boolean removeRequiredItems(Player player) {
        if (!hasRequiredItems(player)) return false;

        int remaining = itemAmount;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == itemMaterial && matchesItemName(item)) {
                int take = Math.min(remaining, item.getAmount());
                remaining -= take;

                if (take >= item.getAmount()) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - take);
                }
            }
        }

        return remaining == 0;
    }

    private boolean matchesItemName(ItemStack item) {
        if (itemName == null || itemName.isEmpty()) {
            return true; // No name requirement
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        String requiredName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', itemName));
        return displayName.equalsIgnoreCase(requiredName);
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (c == ' ') {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    // Getters
    public boolean isEnabled() {
        return enabled;
    }

    public CostType getCostType() {
        return costType;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isVaultAvailable() {
        return vaultAvailable;
    }
}
