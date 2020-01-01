package io.github.alpharlee.dogfight.floatingentity;

import io.github.alpharlee.dogfight.registry.FloatingEntityRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Something that has a location and a definitive lifespan
 * Commonly associated with Minecraft entities that can float, including floating items or floating targets made of entities.
 * However, the Minecraft entity can be non-existent and the FloatingEntity still functional
 *
 * @author R Lee
 */
public abstract class FloatingEntity {
    protected FloatingEntityRegistry registry;

    protected boolean alive = true;
    protected int ticksLived = 0;
    protected int maxTicksLived = 0; //Maximum ticks lived. If 0 or less, then has no influence

    protected Location location;

    protected Entity thisEntity = null; //Self reference for convenience, especially superclasses

    /**
     * Create a floating entity at the specified location
     *
     * @param registry
     * @param location
     */
    public FloatingEntity(FloatingEntityRegistry registry, Location location) {
        this(registry, location, false);
    }

    /**
     * Create a floating entity at the specified location
     *
     * @param registry
     * @param location
     * @param supressSpawn If set to true, prevents invoking {@link #spawn()}
     */
    public FloatingEntity(FloatingEntityRegistry registry, Location location, boolean supressSpawn) {
        setFloatingEntityRegistry(registry);
        setLocation(location);
    }

    public FloatingEntityRegistry getFloatingEntityRegistry() {
        return this.registry;
    }

    public void setFloatingEntityRegistry(FloatingEntityRegistry registry) {
        this.registry = registry;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Get the entity associated with this particular hoop, if set
     * </br>
     * This field is defaulted to null
     *
     * @return
     * @author R Lee
     */
    public Entity getEntity() {
        return thisEntity;
    }

    /**
     * Set the entity associated with this particular hoop
     *
     * @param entity
     * @author R Lee
     */
    public void setEntity(Entity entity) {
        this.thisEntity = entity;
    }

    public boolean isAlive() {
        return this.alive;
    }

    /**
     * Checks if entity is not null and not dead.
     * Defaults to returning false if the entity is not set
     *
     * @return
     * @author R Lee
     */
    protected boolean isEntityAlive() {
        return getEntity() != null && !getEntity().isDead();
    }

    public void setLiving(boolean alive) {
        this.alive = alive;
    }

    public int getTicksLived() {
        return this.ticksLived;
    }

    public void setTicksLived(int ticksLived) {
        this.ticksLived = ticksLived;
    }

    public int getMaxTicksLived() {
        return this.maxTicksLived;
    }

    /**
     * Set the maximum number of ticks lived this hoop has.
     * If set to 0 or less, then entity will live indefinitely
     *
     * @param maxTicksLived
     * @author R Lee
     */
    public void setMaxTicksLived(int maxTicksLived) {
        this.maxTicksLived = maxTicksLived;
    }

    /**
     * Update this floating entity
     *
     * @author R Lee
     */
    public abstract void update();

    /**
     * Check for removal of hoop.
     * If hoop is not alive or hoop outlived maxTicksLived (if set), remove
     *
     * @author R Lee
     */
    protected void checkForRemoval() {
        boolean remove = false;

        if (!isAlive()) {
            remove = true;
        }

        //Remove if maxTicksLived is set and ticksLived is greater than maxTicksLived
        if (ticksLived > maxTicksLived && maxTicksLived > 0) {
            remove = true;
        }

        if (remove) {
            remove(true);
        }
    }

    /**
     * Remove the hoop. If an entity is associated with the hoop, the entity is removed as well
     *
     * @param delay
     * @author R Lee
     */
    public void remove(boolean delay) {
        //Remove reference to this
        setLiving(false);

        if (isEntityAlive()) {
            getEntity().remove();
        }

        getFloatingEntityRegistry().removeFloatingEntity(this, delay);
    }
}
