package DiscordBotCode.CommandFiles;

public abstract class DiscordSubCommand extends DiscordCommand
{
	public DiscordChatCommand baseCommand;
	
	public DiscordSubCommand( DiscordChatCommand baseCommand )
	{
		this.baseCommand = baseCommand;
	}
}
