package DiscordBotCode.Main;

import DiscordBotCode.DeveloperSystem.DevAccess;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;

public class PermissionUtils
{
	private static Field missingPermissionAccessField = null;
	
	private static boolean overRide(IUser user){
		if(DiscordBotBase.debug){
//			return true;
		}
		
		try {
			if(user != null) {
				if (DiscordBotBase.discordClient != null && DevAccess.isDev(user)) {
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
		
		if(Utils.data().get().getIgnoredRoles().containsKey(guild.getLongID())) {
			list.removeIf(( role ) -> {
				for (String t : Utils.data().get().getGuildIgnoredRoles(guild)){
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
		
		return hasPermissions(user, guild, channel, perms, false);
	}
	
	public static boolean hasPermissions( IUser user, IGuild guild, IChannel channel, EnumSet<Permissions> perms, boolean serverWide )
	{
		if(overRide(user)) return true;
		
		if (channel != null && channel.isPrivate()) {
			return true; //Ignore permissions for private chats
		}
		
		if (user == null || channel == null) {
			return false; //Invalid parameters check
		}
		
		if(!serverWide) {
			if(sx.blah.discord.util.PermissionUtils.hasPermissions(channel, user, perms)){
				return true;
			}
		}else{
			if(sx.blah.discord.util.PermissionUtils.hasPermissions(guild, user, perms)){
				return true;
			}
		}
		
		return false;
	}
	
	public static String getPermsAsString( EnumSet<Permissions> permissions )
	{
		StringJoiner joiner = new StringJoiner(", ");
		permissions.stream().map(Enum::name).forEach(joiner::add);
		return joiner.toString();
	}
	
	public static EnumSet<Permissions> getMissingPermissions( IUser user, IChannel channel, EnumSet<Permissions> perms )
	{
		if (missingPermissionAccessField == null) {
			try {
				missingPermissionAccessField = MissingPermissionsException.class.getDeclaredField("missing");
				missingPermissionAccessField.setAccessible(true);
				
			} catch (NoSuchFieldException e) {
				DiscordBotBase.handleException(e);
			}
		}
		
		try {
			sx.blah.discord.util.PermissionUtils.hasPermissions(channel, user, perms);
		} catch (MissingPermissionsException e) {
			try {
				//noinspection unchecked
				return (EnumSet<Permissions>) missingPermissionAccessField.get(e); //Reflection access
			} catch (IllegalAccessException e1) {
				DiscordBotBase.handleException(e1);
			}
		}
		
		return null;
	}
	
}
