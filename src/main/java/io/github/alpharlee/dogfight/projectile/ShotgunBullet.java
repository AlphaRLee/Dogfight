package io.github.alpharlee.dogfight.projectile;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.registry.ProjectileRegistry;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.util.Vector;

public class ShotgunBullet extends DfProjectile {
    public static double speedMultiplier = 2; //TODO: Change arbitary number

    public static int maxTicksLived = 100; //100 ticks = 5 seconds (20 ticks per second)

    protected static double hitRadius = 2.5; //Arbitrary number
    protected static double damageValue = 3.0; //4 half-hearts
    protected static Particle hitParticle = null;
    protected static Sound hitSound = Sound.ENTITY_ARROW_HIT_PLAYER;

    public static int bulletCount = 10; //TODO: Change arbitrary number. Value represents number of bullets spawned per shotgun usage. Not used internally
    public static double maxBulletSpread = 0.3; //TODO: Change arbitrary number

    /**
     * Create a shotgun bullet with its initial velocity set to the original thrown velocity multiplied by the speedMultiplier
     *
     * @param projectileRegistry
     * @param owner
     * @param bulletEntity
     */
    public ShotgunBullet(ProjectileRegistry projectileRegistry, Player owner, Egg bulletEntity) {
        super(projectileRegistry, owner, bulletEntity,
                bulletEntity.getVelocity().multiply(speedMultiplier), maxTicksLived,
                hitRadius, damageValue, hitParticle, hitSound);

        disableEggHatching();
    }

    /**
     * Create a shotgun bullet with the specified initial velocity
     *
     * @param projectileRegistry
     * @param owner
     * @param bulletEntity
     * @param initialVelocity
     */
    public ShotgunBullet(ProjectileRegistry projectileRegistry, Player owner, Egg bulletEntity, Vector initialVelocity) {
        super(projectileRegistry, owner, bulletEntity,
                initialVelocity, maxTicksLived,
                hitRadius, damageValue, hitParticle, hitSound);

        disableEggHatching();
    }

    @Override
    public ProjectileType getType() {
        return ProjectileType.SHOTGUN;
    }

    public Egg getBulletEntity() {
        return (Egg) getProjectileEntity();
    }

    public void setBulletEntity(Egg bulletEntity) {
        setProjectileEntity(bulletEntity);
    }

    /**
     * Randomly "nudge" the initial velocity of this projectile with a maximum random spread defined by maxSpread
     *
     * @param maxSpread
     * @author R Lee
     */
    public void randomRedirect(double maxSpread) {
        double x = getRandomZeroCentered(maxSpread);
        double y = getRandomZeroCentered(maxSpread); //Generate random X Y Z offsets
        double z = getRandomZeroCentered(maxSpread);

        Vector updatedVelocity = getInitialVelocity().add(new Vector(x, y, z));
        setInitialVelocity(updatedVelocity);
        getProjectileEntity().setVelocity(updatedVelocity);
    }

    /**
     * Get a random value that spans between negative max and positive max
     *
     * @param max
     * @return Random value
     * @author R Lee
     */
    private double getRandomZeroCentered(double max) {
        return (Math.random() - 0.5) * 2 * max; //Random number that ranges between -(maxSpread) and maxSpread
    }

    private void disableEggHatching() {
        //Let EventHandler deal with it
        PlayerEggThrowEvent throwEvent = new PlayerEggThrowEvent(getOwner(), getBulletEntity(), false, (byte) 0, EntityType.CHICKEN);
        Dogfight.instance.getServer().getPluginManager().callEvent(throwEvent); //Trigger off the event and therefore let the eggs be cancelled
    }
}
