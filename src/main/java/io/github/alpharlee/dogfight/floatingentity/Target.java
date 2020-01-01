package io.github.alpharlee.dogfight.floatingentity;

import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.registry.FloatingEntityRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class Target extends FloatingEntity {
    /*
     * TODO:
     * -Add spawn/despawn
     * -Add extending class "itemTarget"
     * --Remove item
     * --Sync to end crystal
     */

    protected double health;
    protected double maxHealth;
    protected boolean invincible = true;

    /**
     * Instatinate an invincible target
     *
     * @param registry
     * @param location
     */
    public Target(FloatingEntityRegistry registry, Location location) {
        super(registry, location);
        setInvincible(true);
    }

    /**
     * Instantiate a target at the specified health value
     *
     * @param registry
     * @param location
     * @param health
     */
    public Target(FloatingEntityRegistry registry, Location location, double health) {
        super(registry, location);

        setInvincible(false);
        setHealth(health);
        setMaxHealth(health);
    }

    public double getHealth() {
        return this.health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }

    public boolean isInvincible() {
        return this.invincible;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    /**
     * Set the max health of this target
     *
     * @param maxHealth
     * @author R Lee
     */
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setMetadataTag(Entity targetEntity) {
        MetadataTag.TARGET.setMetadata(targetEntity, this);
    }

    /**
     * Damage the target by the specified amount, subtracting from {@link Target#getHealth()}
     * If damage amount is equal to or greater than remaining health, {@link Target#destroy()} is invoked and health is set to 0
     * Does nothing if {@link Target#isInvincible()} is true
     *
     * @param damageAmount Amount to damage the target by
     * @return true if target was damaged, false if target is invincible
     * @author R Lee
     */
    public boolean damage(double damageAmount) {
        if (isInvincible()) {
            return false;
        }

        setHealth(getHealth() - damageAmount);

        //Destroy target if health <= 0
        if (getHealth() <= 0) {
            destroy();
        }

        return true;
    }

    /**
     * Invokes {@link Target#onDestroy()} followed by {@link Target#remove(boolean)}
     *
     * @author R Lee
     */
    protected void destroy() {
        onDestroy();
        remove(true);
    }

    /**
     * Method called when target is destroyed
     *
     * @author R Lee
     */
    protected abstract void onDestroy();
}
