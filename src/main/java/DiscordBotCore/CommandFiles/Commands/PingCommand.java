package DiscordBotCore.CommandFiles.Commands;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.Utils;
import DiscordBotCore.Misc.Annotation.Command;
import sx.blah.discord.handle.obj.IMessage;

@Command
public class PingCommand extends DiscordCommand
{
	@Override
	public String commandPrefix()
	{
		return "ping";
	}
	
	@Override
	public String getCategory()
	{
		return "info commands";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Shows current ping";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "ping";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Shows the current ping of the bot";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ChatUtils.sendMessage(message.getChannel(), "Response in *" + Utils.getPing(message) + " ms*");
	}
	
	@Override
	public boolean isCommandVisible()
	{
		return false;
	}
}
