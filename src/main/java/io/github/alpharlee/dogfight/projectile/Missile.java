package io.github.alpharlee.dogfight.projectile;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow.PickupStatus;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.util.Vector;

import io.github.alpharlee.dogfight.BoostHandler;
import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.damagesource.ExplosionSource;
import io.github.alpharlee.dogfight.registry.ProjectileRegistry;

public class Missile extends DfProjectile
{
	//Projectile controls. Speed multiplier is standard flight speed and boostSpeed is how strongly it will turn if it can
	public static double speedMultiplier = 2.0; //TODO: Change arbitrary value
	public static double boostSpeed = 0.8; //TODO: Change arbitrary value
	
	//Set max missile life to 2010 ticks (10.5 seconds). Number chosen to accomodate delay targeting life
	public static int maxTicksLived = (int) (10.25 * 20); //TODO: Change arbitrary value
	
	//Hit values
	public static double hitRadius = 1;
	public static double damageValue = 0; //Set to 0 because an explosion is generated instead that handles damage more dynamically
	public static Particle hitParticle = null;
	public static Sound playerHitSound = Sound.BLOCK_STONE_HIT;
	
	public int delayTargetingTicks = (int) (0.25 * 20); //Arbitrary number
	public double searchRadius = 100; //Arbitrary number
	private Entity target = null;
	boolean aimAtPlayer;
	
	//Used in IncomingMissileWarning
	public double distanceToTargetSquared = Dogfight.NOT_FOUND;
	
	/**
	 * Homing missile that attempts to aim at any damageable entity within the searchRadius (defaulted at 100 blocks) or a player, if aimAtPlayer is set to true
	 * @param game Dogfight game the missile belongs to
	 * @param owner Player launching missile
	 * @param missileEntity Arrow representing the missile model
	 * @param aimAtPlayer Set to true to exclusively aim at players
	 */
	public Missile(ProjectileRegistry registry, Player owner, SpectralArrow missileEntity, boolean aimAtPlayer)
	{
		super(registry, owner, missileEntity, 
				owner.getEyeLocation().getDirection().multiply(speedMultiplier), maxTicksLived, hitRadius, 
				damageValue, hitParticle, playerHitSound);
		
		setup(owner, aimAtPlayer);
		
		//Play blaze shoot sound to player
		//1st parameter - Location: Location to play sound from
		//2nd parameter - Sound
		//3rd parameter - "volume": At 1, full volume but barely audible 15 blocks away
		//4th parameter - pitch: 1.0 for normal
		getOwner().playSound(getOwner().getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 0.5f);
	}
	
	/**
	 * Convenience class to set up whatever was not set up in DfProjectile
	 * @param owner Original launcher of missile
	 *
	 * @author R Lee
	 */
	public void setup(Player owner, boolean aimAtPlayer)
	{
		this.setOwner(owner);
		this.aimAtPlayer = aimAtPlayer;
		
		//Do not apply glow to target
		getMissileEntity().setGlowingTicks(0);
		getMissileEntity().setPickupStatus(PickupStatus.DISALLOWED);
	}
	
	@Override
	public ProjectileType getType()
	{
		if (aimAtPlayer)
		{
			return ProjectileType.PLAYER_MISSILE;
		}
		else
		{
			return ProjectileType.MISSILE;
		}
	}
	
	/**
	 * Return the projectileEntity casted as a missile type
	 * @return
	 *
	 * @author R Lee
	 */
	public SpectralArrow getMissileEntity()
	{
		return (SpectralArrow) super.getProjectileEntity();
	}
	
	public void setMissileEntity(SpectralArrow missileEntity)
	{
		super.setProjectileEntity(missileEntity);
	}
	
	/**
	 * Return the target as a damageable entity or null if target is not damageable
	 * @return
	 *
	 * @author R Lee
	 */
	public Entity getTarget()
	{	
		return target;
	}
	
	public void setTarget(Damageable target)
	{
		this.target = target;
	}
	
	/**
	 * Automatically set target to the nearest damageable entity within search radius.
	 * Missile must have line of sight towards entity (ie. no solid blocks in between)
	 * Will not target owner.
	 *
	 * @author R Lee
	 */
	public void setTarget()
	{
		List<Entity> nearbyEntities = getMissileEntity().getNearbyEntities(searchRadius, searchRadius, searchRadius);
		
		if (!nearbyEntities.isEmpty())
		{
			final double NOT_FOUND = -999;
			
			//Set placeholder values for no entity found yet
			double distanceSquared = NOT_FOUND;
			double nearestDistanceSquared = NOT_FOUND;
			Damageable nearestTarget = null;
			
			for (Entity entity : nearbyEntities)
			{
				//Check if entity is damageable, not an armour stand and not the owner
				//TODO: Test for teammates (and not target them)
				if (entity instanceof Damageable && entity.getType() != EntityType.ARMOR_STAND && validateNearbyEntity(entity)) 
				{
					//Test if aimAtPlayer is specified and entity is a player (or aimAtPlayer is not specified, any entity works)
					if (entity.getType() == EntityType.PLAYER || !this.aimAtPlayer)
					{
						/*
						 * Test for line of sight (see javadocs)
						 * @param 3: Max distance, capped at search radius
						 * @param 4: Iteration distance. Arbitrary number. (0.25 signifies scan every 0.25 units, which is 4 checks per block (aligned on axis)) 
						 */
						if (hasLineOfSight(getMissileEntity().getLocation(), entity.getLocation(), searchRadius, 0.25))
						{
							distanceSquared = getMissileEntity().getLocation().distanceSquared(entity.getLocation());
							
							//Set the nearest distance and nearest entity if the scanned entity is closer
							if (nearestDistanceSquared > distanceSquared || nearestDistanceSquared == NOT_FOUND)
							{
								nearestDistanceSquared = distanceSquared;
								nearestTarget = (Damageable) entity; //Safe to cast, checked already
							}
						}
					}
				}
			}
			
			if (nearestTarget != null)
			{
				setTarget(nearestTarget);
				setDistanceToTargetSquared(nearestDistanceSquared);
				
				//Warn player
				if (nearestTarget.getType() == EntityType.PLAYER)
				{
					getProjectileRegistry().getGame().addMissileToWarning((Player) getTarget(), this);
				}
			}
		}
	}
	
	/**
	 * @return the distance squared between the missile entity and the target
	 *
	 * @author R Lee
	 */
	public double getDistanceToTargetSquared()
	{
		return this.distanceToTargetSquared;
	}
	
	/**
	 * @param distanceSquared distance squared between the misile entity and the target
	 *
	 * @author R Lee
	 */
	public void setDistanceToTargetSquared(double distanceSquared)
	{
		this.distanceToTargetSquared = distanceSquared;
	}
	
	/**
	 * Automatically attempt to assign a target if not set yet once the delayTargetingTicks expires
	 */
	@Override
	public void update()
	{	
		//Make sure target still exists, is alive, and is in the same world
		//Vector math between two locations in different worlds can crash the plugin
		if (getTarget() != null && !getTarget().isDead() && getTarget().getWorld().equals(getMissileEntity().getWorld()))
		{
			//Aim at target
			chaseTarget();
		}
		else
		{
			//Decrement delay to targeting
			if (delayTargetingTicks > 0)
			{
				delayTargetingTicks--;
			}
			else
			{
				//Find target and aim at it
				setTarget();
			}
		}
		
		update(true);
	}
	
	/**
	 * Boost towards target if target is set and no solid blocks obscure the path between the missile and the target
	 *
	 * @author R Lee
	 */
	private void chaseTarget()
	{
		Entity target = getTarget();
		
		if (target != null)
		{
			Location targetLocation = target.getLocation().add(0, target.getHeight() / 2, 0); //Center of body rather than feet
			
			/*
			 * Test for line of sight (see javadocs)
			 * @param 3: Max distance, capped at search radius
			 * @param 4: Iteration distance. Arbitrary number. (0.25 signifies scan every 0.25 units, which is 4 checks per block (aligned on axis)) 
			 */
			if (hasLineOfSight(getMissileEntity().getLocation(), targetLocation, searchRadius, 0.25))
			{
				Vector toTarget = targetLocation.toVector().subtract(getMissileEntity().getLocation().toVector());
				
				setDistanceToTargetSquared(toTarget.lengthSquared());
				BoostHandler.boostEntity(getMissileEntity(), getMissileEntity().getVelocity(), toTarget.multiply(Math.sqrt(boostSpeed * boostSpeed / getDistanceToTargetSquared())), super.speed);
				
			}
		}
	}
	
	/**
	 * Play an explosion before removal.
	 * Explosion does damage nearby entities but does not damage blocks
	 */
	@Override
	public void remove(boolean delay)
	{		
		//Check if not already marked for removal (prevent triggering recursive events) and still alive
		if (isAlive() && !getMissileEntity().isDead())
		{
			//Create an explosion with the power of 4 (tnt power), non-incendiary, no block-damage
			new ExplosionSource(this, 4f, false, false);
			
			//Location loc = getProjectileEntity().getLocation();
			
			//getMissileEntity().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4f, false, false);
		}
		
		super.remove(delay);
	}
	
	/**
	 * Get if there is a line of sight (ie. No solid blocks) between the initial location and the final location or to a maximum distance
	 * @param initLoc Initial location to start scanning from
	 * @param finalLoc Final location to scan towards
	 * @param maxDistance Maximum scan distance
	 * @param iterateDistance Distance interval to scan for obstructions. Smaller values are more accurate but increase CPU usage
	 * @return True if no solid blocks are between the initial location and the final location or from the the initial location to the final location, stopping at the maximum distance.
	 * False if a solid block is encountered along the way
	 *
	 * @author R Lee
	 */
	public static boolean hasLineOfSight(Location initLoc, Location finalLoc, double maxDistance, double iterateDistance)
	{
		maxDistance = Math.abs(maxDistance);
		iterateDistance = Math.abs(iterateDistance);
		
		Location iteratedLoc = initLoc.clone();
		Vector initToFinal = finalLoc.toVector().subtract(initLoc.toVector());
		Vector iteratedDirection = initToFinal.clone().normalize().multiply(iterateDistance);
		
		//Get the max iterations: Either the number of iterations to the final location or the number of iterations to the max distance, whichever is smaller
		int maxIterations = (int) Math.floor(Math.min(initToFinal.length(), maxDistance) / iterateDistance);
		
		for (int i = 0; i <= maxIterations; i++)
		{
			iteratedLoc.add(iteratedDirection);
			
			//Iterate until a solid block is encountered
			if (initLoc.getBlock().getType().isSolid())
			{
				//Some obstacle is in the way
				return false;
			}
		}
		
		//No solid obstacle is in the way
		return true;
	}
}
