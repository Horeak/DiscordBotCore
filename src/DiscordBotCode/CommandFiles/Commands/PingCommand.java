package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.Utils;
import DiscordBotCode.Misc.Annotation.DiscordCommand;
import sx.blah.discord.handle.obj.IMessage;

@DiscordCommand
public class PingCommand extends DiscordChatCommand
{
	@Override
	public String commandPrefix()
	{
		return "ping";
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
