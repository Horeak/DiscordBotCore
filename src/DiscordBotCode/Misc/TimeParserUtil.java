package DiscordBotCode.Misc;

import DiscordBotCode.Main.Utils;

import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class TimeParserUtil
{
	public static TimeObject getTime( String[] input )
	{
		long weeks = 0, days = 0, hours = 0, mins = 0, seconds = 0;
		long delay = 0;
		
		ArrayList<String> replaces = new ArrayList<>();
		StringJoiner joiner = new StringJoiner(" ");
		
		
		for (String t : input) {
			t = t.toLowerCase();
			
			if (t.endsWith("w")) {
				t = t.replace("w", "");
				if (Utils.isInteger(t)) {
					weeks = Integer.parseInt(t);
					replaces.add(t + "w");
					joiner.add(t + " week" + (Integer.parseInt(t) > 1 ? "s" : ""));
				}
				
			} else if (t.endsWith("d")) {
				t = t.replace("d", "");
				if (Utils.isInteger(t)) {
					days = Integer.parseInt(t);
					replaces.add(t + "d");
					joiner.add(t + " day" + (Integer.parseInt(t) > 1 ? "s" : ""));
				}
				
			} else if (t.endsWith("h")) {
				t = t.replace("h", "");
				if (Utils.isInteger(t)) {
					hours = Integer.parseInt(t);
					replaces.add(t + "h");
					joiner.add(t + " hour" + (Integer.parseInt(t) > 1 ? "s" : ""));
				}
				
			} else if (t.endsWith("m")) {
				t = t.replace("m", "");
				if (Utils.isInteger(t)) {
					mins = Integer.parseInt(t);
					replaces.add(t + "m");
					joiner.add(t + " minute" + (Integer.parseInt(t) > 1 ? "s" : ""));
				}
				
			} else if (t.endsWith("s")) {
				t = t.replace("s", "");
				if (Utils.isInteger(t)) {
					seconds = Integer.parseInt(t);
					replaces.add(t + "s");
					joiner.add(t + " second" + (Integer.parseInt(t) > 1 ? "s" : ""));
				}
			}
		}
		
		delay += TimeUnit.MILLISECONDS.convert(weeks * 7, TimeUnit.DAYS);
		delay += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
		delay += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
		delay += TimeUnit.MILLISECONDS.convert(mins, TimeUnit.MINUTES);
		delay += TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
		
		return new TimeObject(delay, joiner.toString(), replaces);
	}
	
	public static String getTimeText( long millis, boolean weeksB, boolean daysB, boolean hoursB, boolean minsB, boolean seconds )
	{
		long weeks = TimeUnit.MILLISECONDS.toDays(millis) / 7;
		long days = TimeUnit.MILLISECONDS.toDays(millis) - (weeks * 7);
		long hours = TimeUnit.MILLISECONDS.toHours(millis) - (TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis)));
		long min = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
		long sec = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
		
		StringJoiner joiner = new StringJoiner(", ");
		
		if (weeks > 0 && weeksB) {
			joiner.add(weeks + " week" + (weeks > 1 ? "s" : ""));
		}
		
		if (days > 0 && daysB) {
			joiner.add(days + " day" + (days > 1 ? "s" : ""));
		}
		
		if (hours > 0 && hoursB) {
			joiner.add(hours + " hour" + (hours > 1 ? "s" : ""));
		}
		
		if (min > 0 && minsB) {
			joiner.add(min + " minute" + (min > 1 ? "s" : ""));
		}
		
		if (sec > 0 && seconds) {
			joiner.add(sec + " second" + (sec > 1 ? "s" : ""));
		}
		
		
		return joiner.toString();
	}
	
	public static String getTimeText( long millis, boolean seconds )
	{
		return getTimeText(millis, true, true, true, true, seconds);
	}
	
	public static class TimeObject
	{
		private Long time;
		private String timeName;
		private ArrayList<String> replaces;
		
		public TimeObject( Long time, String timeName, ArrayList<String> replaces )
		{
			this.time = time;
			this.timeName = timeName;
			this.replaces = replaces;
		}
		
		public Long getTime()
		{
			return time;
		}
		
		public String getTimeName()
		{
			return timeName;
		}
		
		public ArrayList<String> getReplaces()
		{
			return replaces;
		}
	}
}
