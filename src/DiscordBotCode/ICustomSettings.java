package DiscordBotCode;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

public interface ICustomSettings {
	
	String[] getSettings();
	
	boolean canUpdateSetting( IMessage message, String[] args);
	void updateSetting(IMessage message, String[] args, String setting);
	
	String getValueOfSetting( IGuild guild, String setting );
}
