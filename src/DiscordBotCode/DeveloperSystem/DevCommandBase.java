package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public abstract class DevCommandBase extends DiscordChatCommand
{
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return DevAccess.isDev(message.getAuthor().getLongID());
	}
	
	@Override
	public String getCommandSign( IChannel channel )
	{
		return "-" + super.getCommandSign(channel);
	}
	
	@Override
	public boolean hasPermissions( IMessage message, String[] args )
	{
		return DevAccess.isDev(message.getAuthor().getLongID());
	}
	
	@Override
	public boolean isCommandVisible()
	{
		return false;
	}
}
