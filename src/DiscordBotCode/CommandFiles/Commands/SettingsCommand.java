package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.CommandBase;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.ICustomSettings;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.ServerSettings;
import DiscordBotCode.Misc.Annotation.DiscordCommand;
import DiscordBotCode.Misc.Annotation.SubCommand;
import DiscordBotCode.Setting;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

import java.util.ArrayList;

@DiscordCommand
public class SettingsCommand extends DiscordChatCommand{
	@Override
	public String getUsage( CommandBase sourceCommand, IMessage callMessage )
	{
		return "settings <command> <setting name> <value>";
	}
	
	@Override
	public String getDescription( CommandBase sourceCommand, IMessage callerMessage )
	{
		return "Allows changing the command specific settings, the settings for each command can be seen with the help command when the user has ADMINISTRATOR permission. To use the command state the command with prefix, then the name of the setting to be changed and finaly the value, the help command will also state the value type if there isnt already a value assigned, the possible value types are: Role, Channel, User, Text, State (State = true/false)";
	}
	
	@Override
	public String commandPrefix() {
		return "settings";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		String text = String.join(" ", args);
		
		CommandBase command = CommandUtils.getDiscordCommand(text, message.getChannel());
		ArrayList<String> replaces = new ArrayList<>();
		
		
		if(command != null) {
			if(command instanceof DiscordChatCommand){
				String t = ((DiscordChatCommand)command).getCommandSign(message.getChannel()) + command.commandPrefix();
				
				if(!replaces.contains(t)) {
					text = text.replaceFirst(t, "");
					replaces.add(t);
					replaces.add(command.commandPrefix());
				}
				
			}else if(command instanceof DiscordSubCommand){
				String t = (((DiscordSubCommand) command).baseCommand).getCommandSign(message.getChannel()) + command.commandPrefix();
				
				if(replaces.contains(t)) {
					text = text.replaceFirst(t, "");
					replaces.add(t);
					replaces.add(command.commandPrefix());
				}
				
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
					for (Setting t : settings.getSettings()) {
						if (arg[ 0 ].equalsIgnoreCase(t.getKey())){
							
							String[] arg1 = new String[arg.length];
							System.arraycopy(arg, 1, arg1, 0, arg.length - 1);
							
							if(settings.canUpdateSetting(message, arg1)){
								settings.updateSetting(message, arg1, t, command);
								ChatUtils.sendMessage(message.getChannel(), "The setting \"" + t.getKey() + "\" has been updated!");
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
	
	
	public boolean canAssignRole(){
		return false;
	}
	
	@Override
	public boolean commandPrivateChat() {
		return false;
	}
	
	@Override
	protected Permissions[] getRequiredPermissions() {
		return new Permissions[]{Permissions.ADMINISTRATOR};
	}
	

	@SubCommand(parent = SettingsCommand.class)
	public static class prefix extends DiscordSubCommand{
		public prefix( DiscordChatCommand baseCommand ) {
			super(baseCommand);
		}
		
		@Override
		public String getUsage( CommandBase sourceCommand, IMessage callMessage )
		{
			return "prefix <prefix>";
		}
		
		@Override
		public String getDescription( CommandBase sourceCommand, IMessage callerMessage )
		{
			return "Allows changing the prefix used for all commands in this server, the prefix can be max 2 characters long";
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
	}
}
