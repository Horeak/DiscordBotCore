package DiscordBotCode.CommandFiles;

import DiscordBotCode.Main.PermissionUtils;
import sx.blah.discord.handle.obj.*;

import java.util.Collections;
import java.util.EnumSet;

public abstract class CommandBase
{
	//TODO Add a fallback permission incase a role is not assigned
	
	public String[] commandPrefixes(){
		return new String[]{commandPrefix()};
	}
	public String getUsage( CommandBase sourceCommand, IMessage callMessage ){return null;}
	public String getDescription( CommandBase sourceCommand, IMessage callerMessage ){return null;}

	public abstract void commandExecuted( IMessage message, String[] args );
	public boolean canExecute( IMessage message, String[] args ){
		return true;
	}

	protected EnumSet<Permissions> permissionsEnumSet = EnumSet.noneOf(Permissions.class); //A EnumSet that is set on launch used for quicker permission checks
	
	public void initPermissions(){
		if(getRequiredPermissions() != null) Collections.addAll(permissionsEnumSet, getRequiredPermissions());
	}

	public EnumSet<Permissions> getPermissionsEnumSet() { return permissionsEnumSet; }
	protected Permissions[] getRequiredPermissions(){ return null; }
	public EnumSet<Permissions> getFallbackPermissions() { return null;}


	public abstract String commandPrefix();
	public boolean caseSensitive(){ return false; }
	public boolean canAssignRole(){ return true; }


	//Is command avaliable in private chats with the bot
	public boolean commandPrivateChat()
	{
		return true;
	}

	//Is command visible in the commands list and similar
	public boolean isCommandVisible()
	{
		return true;
	}

	//This makes it where a command can be marked where it requires the serverwide permission and not the channel specific one
	public boolean useChannelPermission()
	{
		return true;
	}
	
	//Check if user has permission to use command
	public boolean hasPermissions( IMessage message, String[] args )
	{
		if(this instanceof DiscordSubCommand){
			if((PermissionUtils.getRequiredRole(this, message.getChannel()) == null) && (getRequiredPermissions() == null || getRequiredPermissions().length == 0)) {
				return ((DiscordSubCommand) this).baseCommand.hasPermissions(message, args);
			}
		}
		
		if(PermissionUtils.getRequiredRole(this, message.getChannel()) != null) {
			return message.getChannel().isPrivate() || PermissionUtils.hasRole(message.getAuthor(), message.getGuild(), PermissionUtils.getRequiredRole(this, message.getChannel()), true);
		}
		
		if(getFallbackPermissions() != null) {
			if (permissionsEnumSet == null || permissionsEnumSet.size() <= 0) {
				return PermissionUtils.hasPermissions(message.getAuthor(), message.getGuild(), message.getChannel(), getFallbackPermissions(), useChannelPermission());
			}
		}
		
		return PermissionUtils.hasPermissions(message.getAuthor(), message.getGuild(), message.getChannel(), permissionsEnumSet, useChannelPermission());
	}
}
