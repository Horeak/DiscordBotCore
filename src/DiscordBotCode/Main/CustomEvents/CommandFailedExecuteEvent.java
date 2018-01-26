package DiscordBotCode.Main.CustomEvents;

import DiscordBotCode.CommandFiles.DiscordCommand;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

//Event triggered when a command fails to execute
public class CommandFailedExecuteEvent extends Event
{
	private DiscordCommand command;
	private IMessage message;
	
	public CommandFailedExecuteEvent( DiscordCommand command, IMessage message )
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
