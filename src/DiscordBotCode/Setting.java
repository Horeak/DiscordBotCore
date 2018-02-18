package DiscordBotCode;

public class Setting {
	public enum SettingType{
		Channel,
		Role,
		User,
		Text
	}
	
	private SettingType type;
	private String key;
	
	public Setting( SettingType type, String key ) {
		this.type = type;
		this.key = key;
	}
	
	public SettingType getType() {
		return type;
	}
	
	public String getKey() {
		return key;
	}
	
	@Override
	public String toString() {
		return key;
	}
}
