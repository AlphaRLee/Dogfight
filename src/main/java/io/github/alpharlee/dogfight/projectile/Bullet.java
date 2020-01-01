package io.github.alpharlee.dogfight.projectile;

import io.github.alpharlee.dogfight.registry.ProjectileRegistry;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Bullet extends DfProjectile {
    protected static double speedMultiplier = 3.5; //TODO: Change arbitary number

    public static int maxTicksLived = 100; //100 ticks = 5 seconds (20 ticks per second)

    protected static double hitRadius = 2.5; //Arbitrary number
    protected static double damageValue = 4.0; //4 half-hearts
    protected static Particle hitParticle = null;
    protected static Sound hitSound = Sound.ENTITY_ARROW_HIT_PLAYER;

    public Bullet(ProjectileRegistry registry, Player owner, Snowball bulletEntity) {
        this(registry, owner, bulletEntity,
                owner.getEyeLocation().getDirection().multiply(speedMultiplier), maxTicksLived,
                hitRadius, damageValue, hitParticle, hitSound);

        setup();
    }

    /**
     * Same constructor as for a DfProjectile
     *
     * @param projectileRegistry
     * @param owner
     * @param projectileEntity
     * @param initialVelocity
     * @param maxTicksLived
     * @param hitRadius
     * @param damageValue
     * @param hitParticle
     * @param playerHitSound
     */
    public Bullet(ProjectileRegistry projectileRegistry, Player owner, Projectile projectileEntity,
                  Vector initialVelocity, int maxTicksLived,
                  double hitRadius, double damageValue, Particle hitParticle, Sound playerHitSound) {
        super(projectileRegistry, owner, projectileEntity, initialVelocity, maxTicksLived, hitRadius, damageValue, hitParticle, playerHitSound);

        setup();
    }

    private void setup() {
        setBulletEntity((Snowball) getProjectileEntity()); //Set bullet

        //Bullet is already registered in Game#addBullet()
    }

    @Override
    public ProjectileType getType() {
        return ProjectileType.BULLET;
    }

    public Snowball getBulletEntity() {
        return (Snowball) getProjectileEntity();
    }

    public void setBulletEntity(Snowball bulletEntity) {
        super.setProjectileEntity(bulletEntity);
    }
}
