package io.github.alpharlee.dogfight.runnable;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.projectile.Missile;

public class IncomingMissileWarning extends TickRunnable
{
	private Player player;
	private Game game;
	
	private List<Missile> missiles;
		
	private int fadeInTime = 0;
	private int duration = 40; //2 seconds, 40 ticks
	private int fadeOutTime = 5; //0.25 seconds
	
	private Sound warningSound = Sound.BLOCK_NOTE_BLOCK_PLING;
	private SoundCategory warningSoundCategory = SoundCategory.BLOCKS;
	private float warningVolume = 1;
	private float warningPitch = 2F;
	
	private int warnInterval = (int) Dogfight.NOT_FOUND;
	
	public IncomingMissileWarning(Player player, Game game)
	{
		setPlayer(player);
		setGame(game);
		
		missiles = new ArrayList<Missile>();
		
		showWarning();
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public void setPlayer(Player player)
	{
		this.player = player;
	}
	
	public Game getGame()
	{
		return this.game;
	}
	
	public void setGame(Game game)
	{
		this.game = game;
	}
	
	/**
	 * Add a missile to chase the player with
	 * @param missile
	 *
	 * @author R Lee
	 */
	public void addMissile(Missile missile)
	{
		this.missiles.add(missile);
	}
	
	private void showWarning()
	{		
		//Send subtitle (title is blank)
		getPlayer().resetTitle();
		getPlayer().sendTitle("", ChatColor.DARK_RED + "" + ChatColor.ITALIC + "INCOMING MISSILE!", fadeInTime, duration, fadeOutTime);
	}
	
	/**
	 * Set the next warning based on the nearest missile
	 * @return true if missile is eligible, false if not
	 *
	 * @author R Lee
	 */
	private boolean setNextWarn()
	{
		double missileDistance = getNearestMissileDistance();
		final double maxMissileDistance = 100; //Arbitrary number
		
		//If a missile is found and it is within the maxMissileDistance
		if (missileDistance != Dogfight.NOT_FOUND && missileDistance <= maxMissileDistance)
		{
			setNextWarn(missileDistance);
			
			return true; //New missile found
		}
		
		return false; //No missile found
	}
	
	private void setNextWarn(double distance)
	{
		final double periodToDistanceRate = 0.5; //TODO: Arbitrary value based on testing
		
		warnInterval = (int) Math.floor(distance * periodToDistanceRate);
	}
	
	private void playWarnSound()
	{
		getPlayer().playSound(getPlayer().getLocation(), warningSound, warningSoundCategory, warningVolume, warningPitch);
	}
	
	/**
	 * Get the distance between the player and the nearest missile
	 * @return Distance or NOT_FOUND (-999) if no missile found nearby
	 *
	 * @author R Lee
	 */
	private double getNearestMissileDistance()
	{	
		//Set placeholder values for no entity found yet
		double distanceSquared = Dogfight.NOT_FOUND;
		double nearestDistanceSquared = Dogfight.NOT_FOUND;
	
		//TODO: Remove the following: Location playerLocation = player.getLocation();
		
		List<Missile> removedMissiles = new ArrayList<Missile>();
		
		for (Missile missile : missiles)
		{
			if (missile != null && !missile.getMissileEntity().isDead())
			{
				//TODO: Remove the following: distanceSquared = missile.getMissileEntity().getLocation().distanceSquared(playerLocation);
				distanceSquared = missile.getDistanceToTargetSquared();
				
				//Set the nearest distance and nearest entity if the scanned entity is closer
				if (nearestDistanceSquared > distanceSquared || nearestDistanceSquared == Dogfight.NOT_FOUND)
				{
					nearestDistanceSquared = distanceSquared;
				}
			}
			else
			{
				removedMissiles.add(missile);
			}
		}
		
		//Remove obsolete missiles
		missiles.removeAll(removedMissiles);
		removedMissiles.clear();
		
		return (nearestDistanceSquared != Dogfight.NOT_FOUND ? Math.sqrt(nearestDistanceSquared) : Dogfight.NOT_FOUND);
		
	}
	
	@Override
	public void run()
	{	
		if (warnInterval == (int) Dogfight.NOT_FOUND)
		{
			//Start warning
			setNextWarn();
		}
		else if (warnInterval > 0)
		{
			warnInterval--;
		}
		else if (warnInterval == 0)
		{
			//If warn interval hit (not negative number)
			playWarnSound();
			
			//Attempt to set warning
			if (!setNextWarn())
			{	
				//Hooray! Lift the warning, no nearby missiles!
				game.setIncomingMissileWarning(player, null);
				super.cancel(); //End the task timer
				return; //Escape out of this run (prevent iterateTimer())
			}
		}
		
		iterateTimer();
	}
	
}
