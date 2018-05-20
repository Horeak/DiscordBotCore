package DiscordBotCode.CommandFiles.Commands.DevCommands;

import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.DeveloperSystem.DevCommandBase;
import DiscordBotCode.Misc.Annotation.DiscordCommand;
import sx.blah.discord.handle.obj.IMessage;

@DiscordCommand
public class ShutdownCommand extends DevCommandBase
{
	@Override
	public String commandPrefix()
	{
		return "shutdown";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		System.out.println("Shutting down bot!");
		System.exit(0);
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return DevAccess.isOwner(message.getAuthor().getLongID());
	}
}
