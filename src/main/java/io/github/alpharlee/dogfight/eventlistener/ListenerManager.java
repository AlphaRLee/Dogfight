package io.github.alpharlee.dogfight.eventlistener;

import org.bukkit.event.Listener;

import io.github.alpharlee.dogfight.Dogfight;

public class ListenerManager
{
	public EventListener eventListener;
	public PlayerDeathListener playerDeathListener;
	
	public ListenerManager()
	{
		this.eventListener = new EventListener();
		this.playerDeathListener = new PlayerDeathListener();
		
		registerEvents(this.eventListener);
		registerEvents(this.playerDeathListener);
	}
	
	/**
	 * Register all events inside each listener to the server
	 * @param listener
	 *
	 * @author R Lee
	 */
	private void registerEvents(Listener listener)
	{
		Dogfight.instance.getServer().getPluginManager().registerEvents(listener, Dogfight.instance);
	}
}
