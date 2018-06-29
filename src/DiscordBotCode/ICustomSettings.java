package DiscordBotCode;

import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.ServerSettings;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.*;

public interface ICustomSettings {
	Setting[] getSettings();
	
	boolean canUpdateSetting( IMessage message, String[] args);
	
	default String settingToString( IGuild guild, String settingKey, DiscordCommand command ){
		for(Setting set : getSettings()) {
			if(set.getKey().equalsIgnoreCase(settingKey)) {
				Object t = getSettingValue(guild, set, command);
				
				if(set.getType() == Setting.SettingType.Role){
					if(t instanceof IRole){
						return ((IRole)t).mention();
					}
				}else if(set.getType() == Setting.SettingType.Channel){
					if(t instanceof IChannel){
						return ((IChannel)t).mention();
					}
				}else if(set.getType() == Setting.SettingType.User){
					if(t instanceof IUser){
						return ((IUser)t).mention();
					}
				}else if(set.getType() == Setting.SettingType.Text){
					return t != null ? t.toString() : null;
					
				}else if(set.getType() == Setting.SettingType.State){
					if(t instanceof Boolean){
						return Boolean.toString(((Boolean)t));
					}
				}
			}
		}
		
		return null;
	}
	
	default Object getSettingValue(IGuild guild, Setting setting, DiscordCommand command){
		String t = ServerSettings.getValue(guild, CommandUtils.getKeyFromCommand(command) + "$" + setting.getKey());
		
		
		if(setting.getType() == Setting.SettingType.Role){
			if (t != null && !t.isEmpty() && Utils.isLong(t)) {
				IRole role = guild.getClient().getRoleByID(Long.parseLong(t));
				
				if (role != null) {
					return role;
				}
			}
			
		}else if(setting.getType() == Setting.SettingType.Channel){
			if (t != null && !t.isEmpty() && Utils.isLong(t)) {
				IChannel channel = guild.getClient().getChannelByID(Long.parseLong(t));
				
				if (channel != null) {
					return channel;
				}
			}
		}else if(setting.getType() == Setting.SettingType.User){
			if (t != null && !t.isEmpty() && Utils.isLong(t)) {
				IUser user = guild.getClient().getUserByID(Long.parseLong(t));
				
				if (user != null) {
					return user;
				}
			}
		}else if(setting.getType() == Setting.SettingType.Text){
			if (t != null) {
				return t;
			}
			
		}else if(setting.getType() == Setting.SettingType.State){
			if(Utils.isBoolean(t)) {
				return Boolean.parseBoolean(t);
			}
		}
		
		return setting.getDefValue();
	}
	
	default void updateSetting( IMessage message, String[] args, Setting setting, DiscordCommand command){
		String text = String.join("_", args);
		
		if(setting.getType() == Setting.SettingType.Channel){
			IChannel channel = null;
	
			if(message.getChannelMentions().size() > 0){
				channel = message.getChannelMentions().get(0);
			}
	
			for(String t : args){
				if(t != null && !t.isEmpty() && channel == null) {
					if (Utils.isLong(t)) {
						if (message.getClient().getChannelByID(Long.parseLong(t)) != null) {
							channel = message.getClient().getChannelByID(Long.parseLong(t));
							text = text.replace(t, "");
						}
					}
				}
			}
	
			if(channel == null) {
				for (IChannel channel1 : message.getGuild().getChannels()) {
					String ts = channel1.getName().toLowerCase().replace(" ", "_");
					if (text.toLowerCase().contains(ts)) {
						channel = channel1;
						text = text.replace(channel1.getName().replace(" ", "_"), "");
					}
				}
			}
	
	
			if(channel != null){
				ServerSettings.setValue(message.getGuild(), CommandUtils.getKeyFromCommand(command) + "$" + setting.getKey(), channel.getStringID());
			}
			
		}else if(setting.getType() == Setting.SettingType.Role){
			IRole role = null;
	
			if(message.getRoleMentions().size() > 0){
				role = message.getRoleMentions().get(0);
			}
	
			for(String t : args){
				if(t != null && !t.isEmpty() && role == null) {
					if (Utils.isLong(t)) {
						if (message.getClient().getRoleByID(Long.parseLong(t)) != null) {
							role = message.getClient().getRoleByID(Long.parseLong(t));
							text = text.replace(t, "");
						}
					}
				}
			}
	
			if(role == null) {
				for (IRole role1 : message.getGuild().getRoles()) {
					String ts = role1.getName().toLowerCase().replace(" ", "_");
					if (text.toLowerCase().contains(ts)) {
						role = role1;
						text = text.replace(role1.getName().replace(" ", "_"), "");
					}
				}
			}
	
	
			if(role != null){
				ServerSettings.setValue(message.getGuild(), CommandUtils.getKeyFromCommand(command) + "$" + setting.getKey(), role.getStringID());
			}
			
		}else if(setting.getType() == Setting.SettingType.User){
			IUser user = null;
			
			if(message.getMentions().size() > 0){
				user = message.getMentions().get(0);
			}
			
			for(String t : args){
				if(t != null && !t.isEmpty() && user == null) {
					if (Utils.isLong(t)) {
						if (message.getClient().getUserByID(Long.parseLong(t)) != null) {
							user = message.getClient().getUserByID(Long.parseLong(t));
							text = text.replace(t, "");
						}
					}
				}
			}
			
			if(user == null) {
				for (IUser user1 : message.getGuild().getUsers()) {
					String ts = user1.getName().toLowerCase().replace(" ", "_");
					if (text.toLowerCase().contains(ts)) {
						user = user1;
						text = text.replace(user1.getName().replace(" ", "_"), "");
					}
				}
			}
			
			
			if(user != null){
				ServerSettings.setValue(message.getGuild(), CommandUtils.getKeyFromCommand(command) + "$" + setting.getKey(), user.getStringID());
			}
		}else if(setting.getType() == Setting.SettingType.Text){
			text = text.replace("_", " ");
			text = text.replace("null", "");
			
			while(text.startsWith(" ")) text = text.substring(1);
			while(text.endsWith(" ")) text = text.substring(0, text.length() - 1);
			
			ServerSettings.setValue(message.getGuild(), CommandUtils.getKeyFromCommand(command) + "$" + setting.getKey(), text);
			
		}else if(setting.getType() == Setting.SettingType.State){
			boolean tg = false;
			
			if(Utils.isBoolean(text)){
				tg = Boolean.parseBoolean(text);
			}
			
			for(String t : args){
				if(t != null && !t.isEmpty()) {
					if (Utils.isBoolean(t)) {
						tg = Boolean.parseBoolean(t);
					}
				}
			}
			
			ServerSettings.setValue(message.getGuild(), CommandUtils.getKeyFromCommand(command) + "$" + setting.getKey(), Boolean.toString(tg));
		}
	}
	
}
