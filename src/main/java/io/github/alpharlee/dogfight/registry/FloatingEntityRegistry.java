package io.github.alpharlee.dogfight.registry;

import java.util.ArrayList;

import io.github.alpharlee.dogfight.floatingentity.FloatingEntity;
import io.github.alpharlee.dogfight.game.Game;

public class FloatingEntityRegistry extends Registry
{
	private ArrayList<FloatingEntity> activeFloatingEntities;
	private ArrayList<FloatingEntity> removedFloatingEntities;
	
	public FloatingEntityRegistry(Game game)
	{
		super(game);
		
		this.activeFloatingEntities = new ArrayList<FloatingEntity>();
		this.removedFloatingEntities = new ArrayList<FloatingEntity>();
	}
	
	/**
	 * Get all the active floating entities
	 * @return
	 *
	 * @author R Lee
	 */
	public ArrayList<FloatingEntity> getFloatingEntities()
	{
		return this.activeFloatingEntities;
	}
	
	public void addFloatingEntity(FloatingEntity floatingEntity)
	{
		activeFloatingEntities.add(floatingEntity);
	}
	
	/**
	 * Remove a floating entity
	 * @param floatingEntity
	 * @param delay Set to true to mark for removal and remove all marked floating entities at once. Set to false to instantly remove
	 *
	 * @author R Lee
	 */
	public void removeFloatingEntity(FloatingEntity floatingEntity, boolean delay)
	{
		//If requires delay
		if (delay)
		{
			//Mark for delayed removal
			removedFloatingEntities.add(floatingEntity);
		}
		else
		{
			//Instantly remove
			activeFloatingEntities.remove(floatingEntity);
		}
	}
	
	/**
	 * Clear all floating entities marked for removal
	 * 
	 * @author R Lee
	 */
	public void removeMarkedFloatingEntities()
	{
		for (FloatingEntity floatingEntity : removedFloatingEntities)
		{
			activeFloatingEntities.remove(floatingEntity);
		}
	}
	
	/**
	 * Update all active floating entities being tracked by this registry
	 * 
	 * @author R Lee
	 */
	public void updateFloatingEntities()
	{
		for (FloatingEntity floatingEntity : activeFloatingEntities)
		{
			floatingEntity.update();
		}
	}
	
	public void clearFloatingEntities()
	{
		for (FloatingEntity floatingEntity : getFloatingEntities())
		{
			floatingEntity.remove(true);
		}
	}
}
