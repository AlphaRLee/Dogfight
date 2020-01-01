package io.github.alpharlee.dogfight.projectile;

import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.registry.ProjectileRegistry;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

// FIXME Add appropriate import
//import com.comphenix.protocol.PacketType;
//import com.comphenix.protocol.events.PacketAdapter;
//import com.comphenix.protocol.events.PacketContainer;
//import com.comphenix.protocol.events.PacketEvent;
//import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public abstract class DfEntityProjectile extends DfProjectile {

    private Entity boundEntity;
    private EntityType entityType;

    /**
     * Create a Dogfight Projectile
     *
     * @param projectileRegistry Projectile registry to store the projectile in
     * @param owner              Owner of projectile
     * @param projectileEntity   Projectile entity that represents this projectile
     * @param initialVelocity    Initial velocity to launch the projectile at
     * @param maxTicksLived      Maximum number of ticks this projectile will live before calling {@link DfProjectile#remove(false)}
     * @param hitRadius          Radius to trigger collision with another entity. Does not influence projectile's relation to blocks
     * @param damageValue        Amount of damage to apply to an entity when hit. Measured in half-hearts
     * @param hitParticle        Particle to play when entity is hit. Set to null to not play any particle
     * @param playerHitSound     Sound to play to the owner when a player is hit. Set to null to not play any sound
     */
    public DfEntityProjectile(ProjectileRegistry projectileRegistry, Player owner, Projectile projectileEntity, Vector initialVelocity, int maxTicksLived, double hitRadius, double damageValue, Particle hitParticle, Sound playerHitSound) {
        super(projectileRegistry, owner, projectileEntity, initialVelocity, maxTicksLived, hitRadius, damageValue, hitParticle, playerHitSound);
        setup();
    }

    private void setup() {
        this.entityType = getEntityType();
        setBoundEntity(getProjectileEntity().getWorld().spawnEntity(getProjectileEntity().getLocation(), entityType));

        boundEntity.setGravity(false);
        boundEntity.setVelocity(getProjectileEntity().getVelocity()); //Set the direction to be in common with the projectile

        if (boundEntity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) boundEntity;
            livingEntity.setAI(false);
            livingEntity.setCanPickupItems(false);
            livingEntity.setCollidable(false);
            livingEntity.setSilent(true);
        }
        getProjectileEntity().addPassenger(boundEntity);

        MetadataTag.PROJECTILE_PASSENGER.setMetadata(boundEntity, this);

        //TODO: This code is SUPPOSED to hide the projectile entity, but REQUIRES DEBUGGING
//        PacketContainer hideProjectileEntity = Dogfight.instance.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
//        hideProjectileEntity.getIntegerArrays().write(0,new int[projectileEntity.getEntityId()]);
//        for(Player p : Dogfight.instance.getServer().getOnlinePlayers())
//        {
//            try {
//                Dogfight.instance.protocolManager.sendServerPacket(p, hideProjectileEntity);
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }

        //Drop down to be centered along bound entity
        getProjectileEntity().teleport(getProjectileEntity().getLocation().subtract(0, getBoundEntity().getHeight(), 0));

    }

    public Entity getBoundEntity() {
        return this.boundEntity;
    }

    private void setBoundEntity(Entity boundEntity) {
        this.boundEntity = boundEntity;
    }

    public abstract EntityType getEntityType();

    /**
     * Get the center location between both the projectile and its bound entity
     */
    @Override
    public Location getCenterLocation() {
        double height = getProjectileEntity().getHeight();

        //TODO: KLUDGE: Why is the bound entity null in the first place? This check is redundant.
        //Find out why NullPointerException is being thrown and remove if wrapper
        if (getBoundEntity() != null) {
            height += getBoundEntity().getHeight();
        }

        return getProjectileEntity().getLocation().add(0, height / 2, 0);
    }

    @Override
    protected boolean hit(Entity target, boolean delayRemove) {
        if (!(target.equals(getBoundEntity()))) {
            return super.hit(target, delayRemove);
        } else {
            return true; //TODO: Decide on returning true or false
        }
    }

    /**
     * Remove the projectile if the hosting entity is not alive
     */
    @Override
    protected void checkForRemoval() {
        super.checkForRemoval();

        if (!entityIsAlive()) {
            remove(true);
        }
    }

    @Override
    public void remove(boolean delay) {

        if (isAlive() || !getProjectileEntity().isDead()) {
            getProjectileEntity().eject();
        }

        if (entityIsAlive()) {
            getBoundEntity().remove();
            this.boundEntity = null;
        }

        super.remove(delay);
    }

    public boolean entityIsAlive() {
        return !(this.boundEntity == null || this.boundEntity.isDead());
    }

    /**
     * Validate entities that are not the bound entity along with applying {@link DfProjectile#validateNearbyEntity(Entity)}
     * return true if entity is not bound entity and {@link DfProjectile#validateNearbyEntity(Entity)} returns true
     */
    @Override
    protected boolean validateNearbyEntity(Entity entity) {
        return !entity.equals(getBoundEntity()) && super.validateNearbyEntity(entity);
    }
}
