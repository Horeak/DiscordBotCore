package DiscordBotCore.Main;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.CommandFiles.Commands.Settings.IgnoredRolesCommand;
import DiscordBotCore.DeveloperSystem.DevAccess;
import DiscordBotCore.Main.CommandHandeling.CommandUtils;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PermissionsUtils
{
	private static boolean overRide(IUser user){
		try {
			if(user != null) {
				if (DevAccess.isDev(user)) {
					return true;
				}
			}
		} catch (DiscordException e) {
			DiscordBotBase.handleException(e);
		}
		
		return false;
	}
	
	public static ArrayList<IRole> filterRoles( IGuild guild, List<IRole> roles){
		ArrayList<IRole> list = new ArrayList<>();
		list.addAll(roles);
		
		if(IgnoredRolesCommand.getIgnoredRoles().containsKey(guild.getLongID())) {
			list.removeIf(( role ) -> {
				for (String t : IgnoredRolesCommand.getGuildIgnoredRoles(guild)){
					if(role.getName().matches(t)){
						return true;
					}
				}
				
				return false;
			});
		}
		
		return list;
	}
	
	public static boolean hasRole( IUser user, IGuild guild, IRole role, boolean above, boolean filter )
	{
		if(overRide(user)) return true;
		
		if (user == null || guild == null || role == null) {
			return false;
		}
		
		if(filter && filterRoles(guild, user.getRolesForGuild(guild)).contains(role) || !filter && user.getRolesForGuild(guild).contains(role)){
			return true;
		}
		
		for (IRole role_ : filter ? filterRoles(guild, user.getRolesForGuild(guild)) : user.getRolesForGuild(guild)) {
			if (role_ != null) {
				if (role_.getStringID().equalsIgnoreCase(role.getStringID())) {
					return true;
				}
				
				if(above){
					if(role_.getPosition() >= role.getPosition()){
						if(role_.getPermissions().containsAll(role.getPermissions()) && role_.getPermissions().size() >= role.getPermissions().size()){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public static boolean hasRole( IUser user, IGuild guild, IRole role, boolean above )
	{
		return hasRole(user, guild, role, above, true);
	}
	
	public static boolean canConnect( IUser user, IVoiceChannel channel )
	{
		
		return hasPermissions(user, channel.getGuild(), channel, EnumSet.of(Permissions.VOICE_CONNECT));
	}
	
	public static boolean hasPermissions( IUser user, IGuild guild, IChannel channel, EnumSet<Permissions> perms )
	{
		return hasPermissions(user, guild, channel, perms, true);
	}
	
	public static boolean hasPermissions( IUser user, IGuild guild,  EnumSet<Permissions> perms )
	{
		
		return hasPermissions(user, guild, null, perms, false);
	}
	
	public static boolean hasPermissions( IUser user, IGuild guild, IChannel channel, EnumSet<Permissions> perms, boolean useChannelPerms )
	{
		if(overRide(user)) return true;
		
		if(useChannelPerms && guild != null) {
			if (channel != null && channel.isPrivate()) {
				return true; //Ignore permissions for private chats
			}
		}
		
		if (user == null) {
			return false; //Invalid parameters check
		}
		
		if(perms != null && perms.size() > 0) {
			if (useChannelPerms) {
				if (sx.blah.discord.util.PermissionUtils.hasPermissions(channel, user, perms)) {
					return true;
				}
			} else {
				if (sx.blah.discord.util.PermissionUtils.hasPermissions(guild, user, perms)) {
					return true;
				}
			}
		}else{
			return true;
		}
		
		return false;
	}
	
	public static boolean hasPermissions( IRole role, EnumSet<Permissions> perms )
	{
		if(perms != null && perms.size() > 0) {
			if (sx.blah.discord.util.PermissionUtils.hasPermissions(role.getPermissions(), perms)) {
				return true;
			}
		}else{
			return true;
		}
		
		return false;
	}
	
	
	public static IRole getRequiredRole( DiscordCommand commandBase, IChannel channel ){
		if(channel.isPrivate()){
			return null;
		}

		String key = CommandUtils.getKeyFromCommand(commandBase);
		String t = ServerSettings.getValue(channel.getGuild(), key);

		if(t != null && !t.isEmpty()) {
			if (Utils.isLong(t)) {
				IRole role = channel.getGuild().getRoleByID(Long.parseLong(t));

				if (role != null) {
					return role;
				}
			}
		}

		return null;
	}
}
