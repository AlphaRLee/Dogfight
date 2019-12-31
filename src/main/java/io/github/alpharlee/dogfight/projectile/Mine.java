package io.github.alpharlee.dogfight.projectile;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import io.github.alpharlee.dogfight.damagesource.ExplosionSource;
import io.github.alpharlee.dogfight.registry.ProjectileRegistry;

public class Mine extends DfEntityProjectile{

    public static double speedMultiplier = -0.01; //KLUDGE: DfProjectile#hit() fails if this value is 0. Temporarily set to a very slow value

    public static int maxTicksLived = 600 * 20; //TODO: Change arbitrary value
    
    public static double hitRadius = 2.5;
	public static double damageValue = 10; 
	public static Particle hitParticle = Particle.EXPLOSION_HUGE;
	public static Sound playerHitSound = Sound.ENTITY_GENERIC_EXPLODE;
    
    /**
     * Create a Dogfight Projectile
     *
     * @param projectileRegistry Projectile registry to store the projectile in
     * @param owner              Owner of projectile
     * @param projectileEntity   Projectile entity that represents this projectile
     */
    public Mine(ProjectileRegistry projectileRegistry, Player owner, Projectile projectileEntity) {
        super(projectileRegistry, owner, projectileEntity, projectileEntity.getVelocity().multiply(speedMultiplier),
                maxTicksLived, hitRadius, damageValue, hitParticle, playerHitSound);
    }

    @Override
   	public ProjectileType getType()
   	{
   		return ProjectileType.MINE;
   	}
    
    @Override
    public EntityType getEntityType() {
        return EntityType.GUARDIAN;
    }

    @Override
    public void remove(boolean delay)
    {
    	//Check if not already marked for removal (prevent triggering recursive events) and still alive
		if (isAlive() && !getProjectileEntity().isDead() && !isMarkedForRemoval())
		{	
			setMarkedForRemoval(true); //Must occur prior to explosion
			
			if (entityIsAlive())
			{
				getBoundEntity().remove();
			}
					
			//Create an explosion with the power of 4 (tnt power), non-incendiary, no block-damage
			new ExplosionSource(this, 4f, false, false);
		}
    	
		super.remove(delay);
    }
}
