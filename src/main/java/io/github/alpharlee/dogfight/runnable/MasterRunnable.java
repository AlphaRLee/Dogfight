package io.github.alpharlee.dogfight.runnable;

import org.bukkit.entity.Player;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.game.Game;

public class MasterRunnable extends TickRunnable
{		
	public MasterRunnable()
	{
		//TODO: Fill with relevant fields (operates fine without any)
	}
	
	@Override
	public void run()
	{
		//Iterate through every game
		for (Game game : Dogfight.instance.getGames())
		{
			game.update();
			
			//Iterate through every player
			for (Player player : game.getPlayerRegistry().getPlayers().keySet())
			{
				//Boost players if applicable
				Dogfight.instance.getBoostHandler().checkBoost(player);
				Dogfight.instance.getBoostHandler().checkForceGlide(player);
			}
		}
	
		iterateTimer();
	}
}
