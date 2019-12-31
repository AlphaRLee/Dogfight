package io.github.alpharlee.dogfight.eventlistener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import io.github.alpharlee.dogfight.CommandHandler;
import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.damagesource.DamageSource;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.game.Team;
import io.github.alpharlee.dogfight.projectile.DfProjectile;
import io.github.alpharlee.dogfight.projectile.ProjectileType;
import io.github.alpharlee.dogfight.score.Score;
import io.github.alpharlee.dogfight.score.Scorestreak;

public class PlayerDeathListener implements Listener
{
	public PlayerDeathListener()
	{
		
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Game game = Dogfight.instance.getGame(player);
		
		//Validate game
		if (game != null)
		{
			DamageSource source = game.getDamageRegistry().getDamageSource(player);
			
			if (source != null)
			{	
				//Attempt to see if damager was the projectile
				DfProjectile projectile = source.getAttackProjectile();
				
				if (projectile != null)
				{
					onDeathByProjectile(event, player, projectile);
				}
				
				//TODO: Handle other causes of death (eg. burning to death from fire bullet)
				
				//Must clear out source manually from here
				source.remove(false);
			}
			else
			{
				onDeathByEnvironment(event, player);
			}
			
			//Send message to player if they were sending text at the moment of their death
			if (player.isConversing())
			{
				CommandHandler.sendMessage(player, ChatColor.GRAY + "" + ChatColor.ITALIC + "Don't text and fly");
			}
			
			//Extinguish the player if they are on fire
			player.setFireTicks(0);
		}
	}
	
	/**
	 * Play the death messsage, increment scorestreak
	 * @param player
	 * @param projectile
	 *
	 * @author R Lee
	 */
	public void onDeathByProjectile(PlayerDeathEvent event, Player player, DfProjectile projectile)
	{
		broadcastDeathByProjectileMessage(event, player, projectile);
		
		Player attacker = projectile.getOwner();
		Game game = Dogfight.instance.getGame(player);
		
		//Validate game
		if (game != null)
		{
			Team attackersTeam =  game.getPlayerRegistry().getTeam(attacker);
			
			if (attackersTeam != null)
			{
				//Increment points of the attacker if the victim is NEITHER the attacker or a teammate of the attacker
				if (!(attacker.equals(player) || attackersTeam.containsMember(player)))
				{
					int scoreIncrement = getScore(attacker).getKillIncrement();
					int scorestreakIncrement = getScorestreak(attacker).getKillIncrement();
					incrementScore(attacker, scoreIncrement, scorestreakIncrement);
					
					//Alert the team of the increase in points, alert the player of the scorestreak points
					CommandHandler.broadcast(attackersTeam, ChatColor.YELLOW + (scoreIncrement >= 0 ? "+" : "") + ChatColor.DARK_AQUA + scoreIncrement + ChatColor.YELLOW + " points");
					//TODO: Enable this line once scorestreak is actively used
					//CommandHandler.sendMessage(attacker, ChatColor.DARK_AQUA + "+" + ChatColor.LIGHT_PURPLE + scoreIncrement + ChatColor.DARK_AQUA +  " scorestreak points");
				}
				else
				{
					//Decrement score for friendly fire
					//TODO: Give better name
					onDeathByEnvironment(event, player);
				}
			}
		}
			//TODO: See local task list for more things to do here (Death-related events)
	}
	
	/**
	 * Deaths where the player was killed either accidentally or suicidally, but not involving an enemy projectile
	 * @param event
	 * @param player
	 *
	 * @author R Lee
	 */
	public void onDeathByEnvironment(PlayerDeathEvent event, Player player)
	{
		Game game = Dogfight.instance.getGame(player);
		
		//Validate game
		if (game != null)
		{
			Team team = game.getPlayerRegistry().getTeam(player);
			
			if (team != null)
			{
				int scoreIncrement = getScore(player).getDeathIncrement();
				//int scorestreakIncrement = getScorestreak(player).getDeathIncrement();
				incrementScore(player, scoreIncrement, 0);
				
				//Alert the team of the increase in points, alert the player of the scorestreak points
				CommandHandler.broadcast(team, ChatColor.RED + (scoreIncrement >= 0 ? "+" : "") + scoreIncrement + ChatColor.YELLOW + " points");
			}
		}
	}
	
	/**
	 * Increment the score and scorestreak of a player by specified amounts.
	 * Inputting 0 as an amount will cause no changes
	 * @param player Player to incrememnt score of. Must be on a team within a game
	 * @param incrementScoreAmount Amount to increment the score of player's team by. Can be negative to decrement
	 * @param incrementScorestreakAmount Amount to increment the scorestreak of the player by. Can be negative to decrement
	 *
	 * @author R Lee
	 */
	private void incrementScore(Player player, int incrementScoreAmount, int incrementScorestreakAmount)
	{
		Game game = Dogfight.instance.getGame(player);
		
		//Validate game
		if (game != null)
		{
			Team team = game.getPlayerRegistry().getTeam(player);
			
			if (team != null && incrementScoreAmount != 0)
			{
				//Increment the score, with min score of 0
				game.getScoreRegistry().getScore(team).increment(incrementScoreAmount, true);
			}
			
			if (incrementScorestreakAmount != 0)
			{
				//Increment the score, with min score of 0
				getScorestreak(player).increment(incrementScorestreakAmount, true);
			}
		}
	}
	
	/**
	 * Broadcast death by projectile message to all players in the same game as the player who just died
	 * @param player Player who died
	 * @param projectile Projectile that killed the player
	 *
	 * @author R Lee
	 */
	private void broadcastDeathByProjectileMessage(PlayerDeathEvent event, Player player, DfProjectile projectile)
	{
		Game game = Dogfight.instance.getGame(player);
		
		//Validate game
		if (game == null)
		{
			return;
		}
		
		String actionMessage = "";
		String actionMessage2 = ""; //Message displayed after attacker's name is displayed
		Player attacker = projectile.getOwner();
		
		switch (projectile.getType())
		{
		case BULLET:
			actionMessage = " was shot down by ";
			break;
			
		case FIRE_BULLET:
			actionMessage = " got incinerated by ";
			actionMessage2 = " with a " + ProjectileType.FIRE_BULLET.getItemName();
			break;
			
		case MINE:
			actionMessage = " ran into a present left by ";
			break;
			
		case MISSILE: case PLAYER_MISSILE:
			actionMessage = " was obliterated by ";
			actionMessage2 = " with a " + ProjectileType.MISSILE.getItemName();
			break;
			
		case SHOTGUN:
			actionMessage = " got blasted by ";
			actionMessage2 = "'s " + ProjectileType.SHOTGUN.getItemName();
			break;
			
		case SNIPER:
			actionMessage = " was sniped by ";
			break;
			
		case SQUID_SHOT:
			//If there is a carry-over damage (eg. damage now, target runs away and dies later), this is useful
			actionMessage = " died by...being blinded by ";
			actionMessage2 = "?";
			break;
			
		default:
			break;
		}
		
		//TODO: Get team colors
		String deathMessage = ChatColor.RESET + player.getDisplayName() + ChatColor.YELLOW + actionMessage + ChatColor.RESET + attacker.getDisplayName() + ChatColor.YELLOW + actionMessage2;
		
		//TODO: Show death message to players only in game. Maybe requires packets?
		event.setDeathMessage(deathMessage);
	}
	
	/**
	 * Convenience method to get the score of the team the player is on
	 * If the player has no score, team or game registered to them, return null
	 * @param player Member of team to get score of. Must belong to a game
	 * @return Team's scorestreak. Returns null if player is not in game or no team found
	 *
	 * @author R Lee
	 */
	private Score getScore(Player player)
	{
		Game game = Dogfight.instance.getGame(player);
		
		if (game != null)
		{
			Team team = game.getPlayerRegistry().getTeam(player);
			
			if (team != null)
			{
				return game.getScoreRegistry().getScore(team);
			}
		}
		
		return null; //No game found
	}	
	
	/**
	 * Convenience method to get the player's scorestreak
	 * If the player has no scorestreak registered to them, return null
	 * @param player Player to get scorestreak of. Must belong to a game
	 * @return Player's scorestreak. Returns null if player is not in game or no scorestreak found
	 *
	 * @author R Lee
	 */
	private Scorestreak getScorestreak(Player player)
	{
		Game game = Dogfight.instance.getGame(player);
		
		if (game != null)
		{
			return game.getScoreRegistry().getScorestreak(player);
		}
		
		return null; //No game found
	}
}
