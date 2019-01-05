package DiscordBotCore.CommandFiles;

import DiscordBotCore.Main.DiscordBotBase;
import DiscordBotCore.Main.PermissionsUtils;
import DiscordBotCore.Main.ServerSettings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class DiscordCommand
{
	public CopyOnWriteArrayList<DiscordCommand> subCommands = new CopyOnWriteArrayList<>();
	public DiscordCommand baseCommand;

	public boolean isSubCommand(){return baseCommand != null;}
	
	public String[] commandPrefixes(){
		return new String[]{commandPrefix()};
	}
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage ){return null;}
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage ){return null;}
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage ){return null;}
	
	public abstract void commandExecuted( IMessage message, String[] args );
	public boolean canExecute( IMessage message, String[] args ){
		return true;
	}
	
	public EnumSet<Permissions> getRequiredPermissions() { return null; }
	public EnumSet<Permissions> getFallbackPermissions() { return null;}

	public abstract String commandPrefix();
	public boolean caseSensitive(){ return false; }
	public boolean canAssignRole(){ return true; }

	public boolean canBeDisabled(){ return true; }
	
	public boolean longCommandTime(){return false;}


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
		if(baseCommand != null){
			if((PermissionsUtils.getRequiredRole(this, message.getChannel()) == null) && (getRequiredPermissions() == null || getRequiredPermissions().size() == 0)) {
				return baseCommand.hasPermissions(message, args);
			}
		}
		
		if(PermissionsUtils.getRequiredRole(this, message.getChannel()) != null) {
			return message.getChannel().isPrivate() || PermissionsUtils.hasRole(message.getAuthor(), message.getGuild(), PermissionsUtils.getRequiredRole(this, message.getChannel()), true);
		}
		
		if(getFallbackPermissions() != null) {
			if (getRequiredPermissions() == null || getRequiredPermissions().size() <= 0) {
				return PermissionsUtils.hasPermissions(message.getAuthor(), message.getGuild(), message.getChannel(), getFallbackPermissions(), useChannelPermission());
			}
		}
		
		return PermissionsUtils.hasPermissions(message.getAuthor(), message.getGuild(), message.getChannel(), getRequiredPermissions(), useChannelPermission());
	}
	
	public boolean hasPermissions( IMessage message, IRole role )
	{
		if(baseCommand != null){
			if((PermissionsUtils.getRequiredRole(this, message.getChannel()) == null) && (getRequiredPermissions() == null || getRequiredPermissions().size() == 0)) {
				return baseCommand.hasPermissions(message, role);
			}
		}
		
		if(PermissionsUtils.getRequiredRole(this, message.getChannel()) != null) {
			if(message.getChannel().isPrivate()){
				return true;
			}else{
				IRole role1 = PermissionsUtils.getRequiredRole(this, message.getChannel());
				
				if(role.getLongID() == role1.getLongID()){
					return true;
				}
				
				if(role.getPosition() >= role1.getPosition()){
					return true;
				}
			}
		}
		
		if(getFallbackPermissions() != null) {
			if (getRequiredPermissions() == null || getRequiredPermissions().size() <= 0) {
				return PermissionsUtils.hasPermissions(role, getFallbackPermissions());
			}
		}
		
		return PermissionsUtils.hasPermissions(role, getRequiredPermissions());
	}

	public String getCommandSign( IChannel channel )
	{
		if(channel == null || channel != null && channel.isPrivate()){
			return DiscordBotBase.getCommandSign();
			
		}else if(ServerSettings.valueExsists(channel.getGuild(), "prefix")){
			return ServerSettings.getValue(channel.getGuild(), "prefix");
		}
		
		return DiscordBotBase.getCommandSign();
	}

	public String getCategory(){return null;}
}
