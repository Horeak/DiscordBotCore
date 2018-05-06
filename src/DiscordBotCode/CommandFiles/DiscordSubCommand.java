package DiscordBotCode.CommandFiles;

public abstract class DiscordSubCommand extends CommandBase
{
	public DiscordChatCommand baseCommand;
	
	public DiscordSubCommand( DiscordChatCommand baseCommand )
	{
		this.baseCommand = baseCommand;
	}
}
