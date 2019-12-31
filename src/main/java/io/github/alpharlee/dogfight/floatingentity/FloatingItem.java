package io.github.alpharlee.dogfight.floatingentity;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.registry.FloatingEntityRegistry;

public class FloatingItem extends Hoop
{
	public FloatingItem(FloatingEntityRegistry registry, Location location, Item item, double radius, Particle particle)
	{
		super(registry, location, radius, particle);
		setItem(item);
	}
	
	public FloatingItem(FloatingEntityRegistry registry, Location location, ItemStack itemStack, double radius, Particle particle)
	{
		super(registry, location, radius, particle);
		setItemStack(itemStack);
	}

	public Item getItem()
	{
		return (Item) super.getEntity();
	}
	
	/**
	 * Set the item entity of this item
	 * @param item
	 *
	 * @author R Lee
	 */
	public void setItem(Item item)
	{
		super.setEntity(item);
		itemSetup();
	}
	
	public ItemStack getItemStack()
	{
		return getItem().getItemStack();
	}
	
	/**
	 * Set the item stack for this item.
	 * If no item exists yet, spawn one in using {@link FloatingItem#setItem(Item)} at {@link FloatingItem#getLocation()}
	 * @param itemStack
	 *
	 * @author R Lee
	 */
	public void setItemStack(ItemStack itemStack)
	{
		if (getItem() != null)
		{
			getItem().setItemStack(itemStack);
		}
		else
		{
			setItem(getLocation().getWorld().dropItem(getLocation(), itemStack));
		}
	}
	
	private void itemSetup()
	{
		//Look ma, I can fly!
		getItem().setGravity(false);
		getItem().setPickupDelay(maxTicksLived); //KLUDGE: Delay until max ticks lived. Rely on trigger for retrieval
		MetadataTag.FLOATING_ITEM.setMetadata(getItem(), this);
	}
	
	/**
	 * Recalculate the ring location based on this item's location
	 * Set the ticks lived based off the item's ticksLived
	 * 
	 * @author R Lee
	 */
	@Override
	public void update()
	{
		update(true);
	}
	
	@Override
	public void checkForRemoval()
	{
		super.checkForRemoval();
		
		if (!isEntityAlive())
		{
			remove(true);
		}
	}
	
	@Override
	public void trigger(Entity entity)
	{
		if (!isEntityAlive())
		{
			return;
		}
		
		//Only allow players
		if (!(entity instanceof Player))
		{
			return;
		}
		
		Player player = (Player) entity;
		
		//Attempt to add items. Items that cannot be added are placed in excess items
		//In this case here, only one item is being added
		HashMap<Integer, ItemStack> excessItems = player.getInventory().addItem(getItemStack());
		
		if (excessItems.isEmpty())
		{
			getItem().remove();
			
			//Play sound at player, from SoundCategory Blocks, at a volume of 0.5 and a pitch of 1
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1);
		}
			
		return;
	}
}
