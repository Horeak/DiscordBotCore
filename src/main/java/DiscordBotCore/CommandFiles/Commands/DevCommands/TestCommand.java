package DiscordBotCore.CommandFiles.Commands.DevCommands;

import DiscordBotCore.DeveloperSystem.DevCommandBase;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Misc.Annotation.DataObject;
import DiscordBotCore.Misc.Annotation.Debug;
import DiscordBotCore.Misc.Annotation.Command;
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
