package DiscordBotCode.Main.CustomEvents;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import sx.blah.discord.api.events.Event;

//Event triggered when a command is disabled
public class CommandRemoveEvent extends Event
{
	private DiscordChatCommand commandRegistered;
	private String commandKey;
	
	public CommandRemoveEvent( DiscordChatCommand commandRegistered, String commandKey )
	{
		this.commandRegistered = commandRegistered;
		this.commandKey = commandKey;
	}
	
	public DiscordChatCommand getCommandRegistered()
	{
		return commandRegistered;
	}
	
	public String getCommandKey()
	{
		return commandKey;
	}
}
