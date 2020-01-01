package io.github.alpharlee.dogfight.projectile;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.alert.AlertLevel;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.game.Team;
import io.github.alpharlee.dogfight.registry.ProjectileRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class DfProjectile {
    protected ProjectileRegistry projectileRegistry;
    protected Player owner; //Owner of who launched the projectile
    protected Projectile projectileEntity;

    protected Vector initialVelocity;
    protected double speed; //Speed to set projectile at. Not necessarily in same direction as initial velocity
    private double speedMultiplier = -999;    //Constant to multiply current velocity by to achieve speed
    //Defaults to -999 (invalid number, must be greater than 0)
    protected int ticksLived;
    protected int maxTicksLived;

    protected double hitRadius;
    protected double damageValue;
    protected Particle hitParticle;
    protected Sound playerHitSound;

    public boolean updateSpeedMultiplier = true; //Flag indicating speed multiplier needs to be set for optimization
    public boolean markedForRemoval = false;

    /**
     * Create a Dogfight Projectile
     *
     * @param projectileRegistry Projectile registry to store the projectile in
     * @param owner              Owner of projectile
     * @param projectileEntity   Projectile entity that represents this projectile
     * @param initialVelocity    Initial velocity to launch the projectile at
     * @param maxTicksLived      Maximum number of thicks this projectile will live before being calling {@link DfProjectile#remove(false)}
     * @param hitRadius          Radius to trigger collision with another entity. Does not influence projectile's relation to blocks
     * @param damageValue        Amount of damage to apply to an entity when hit. Measured in half-hearts
     * @param hitParticle        Particle to play when entity is hit. Set to null to not play any particle
     * @param playerHitSound     Sound to play to the owner when a player is hit. Set to null to not play any sound
     */
    public DfProjectile(ProjectileRegistry projectileRegistry, Player owner, Projectile projectileEntity,
                        Vector initialVelocity, int maxTicksLived,
                        double hitRadius, double damageValue, Particle hitParticle, Sound playerHitSound) {
        setup(projectileRegistry, owner, projectileEntity,
                initialVelocity, maxTicksLived,
                hitRadius, damageValue, hitParticle, playerHitSound);
    }

    private void setup(ProjectileRegistry projectileRegistry, Player owner, Projectile projectileEntity,
                       Vector initialVelocity, int maxTicksLived,
                       double hitRadius, double damageValue, Particle hitParticle, Sound playerHitSound) {
        setProjectileRegistry(projectileRegistry);
        setProjectileEntity(projectileEntity); //Set projectile and owner

        //Store the traveling speed set at the initial velocity multiplied by the speed multiplier
        setInitialVelocity(initialVelocity);
        getProjectileEntity().setVelocity(initialVelocity);

        ticksLived = getProjectileEntity().getTicksLived();
        this.maxTicksLived = maxTicksLived;

        setHitRadius(hitRadius);
        setDamageValue(damageValue);
        setHitParticle(hitParticle);
        setPlayerHitSound(playerHitSound);

        //Remove gravity (fire linearly)
        getProjectileEntity().setGravity(false);

        //Projectile is already registered in Game#addProjectile()
    }

    public abstract ProjectileType getType();

    public ProjectileRegistry getProjectileRegistry() {
        return this.projectileRegistry;
    }

    public void setProjectileRegistry(ProjectileRegistry projectileRegistry) {
        this.projectileRegistry = projectileRegistry;
    }

    /**
     * Get the game that this projectile belongs to.
     * More formally, execute {@link DfProjectile#getProjectileRegistry()#getGame()}
     *
     * @return
     * @author R Lee
     */
    public Game getGame() {
        return getProjectileRegistry().getGame();
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Projectile getProjectileEntity() {
        return this.projectileEntity;
    }

    public void setProjectileEntity(Projectile entity) {
        this.projectileEntity = entity;
        MetadataTag.PROJECTILE.setMetadata(getProjectileEntity(), this);

        if (entity.getShooter() instanceof Player) {
            setOwner((Player) entity.getShooter());
        }

        //TODO: Handle non-player shooters
    }

    public Vector getInitialVelocity() {
        return this.initialVelocity;
    }

    public void setInitialVelocity(Vector velocity) {
        getProjectileEntity().setVelocity(velocity);
        this.initialVelocity = velocity;
        this.speed = velocity.length();
        updateSpeedMultiplier = true;
    }

    /**
     * Get the center location of this entity, defined by the center of the entity along X, Y and Z axis
     *
     * @return
     * @author R Lee
     */
    public Location getCenterLocation() {
        return getProjectileEntity().getLocation().add(0, getProjectileEntity().getHeight() / 2, 0);
    }

    public double getHitRadius() {
        return this.hitRadius;
    }

    /**
     * Set the maximum radius away at which this entity will trigger {@link DfProjectile#hit(Entity, true)}.
     * If lesser than 0, set to 0
     *
     * @param radius
     * @author R Lee
     */
    public void setHitRadius(double radius) {
        this.hitRadius = Math.max(radius, 0);
    }

    public double getDamageValue() {
        return this.damageValue;
    }

    /**
     * Set the damage value.
     * If lesser than 0, set to 0
     *
     * @param damageValue
     * @author R Lee
     */
    public void setDamageValue(double damageValue) {
        this.damageValue = Math.max(damageValue, 0);
    }

    public Particle getHitParticle() {
        return this.hitParticle;
    }

    public void setHitParticle(Particle particle) {
        this.hitParticle = particle;
    }

    public Sound getPlayerHitSound() {
        return this.playerHitSound;
    }

    public void setPlayerHitSound(Sound sound) {
        this.playerHitSound = sound;
    }

    /**
     * General update on the projectile based on its projectileEntity properties
     * Updates projectileEntity velocity to initial velocity
     *
     * @author R Lee
     */
    public void update() {
        update(true);
    }

    /**
     * Perform the general update on the projectile
     *
     * @param updateVelocity Set to true to set the velocity in the given direction at the fixed, defined speed
     *                       Velocity will not be updated for projectile entities of the Fireball.class.
     *                       All fireball-type projectiles must have their velocities updated manually
     * @author R Lee
     */
    protected void update(boolean updateVelocity) {
        //Attempt to remove projectile if it meets removal criteria
        checkForRemoval();
        checkHitNearby();

        //This assumes speed is to be set at the initial velocity every tick and that it will naturally slow
        //Since fireballs do not slow in mid-air, do not update their velocity
        if (updateVelocity && !(getProjectileEntity() instanceof Fireball)) {
            Vector currentVelocity = getProjectileEntity().getVelocity();

            //If speed multiplier is not set
            if (updateSpeedMultiplier) {
                calculateSpeedMultiplier();
            }

            //TODO: Do proper debugging and remove try-catch container
            try {
                //Set velocity to speed, which should be slightly faster than the current velocity
                Vector finalVelocity = currentVelocity.multiply(speedMultiplier);
                getProjectileEntity().setVelocity(finalVelocity);
            } catch (IllegalArgumentException exception) {
                //Catching IllegalArgumentException: x not finite
                String oldSpeedMultiplier = "";
                if (speedMultiplier > 999999) {
                    oldSpeedMultiplier = "> 999999";
                } else if (speedMultiplier < -999999) {
                    oldSpeedMultiplier = "< -999999";
                } else {
                    oldSpeedMultiplier = Double.toString(speedMultiplier);
                }

                calculateSpeedMultiplier();

                String newSpeedMultiplier = "";
                if (speedMultiplier > 999999) {
                    newSpeedMultiplier = "> 999999";
                } else if (speedMultiplier < -999999) {
                    newSpeedMultiplier = "< -999999";
                } else {
                    newSpeedMultiplier = Double.toString(speedMultiplier);
                }

                Dogfight.instance.logAlert(AlertLevel.FATAL, "Illegal argument at DfProjectile.update(267): x not finite");
                Dogfight.instance.logAlert(AlertLevel.INFO, "Old speed multiplier: " + oldSpeedMultiplier + ". New speed multiplier: " + newSpeedMultiplier);
            }
        }

        ticksLived = getProjectileEntity().getTicksLived();
    }

    /**
     * Calculate the speed multiplier to multiply the velocity by per tick
     * speedMultiplier will be set to 0 if the currentVelocity is 0 so as to prevent an infinite speed multiplier
     *
     * @author R Lee
     */
    private void calculateSpeedMultiplier() {
        Vector currentVelocity = getProjectileEntity().getVelocity();

        //Prevent divide by 0 errors
        if (currentVelocity.lengthSquared() != 0) {
            speedMultiplier = Math.sqrt(speed * speed / currentVelocity.lengthSquared());
        } else {
            speedMultiplier = 0;
        }

        updateSpeedMultiplier = false; // Flag as updated
    }

    private boolean isInLiquid() {
        List<Material> liquids = new ArrayList<Material>();
        liquids.add(Material.WATER);
//		liquids.add(Material.LEGACY_STATIONARY_WATER); FIXME Attempt deleting
        liquids.add(Material.LAVA);
//		liquids.add(Material.LEGACY_STATIONARY_LAVA); FIXME Attempt deleting

        //Report projectile is in liquid
        if (liquids.contains(getCenterLocation().getBlock().getType())) {
            return true;
        }

        return false;
    }

    /**
     * Test if projectile is still alive (is not null and is not dead)
     *
     * @return True if alive, false if not
     * @author R Lee
     */
    public boolean isAlive() {
        return getProjectileEntity() != null && !getProjectileEntity().isDead();
    }

    /**
     * Check if projectile has lived more ticks than maximum tick account
     *
     * @author R Lee
     */
    protected void checkForRemoval() {
        //Check if expired lifespan or is no longer alive
        //TODO: KLUDGE: For now, remove all projectiles that enter liquids. Remove this later and fix other critical removal bugs
        if (ticksLived > maxTicksLived || !isAlive() || isInLiquid()) {
            remove(true);
        }
    }

    /**
     * Remove the projectile
     *
     * @param delay Set to true to mark for removal and remove all marked projectiles at once. Set to false to instantly remove
     * @author R Lee
     */
    public void remove(boolean delay) {
        if (isAlive() || !getProjectileEntity().isDead()) {
            getProjectileEntity().remove();
        }

        markedForRemoval = true;
        getProjectileRegistry().removeProjectile(this, delay);
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void setMarkedForRemoval(boolean state) {
        this.markedForRemoval = state;
    }

    /**
     * Instantly hit a target and apply damage
     *
     * @param target Entity to hit
     * @return True if hit event should be cancelled, false if not
     * @author R Lee
     */
    public boolean hit(Entity target) {
        return hit(target, false);
    }

    /**
     * Hit a target
     *
     * @param target
     * @param delayRemove
     * @param damageValue
     * @param playerHitSound
     * @return
     * @author R Lee
     */
    protected boolean hit(Entity target, boolean delayRemove) {
        boolean shouldCancel = true;
        boolean damageTarget = true;
        boolean isValidTarget = validateNearbyEntity(target); //Checks for: owner, teammate, armor stand or item

        if (isValidTarget) {
            if (target.getType().equals(EntityType.PLAYER)) {
                //Play sound if set
                if (getPlayerHitSound() != null) {
                    //Play sound to owner
                    //1st parameter - Location: Location to play sound from
                    //2nd parameter - Sound
                    //3rd parameter - "volume": At 1, full volume but barely audible 15 blocks away
                    //4th parameter - pitch: 1.0 for normal
                    owner.playSound(owner.getLocation(), getPlayerHitSound(), 1F, 1F);
                }
            }

            onHit(target);
        }

        //Spawn hit particle if set
        if (getHitParticle() != null) {
            //Summon particles at projectile's location
            //TODO: Customize/show/hide effect here
            /**
             * Particle effect parameters:
             * 1st: Particle effect
             * 2nd: Locations
             * 3rd: Count
             * 4th-6th: X, Y, Z offsets
             * 7th: Extra (usually speed)
             */
            getProjectileEntity().getWorld().spawnParticle(getHitParticle(), getCenterLocation(), 1, 0, 0, 0, 0);
        }

        //Projectile hit target. Remove projectile
        remove(delayRemove);
        return shouldCancel;
    }

    /**
     * Executes when a projectile hits a target. Damages target by {@link DfProjectile#getDamageValue()}.
     * Does not apply damage if damage value is 0 or less
     * Requires entity to be damageable and not of EntityType armor stand. The target cannot be this owner.
     * </br>
     * Execution requirements can be bypassed by manually invoking this method
     *
     * @param target Hit target
     * @author R Lee
     */
    protected void onHit(Entity target) {
        //Register dogfight-related damage
        //MUST precede actual application of damage
        if (target.getType() == EntityType.PLAYER) {
            getProjectileRegistry().getGame().getDamageRegistry().setPlayerDamage((Player) target, this);
        }

        if (getDamageValue() > 0) {
            //Apply custom damage if attacking a io.github.alpharlee.dogfight.floatingentity.target instad of a typical entity
            if (MetadataTag.TARGET.hasMetadata(target)) {
                MetadataTag.TARGET.getTarget(target).damage(getDamageValue());
            } else if (target instanceof Damageable) {
                ((Damageable) target).damage(getDamageValue(), getOwner()); //Damage target and set attacker as owner
            }
        }
    }

    /**
     * Test if a damageable entity is nearby within the specified radius
     * If so, trigger {@link DfProjectile#hit(Entity, boolean, double, Particle, Sound)}
     *
     * @param radius Radius entity must be within to trigger hit event
     * @author R Lee
     */
    public void checkHitNearby() {
        List<Entity> nearbyEntities = getProjectileEntity().getNearbyEntities(hitRadius, hitRadius, hitRadius);

        for (Entity entity : nearbyEntities) {
            //TODO: Remove redundant filter after testing
            //Check that the entity can be damaged
            //if (entity instanceof Damageable)
            //{
            Location entityLocation = entity.getLocation().add(0, entity.getHeight() / 2, 0); //Center of entity, not base
            //Validate distance is within desired radius
            if (getCenterLocation().distanceSquared(entityLocation) <= hitRadius * hitRadius) {
                //Don't hit invalid target
                if (validateNearbyEntity(entity)) {
                    //Hit the entity and delay the projectile removal
                    hit(entity, true);
                    break;
                }
            }
            //}
        }
    }

    /**
     * Validate the nearby entity is not the owner nor a teammate (if friendly fire is not enabled)
     * Entity cannot be an armor stand nor an item
     *
     * @param entity
     * @return True if entity is not owner
     * @author R Lee
     */
    protected boolean validateNearbyEntity(Entity entity) {
        //Validate owner is not hitting themselves
        if (entity.equals(getOwner())) {
            return false;
        }

        //Validate entity not being an armor stand, dropped item, painting or item frame
        switch (entity.getType()) {
            case ARMOR_STAND:
            case DROPPED_ITEM:
            case PAINTING:
            case ITEM_FRAME:
                return false;
        }

        //Prevent friendly fire
        Team team = getGame().getPlayerRegistry().getTeam(getOwner());
        if (team != null && !team.hasFriendlyFire()) {
            if (entity instanceof Player && team.containsMember((Player) entity)) {
                return false; //TODO: Clean this return statement
            }
        }

        return true;
    }
}
