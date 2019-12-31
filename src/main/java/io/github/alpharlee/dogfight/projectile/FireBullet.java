package io.github.alpharlee.dogfight.projectile;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;

import io.github.alpharlee.dogfight.registry.ProjectileRegistry;

public class FireBullet extends Bullet {

	private static final int fireTickTime = 60; //3 seconds
	private static Particle hitParticle = Particle.FLAME;

    /**
     * Same constructor as for a DfProjectile
     *
     * @param projectileRegistry
     * @param owner
     * @param projectileEntity
     */
    public FireBullet(ProjectileRegistry projectileRegistry, Player owner, Projectile projectileEntity) {
        super(projectileRegistry, owner, projectileEntity, projectileEntity.getVelocity().multiply(speedMultiplier), maxTicksLived, hitRadius, damageValue, hitParticle, Sound.ENTITY_BLAZE_SHOOT);
    }

    @Override
   	public ProjectileType getType()
   	{
   		return ProjectileType.FIRE_BULLET;
   	}
    
    @Override
    protected void onHit(Entity target) {
    	super.onHit(target);
        target.setFireTicks(fireTickTime);
    }
}
