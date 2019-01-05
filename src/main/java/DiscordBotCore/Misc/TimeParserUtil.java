package DiscordBotCore.Misc;

import DiscordBotCore.Main.Utils;
import org.joda.time.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class TimeParserUtil
{
	private static String[] types = new String[]{"y", "w", "d", "h", "m", "s"};
	
	public static ArrayList<String> getTimeArguments(String[] strings){
		ArrayList<String> list = new ArrayList<>();
		
		for(String t : strings){
			String type = t.substring(t.length() - 1).toLowerCase();
			String numT = t.substring(0, t.length() - 1);
			
			if(!Utils.isInteger(numT)){
				continue;
			}
			
			for(String tg : types){
				if(type.equalsIgnoreCase(tg)){
					list.add(t);
					break;
				}
			}
		}
		
		return list;
	}
	
	public static long getDaysTime( String[] input )
	{
		long delay = 0;
		
		for (String t : input) {
			String type = t.substring(t.length() - 1).toLowerCase();
			String numT = t.substring(0, t.length() - 1);
			
			if(!Utils.isInteger(numT)){
				continue;
			}
			
			int num = Integer.parseInt(numT);
			
			switch(type){
				case "y":
					delay += TimeUnit.MILLISECONDS.convert(num * 365, TimeUnit.DAYS);
					break;
				
				case "m":
					delay += TimeUnit.MILLISECONDS.convert(num * 30, TimeUnit.DAYS);
					break;
				
				default:
					delay = getDelay(delay, type, num);
			}
		}
		
		return delay;
	}
	
	public static long getTime( String[] input )
	{
		long delay = 0;
		
		for (String t : input) {
			String type = t.substring(t.length() - 1).toLowerCase();
			String numT = t.substring(0, t.length() - 1);
			
			if(!Utils.isInteger(numT)){
				continue;
			}
			int num = Integer.parseInt(numT);
			delay = getDelay(delay, type, num);
		}
		
		return delay;
	}
	
	protected static long getDelay( long delay, String type, int num )
	{
		switch(type){
			case "w":
				delay += Weeks.weeks(num).toStandardDuration().getMillis();
				break;
				
			case "d":
				delay += Days.days(num).toStandardDuration().getMillis();
				break;
				
			case "h":
				delay += Hours.hours(num).toStandardDuration().getMillis();
				break;
				
			case "m":
				delay += Minutes.minutes(num).toStandardDuration().getMillis();
				break;
				
			case "s":
				delay += Seconds.seconds(num).toStandardDuration().getMillis();
				break;
		}
		
		return delay;
	}
	
	public static String getTimeText(String[] strings){
		return getTimeText(getTime(strings));
	}
	
	//Millis is milliseconds from base time, if it gives 1970 add System.currentTimeMillis() to input millis
	public static String getTimeText( Date date, boolean yearsB, boolean monthsB, boolean weeksB, boolean daysB, boolean hoursB, boolean minsB, boolean secondsB )
	{
		long s1 = date.getTime() > System.currentTimeMillis() ? System.currentTimeMillis() : date.getTime();
		long s2 = System.currentTimeMillis() > date.getTime() ? System.currentTimeMillis() : date.getTime();
		
		Interval interval = new Interval(Instant.ofEpochMilli(s1), Instant.ofEpochMilli(s2));
		Period period = interval.toPeriod();
		
		int years   = period.getYears();
		int months  = period.getMonths();
		int weeks   = period.getWeeks();
		int days    = period.getDays();
		int hours   = period.getHours();
		int minutes = period.getMinutes();
		int seconds = period.getSeconds();
		
		StringJoiner joiner = new StringJoiner(", ");
		
		if (years   > 0 && yearsB)   joiner.add(years + " year" +     (years > 1 ? "s" : ""));
		if (months  > 0 && monthsB)  joiner.add(months + " month" +   (months > 1 ? "s" : ""));
		if (weeks   > 0 && weeksB)   joiner.add(weeks + " week" +     (weeks > 1 ? "s" : ""));
		if (days    > 0 && daysB)    joiner.add(days + " day" +       (days > 1 ? "s" : ""));
		if (hours   > 0 && hoursB)   joiner.add(hours + " hour" +     (hours > 1 ? "s" : ""));
		if (minutes > 0 && minsB)    joiner.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
		if (seconds > 0 && secondsB) joiner.add(seconds + " second" + (seconds > 1 ? "s" : ""));
		
		return joiner.toString();
	}
	
	public static String getTimeText( Date millis, boolean seconds )
	{
		return getTimeText(millis, true, true, true, true, true, true, seconds);
	}
	
	public static String getTimeText( Date millis )
	{
		return getTimeText(millis, true, true, true, true, true, true, true);
	}
	
	
	public static String getTimeText( long millis, boolean yearsB, boolean monthsB, boolean weeksB, boolean daysB, boolean hoursB, boolean minsB, boolean secondsB )
	{
		return getTimeText(new Date(System.currentTimeMillis() + millis), yearsB, monthsB, weeksB, daysB, hoursB, minsB, secondsB);
	}
	
	public static String getTimeText( long millis, boolean seconds )
	{
		return getTimeText(millis, true, true, true, true, true, true, seconds);
	}
	
	public static String getTimeText( long millis )
	{
		return getTimeText(millis, true, true, true, true, true, true, true);
	}
	
	
	public static String getTimeLargestOnly( Date date )
	{
		String t = getTimeText(date);
		String[] tk = t.split(",");
		
		if(tk.length > 0)
		{
			return tk[0];
		}
		
		return "";
	}
	
	public static String getTimeLargestOnly( long millis)
	{
		return getTimeLargestOnly(new Date(System.currentTimeMillis() + millis));
	}
}
