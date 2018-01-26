package DiscordBotCode.CommandFiles;

import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.ServerSettings;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.*;

import java.util.Collections;
import java.util.EnumSet;

public abstract class DiscordCommand
{
	protected EnumSet<Permissions> permissionsEnumSet = EnumSet.noneOf(Permissions.class); //A EnumSet that is set on launch used for quicker permission checks
	
	public void initPermissions(){
		if(getRequiredPermissions() != null) {
			Collections.addAll(permissionsEnumSet, getRequiredPermissions());
		}
	}
	public EnumSet<Permissions> getPermissionsEnumSet()
	{
		return permissionsEnumSet;
	}
	
	public abstract String commandPrefix();
	public boolean caseSensitive(){ return false; }
	
	public String[] commandPrefixes(){
		return new String[]{commandPrefix()};
	}
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage ){return null;}
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage ){return null;}
	
	public boolean fuzzyDetection()
	{
		return false;
	}
	public boolean canCommandBePrivateChat()
	{
		return true;
	}
	public boolean listCommand()
	{
		return true;
	}
	//This makes it where a command can be marked where it requires the serverwide permission and not the channel overrided one
	public boolean isCommandPermissionServerwide()
	{
		return false;
	}
	
	
	public boolean hasPermissions( IMessage message, String[] args )
	{
		if(this instanceof DiscordSubCommand){
			if((getRequiredRole(message.getChannel()) == null) && (getRequiredPermissions() == null || getRequiredPermissions().length == 0)) {
				return ((DiscordSubCommand) this).baseCommand.hasPermissions(message, args);
			}
		}
		
		if(getRequiredRole(message.getChannel()) != null){
			if(message.getChannel().isPrivate()){
				return true;
			}

			if (PermissionUtils.hasRole(message.getAuthor(), message.getGuild(), getRequiredRole(message.getChannel()), true)) {
				return true;
			}
			
			return false;
		}
		
		return PermissionUtils.hasPermissions(message.getAuthor(), message.getGuild(), message.getChannel(), permissionsEnumSet, isCommandPermissionServerwide());
	}
	
	public String[] getUserWhitelist()
	{
		return null;
	}
	
	public IRole getRequiredRole( IChannel channel ){
		if(channel.isPrivate()){
			return null;
		}
		
		String t = ServerSettings.getValue(channel.getGuild(), CommandUtils.getKeyFromCommand(this));
		
		if(t != null && !t.isEmpty()) {
			if (Utils.isLong(t)) {
				IRole role = DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t));
				
				if (role != null) {
					return role;
				}
			}
		}
		
		return null;
	}
	
	public boolean canAssignRole(){return true;}
	
	
	protected Permissions[] getRequiredPermissions(){return null;}
	
	public abstract void commandExecuted( IMessage message, String[] args );
	public abstract boolean canExecute( IMessage message, String[] args );
	
	public String unableToExecuteCommand( IMessage message )
	{
		return "*Could not execute command: " + message.getContent() + "*";
	}
}
