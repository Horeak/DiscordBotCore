package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.Main.ChatUtils;
import sx.blah.discord.handle.obj.IMessage;

public class MemoryUsageCommand extends DevAccessCommand
{
	@Override
	public String commandPrefix()
	{
		return "memory_usage";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		Runtime runtime = Runtime.getRuntime();
		int mb = 1024 * 1024;
		
		String builder = "```perl\n"
		                 + "######################################" + "\n"
		                 + "Used Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / mb + "mb\n"
		                 + "Free Memory:" + runtime.freeMemory() / mb + "mb\n"
		                 + "Total Memory:" + runtime.totalMemory() / mb + "mb\n"
		                 + "Max Memory:" + runtime.maxMemory() / mb + "mb\n"
		                 + "######################################" + "\n"
		                 + "```";
		
		ChatUtils.sendMessage(message.getChannel(), builder);
	}
}
