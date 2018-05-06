package DiscordBotCode.CommandFiles.Commands.DevCommands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Misc.Annotation.DataObject;
import DiscordBotCode.Misc.Annotation.DiscordCommand;
import sx.blah.discord.handle.obj.IMessage;

@DiscordCommand(debug = true)
public class TestCommand extends DiscordChatCommand
{
	@DataObject(file_path = "times.json", name = "times")
	public static int times = 0;
	
	@Override
	public String commandPrefix()
	{
		return "times";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		times++;
		ChatUtils.sendMessage(message.getChannel(), "Cur times: " + times);
	}
}
