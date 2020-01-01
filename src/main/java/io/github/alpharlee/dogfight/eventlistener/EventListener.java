package io.github.alpharlee.dogfight.eventlistener;

import io.github.alpharlee.dogfight.CommandHandler;
import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.floatingentity.FloatingItem;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.projectile.DfProjectile;
import io.github.alpharlee.dogfight.projectile.ProjectileType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.List;

public class EventListener implements Listener {
    public EventListener() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        /*
         * TODO: Migrate ALL of the code in this method to another method not called on playerJoin explicitly
         */

        Player player = event.getPlayer();

        //TODO: Remove auto-join feature
        List<String> autoJoinNames = Dogfight.instance.getConfig().getStringList("autojoin");

        if (autoJoinNames.contains(player.getName())) {
            Dogfight.instance.testGame.getPlayerRegistry().addPlayer(player); //TODO: Change this line for team compatability
            CommandHandler.sendMessage(player, ChatColor.AQUA + "You have automatically joined the Dogfight test game. "
                    + "Type '/df leave' to leave the game or '/df autojoin' to stop automatically joining the game on login ");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        //Remove player from game
        Game game = Dogfight.instance.getGame(player);

        if (game != null) {
            //Remove player from game and from registry
            game.getPlayerRegistry().removePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        boolean shouldCancel = false;

        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        //Test to see if player right clicked
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            //Send to itemHandler to deal with
            shouldCancel = Dogfight.instance.getItemHandler().useItem(player, mainHandItem, event.getAction());
        }

        safeCancel(event, shouldCancel);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            Game game = Dogfight.instance.getGame(player);
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();

            if (game != null) {
                //Add bullet if player is in game and player threw a proper bullet
                if (mainHandItem.isSimilar(ProjectileType.BULLET.getItemStack())) {
                    //TODO: Verify casting to snowball
                    game.getProjectileRegistry().addBullet((Snowball) projectile);
                } else if (mainHandItem.isSimilar(ProjectileType.FIRE_BULLET.getItemStack())) {
                    game.getProjectileRegistry().addFireBullet((Snowball) projectile);
                }
            }
        } else {
            //TODO: Handle non-human throwers
        }

        //Do not cancel this event (allow snowball to be naturally thrown)
    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event) {
        //Prevent egg from hatching if egg is a projectile
        if (MetadataTag.PROJECTILE.hasMetadata(event.getEgg())) {
            event.setHatching(false);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == SpawnReason.EGG) {
            //KLUDGE: Iterate nearby eggs
            List<Entity> nearbyEntities = event.getEntity().getNearbyEntities(2, 2, 2); //Arbitrary numbers

            //Iterate all nearby eggs and cancel event if one is a projectile
            for (Entity nearbyEntity : nearbyEntities) {
                if (MetadataTag.PROJECTILE.hasMetadata(nearbyEntity) && nearbyEntity.getType() == EntityType.EGG) {
                    safeCancel(event, true);
                    return;
                }
            }
        }
    }

    /**
     * ------------------------------------------------------
     * TODO: KLUDGE: TEST FUNCTION REMOVE WHEN FINISHED
     * Replace a sugar item with a random dogfight item
     * ------------------------------------------------------
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();

        //KLUDGE: TEMP PLACEHOLDER, OVERRIDES ALL SUGAR
        if (item.getItemStack().getType() == Material.CLAY_BALL) {
            int amount = item.getItemStack().getAmount();
            double x = Math.random();

            ItemStack newItemStack = null;
            Particle particle = Particle.SMOKE_NORMAL;
            double radius = 1.5;

            if (x < 0.5) {
                newItemStack = new ItemStack(Material.POTION);
                PotionMeta pMeta = (PotionMeta) newItemStack.getItemMeta();

                pMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
                newItemStack.setItemMeta(pMeta);
            } else if (x >= 0.5 && x < 0.7) {
                newItemStack = new ItemStack(ProjectileType.SHOTGUN.getItemStack());
            } else if (x >= 0.7 && x < 0.9) {
                newItemStack = new ItemStack(ProjectileType.SNIPER.getItemStack());
            } else if (x >= 0.9 && x < 0.975) {
                newItemStack = new ItemStack(ProjectileType.MISSILE.getItemStack());
                particle = Particle.FLAME;
            } else {
                newItemStack = new ItemStack(ProjectileType.PLAYER_MISSILE.getItemStack());
                particle = Particle.FLAME;
            }

            newItemStack.setAmount(amount);
            item.setItemStack(newItemStack);

            //Definitely fix this: Reference to test game
            Dogfight.instance.testGame.getFloatingEntityRegistry().addFloatingEntity(new FloatingItem(Dogfight.instance.testGame.getFloatingEntityRegistry(), item.getLocation(), item, radius, particle));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        DfProjectile dfProjectile = MetadataTag.getProjectile(event.getEntity());

        if (dfProjectile != null) {
            //Apply damage through EventListener#onEntityDamageByEntity()

            if (dfProjectile.isAlive()) {
                //Remove if projectile hit a block
                dfProjectile.remove(false);
            }
        }
    }

    /*
     * Cancel the damage done by the projectile's original value and apply dfProjectile's hit damage
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        DfProjectile dfProjectile = MetadataTag.getProjectile(event.getDamager());

        if (dfProjectile != null) {
            //Apply the projectile hitting the target
            //This will follow up with a remove call
            dfProjectile.hit(event.getEntity());

            safeCancel(event, true);
        }
    }

    /**
     * Negate entity drops and exp drops if entity is a projectile
     *
     * @param event
     * @author R Lee
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        //If entity is a projectile or projectile passenger
        if (MetadataTag.getProjectile(entity) != null) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        //Assuming ExplosionSource will handle preventing damage there
        if (MetadataTag.PROJECTILE.hasMetadata(event.getRemover())) {
            safeCancel(event, true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        //Floating items have their own handler for item interaction
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (MetadataTag.TARGET.hasMetadata(event.getEntity()) || MetadataTag.PROJECTILE_PASSENGER.hasMetadata(event.getEntity()) || MetadataTag.TARGET.hasMetadata(event.getEntity())) {
            event.blockList().clear();
        }
    }

    /**
     * Convenience method: Cancel an event only if the event should be cancelled. Otherwise, do not cancel event (prevents damaging event cancellations for other plugins)
     *
     * @param cancellable  Body to cancel
     * @param setCancelled Set to true to cancel the event. Setting to false will not cancel the event nor force it to stay un-cancelled
     */
    public static void safeCancel(Cancellable cancellable, boolean setCancelled) {
        //Only cancel event if commanded to
        //NOTE: Do not explicitly set cancelled to false, other plugins may be relying on the event
        if (setCancelled) {
            cancellable.setCancelled(true);
        }
    }
}
