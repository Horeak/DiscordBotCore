package DiscordBotCode.Main.CustomEvents;

import DiscordBotCode.CommandFiles.DiscordCommand;
import sx.blah.discord.api.events.Event;

//Event triggered when a command is registered
public class CommandRegisterEvent extends Event
{
	private DiscordCommand commandRegistered;
	private String commandKey;
	
	public CommandRegisterEvent( DiscordCommand commandRegistered, String commandKey )
	{
		this.commandRegistered = commandRegistered;
		this.commandKey = commandKey;
	}
	
	public DiscordCommand getCommandRegistered()
	{
		return commandRegistered;
	}
	
	public String getCommandKey()
	{
		return commandKey;
	}
}
