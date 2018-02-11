package DiscordBotCode;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.ServerSettings;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

import java.util.ArrayList;

public class SettingsCommand extends DiscordChatCommand{
	public SettingsCommand() {
		subCommands.add(new prefix(this));
		subCommands.add(new role(this));
		subCommands.add(new commandSettings(this));
	}
	
	@Override
	public String commandPrefix() {
		return "settings";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		//TODO Print settings here
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args ) {
		return true;
	}
	
	public boolean canAssignRole(){
		return false;
	}
	
	@Override
	public boolean canCommandBePrivateChat() {
		return false;
	}
	
	@Override
	protected Permissions[] getRequiredPermissions() {
		return new Permissions[]{Permissions.ADMINISTRATOR};
	}
	
	class commandSettings extends DiscordSubCommand{
		public commandSettings( DiscordChatCommand baseCommand ) {
			super(baseCommand);
		}
		
		@Override
		public String commandPrefix() {
			return "commandSettings";
		}
		
		@Override
		public void commandExecuted( IMessage message, String[] args ) {
			String text = String.join(" ", args);
			
			DiscordCommand command = CommandUtils.getDiscordCommand(text, message.getChannel());
			
			if(command != null) {
				if(command instanceof DiscordChatCommand){
					text = text.replace(((DiscordChatCommand)command).getCommandSign(message.getChannel()) + command.commandPrefix(), "");
					
				}else if(command instanceof DiscordSubCommand){
					text = text.replace((((DiscordSubCommand) command).baseCommand).getCommandSign(message.getChannel()) + command.commandPrefix(), "");
					
				}
				
				text = text.replace(command.commandPrefix(), "");
				
				for(String tg : command.commandPrefixes()){
					text = text.replace(tg, "");
				}
			}
			
			while(text.startsWith(" ")){
				text = text.substring(1);
			}
			
			if(command != null && command instanceof ICustomSettings){
				ICustomSettings settings = (ICustomSettings)command;
				
				if(settings != null) {
					String[] arg = text.split(" ");
					
					if(arg.length > 0) {
						for (String t : settings.getSettings()) {
							if (arg[ 0 ].equalsIgnoreCase(t)){
								
								String[] arg1 = new String[arg.length];
								System.arraycopy(arg, 1, arg1, 0, arg.length - 1);
								
								if(settings.canUpdateSetting(message, arg1)){
									settings.updateSetting(message, arg1, t);
									ChatUtils.sendMessage(message.getChannel(), "The setting \"" + t + "\" has been updated!");
									return;
								}else{
									ChatUtils.sendMessage(message.getChannel(), "Unable to update \"" + t + "\"");
									return;
								}
							}
						}
					}
				}
				
			}else{
				ChatUtils.sendMessage(message.getChannel(), "Invalid input command!");
			}
		}
		
		@Override
		public boolean canExecute( IMessage message, String[] args ) {
			return true;
		}
	}
	
	class prefix extends DiscordSubCommand{
		public prefix( DiscordChatCommand baseCommand ) {
			super(baseCommand);
		}
		
		@Override
		public String commandPrefix() {
			return "prefix";
		}
		
		@Override
		public void commandExecuted( IMessage message, String[] args ) {
			String pre = String.join("_", args);
			
			if(pre == null || pre.isEmpty()){
				if(ServerSettings.valueExsists(message.getGuild(), "prefix")){
					ServerSettings.removeValue(message.getGuild(), "prefix");
					ChatUtils.sendMessage(message.getChannel(), "Command prefix has now been reset!");
					return;
				}else{
					ChatUtils.sendMessage(message.getChannel(), "Please enter a prefix!");
					return;
				}
			}
			
			if(pre.length() > 2){
				ChatUtils.sendMessage(message.getChannel(), "Command prefix can't be longer than 2 characters!");
				return;
			}
			
			ServerSettings.setValue(message.getGuild(), "prefix", pre);
			ChatUtils.sendMessage(message.getChannel(), "Command prefix has successfully been set to `" + pre + "`");
			
		}
		
		@Override
		public boolean canExecute( IMessage message, String[] args ) {
			return true;
		}
	}
	
	class role extends DiscordSubCommand{
		public role( DiscordChatCommand baseCommand ) {
			super(baseCommand);
		}
		
		@Override
		public String commandPrefix() {
			return "assignRole";
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
						if (DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t)) != null) {
							role = DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t));
							text = text.replace(t, "");
						}
					}
				}
				
				DiscordCommand command = CommandUtils.getDiscordCommand(t, message.getChannel());
				
				boolean has = false;
				
				for(DiscordCommand dc : commands){
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
			
			for(DiscordCommand dc : commands){
				String key = CommandUtils.getKeyFromCommand(dc);
				
				if(ServerSettings.valueExsists(message.getGuild(), key) && role == null){
					ServerSettings.removeValue(message.getGuild(), key);
					ChatUtils.sendMessage(message.getChannel(), "The assigned role for the `" + dc.commandPrefix() + "` command has been removed!");
					deleted = true;
					continue;
				}
				
				if(role != null){
					ServerSettings.setValue(message.getGuild(), key, role.getStringID());
					found = true;
				}
			}
			
			if(found){
				ChatUtils.sendMessage(message.getChannel(), "The role has been assigned to the specific commands!");
			}else if(!found && deleted){
				ChatUtils.sendMessage(message.getChannel(), "The specific commands have had their roles unassigned!");
			}else if(role == null){
				ChatUtils.sendMessage(message.getChannel(), "Please specify a role to assign!");
			}else if(commands.size() <= 0){
				ChatUtils.sendMessage(message.getChannel(), "Please specify a command to assign!");
			}
		}
		
		@Override
		public boolean canExecute( IMessage message, String[] args ) {
			return true;
		}
	}
}
