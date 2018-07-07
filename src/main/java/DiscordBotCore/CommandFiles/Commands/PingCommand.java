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
