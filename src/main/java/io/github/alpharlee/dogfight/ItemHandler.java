package io.github.alpharlee.dogfight;

import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.projectile.ProjectileType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ItemHandler {
    /**
     * Set the meta for an item stack (cosmetic). Item will default to unbreakable and always hide enchants and unbreakable flags.
     *
     * @param item            ItemStack to set meta for
     * @param displayName     Display name
     * @param lore            Lore
     * @param addEnchantShine Set to true to add unbreaking X to item (give a shiny luster)
     */
    public static void setItemStackMeta(ItemStack item, String displayName, String[] lore, boolean addEnchantShine) {
        Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();

        if (addEnchantShine) {
            //Random enchant. Not terribly important which one, since it will be hidden anyway
            enchants.put(Enchantment.DURABILITY, 10);
        }

        setItemStackMeta(item, displayName, lore, enchants, true, true);
    }

    /**
     * Set the meta for an item stack
     *
     * @param item        ItemStack to set meta for
     * @param displayName Display name
     * @param lore        Lore
     * @param enchants    Key represents desired enchant, values represents level. Will always aloow enchants beyond the maximum conventional level
     * @param unbreakable Set to true for unbreakable
     * @param hideFlags   Set to true to hide enchants and unbreakable flag
     */
    public static void setItemStackMeta(ItemStack item, String displayName, String[] lore, Map<Enchantment, Integer> enchants, boolean unbreakable, boolean hideFlags) {
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(getArrayListFromArray(lore));

        for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

//		itemMeta.spigot().setUnbreakable(unbreakable);
        itemMeta.setUnbreakable(unbreakable);

        if (hideFlags) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        item.setItemMeta(itemMeta);
    }

    /**
     * Convenience method: Convert an array into an arraylist
     *
     * @param args Array of strings
     * @return ArrayList instance of array
     */
    private static ArrayList<String> getArrayListFromArray(String[] args) {
        ArrayList<String> returnList = new ArrayList<String>();

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                returnList.add(args[i]);
            }
        }

        return returnList;
    }

    /**
     * Handles the player interacting with an item
     *
     * @param player Player
     * @param item   Item being interacted with
     * @param action Item interaction (Eg. Action.LEFT_CLICK_AIR)
     * @return true if the handler ran successfully and the event source is recommended to be cancelled.
     */
    public boolean useItem(Player player, ItemStack item, Action action) {
        boolean suggestCancel = false;
        Game game = Dogfight.instance.getGame(player);

        //TODO: Fill with item-related events

        //Game related items:
        if (game != null) {
            //Spawn a missile
            if (item.isSimilar(ProjectileType.MISSILE.getItemStack())) {
                //Standard missile
                suggestCancel = game.getProjectileRegistry().spawnMissile(player, false);
            } else if (item.isSimilar(ProjectileType.PLAYER_MISSILE.getItemStack())) {
                //Player missile
                suggestCancel = game.getProjectileRegistry().spawnMissile(player, true);
            } else if (item.isSimilar(ProjectileType.SHOTGUN.getItemStack())) {
                suggestCancel = game.getProjectileRegistry().spawnShotgunBullets(player);
            } else if (item.isSimilar(ProjectileType.SNIPER.getItemStack())) {
                suggestCancel = game.getProjectileRegistry().spawnSniperBullet(player);
            } else if (item.isSimilar(ProjectileType.SQUID_SHOT.getItemStack())) {
                suggestCancel = game.getProjectileRegistry().spawnSquidShot(player);
            } else if (item.isSimilar(ProjectileType.MINE.getItemStack())) {
                suggestCancel = game.getProjectileRegistry().spawnMine(player);
            } else {
                /*
                 * TODO: Remove temp function
                 * Give the player a projectile and remove the item in their hand
                 */
                suggestCancel = tempGiveProjectile(player, item);
            }
        }

        return suggestCancel;
    }

    /**
     * TODO: Remove this convenience function
     * Exchange regular looking items for deadly weapons! Fun fun fun!
     *
     * @author R Lee
     */
    private boolean tempGiveProjectile(Player player, ItemStack item) {
        Game game = Dogfight.instance.getGame(player);

        if (game != null && player.hasPermission("dogfight.quickgetitem")) {
            ItemStack receivedItem = null;

            switch (item.getType()) {
                case GOLD_NUGGET:
                case GHAST_TEAR:

                    receivedItem = new ItemStack(ProjectileType.SNIPER.getItemStack());
                    break;

                case PUMPKIN_SEEDS:
                    receivedItem = new ItemStack(ProjectileType.SHOTGUN.getItemStack());
                    break;

                case INK_SAC:

                    switch (item.getDurability()) {
                        case 0: //Black
                            receivedItem = new ItemStack(ProjectileType.SQUID_SHOT.getItemStack());
                            break;

                        case 1: //Red
                            receivedItem = new ItemStack(ProjectileType.MINE.getItemStack());
                            break;

                        case 14: //Orange
                            receivedItem = new ItemStack(ProjectileType.FIRE_BULLET.getItemStack());
                            break;

                        default:
                            //Nothing
                            break;
                    }

                    break;

                case SUGAR:
                    receivedItem = new ItemStack(ProjectileType.MISSILE.getItemStack());
                    break;

                case GLOWSTONE_DUST:
                    receivedItem = new ItemStack(ProjectileType.PLAYER_MISSILE.getItemStack());
                    break;

                default:
                    //Don't do anything
                    break;
            }

            //If item not equal null and requested item is slightly different than original item
            if (receivedItem != null && !receivedItem.isSimilar(item)) {
                receivedItem.setAmount(item.getAmount());
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), receivedItem);
                game.getPlayerRegistry().updatePlayer(player);

                player.sendMessage("Dogfight DEBUG: Projectile(s) received!");

                return true;
            }
        }

        return false;
    }

    /**
     * Handles the player dropping an item
     *
     * @param player Player
     * @param item   Item being dropped
     * @return true if the handler ran successfully and the event is recommended to be cancelled.
     */
    public boolean dropItem(Player player, ItemStack item) {
        boolean suggestCancel = false;
		
		/*
		 * Sample code
		if (item.isSimilar(ItemHandler.selectWand) || item.isSimilar(ItemHandler.commandWand))
		{
			selector.clearPos();
			selector.clearSelectedEntities();
			
			player.sendMessage(ChatColor.DARK_AQUA + "Mob selection cleared");
			
			suggestCancel = true;
		}
		*/

        return suggestCancel;
    }
}
