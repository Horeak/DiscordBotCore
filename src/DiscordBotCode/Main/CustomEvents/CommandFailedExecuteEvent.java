package DiscordBotCode.Main.CustomEvents;

import DiscordBotCode.CommandFiles.DiscordCommand;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

//Event triggered when a command fails to execute
public class CommandFailedExecuteEvent extends Event
{
	private DiscordCommand command;
	private IMessage message;
	private Thread thread;
	
	public CommandFailedExecuteEvent( DiscordCommand command, IMessage message, Thread thread )
	{
		this.command = command;
		this.message = message;
		this.thread = thread;
	}
	
	public DiscordCommand getCommand()
	{
		return command;
	}
	
	public IMessage getMessage()
	{
		return message;
	}
	
	public Thread getThread()
	{
		return thread;
	}
}
