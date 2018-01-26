package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import org.apache.commons.lang.WordUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.StringJoiner;

public class HelpCommand extends DiscordChatCommand
{
	@Override
	public String commandPrefix()
	{
		return "Help";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "Help <command>";
	}
	
	@Override
	protected Permissions[] getRequiredPermissions()
	{
		return new Permissions[]{  };
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(args.length <= 0){
			ChatUtils.sendMessage(message.getChannel(), "Help command needs a input!");
			return;
		}
		
		String commandName = String.join(" ", args);
		
		DiscordCommand command = CommandUtils.getCommandName(commandName, message.getChannel());
		
		if(command == null){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no command with name \"" + commandName + "\"");
			return;
		}
		
		if (!command.hasPermissions(message, new String[]{})) {
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " You do not have permission to use \"" + commandName + "\"");
			return;
		}
		
		if(message.getChannel().isPrivate() && !command.canCommandBePrivateChat()){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This command is not allowed in private chats!");
			return;
		}
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		StringJoiner joinerPerms = new StringJoiner(",");
		StringJoiner joinerSubCommands = new StringJoiner("");
		
		command.getPermissionsEnumSet().stream().map(Enum::name).forEach((u) -> joinerPerms.add(WordUtils.capitalize(u.replace("_", " "))));
		
		if (command instanceof DiscordChatCommand) {
			for (DiscordSubCommand subCommand : ((DiscordChatCommand) command).subCommands) {
				if(subCommand.listCommand() && subCommand.hasPermissions(message, args)) {
					String t = WordUtils.capitalize(subCommand.commandPrefix());
					String usage = subCommand.getUsage(command, message);
					String u = "\"" + command.commandPrefix() + " " + (usage != null ? usage : subCommand.commandPrefix()) + "\"";
					
					t += "\n> Usage: " + (u != null && !u.isEmpty() ? u : subCommand.commandPrefix()) ;
					joinerSubCommands.add("```markdown\n" + t + "```");
				}
			}
		}
		
		embedBuilder.withFooterText("Usage details: <field> = required, [field] = optional");
		
		embedBuilder.withColor(Color.orange);
		if(!message.getChannel().isPrivate()) embedBuilder.withColor(message.getAuthor().getColorForGuild(message.getGuild()));
		
//		embedBuilder.withFooterIcon(DiscordBotBase.discordClient.getOurUser().getAvatarURL());
		
		if(command.commandPrefixes().length > 1) embedBuilder.appendField("Prefixes", String.join(", ", command.commandPrefixes()), false);
		
		embedBuilder.withDescription("'" + WordUtils.capitalize(command.commandPrefix()) + "' command");
		
		if(command instanceof DiscordSubCommand){
			DiscordChatCommand command1 = ((DiscordSubCommand)command).baseCommand;
			embedBuilder.withDescription("Sub-command of \"" + command1.commandPrefix() + "\"");
		}
		
		if(command.getUsage(this, message) != null) {
			String usage = command.getUsage(this, message);
			
			if(command instanceof DiscordSubCommand){
				DiscordChatCommand command1 = ((DiscordSubCommand)command).baseCommand;
				usage = command1.commandPrefix() + " " + usage;
			}
			
			embedBuilder.appendField("Usage", "> \"" + usage + "\"", false);
		}
		
		if (command.getDescription(this, message) != null) {
			embedBuilder.appendField("Description", command.getDescription(this, message), false);
		}
		
		
		if(joinerPerms.length() > 0) {
			embedBuilder.appendField("Required Permissions", joinerPerms.toString(), false);
		}
		
		if(command.getRequiredRole(message.getChannel()) != null){
			embedBuilder.appendField("Required Role", command.getRequiredRole(message.getChannel()).getName(), false);
		}
		
		if (command instanceof DiscordChatCommand && ((DiscordChatCommand) command).subCommands.size() > 0) {
			if(joinerSubCommands.toString() != null && joinerSubCommands.length() > 0) {
				String t = joinerSubCommands.toString();
				if(t.length() >= EmbedBuilder.FIELD_CONTENT_LIMIT) t = t.substring(0, EmbedBuilder.FIELD_CONTENT_LIMIT - 4) + "...";
				embedBuilder.appendField("Sub-commands", t, false);
			}
		}
		
		ChatUtils.sendMessage(message.getChannel(), embedBuilder.build());
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		
		return null;
	}
}
