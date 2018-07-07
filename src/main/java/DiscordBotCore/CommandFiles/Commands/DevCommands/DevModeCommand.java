package DiscordBotCore.CommandFiles.Commands.DevCommands;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.DeveloperSystem.DevCommandBase;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.DiscordBotBase;
import DiscordBotCore.Misc.Annotation.Command;
import sx.blah.discord.handle.obj.IMessage;

@Command
public class DevModeCommand extends DevCommandBase {
	@Override
	public String commandPrefix() {
		return "devMode";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Toggles dev mode";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		DiscordBotBase.devMode ^= true;
		
		ChatUtils.sendMessage(message.getChannel(), "Dev mode is now: " + DiscordBotBase.devMode);
	}
}
