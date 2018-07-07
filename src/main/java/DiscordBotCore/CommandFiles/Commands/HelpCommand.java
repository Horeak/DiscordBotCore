package DiscordBotCore.CommandFiles.Commands;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.ICustomSettings;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.CommandHandeling.CommandUtils;
import DiscordBotCore.Main.PermissionUtils;
import DiscordBotCore.Misc.Annotation.Command;
import DiscordBotCore.Setting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.EnumSet;
import java.util.StringJoiner;

@Command
public class HelpCommand extends DiscordCommand
{
	@Override
	public String commandPrefix()
	{
		return "Help";
	}
	
	@Override
	public String getCategory()
	{
		return "info commands";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "Help <command>";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(args.length <= 0){
			ChatUtils.sendMessage(message.getChannel(), "Help command needs to be given a command name!");
			return;
		}
		
		String commandName = String.join(" ", args);
		
		boolean hasAdmin = PermissionUtils.hasPermissions(message.getAuthor(), message.getGuild(), message.getChannel(), EnumSet.of(Permissions.ADMINISTRATOR));
		
		DiscordCommand command = CommandUtils.getDiscordCommand(commandName, message.getChannel(),  false, !hasAdmin);
		
		if(command == null){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no command with name \"" + commandName + "\", Please use \"" + getCommandSign(message.getChannel()) + "commands\" to view a list of all commands");
			return;
		}
		
		if(!message.getChannel().isPrivate() && message.getGuild() != null) {
			if (CommandUtils.isCommandDisabled(message.getGuild(), command)) {
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This command has been disabled!");
				return;
			}
		}
		
		if (!command.hasPermissions(message, new String[]{})) {
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " You do not have permission to use \"" + commandName + "\"");
			return;
		}
		
		if(message.getChannel().isPrivate() && !command.commandPrivateChat()){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This command is not allowed in private chats!");
			return;
		}
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		StringJoiner joinerPerms = new StringJoiner(", ");
		StringJoiner joinerSubCommands = new StringJoiner("");
		
		if(command.getRequiredPermissions() != null) {
			command.getRequiredPermissions().stream().map(Enum::name).forEach(( u ) -> joinerPerms.add(WordUtils.capitalize(u.toLowerCase().replace("_", " "))));
		}

		for (DiscordCommand subCommand : command.subCommands) {
			if(subCommand.isCommandVisible() && subCommand.hasPermissions(message, args)) {
				if(message.getChannel().isPrivate() || message.getGuild() != null && !CommandUtils.isCommandDisabled(message.getGuild(), subCommand)) {
					String t = WordUtils.capitalize(subCommand.commandPrefix());
					String usage = subCommand.getUsage(command, message);
					String u = "\"" + command.commandPrefix() + " " + (usage != null ? usage : subCommand.commandPrefix()) + "\"";

					t += "\n> Usage: " + (u != null && !u.isEmpty() ? u : subCommand.commandPrefix());
					joinerSubCommands.add("```markdown\n" + t + "```");
				}
			}
		}
		
		embedBuilder.withFooterText("Usage details: <field> = required, [field] = optional");
		
		embedBuilder.withColor(Color.orange);
		if(!message.getChannel().isPrivate()) embedBuilder.withColor(message.getAuthor().getColorForGuild(message.getGuild()));
		
//		embedBuilder.withFooterIcon(DiscordBotBase.discordClient.getOurUser().getAvatarURL());
		
		if(command.commandPrefixes().length > 1) embedBuilder.appendField("Prefixes", String.join(", ", command.commandPrefixes()), false);
		
		embedBuilder.withDescription("**`'" + WordUtils.capitalize(command.commandPrefix()) + "'` command**");
		
		if(command.isSubCommand()){
			DiscordCommand command1 = command.baseCommand;
			embedBuilder.withDescription("Sub-command of \"" + WordUtils.capitalize(command1.commandPrefix()) + "\"");
		}
		
		if(command.getUsage(this, message) != null) {
			String usage = command.getUsage(this, message);
			
			if(command.isSubCommand()){
				DiscordCommand command1 = command.baseCommand;
				usage = WordUtils.capitalize(command1.commandPrefix()) + " " + usage;
			}
			
			embedBuilder.appendField("Usage", "> \"" + usage + "\"", false);
		}
		
		if (command.getDescription(this, message) != null) {
			embedBuilder.appendField("Description", command.getDescription(this, message), false);
		}
		
		
		if(joinerPerms.length() > 0) {
			embedBuilder.appendField("Required Permissions", joinerPerms.toString(), false);
		}
		
		if(PermissionUtils.getRequiredRole(command, message.getChannel()) != null){
			embedBuilder.appendField("Required Role", PermissionUtils.getRequiredRole(command, message.getChannel()).mention(), false);
		}
		
		if(!message.getChannel().isPrivate() && PermissionUtils.hasPermissions(message.getAuthor(), message.getGuild(), EnumSet.of(Permissions.ADMINISTRATOR))) {
			if(command.getFallbackPermissions() != null && command.getFallbackPermissions().size() > 0){
				StringJoiner joinerFallbackPerms = new StringJoiner(",");
				command.getFallbackPermissions().stream().map(Enum::name).forEach((u) -> joinerFallbackPerms.add(WordUtils.capitalize(u.toLowerCase().replace("_", " "))));
				
				embedBuilder.appendField("Fallback Permissions", joinerFallbackPerms.toString(), false);
			}
			
			if (command instanceof ICustomSettings) {
				ICustomSettings settings = (ICustomSettings) command;
				StringBuilder builder = new StringBuilder();
				
				int longest = 0;

				for (Setting t : settings.getSettings()) {
					if(t.getKey().length() > longest){
						longest = t.getKey().length();
					}
				}
				
				for (Setting t : settings.getSettings()) {
					String value = settings.settingToString(message.getGuild(), t.getKey(), command);
					builder.append("`\n" + t.getKey());
					
					builder.append(StringUtils.repeat(' ', (longest + 3) - t.getKey().length()));
					
					builder.append("=`\t");
					
					builder.append(value != null && !value.isEmpty() ? value : "[" + t.getType().name().toUpperCase() + "]").append("\n");
				}
				
				String tg = builder.toString();
				
				if (tg.length() > 0) {
					embedBuilder.appendField("Settings", tg, false);
				}
			}
		}
		
		if (command.subCommands.size() > 0) {
			if(joinerSubCommands.toString() != null && joinerSubCommands.length() > 0) {
				String t = joinerSubCommands.toString();
				if(t.length() >= EmbedBuilder.FIELD_CONTENT_LIMIT) t = t.substring(0, EmbedBuilder.FIELD_CONTENT_LIMIT - 4) + "...";
				embedBuilder.appendField("Sub-commands", t, false);
			}
		}
		
		if(embedBuilder.getFieldCount() == 0){
			ChatUtils.sendMessage(message.getChannel(), "**No info available for `" + WordUtils.capitalize(command.commandPrefix()) + "`**");
			return;
		}
		
		ChatUtils.sendMessage(message.getChannel(), embedBuilder.build());
	}
	

	
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return getShortDescription(sourceCommand, callerMessage);
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Shows extended info about a specific command";
	}
}
