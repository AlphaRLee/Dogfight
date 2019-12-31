package io.github.alpharlee.dogfight.floatingentity;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.projectile.DfProjectile;
import io.github.alpharlee.dogfight.registry.FloatingEntityRegistry;

public abstract class Hoop extends FloatingEntity
{
	protected double radius;
		
	private static ArrayList<Vector> ringBaseCoordinates;
	protected ArrayList<Location> ringCoordinates;
	protected Particle particle;
	protected int particlePeriod = 2; //Number of ticks to wait before spawning the particles again
	
	protected HoopTriggerType triggerType;

	protected DfProjectile nearestProjectile = null; //Convenience container for the nearest projectile
	
	/**
	 * Populate the default ring coordinates for lighter computation
	 */
	static
	{
		double dTheta = 2 * Math.PI / 16; //Make 16 iterations in total for theta
		//TODO: Change arbitrary number
		
		ringBaseCoordinates = new ArrayList<Vector>();
		populateRingBaseCoordinates(dTheta);
	}
	
	/**
	 * Create a new hoop with the specified game, location, radius, and particle.
	 * Hoop trigger type is defaulted to HoopTriggerType.PLAYER_NEAR
	 * @param game
	 * @param location
	 * @param radius
	 * @param particle
	 */
	public Hoop(FloatingEntityRegistry registry, Location location, double radius, Particle particle)
	{
		this(registry, location, radius, particle, HoopTriggerType.PLAYER_NEAR);
	}
	
	public Hoop(FloatingEntityRegistry registry, Location location, double radius, Particle particle, HoopTriggerType triggerType)
	{
		super(registry, location);
		
		setRadius(radius);
		setParticle(particle);
		setTriggerType(triggerType);
		
		ringCoordinates = new ArrayList<Location>();
		
		calculateRingCoordinates(); //NOTE: Must be called after setLocation() is called
	}
	
	public double getRadius()
	{
		return this.radius;
	}
	
	/**
	 * Set the radius of this hoop.
	 * Will set to the absolute value of the inputted radius
	 * @param radius
	 *
	 * @author R Lee
	 */
	public void setRadius(double radius)
	{
		this.radius = Math.abs(radius);
	}
	
	protected ArrayList<Location> getRingCoordinates()
	{
		return this.ringCoordinates;
	}
	
	protected void setRingPointCoordinates(ArrayList<Location> coordinates)
	{
		this.ringCoordinates = coordinates;
	}
	
	//TODO: Upgrade to particlePacket class (more control over particle spawning)
	public Particle getParticle()
	{
		return this.particle;
	}
	
	public void setParticle(Particle particle)
	{
		this.particle = particle;
	}
	
	public HoopTriggerType getTriggerType()
	{
		return this.triggerType;
	}
	
	public void setTriggerType(HoopTriggerType triggerType)
	{
		this.triggerType = triggerType;
	}
	
	/**
	 * Get the period interval for how often the particle effect will spawn
	 * @return
	 *
	 * @author R Lee
	 */
	public int getParticlePeriod()
	{
		return this.particlePeriod;
	}
	
	/**
	 * Set the period for how often the particle effect should spawn
	 * @param period Time delay in ticks (1/20th of a second)
	 *
	 * @author R Lee
	 */
	public void setParticlePeriod(int period)
	{
		this.particlePeriod = period;
	}
	
	/**
	 * Default general update to hoop per tick. Does not updateRingLocation. Does increment ticks lived  
	 * Tests for removal
	 * Plays particles
	 * </br></br>
	 * Override and use {@link Hoop#update(true)} within to update the ring locations
	 * @author R Lee
	 */
	@Override
	public void update()
	{
		this.update(false);
	}
	
	/**
	 * Default general update to hoop per tick.
	 * Tests for removal
	 * Plays particles.
	 * If {@link Hoop#getEntity()} returns something other than null, the location of the hoop is set to the center of the entity and the ticks lived is the age of the entity
	 * @param updateRingLocations Set to true to recalculate the location of the ring. Useful for moving hoops
	 * 
	 * @author R Lee
	 */
	public void update(boolean updateRingLocations)
	{
		checkForRemoval();
		checkForTrigger();
		
		if (isEntityAlive())
		{
			//Center location about center of entity, if applicable
			setLocation(getEntity().getLocation().add(0, getEntity().getHeight() / 2, 0));
			setTicksLived(getEntity().getTicksLived());
		}
		else
		{
			ticksLived++;
		}
		
		if (updateRingLocations)
		{
			calculateRingCoordinates();
		}
		
		showRings();
	}
	
	/**
	 * Check for any nearby entities that can potentially trigger off this hoop
	 * If multiple candidates are available, use {@link Hoop#trigger(Entity)} on the nearest one
	 *
	 * @author R Lee
	 */
	protected void checkForTrigger()
	{
		double radius = getRadius();
		Collection<Entity> nearbyEntities = getLocation().getWorld().getNearbyEntities(getLocation(), radius, radius, radius);
		
		boolean entityIsValid = false;
		
		//Set placeholder values for no entity found yet
		double distanceSquared = Dogfight.NOT_FOUND;
		double nearestDistanceSquared = Dogfight.NOT_FOUND;
		Entity nearestEntity = null;
		
		//Clean off
		nearestProjectile = null;
		
		for (Entity entity : nearbyEntities)
		{	
			entityIsValid = false; //Prep up for next iteration
			distanceSquared = getLocation().distanceSquared(entity.getLocation());
			
			//Validate not checking self
			if (isEntityAlive() && getEntity().equals(entity))
			{
				continue; //This entity, do not consider
			}
			
			//Validate radial distance
			if (distanceSquared > radius * radius)
			{
				continue; //Too far, do not consider
			}
			
			switch (getTriggerType())
			{
			case ENTITY_NEAR:
				
				//TODO: Monitor if nearby entity instance of game
				entityIsValid = true;
				
				break;
				
			case PLAYER_NEAR:
				
				if (entity.getType() == EntityType.PLAYER)
				{
					for (Player player : getFloatingEntityRegistry().getGame().getPlayerRegistry().getPlayers().keySet())
					{
						if (entity.equals(player))
						{
							entityIsValid = true;
							break; //Player found, no more iterations
						}
					}
				}
				
				break;
				
			case PROJECTILE_NEAR:
				
				//TODO: Validate that all DfProjectiles entities are actually projectiles (think squid shot and sniper)
				//TODO: Remove following if condition if such case is so
				//TODO: Migrate EventListener's getProjectile class to somewhere more universal (try Game)
				//Attempt to get the projectile from this entity if applicable
				nearestProjectile = MetadataTag.getProjectile(entity);
				
				//If projectile does exist
				if (nearestProjectile != null)
				{
					entityIsValid = true;
				}
				
				break;
			
			default:
				//Nothing set, no criteria will ever be met
				return;
			}
			
			if (entityIsValid)
			{
				//Set the nearest distance and nearest entity if the scanned entity is closer
				if (nearestDistanceSquared > distanceSquared || nearestDistanceSquared == Dogfight.NOT_FOUND)
				{
					nearestDistanceSquared = distanceSquared;
					nearestEntity = entity;
				}
			}
		}
		
		if (nearestEntity != null)
		{
			trigger(nearestEntity);
		}
	}
	
	/**
	 * Show particles specified at coordinates from {@link Hoop#getRingCoordinates()}
	 * Will only display if {@link Hoop#getTicksLived()} is evenly divided by {@link Hoop#getParticlePeriod()} and location is set
	 * @author R Lee
	 */
	public void showRings()
	{
		if (getLocation() != null && getLocation().getWorld() != null)
		{
			//Only display particles if delay interval has passed
			if (getTicksLived() % getParticlePeriod() == 0)
			{
				World world = getLocation().getWorld();
				
				for (Location ringLoc : getRingCoordinates())
				{
					//TODO: Fix with particlePacket
					/*
					 * Spawn particle:
					 * param 1: particle 
					 * param 2: locatoin
					 * param 3: count
					 * param 4-6: x, y, z offsets
					 * param 7: Extra (usually speed)
					 */
					world.spawnParticle(getParticle(), ringLoc, 1, 0, 0, 0, 0);
				}
			}
		}
	}
	
	/**
	 * Calculate the coordinates of 3 rings (circles along XY, XZ and YZ planes) relative to the center of the hoop 
	 * and store them in a static array copy for quick grabbing
	 * @param dTheta Delta theta: Amount to iterate individual coordinate points by
	 *
	 * @author R Lee
	 */
	private static void populateRingBaseCoordinates(double dTheta)
	{
		double x2 = 0; //X and Y coordinates on a 2-D plane
		double y2 = 0; 
		
		//NOTE ITERATOR: Iterates in units of delta theta until 1 full circle made
		for (double theta = 0; theta <= 2 * Math.PI; theta += dTheta)
		{
			x2 = Math.cos(theta);
			y2 = Math.sin(theta);
			
			ringBaseCoordinates.add(new Vector(x2, y2, 0));
			ringBaseCoordinates.add(new Vector(x2, 0, y2)); //Store circular array elements along XY, XZ and YZ planes
			ringBaseCoordinates.add(new Vector(0, x2, y2));
		}
	}
	
	/**
	 * Calculate the location of the points of the rings for this instance
	 * 
	 * @author R Lee
	 */
	private void calculateRingCoordinates()
	{
		//Validate location
		if (getLocation() != null && getLocation().getWorld() != null)
		{
			World world = getLocation().getWorld();
			
			//Clean out current
			getRingCoordinates().clear();
			
			for (Vector baseCoordinate : ringBaseCoordinates)
			{
				//Get a location at the base coordinate
				Location loc = new Location(world, baseCoordinate.getX(), baseCoordinate.getY(), baseCoordinate.getZ());
				
				loc.multiply(getRadius()); //Set to the specified radius
				loc.add(getLocation()); //Reposition at specified location
				
				//Add to instance copy
				getRingCoordinates().add(loc);
			}
		}
	}
	
	/**
	 * Executes when a hoop detects an entity passing through the hoop
	 * @param entity Entity that triggered the hoop
	 * 
	 * @author R Lee
	 */
	public abstract void trigger(Entity entity);
}
