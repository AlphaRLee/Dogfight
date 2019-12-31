package io.github.alpharlee.dogfight.runnable;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * A BukkitRunnable that has incrementTimeUnits configured to operate per tick
 * Repeat delays larger than a tick may be used, but "secondCount" will 20x the repeat delay, "minuteCount" will be 60x of "secondCount" and so on
 * @author R Lee
 *
 */
public abstract class TickRunnable extends BukkitRunnable
{
	protected int tickCount = 0;
	protected int secondCount = 0;
	protected int minuteCount = 0;
	protected int hourCount = 0;
	
	@Override
	public abstract void run();
	
	protected void iterateTimer()
	{
		final int ticksPerSecond = 20; //Predefined Minecraft default: 20 ticks in 1 second
		final int secondsPerMinute = 60;
		final int minutesPerHour = 60;
		
		//Manually iterate up tick timer
		tickCount++;
		
		//Reduce ticks to 0 when reaching 20, seconds to 0 when reaching 60, etc.
		incrementTimeUnit(tickCount, secondCount, ticksPerSecond);
		incrementTimeUnit(secondCount, minuteCount, secondsPerMinute);
		incrementTimeUnit(minuteCount, hourCount, minutesPerHour);
	}
	
	/**
	 * Increment time intervals if the small unit has reached the conversion size. Eg: For every 20 ticks, increment the second timer
	 * @param smallUnit Smaller time unit. Eg: tick when compared to seconds
	 * @param largeUnit Larger time unit. Eg: second when compared to ticks
	 * @param smallUnitPerLargeUnit How many small units are in each large unit. Eg: 20 ticks per second
	 *
	 * @author R Lee
	 */
	protected void incrementTimeUnit(int smallUnit, int largeUnit, int smallUnitPerLargeUnit)
	{
		if (smallUnit >= smallUnitPerLargeUnit)
		{
			smallUnit = 0;
			largeUnit++;
		}
	}
}
