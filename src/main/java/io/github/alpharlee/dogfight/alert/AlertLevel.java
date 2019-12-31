package io.github.alpharlee.dogfight.alert;

import java.util.ArrayList;
import java.util.List;

/**
 * Debug handler that can be used to notify either console or online staff of Dogfight alerts
 * </br>
 * OFF is to be reserved for display levels only. No messages should be logged at this level
 * </br>
 * INFO is for feedback information about an event. Should be reserved for debugging or major actions
 * </br>
 * WARNING is for events that should not occur or events that can lead up to severe or fatal events
 * </br>
 * SEVERE is for events that damage usage or content
 * </br>
 * FATAL is for events that trigger an error within Dogfight source code. NOTE: This is not a fool-proof mechanism and primarily is used for weak code or for debugging
 * @author R Lee
 *
 */
public enum AlertLevel
{
	INFO("Info", "info", "i", "1"),
	WARNING("Warning!", "warning", "danger", "warn", "w", "2"),
	SEVERE("Severe!", "severe", "s", "3"),
	FATAL("FATAL!", "fatal", "f", "4");
	
	private String displayName;
	private List<String> synonyms;
	
	private AlertLevel(String displayName, String... altNames)
	{
		synonyms = new ArrayList<String>();
		synonyms.add(displayName);
		
		this.displayName = displayName;
		
		for (String altName : altNames)
		{
			synonyms.add(altName);
		}
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	public List<String> getNames()
	{
		return this.synonyms;
	}
	
	/**
	 * Find the alert type by one of its names
	 * @param searchName
	 * @return ShowAlertType with specified name or null if none found
	 *
	 * @author R Lee
	 */
	public static AlertLevel find(String searchName)
	{
		String name = searchName.toLowerCase();
		
		for (AlertLevel level : AlertLevel.values())
		{
			if (level.getNames().contains(name))
			{
				return level;
			}
		}
		
		return null;
	}
}
