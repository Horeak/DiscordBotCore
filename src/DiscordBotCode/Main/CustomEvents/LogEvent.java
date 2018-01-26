package DiscordBotCode.Main.CustomEvents;

import sx.blah.discord.api.events.Event;
//Used to gain log output for use in costume UIs for bots
public class LogEvent extends Event
{
	private String text;
	
	public LogEvent( String text )
	{
		this.text = text;
	}
	
	public String getText()
	{
		return text;
	}
}
