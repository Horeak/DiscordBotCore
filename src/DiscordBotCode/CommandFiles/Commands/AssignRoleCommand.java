package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.CommandBase;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.ServerSettings;
import DiscordBotCode.Main.Utils;
import DiscordBotCode.Misc.Annotation.DiscordCommand;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;

import java.util.ArrayList;
import java.util.StringJoiner;

@DiscordCommand
public class AssignRoleCommand extends DiscordChatCommand
{
	@Override
	public String getUsage( CommandBase sourceCommand, IMessage callMessage )
	{
		return "assignRole <command(s)> <role>";
	}
	
	@Override
	public String getDescription( CommandBase sourceCommand, IMessage callerMessage )
	{
		return "Allows assigning a role to any command on the bot, the role assigned will be the minimum role required to use the command. When using the command the input can have multiple commands at once which all will be assigned the same role, the commands that are being used for this command will require prefix and command";
	}
	
	@Override
	public String commandPrefix() {
		return "assignRole";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		String text = String.join("_", args);
		
		IRole role = null;
		ArrayList<CommandBase> commands = new ArrayList<>();
		
		if(message.getRoleMentions().size() > 0){
			role = message.getRoleMentions().get(0);
		}
		
		
		CommandBase command1 = CommandUtils.getDiscordCommand(text.replace("_", " "), message.getChannel());
		
		if(command1 != null) {
			commands.add(command1);
			text = text.replace(command1.commandPrefix(), "");
			
			for(String tg : command1.commandPrefixes()){
				text = text.replace(tg, "");
			}
		}
		
		for(String t : args){
			if(role == null) {
				if (Utils.isLong(t)) {
					if (DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t)) != null) {
						role = DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t));
						text = text.replace(t, "");
					}
				}
			}
			
			CommandBase command = CommandUtils.getDiscordCommand(t, message.getChannel());
			
			boolean has = false;
			
			for(CommandBase dc : commands){
				if(dc instanceof DiscordSubCommand){
					if(((DiscordSubCommand) dc).baseCommand == command){
						has = true;
					}
				}
			}
			
			if(command != null && !commands.contains(command) && !has){
				commands.add(command);
				text = text.replace(command.commandPrefix(), "");
				
				for(String tg : command.commandPrefixes()){
					text = text.replace(tg, "");
				}
			}
		}
		
		commands.removeIf((c) -> !c.canAssignRole());
		
		if(role == null) {
			for (IRole role1 : message.getGuild().getRoles()) {
				String ts = role1.getName().toLowerCase().replace(" ", "_");
				if (text.toLowerCase().contains(ts)) {
					role = role1;
					text = text.replace(role1.getName().replace(" ", "_"), "");
				}
			}
		}
		
		boolean found = false;
		boolean deleted = false;
		
		StringJoiner builder = new StringJoiner(", ");
		
		for(CommandBase dc : commands){
			String key = CommandUtils.getKeyFromCommand(dc);
			
			if(ServerSettings.valueExsists(message.getGuild(), key) && role == null){
				ServerSettings.removeValue(message.getGuild(), key);
				if(commands.size() > 1) ChatUtils.sendMessage(message.getChannel(), "The assigned role for the `" + dc.commandPrefix() + "` command has been removed!");
				deleted = true;
				continue;
			}
			
			if(role != null){
				ServerSettings.setValue(message.getGuild(), key, role.getStringID());
				found = true;
				builder.add(dc.commandPrefix());
			}
		}
		
		if(found){
			ChatUtils.sendMessage(message.getChannel(), "\"" + role.getName() + "\" has been assigned to " + "\"" + builder.toString() + "\"");
		}else if(!found && deleted){
			ChatUtils.sendMessage(message.getChannel(), "\"" + builder.toString() + "\"" + " have had their roles unassigned!");
		}else if(role == null){
			ChatUtils.sendMessage(message.getChannel(), "Please specify a role to assign!");
		}else if(commands.size() <= 0){
			ChatUtils.sendMessage(message.getChannel(), "Please specify a command to assign!");
		}
	}
}
