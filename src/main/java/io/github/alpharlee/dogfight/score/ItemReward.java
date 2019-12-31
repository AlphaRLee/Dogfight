package io.github.alpharlee.dogfight.score;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemReward extends ScorestreakReward
{
	private ItemStack rewardItem; //Item to grant player upon granting this reward
	
	public ItemReward(Player player, int requiredScore, ItemStack rewardItem)
	{
		super(player, requiredScore);
		setRewardItem(rewardItem);
	}
	
	public ItemStack getRewardItem()
	{
		return rewardItem;
	}
	
	public void setRewardItem(ItemStack itemStack)
	{
		this.rewardItem = itemStack;
	}
	
	/**
	 * Give the player the reward item
	 */
	@Override
	protected void onGrant()
	{
		//TODO: Check for inventory space
		getPlayer().getInventory().addItem(getRewardItem());
	}
}
