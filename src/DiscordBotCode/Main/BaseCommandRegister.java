package DiscordBotCode.Main;

import DiscordBotCode.CommandFiles.Commands.*;
import DiscordBotCode.DeveloperSystem.DevAccessCommand;
import DiscordBotCode.DeveloperSystem.DevNotifications;
import DiscordBotCode.DeveloperSystem.IssuesSystem.IssueCommand;
import DiscordBotCode.DeveloperSystem.MemoryUsageCommand;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.SettingsCommand;

public class BaseCommandRegister
{
	public static void initDefaultCommands(){
		CommandUtils.registerCommand(ListCommandsCommand.class, "listCommands");
		CommandUtils.registerCommand(HelpCommand.class, "helpCommand");
		CommandUtils.registerCommand(MemoryUsageCommand.class, "memoryUsageCommand");
		CommandUtils.registerCommand(PingCommand.class, "pingCommand");
		CommandUtils.registerCommand(VersionCommand.class, "versionCommand");
		CommandUtils.registerCommand(RolesDebugCommand.class, "rolesCommand");
		
		CommandUtils.registerCommand(DevAccessCommand.class, "devAccessCommand");
		CommandUtils.registerCommand(DevNotifications.class, "devNotifCommand");
		
		CommandUtils.registerCommand(IssueCommand.class, "issueCommand");
		
		CommandUtils.registerCommand(BotStatusCommand.class, "botStatusCommand");
		CommandUtils.registerCommand(SettingsCommand.class, "settingsCommand");
		CommandUtils.registerCommand(IgnoredRolesCommand.class, "ignoredRolesCommand");
		
		CommandUtils.registerCommand(MembersCommand.class, "membersCommand");
		
		CommandUtils.registerCommand(DevModeCommand.class, "devModeCommand");
	}
}
