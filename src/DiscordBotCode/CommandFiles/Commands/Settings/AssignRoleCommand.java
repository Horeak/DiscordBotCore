package DiscordBotCode.CommandFiles.Commands.Settings;

import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.ServerSettings;
import DiscordBotCode.Main.Utils;
import DiscordBotCode.Misc.Annotation.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.StringJoiner;

@Command
public class AssignRoleCommand extends DiscordCommand
{
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "assignRole <command(s)> <role>";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Allows assigning a role to any command on the bot, the role assigned will be the minimum role required to use the command. When using the command the input can have multiple commands at once which all will be assigned the same role, the commands that are being used for this command will require prefix and command";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Specify required role for commands";
	}
	
	@Override
	public String commandPrefix() {
		return "assignRole";
	}
	
	@Override
	public String getCategory()
	{
		return "settings";
	}
	
	@Override
	public EnumSet<Permissions> getRequiredPermissions()
	{
		return EnumSet.of(Permissions.ADMINISTRATOR);
	}
	
	@Override
	public boolean commandPrivateChat()
	{
		return false;
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		String text = String.join("_", args);
		
		IRole role = null;
		ArrayList<DiscordCommand> commands = new ArrayList<>();
		
		if(message.getRoleMentions().size() > 0){
			role = message.getRoleMentions().get(0);
		}
		
		
		DiscordCommand command1 = CommandUtils.getDiscordCommand(text.replace("_", " "), message.getChannel());
		
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
					if (message.getClient().getRoleByID(Long.parseLong(t)) != null) {
						role = message.getClient().getRoleByID(Long.parseLong(t));
						text = text.replace(t, "");
					}
				}
			}
			
			DiscordCommand command = CommandUtils.getDiscordCommand(t, message.getChannel());
			
			boolean has = false;
			
			for(DiscordCommand dc : commands){
				if(dc.isSubCommand()){
					if(dc.baseCommand == command){
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
		
		for(DiscordCommand dc : commands){
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
