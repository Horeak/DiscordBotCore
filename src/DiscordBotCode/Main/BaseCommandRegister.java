package DiscordBotCode.Main;

import DiscordBotCode.CommandFiles.Commands.*;
import DiscordBotCode.DeveloperSystem.DevAccessCommand;
import DiscordBotCode.DeveloperSystem.DevNotifications;
import DiscordBotCode.DeveloperSystem.IssuesSystem.IssueCommand;
import DiscordBotCode.DeveloperSystem.MemoryUsageCommand;
import DiscordBotCode.Main.CustomEvents.InitEvents.InitCommandRegisterEvent;
import DiscordBotCode.SettingsCommand;
import sx.blah.discord.api.events.IListener;

public class BaseCommandRegister implements IListener<InitCommandRegisterEvent>
{
	@Override
	public void handle( InitCommandRegisterEvent event )
	{
		DiscordBotBase.registerCommand(ListCommandsCommand.class, "listCommands");
		DiscordBotBase.registerCommand(HelpCommand.class, "helpCommand");
		DiscordBotBase.registerCommand(MemoryUsageCommand.class, "memoryUsageCommand");
		DiscordBotBase.registerCommand(PingCommand.class, "pingCommand");
		DiscordBotBase.registerCommand(VersionCommand.class, "versionCommand");
		DiscordBotBase.registerCommand(RolesDebugCommand.class, "rolesCommand");
		
		DiscordBotBase.registerCommand(DevAccessCommand.class, "devAccessCommand");
		DiscordBotBase.registerCommand(DevNotifications.class, "devNotifCommand");
		
		DiscordBotBase.registerCommand(IssueCommand.class, "issueCommand");
		
		DiscordBotBase.registerCommand(BotStatusCommand.class, "botStatusCommand");
		DiscordBotBase.registerCommand(SettingsCommand.class, "settingsCommand");
		
		DiscordBotBase.registerCommand(MembersCommand.class, "membersCommand");
		
		DiscordBotBase.registerCommand(DevModeCommand.class, "devModeCommand");
		
		
	}
}
