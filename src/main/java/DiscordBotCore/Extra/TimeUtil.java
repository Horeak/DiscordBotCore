package DiscordBotCore.Extra;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings( { "unused", "SameParameterValue", "WeakerAccess" } )
public class TimeUtil
{
	private static final HashMap<String, Long> startTimers = new HashMap<>();
	
	public static void startTimeTaker( String id )
	{
		startTimeTaker(id, System.currentTimeMillis());
	}

	public static void startTimeTaker( String id, Long time )
	{
		if (startTimers.containsKey(id)) {
			startTimers.remove(id);
		}
		
		startTimers.put(id, time);
	}
	
	public static void clearTimeTaker()
	{
		startTimers.clear();
	}
	
	public static void removeTimeTaker( String id )
	{
		startTimers.remove(id);
	}
	
	public static long getStartTime( String id )
	{
		if (startTimers.containsKey(id)) {
			return startTimers.get(id);
		}
		
		return -1;
	}
	
	public static long getTime( String id )
	{
		if (startTimers.containsKey(id)) {
			return System.currentTimeMillis() - startTimers.get(id);
		}
		
		return -1;
	}
	
	public static String getText( String id, String formate, boolean includeZero )
	{
		return getText(id, 0, formate, includeZero);
	}
	
	public static String getText( String id, long starFrom, String formate, boolean includeZero )
	{
		return getText(getTime(id) - starFrom, formate, includeZero);
	}
	
	public static String getText( long time, String formate, boolean includeZero )
	{
		long secs = TimeUnit.MILLISECONDS.toSeconds(time);
		long mins = TimeUnit.MILLISECONDS.toMinutes(time);
		long hours = TimeUnit.MILLISECONDS.toHours(time);
		long days = TimeUnit.MILLISECONDS.toDays(time);
		
		if (days > 0) {
			hours -= TimeUnit.DAYS.toHours(days);
			mins -= TimeUnit.DAYS.toMinutes(days);
			secs -= TimeUnit.DAYS.toSeconds(days);
		}
		
		if (hours > 0) {
			mins -= TimeUnit.HOURS.toMinutes(hours);
			secs -= TimeUnit.HOURS.toSeconds(hours);
		}
		
		if (mins > 0) {
			secs -= TimeUnit.MINUTES.toSeconds(mins);
		}
		
		String text = formate;
		
		if (includeZero || time > 0) {
			text = text.replace("<ms>", time + "ms ");
		}
		
		if (includeZero || secs > 0) {
			text = text.replace("<secs>", secs + "s ");
		}
		
		if (includeZero || mins > 0) {
			text = text.replace("<mins>", mins + "m ");
		}
		
		if (includeZero || hours > 0) {
			text = text.replace("<hours>", hours + "h ");
		}
		
		if (includeZero || days > 0) {
			text = text.replace("<days>", days + "d ");
		}
		
		text = text.replace("<ms>", "");
		text = text.replace("<secs>", "");
		text = text.replace("<mins>", "");
		text = text.replace("<hours>", "");
		text = text.replace("<days>", "");
		
		return text;
	}
}
