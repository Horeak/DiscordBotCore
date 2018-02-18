package DiscordBotCode;

import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.ServerSettings;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.*;

public interface ICustomSettings {
	Setting[] getSettings();
	
	boolean canUpdateSetting( IMessage message, String[] args);
	default String getValueOfSetting( IGuild guild, String settingKey, DiscordCommand command ){
		for(Setting set : getSettings()) {
			if(set.getKey().equalsIgnoreCase(settingKey)) {
				
				if(set.getType() == Setting.SettingType.Role){
					String t = ServerSettings.getValue(guild, CommandUtils.getKeyFromCommand(command) + "$" + set.getKey());
					
					if (t != null && !t.isEmpty() && Utils.isLong(t)) {
						IRole role = DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t));
						
						if (role != null) {
							return role.mention();
						}
					}
					
				}else if(set.getType() == Setting.SettingType.Channel){
					String t = ServerSettings.getValue(guild, CommandUtils.getKeyFromCommand(command) + "$" + set.getKey());
					
					if (t != null && !t.isEmpty() && Utils.isLong(t)) {
						IChannel channel = DiscordBotBase.discordClient.getChannelByID(Long.parseLong(t));
						
						if (channel != null) {
							return channel.mention();
						}
					}
				}else if(set.getType() == Setting.SettingType.User){
					String t = ServerSettings.getValue(guild, CommandUtils.getKeyFromCommand(command) + "$" + set.getKey());
					
					if (t != null && !t.isEmpty() && Utils.isLong(t)) {
						IUser user = DiscordBotBase.discordClient.getUserByID(Long.parseLong(t));
						
						if (user != null) {
							return user.mention();
						}
					}
				}else if(set.getType() == Setting.SettingType.Text){
					String t = ServerSettings.getValue(guild, CommandUtils.getKeyFromCommand(command) + "$" + set.getKey());
	
					if (t != null) {
						return t;
					}
				}

			}
		}
		
		return null;
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
						if (DiscordBotBase.discordClient.getChannelByID(Long.parseLong(t)) != null) {
							channel = DiscordBotBase.discordClient.getChannelByID(Long.parseLong(t));
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
						if (DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t)) != null) {
							role = DiscordBotBase.discordClient.getRoleByID(Long.parseLong(t));
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
						if (DiscordBotBase.discordClient.getUserByID(Long.parseLong(t)) != null) {
							user = DiscordBotBase.discordClient.getUserByID(Long.parseLong(t));
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
			ServerSettings.setValue(message.getGuild(), CommandUtils.getKeyFromCommand(command) + "$" + setting.getKey(), String.join(" ", args));
		}
	}
	
}
