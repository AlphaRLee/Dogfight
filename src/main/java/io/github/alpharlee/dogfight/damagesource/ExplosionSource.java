package io.github.alpharlee.dogfight.damagesource;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.eventlistener.EventListener;
import io.github.alpharlee.dogfight.projectile.DfProjectile;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionSource implements Listener {
    private DfProjectile projectile;
    private float power;
    private boolean isIncendiary;
    private boolean destroyBlocks;

    public ExplosionSource(DfProjectile projectile, float power, boolean isIncendiary, boolean destroyBlocks) {
        this.projectile = projectile;
        this.power = power;
        this.isIncendiary = isIncendiary;
        this.destroyBlocks = destroyBlocks;

        //Register upcoming explosion
        Dogfight.instance.getServer().getPluginManager().registerEvents(this, Dogfight.instance);

        //Immediately trigger an explosion
        //KLUDGE: Exploiting single-thread nature of Bukkit/Spigot
        explode();
    }

    /**
     * Generate an explosion and exploit Bukkit/Spigot's single-thread nature to handle the explosion event prior to the completion
     * of this method
     * More details at: https://www.spigotmc.org/threads/how-to-make-a-player-responcible-for-an-explosion.41973/
     *
     * @author R Lee
     */
    private void explode() {
        Location loc = projectile.getProjectileEntity().getLocation();

        /*
         * Create an explosion at the site of the projectile
         * param 1-3: Coordinates
         * param 4: Power (4f is equivalent to Minecraft TNT explosion)
         * param 5: setFire
         * param 6: breakBlocks
         */
        projectile.getProjectileEntity().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, isIncendiary, destroyBlocks);

        /*
         * KLUDGE: Exploiting single-thread nature of Bukkit/Spigot
         * The event handler for the explosion will trigger prior to the following lines
         */

        projectile = null; //Remove reference (use reference for event handler
        HandlerList.unregisterAll(this); //Remove this listener
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        //TODO: Get source of explosion and ignore non-projectile related explosions
        //If a projectile is caught in an explosion
        if (event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION) {
            DfProjectile damagedProjectile = MetadataTag.getProjectile(entity);
            /*
             * If projectile is:
             * Not null
             * Not this projectile (prevent recursive removal errors)
             * Alive
             *
             * then remove
             */
            if (damagedProjectile != null && !damagedProjectile.equals(this.projectile) && !damagedProjectile.markedForRemoval && damagedProjectile.isAlive()) {
                damagedProjectile.remove(true);
            }

            switch (entity.getType()) {
                //Do not damage the following
                case PAINTING:
                case ITEM_FRAME:
                case ARMOR_STAND:
                    EventListener.safeCancel(event, true);
                    break;

                default:

                    //KLUDGE Exploit: Projectile will only not be null if the damage source was this explosion
                    if (this.projectile != null) {
                        //Apply custom damage if attacking a io.github.alpharlee.dogfight.floatingentity.target instead of a typical entity
                        if (MetadataTag.TARGET.hasMetadata(entity)) {
                            MetadataTag.TARGET.getTarget(entity).damage(event.getFinalDamage());
                        } else if (entity instanceof Damageable) //Check if non-target is damageable
                        {
                            //Register dogfight-related damage
                            //MUST precede actual application of damage
                            if (entity.getType() == EntityType.PLAYER) {
                                projectile.getProjectileRegistry().getGame().getDamageRegistry().setPlayerDamage((Player) entity, projectile);
                            }

                            //Explicitly deal damage to the entity to bypass WorldGuard's blocking technique. Set damager to be owner
                            ((Damageable) entity).damage(event.getFinalDamage(), this.projectile.getOwner());

                            //Do not apply any further damage
                            EventListener.safeCancel(event, true);
                        }
                    }

                    break;
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (MetadataTag.PROJECTILE.hasMetadata(event.getEntity()) || MetadataTag.PROJECTILE_PASSENGER.hasMetadata(event.getEntity())) {
            event.blockList().clear();
        }
    }
}
