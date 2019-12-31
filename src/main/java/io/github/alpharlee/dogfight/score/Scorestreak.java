package io.github.alpharlee.dogfight.score;

import org.bukkit.entity.Player;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.projectile.ProjectileType;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Handles the current scorestreak a player holds
 * @author R Lee
 */
public class Scorestreak extends ScoreKeeper
{	
	public Player player;
	
	//Configurable values through scorestreak.yml
	private int killIncrement;
	private Map<ProjectileType, Integer> projectileCost;
	
	private static int defaultKillIncrement = 100;
	private static Map<ProjectileType, Integer> defaultProjectileCost = new HashMap<ProjectileType, Integer>();
	
	//TODO: Fill in with more appropriate values
	
	public Scorestreak(Game game, Player player) //TODO: Fill in properly
	{
		super(game);
		setup(player);
		//TODO: Load configured values for scorestreak value, rewardInterval and rewardValue
	}
	
	private void setup(Player player)
	{
		setPlayer(player);
		
		setKillIncrement(defaultKillIncrement);
		projectileCost = defaultProjectileCost;
	}
	
	/**
	 * Get values from scorestreak.yml and store them.
	 * Must be called after scorestreak.yml is loaded
	 *
	 * @author R Lee
	 */
	public static void setupDefaultValues()
	{
		YamlConfiguration streakData = Dogfight.instance.getScorestreakData();
		
		/*
		 * streakData.get____ parameters:
		 * param 1: Key path. Follows section.subsection.subsection notation
		 * param 2: Default value if value cannot be found
		 */
		defaultKillIncrement = streakData.getInt("increment.kill", 100);
		defaultProjectileCost.put(ProjectileType.FIRE_BULLET, streakData.getInt("cost.firebullet"));
		defaultProjectileCost.put(ProjectileType.MINE, streakData.getInt("cost.mine"));
		defaultProjectileCost.put(ProjectileType.MISSILE, streakData.getInt("cost.missile"));
		defaultProjectileCost.put(ProjectileType.PLAYER_MISSILE, streakData.getInt("cost.playermissile"));
		defaultProjectileCost.put(ProjectileType.SHOTGUN, streakData.getInt("cost.shotgun"));
		defaultProjectileCost.put(ProjectileType.SNIPER, streakData.getInt("cost.sniper"));
		defaultProjectileCost.put(ProjectileType.SQUID_SHOT, streakData.getInt("cost.squidshot"));
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public void setPlayer(Player player)
	{
		this.player = player;
	}
		
	public int getKillIncrement()
	{
		return this.killIncrement;
	}
	
	/**
	 * Set the amount players scores increment by when they make a kill
	 * @param value
	 *
	 * @author R Lee
	 */
	public void setKillIncrement(int value)
	{
		this.killIncrement = 0;
	}
	
	public Map<ProjectileType, Integer> getProjectileCost()
	{
		return this.projectileCost;
	}
	
	public void setProjectileCost(Map<ProjectileType, Integer> projectileCost)
	{
		this.projectileCost = projectileCost;
	}
	
	public int getProjectileCost(ProjectileType type)
	{
		return getProjectileCost().get(type);
	}
	
	public void setProjectileCost(ProjectileType type, int cost)
	{
		getProjectileCost().put(type, cost);
	}
}
