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
 * Manages station creation costs.
 * Supports XP, Vault economy, and item costs.
 */
public class CreationCostManager {

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

    public CreationCostManager(SimpleTrains plugin) {
        this.plugin = plugin;
        loadConfig();
        setupVault();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("creation_cost.enabled", true);

        // Parse cost type
        String typeStr = config.getString("creation_cost.type", "XP").toUpperCase();
        try {
            this.costType = CostType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid creation cost type '" + typeStr + "', defaulting to XP.");
            this.costType = CostType.XP;
        }

        this.amount = config.getInt("creation_cost.amount", 5);

        // Item configuration
        String materialStr = config.getString("creation_cost.item.material", "GOLD_INGOT");
        try {
            this.itemMaterial = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid item material '" + materialStr + "', defaulting to GOLD_INGOT.");
            this.itemMaterial = Material.GOLD_INGOT;
        }

        this.itemAmount = config.getInt("creation_cost.item.amount", 1);
        this.itemName = config.getString("creation_cost.item.name", "");

        // Backwards compatibility: check old config format
        if (!config.contains("creation_cost.enabled")) {
            int oldCost = config.getInt("settings.creation_xp_cost", 5);
            this.enabled = oldCost > 0;
            this.amount = oldCost;
            this.costType = CostType.XP;
        }
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
     * Check if the player can afford the creation cost.
     */
    public boolean canAfford(Player player) {
        if (!enabled) return true;

        // Admins bypass creation cost
        if (player.hasPermission("simpletrains.admin")) {
            return true;
        }

        switch (costType) {
            case XP:
                return player.getLevel() >= amount;

            case VAULT:
                if (!vaultAvailable || economy == null) {
                    return true;
                }
                return economy.has(player, amount);

            case ITEM:
                return hasRequiredItems(player);

            default:
                return true;
        }
    }

    /**
     * Charge the player the creation cost.
     */
    public boolean charge(Player player) {
        if (!enabled) return true;

        // Admins bypass creation cost
        if (player.hasPermission("simpletrains.admin")) {
            player.sendMessage(SimpleTrains.getMessages().get("creation-cost-bypass"));
            return true;
        }

        switch (costType) {
            case XP:
                if (player.getLevel() >= amount) {
                    player.setLevel(player.getLevel() - amount);
                    return true;
                }
                return false;

            case VAULT:
                if (!vaultAvailable || economy == null) {
                    player.sendMessage(SimpleTrains.getMessages().getWithPrefix("vault-not-found"));
                    return true;
                }
                if (economy.has(player, amount)) {
                    economy.withdrawPlayer(player, amount);
                    return true;
                }
                return false;

            case ITEM:
                return removeRequiredItems(player);

            default:
                return true;
        }
    }

    /**
     * Send the appropriate "not enough" message to the player.
     */
    public void sendNotEnoughMessage(Player player) {
        Messages msg = SimpleTrains.getMessages();

        switch (costType) {
            case XP:
                player.sendMessage(msg.getWithPrefix("creation-not-enough-xp",
                    "COST", String.valueOf(amount),
                    "CURRENT", String.valueOf(player.getLevel())));
                break;

            case VAULT:
                if (vaultAvailable && economy != null) {
                    double balance = economy.getBalance(player);
                    player.sendMessage(msg.getWithPrefix("creation-not-enough-money",
                        "COST", String.valueOf(amount),
                        "CURRENT", String.format("%.2f", balance)));
                }
                break;

            case ITEM:
                String itemDisplayName = itemName.isEmpty() ?
                    formatMaterialName(itemMaterial) :
                    ChatColor.translateAlternateColorCodes('&', itemName);
                player.sendMessage(msg.getWithPrefix("creation-not-enough-items",
                    "AMOUNT", String.valueOf(itemAmount),
                    "ITEM", itemDisplayName));
                break;
        }
    }

    /**
     * Get a formatted cost string for display.
     */
    public String getCostDisplay() {
        if (!enabled) return "Free";

        switch (costType) {
            case XP:
                return amount + " XP";
            case VAULT:
                return "$" + amount;
            case ITEM:
                String name = itemName.isEmpty() ? formatMaterialName(itemMaterial) : itemName;
                return itemAmount + "x " + name;
            default:
                return "Free";
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
            return true;
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
