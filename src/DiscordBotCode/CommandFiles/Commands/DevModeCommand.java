package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.DeveloperSystem.DevCommandBase;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.handle.obj.IMessage;

public class DevModeCommand extends DevCommandBase {
	@Override
	public String commandPrefix() {
		return "devMode";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		DiscordBotBase.devMode ^= true;
		
		ChatUtils.sendMessage(message.getChannel(), "Dev mode is now: " + DiscordBotBase.devMode);
	}
}
