package io.github.alpharlee.dogfight.projectile;

import io.github.alpharlee.dogfight.registry.ProjectileRegistry;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class SniperBullet extends DfProjectile {
    public static double speedMultiplier = 3.5; //TODO: Change arbitrary number

    public static int maxTicksLived = 100;
    protected static double hitRadius = 2.0; //Arbitrary number
    protected static double damageValue = 20.0; //20 half-hearts (lethal to player)
    protected static Particle hitParticle = null;
    protected static Sound hitSound = Sound.ENTITY_ARROW_HIT_PLAYER;

    public SniperBullet(ProjectileRegistry registry, Player owner, Arrow bulletEntity) {
        super(registry, owner, bulletEntity,
                bulletEntity.getVelocity().multiply(speedMultiplier), maxTicksLived,
                hitRadius, damageValue, hitParticle, hitSound);

        //Play blaze shoot sound to player
        //1st parameter - Location: Location to play sound from
        //2nd parameter - Sound
        //3rd parameter - "volume": At 1, full volume but barely audible 15 blocks away
        //4th parameter - pitch: 1.0 for normal
        getOwner().playSound(getOwner().getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1, 1.5f);
    }

    @Override
    public ProjectileType getType() {
        return ProjectileType.SNIPER;
    }

    /**
     * Apply trail effect
     */
    @Override
    public void update() {
        super.update(true);

        //Summon particles at bullet's location
        //TODO: Customize/show/hide effect here
        /*
         * Particle effect parameters:
         * 1st: Particle effect
         * 2nd: Location
         * 3rd: Count
         * 4th-6th: X, Y, Z offsets
         * 7th: Extra (usually speed)
         */
        getProjectileEntity().getWorld().spawnParticle(Particle.CRIT, getProjectileEntity().getLocation(), 1, 0, 0, 0, 0);
    }
}
