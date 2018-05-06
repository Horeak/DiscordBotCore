package DiscordBotCode.Main.CustomEvents;

import DiscordBotCode.CommandFiles.CommandBase;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

//Event triggered when a command successfully executes
public class CommandExecutedEvent extends Event
{
	private CommandBase command;
	private IMessage message;
	private Thread thread;
	
	public CommandExecutedEvent( CommandBase command, IMessage message, Thread thread )
	{
		this.command = command;
		this.message = message;
		this.thread = thread;
	}
	
	public CommandBase getCommand()
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
