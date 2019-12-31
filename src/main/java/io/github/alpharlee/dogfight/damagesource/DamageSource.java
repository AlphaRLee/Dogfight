package io.github.alpharlee.dogfight.damagesource;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.github.alpharlee.dogfight.projectile.DfProjectile;
import io.github.alpharlee.dogfight.registry.DamageRegistry;

public class DamageSource
{
	private DamageRegistry damageRegistry;
	
	private Player target;
	
	private Entity attacker;
	private DfProjectile attackProjectile;
	
	private int ticksLived = 0;		//Legacy terms referring to entities: ticksLived for how long this DamageSource has lived
	private int maxTicksLived;	//maxTicksLived for maximum tick amount to keep this damage source recorded
	
	public DamageSource(DamageRegistry damageRegistry, Player target, DfProjectile attackProjectile, int maxTicksLived)
	{
		this.setDamageRegistry(damageRegistry);
		this.setTarget(target);
		this.setAttackProjectile(attackProjectile);
		this.setAttacker(getAttackProjectile().getOwner()); //Must come after setAttackProjectile()
		this.setMaxTicksLived(maxTicksLived);
	}
	
	public DamageRegistry getDamageRegistry()
	{
		return this.damageRegistry;
	}
	
	public void setDamageRegistry(DamageRegistry registry)
	{
		this.damageRegistry = registry;
	}
	
	/**
	 * @return the target, the one who was damaged
	 */
	public Player getTarget()
	{
		return target;
	}

	/**
	 * @param target the target to set, the one who was damaged
	 */
	public void setTarget(Player target)
	{
		this.target = target;
	}

	public Entity getAttacker()
	{
		return this.attacker;
	}
	
	public void setAttacker(Entity attacker)
	{
		this.attacker = attacker;
	}
	
	public DfProjectile getAttackProjectile()
	{
		return this.attackProjectile;
	}
	
	public void setAttackProjectile(DfProjectile projectile)
	{
		this.attackProjectile = projectile;
	}
	
	public int getTicksLived()
	{
		return this.ticksLived;
	}
	
	public void setTicksLived(int ticksLived)
	{
		this.ticksLived = ticksLived;
	} 
	
	public int getMaxTicksLived()
	{
		return this.maxTicksLived;
	}
	
	public void setMaxTicksLived(int maxTicksLived)
	{
		this.maxTicksLived = maxTicksLived;
	}
	
	public void update()
	{
		//TODO: Fill me!
		
		checkForRemoval();
		
		ticksLived++;
	}
	
	/**
	 * Check if ticksLived is greater than maxTicksLived.
	 * </br></br>
	 * Does NOT check if target is dead
	 *
	 * @author R Lee
	 */
	private void checkForRemoval()
	{
		if (ticksLived > maxTicksLived)
		{
			remove(true);
		}
	}
	
	public void remove(boolean delay)
	{
		getDamageRegistry().remove(this, delay);
	}
}
