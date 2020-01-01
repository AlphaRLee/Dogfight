package io.github.alpharlee.dogfight.projectile;

import io.github.alpharlee.dogfight.ItemHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ProjectileType {
    BULLET(Material.SNOWBALL, ChatColor.YELLOW + "" + ChatColor.ITALIC + "Bullet", new String[]{
            ChatColor.YELLOW + "Throw at your",
            ChatColor.YELLOW + "opponents",
            "",
            ChatColor.WHITE + "" + ChatColor.ITALIC + "Ready! Aim! FIRE!"
    }),
    FIRE_BULLET(Material.SNOWBALL, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "Fire " + ChatColor.YELLOW + "" + ChatColor.ITALIC + "Bullet", new String[]{
            ChatColor.YELLOW + "Throw at your opponents",
            ChatColor.YELLOW + "to light them up",
            "",
            ChatColor.WHITE + "They'll go down in flames"
    }, true),
    MINE(Material.TNT, ChatColor.RED + "" + ChatColor.ITALIC + "Mine", new String[]{
            ChatColor.YELLOW + "A tricky mine",
            ChatColor.YELLOW + "to blow you opponents",
            ChatColor.YELLOW + "up with",
            "",
            ChatColor.WHITE + "Mine your own business"
    }),
    MISSILE(Material.SPECTRAL_ARROW, ChatColor.RED + "" + ChatColor.ITALIC + "Missile", new String[]{
            ChatColor.YELLOW + "Homing missile to",
            ChatColor.YELLOW + "aim at your targets",
            "",
            ChatColor.WHITE + "INCOMING!"
    }),
    PLAYER_MISSILE(Material.SPECTRAL_ARROW, ChatColor.RED + "" + ChatColor.ITALIC + "Missile", new String[]{
            ChatColor.YELLOW + "Homing missile to",
            ChatColor.YELLOW + "aim at your targets",
            "",
            ChatColor.WHITE + "Human hunter mode: " + ChatColor.GREEN + "Enabled"
    }, true),
    SHOTGUN(Material.PUMPKIN_SEEDS, ChatColor.GOLD + "" + ChatColor.ITALIC + "Shotgun", new String[]{
            ChatColor.YELLOW + "A burst of fun",
            ChatColor.YELLOW + "for everyone",
            "",
            ChatColor.WHITE + "I call shotgun!"
    }),
    SNIPER(Material.GHAST_TEAR, ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.ITALIC + "Sniper" + ChatColor.YELLOW + ChatColor.ITALIC + " Bullet", new String[]{
            ChatColor.YELLOW + "Eliminate your",
            ChatColor.YELLOW + "target with ease",
            "",
            ChatColor.WHITE + "The real bang for your buck"
    }),
    SQUID_SHOT(Material.INK_SAC, ChatColor.DARK_BLUE + "" + ChatColor.ITALIC + "Squid", new String[]{
            ChatColor.YELLOW + "Squid doesn't hurt",
            ChatColor.YELLOW + "but it will blind",
            "",
            ChatColor.WHITE + "Oops!... I Squid It Again"
    });

    private Material material;
    private String itemName;
    private String[] itemLore;
    private boolean enchantShine;

    private ItemStack itemStack;

    /**
     * Create a ProjectileType where the item stack does NOT have an enchant shine
     *
     * @param material
     * @param itemName
     * @param itemLore
     */
    private ProjectileType(Material material, String itemName, String[] itemLore) {
        this(material, itemName, itemLore, false);
    }

    private ProjectileType(Material material, String itemName, String[] itemLore, boolean enchantShine) {
        this.material = material;
        this.itemName = itemName;
        this.itemLore = itemLore;
        this.enchantShine = enchantShine;

        this.itemStack = new ItemStack(material);
        ItemHandler.setItemStackMeta(itemStack, itemName, itemLore, enchantShine);
    }

    /**
     * Get this item stack's material
     *
     * @return
     * @author R Lee
     */
    public Material getMaterial() {
        return this.material;
    }

    public String getItemName() {
        return this.itemName;
    }

    /**
     * Get the item stack associated with this item
     *
     * @return
     * @author R Lee
     */
    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
