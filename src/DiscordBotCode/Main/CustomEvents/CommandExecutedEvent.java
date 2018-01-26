package DiscordBotCode.Main.CustomEvents;

import DiscordBotCode.CommandFiles.DiscordCommand;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

//Event triggered when a command successfully executes
public class CommandExecutedEvent extends Event
{
	private DiscordCommand command;
	private IMessage message;
	
	public CommandExecutedEvent( DiscordCommand command, IMessage message )
	{
		this.command = command;
		this.message = message;
	}
	
	public DiscordCommand getCommand()
	{
		return command;
	}
	
	public IMessage getMessage()
	{
		return message;
	}
}
