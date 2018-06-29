package DiscordBotCode.CommandFiles.Commands.DevCommands;

import DiscordBotCode.DeveloperSystem.DevCommandBase;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Misc.Annotation.DataObject;
import DiscordBotCode.Misc.Annotation.Debug;
import DiscordBotCode.Misc.Annotation.Command;
import sx.blah.discord.handle.obj.IMessage;

@Debug
@Command
public class TestCommand extends DevCommandBase
{
	@Debug
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
