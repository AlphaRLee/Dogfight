package io.github.alpharlee.dogfight.registry;

import io.github.alpharlee.dogfight.damagesource.DamageSource;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.projectile.DfProjectile;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class DamageRegistry extends Registry {
    private HashMap<Player, DamageSource> playerDamages;
    private ArrayList<Player> removedPlayerDamages;

    public static int damageSourceMaxTicksLived = 20 * 3; //Keep 3 seconds of damage recorded

    public DamageRegistry(Game game) {
        super(game);

        playerDamages = new HashMap<Player, DamageSource>();
        removedPlayerDamages = new ArrayList<Player>();
    }

    /**
     * Get a map of all players and the damages applied to them
     *
     * @return
     * @author R Lee
     */
    public HashMap<Player, DamageSource> getPlayerDamages() {
        return this.playerDamages;
    }

    /**
     * Get the damage source associated to this player
     *
     * @param player
     * @return Damage source or null if not found
     * @author R Lee
     */
    public DamageSource getDamageSource(Player player) {
        return getPlayerDamages().get(player);
    }

    /**
     * Generically set the last damage of the player to the projectile specified.
     * Max lifespan of the damage source is specified by {@link DamageRegistry#damageSourceMaxTicksLived};
     *
     * @param player
     * @param projectile
     * @author R Lee
     */
    public void setPlayerDamage(Player player, DfProjectile projectile) {
        setPlayerDamage(player, new DamageSource(this, player, projectile, damageSourceMaxTicksLived));
    }

    /**
     * Set the last damager to the player (related to Dogfight)
     *
     * @param player
     * @param source
     * @author R Lee
     */
    public void setPlayerDamage(Player player, DamageSource source) {
        getPlayerDamages().put(player, source);
    }

    /**
     * Update all damage sources being tracked by this registry
     *
     * @author R Lee
     */
    public void updateDamageSources() {
        for (DamageSource source : playerDamages.values()) {
            source.update();
        }
    }

    /**
     * Remove the damage source from the registry
     *
     * @param source Damage source to remove
     * @param delay  Set to true to mark damage source for removal before fully removing
     * @author R Lee
     */
    public void remove(DamageSource source, boolean delay) {
        if (delay) {
            removedPlayerDamages.add(source.getTarget());
        } else {
            playerDamages.remove(source.getTarget());
        }
    }

    /**
     * Remove all damage sources marked for removal
     *
     * @author R Lee
     */
    public void removeMarkedDamageSources() {
        for (Player player : removedPlayerDamages) {
            playerDamages.remove(player);
        }

        removedPlayerDamages.clear();
    }

    /**
     * Remove all damage sources being tracked by this registry
     *
     * @author R Lee
     */
    public void clearDamageSources() {
        for (DamageSource damageSource : getPlayerDamages().values()) {
            damageSource.remove(true);
        }
    }
}
