package io.github.alpharlee.dogfight.registry;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.projectile.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.inventory.ItemStack;

public class ProjectileRegistry
{
	private Game game;
	
	private HashMap<Projectile, DfProjectile> activeProjectiles; //Registry of active projectiles, categorized by their projectile entity for quick identification
	private ArrayList<Projectile> removedProjectiles; //Temporary list for projectiles to be removed
	
	public ProjectileRegistry(Game game)
	{
		this.game = game;
		
		this.activeProjectiles = new HashMap<Projectile, DfProjectile>();
		this.removedProjectiles = new ArrayList<Projectile>();
	}
	
	public Game getGame()
	{
		return this.game;
	}
	
	/**
	 * Instantiate a new bullet from a projectile and add it to the game registry
	 * @param bulletEntity DfProjectile to base bullet off of
	 *
	 * @author R Lee
	 */
	public void addBullet(Snowball bulletEntity)
	{
		if (bulletEntity.getShooter() instanceof Player)
		{
			Player player = (Player) bulletEntity.getShooter();
			Bullet bullet = new Bullet(this, player, bulletEntity);
			
			addProjectile(bullet, true, true);
		}	
		else
		{
			//TODO: Allow non-human bullet throwers
		}
	}

	public void addFireBullet(Snowball bulletEntity)
	{	
		if(bulletEntity.getShooter() instanceof Player)
		{
			Player player = (Player) bulletEntity.getShooter();
			FireBullet fireBullet = new FireBullet(this,player,bulletEntity);

			addProjectile(fireBullet, false, true);
		}
	}
	
	public boolean spawnSniperBullet(Player owner)
	{
		return addSniperBullet(owner.launchProjectile(Arrow.class, owner.getEyeLocation().getDirection().multiply(SniperBullet.speedMultiplier)));
	}
	
	/**
	 * Instantiate a new sniper bullet from a projectile and add it to the game registry
	 * @param bulletEntity DfProjectile to base bullet off of
	 *
	 * @author R Lee
	 */
	public boolean addSniperBullet(Arrow bulletEntity)
	{
		if (bulletEntity.getShooter() instanceof Player)
		{
			Player player = (Player) bulletEntity.getShooter();
			SniperBullet bullet = new SniperBullet(this, player, bulletEntity);
			
			addProjectile(bullet, false, false);
		}	
		else
		{
			//TODO: Allow non-human bullet throwers
		}
		
		return true;
	}

	public boolean spawnSquidShot(Player owner){
		return addSquidShot(owner.launchProjectile(Arrow.class, owner.getEyeLocation().getDirection().multiply(SquidShot.speedMultiplier)));
	}

	public boolean addSquidShot(Arrow bulletEntity)
	{
		if (bulletEntity.getShooter() instanceof Player)
		{
			Player p = (Player) bulletEntity.getShooter();
			SquidShot shot = new SquidShot(this, p, bulletEntity);
			addProjectile(shot, false, false);
		}
		else
		{
			//TODO: Handle non-human shooters
		}
		
		return true;
	}

	public boolean spawnMine(Player owner){
		return addMine(owner.launchProjectile(Arrow.class, owner.getEyeLocation().getDirection().multiply(Mine.speedMultiplier)));
	}

	public boolean addMine(Arrow bulletEntity)
	{
		if (bulletEntity.getShooter() instanceof Player)
		{
			Player  p = (Player) bulletEntity.getShooter();
			Mine mine = new Mine(this, p, bulletEntity);
			addProjectile(mine, false, false);
		}
		else
		{
			//TODO: Handle non-human shooters
		}
		
		return true;
	}
	
	/**
	 * Spawn a random number of shotgun bullets from the owner
	 * @param owner
	 * @return
	 *
	 * @author R Lee
	 */
	public boolean spawnShotgunBullets(Player owner)
	{
		//Arbitrary numbers dictating minimum number of shotgun bullets and random devience factor, respectively
		int minCount = 8;
		int countDeviance = 5;
		
		return spawnShotgunBullets(owner, (int) Math.floor(Math.random() * countDeviance + minCount));
	}
	
	/**
	 * Spawn a set of shotgun bullets that will spray outwards
	 * @param owner Player who shot the bullets
	 * @param bulletCount Number of bullets to shoot, excluding the original linear shot
	 * @return true
	 *
	 * @author R Lee
	 */
	public boolean spawnShotgunBullets(Player owner, int bulletCount)
	{
		for (int i = 0; i < bulletCount; i++)
		{
			//Manually spawn the bullet in to prevent recursive event calls
			Egg bullet = (Egg) owner.getWorld().spawnEntity(owner.getEyeLocation().clone().add(owner.getEyeLocation().getDirection()), EntityType.EGG);
			bullet.setVelocity(owner.getEyeLocation().getDirection().multiply(ShotgunBullet.speedMultiplier));
			bullet.setShooter(owner);
			
			addShotgunBullet(bullet, true);
		}
		
		//TODO KLUDGE: Move the following 2 lines to a more appropriate spot
		/*
		 * Play an explosion at the player with volume 1 and pitch 1.2 
		 */
		owner.playSound(owner.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1.2f);
		checkConsumeHandItem(owner, false, false);
		
		//TODO Formalize return statement
		return true;
	}
	
	/**
	 * Instantiate a new, single shotgun bullet
	 * @param bulletEntity DfProjectile to base bullet off of
	 * @param spreadShot Set to true if bullet is spawned to be part of the shotgun burst cloud. 
	 * Setting to false will consume the player's hand item and spawn a shotgun bullet that travels in the direction the player aimed
	 *
	 * @author R Lee
	 */
	public boolean addShotgunBullet(Egg bulletEntity, boolean spreadShot)
	{
		if (bulletEntity.getShooter() instanceof Player)
		{
			Player player = (Player) bulletEntity.getShooter();
			ShotgunBullet bullet = new ShotgunBullet(this, player, bulletEntity);
			
			if (spreadShot)
			{
				bullet.randomRedirect(ShotgunBullet.maxBulletSpread);
				
				//Do not consume any of the extra bullets
				addProjectile(bullet);
			}
			else
			{
				//TODO: Old code, remove
				
				//Consume the player's hand item
				addProjectile(bullet, false, false);
			}
		}	
		else
		{
			//TODO: Allow non-human bullet throwers
		}
		
		return true;
	}
	
	/**
	 * Spawn a missile launched from the owner
	 * @param owner
	 * @retrun True for successful missile spawn (always return true)
	 *
	 * @author R Lee
	 */
	public boolean spawnMissile(Player owner, boolean aimAtPlayer)
	{
		//Spawn a missile with velocity of the owner's velocity and direction (prevent collision with owner)
		SpectralArrow missileEntity = owner.launchProjectile(SpectralArrow.class, owner.getVelocity().clone().add(owner.getEyeLocation().getDirection().multiply(Missile.speedMultiplier)));
		
		return addMissile(owner, missileEntity, aimAtPlayer);
	}
	
	/**
	 * Add a missile based on a shulker bullet and assign a player as its owner
	 * @param owner
	 * @param missileEntity
	 * @return True for successful missile addition (always returns true)
	 *
	 * @author R Lee
	 */
	public boolean addMissile(Player owner, SpectralArrow missileEntity, boolean aimAtPlayer)
	{
		addProjectile(new Missile(this, owner, missileEntity, aimAtPlayer), false, false);
		return true;
	}
	
	public HashMap<Projectile, DfProjectile> getProjectiles()
	{
		return this.activeProjectiles;
	}
	
	public void setProjectiles(HashMap<Projectile, DfProjectile> projectiles)
	{
		this.activeProjectiles = projectiles;
	}
	
	/**
	* Add a projectile to the active projectile registry without consuming the player's hand item
	 * @param projectile Projectile to add
	 *
	 * @author R Lee
	 */
	public void addProjectile(DfProjectile projectile)
	{
		getProjectiles().put(projectile.getProjectileEntity(), projectile);
	}
	
	/**
	 * Add a projectile to the active projectile registry then consume the player's hand item if they are in survival or adventure mode
	 * @param projectile Projectile to add
	 * @param unlimited Set to true to automatically stock the player with another of the item in their main hand
	 * @param itemConsumed Set to true if the item in hand is normally consumed if this method did not operate and the player was not in creative mode
	 * (eg. throwing a snowball would normally consume the item if the player was not in creative, right clicking most other items would normally not consume the item if the event was cancelled)
	 *
	 * @author R Lee
	 */
	public void addProjectile(DfProjectile projectile, boolean unlimited, boolean normallyConsumed)
	{
		//Start by conventional addProjectil
		addProjectile(projectile);
		
		checkConsumeHandItem(projectile.getOwner(), unlimited, normallyConsumed);
	}
	
	/**
	 * Attempt to consume the player's hand item if they are in survival or adventure mode
	 * @param player Player who's hand item is to be consumed
	 * @param unlimited Set to true to automatically stock the player with another of the item in their main hand
	 * @param itemConsumed Set to true if the item in hand is normally consumed if this method did not operate and the player was not in creative mode
	 * (eg. throwing a snowball would normally consume the item if the player was not in creative, right clicking most other items would normally not consume the item if the event was cancelled)
	 *
	 * @author R Lee
	 */
	private void checkConsumeHandItem(Player player, boolean unlimited, boolean normallyConsumed)
	{
		ItemStack mainHandItem = player.getInventory().getItemInMainHand();
		
		if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
		{
			//Restock item if item is normally consumed (eg. projectile throw in survival mode)
			if (unlimited && normallyConsumed)
			{
				mainHandItem.setAmount(mainHandItem.getAmount() + 1);
			}
			//Take the item if the item is normally not consumed nor unlimited
			else if (!unlimited && !normallyConsumed)
			{
				if (mainHandItem.getAmount() > 1)
				{
					mainHandItem.setAmount(mainHandItem.getAmount() - 1);
				}
				else
				{
					//TODO: Take item in arbitrary slot, not just main hand
					player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
				}
			}
			
			getGame().getPlayerRegistry().updatePlayer(player); //Mark player for inventory update
		}
	}
		
	/**
	 * Remove a projectile
	 * @param projectileEntity
	 * @param delay Set to true to mark for removal and remove all marked projectiles at once. Set to false to instantly remove
	 *
	 * @author R Lee
	 */
	public void removeProjectile(Projectile projectileEntity, boolean delay)
	{
		//If requires delay
		if (delay)
		{
			//Mark for delayed removal
			removedProjectiles.add(projectileEntity);
		}
		else
		{
			//Instantly remove
			activeProjectiles.remove(projectileEntity);
		}
	}
	
	/**
	 * Remove a projectile
	 * @param projectile DfProjectile instance of a projectile
	 * @param delay Set to true to mark for removal and remove all marked projectiles at once. Set to false to instantly remove
	 *
	 * @author R Lee
	 */
	public void removeProjectile(DfProjectile projectile, boolean delay)
	{
		removeProjectile(projectile.getProjectileEntity(), delay);
	}
	
	/**
	 * Clear all projectiles marked for removal
	 * 
	 * @author R Lee
	 */
	public void removeMarkedProjectiles()
	{
		for (Projectile removedProjectileEntity : removedProjectiles)
		{
			activeProjectiles.remove(removedProjectileEntity);
		}
	}
	
	/**
	 * Update all active projectiles being tracked by this registry
	 * 
	 * @author R Lee
	 */
	public void updateProjectiles()
	{
		for (DfProjectile projectile : activeProjectiles.values())
		{
			projectile.update();
		}
	}
	
	/**
	 * Remove all projectiles being tracked by this registry
	 * 
	 * @author R Lee
	 */
	public void clearProjectiles()
	{
		for (DfProjectile projectile : getProjectiles().values())
		{
			projectile.remove(true);
		}
	}
}
