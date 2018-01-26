package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.handle.obj.IMessage;

public class VersionCommand extends DiscordChatCommand
{
	@Override
	public String commandPrefix()
	{
		return "version";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ChatUtils.sendMessage(message.getChannel(), "Currently running version: " + DiscordBotBase.getVersion());
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public boolean listCommand()
	{
		return false;
	}
}
