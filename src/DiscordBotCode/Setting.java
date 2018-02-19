package DiscordBotCode;

public class Setting {
	public enum SettingType{
		Channel,
		Role,
		User,
		State,
		Text
	}
	
	private SettingType type;
	private String key;
	private Object defValue;
	
	public Setting( SettingType type, String key ) {
		this.type = type;
		this.key = key;
	}
	
	public Setting( SettingType type, String key, Object defValue ) {
		this.type = type;
		this.key = key;
		this.defValue = defValue;
	}
	
	public SettingType getType() {
		return type;
	}
	
	public String getKey() {
		return key;
	}
	
	public Object getDefValue() {
		return defValue;
	}
	
	@Override
	public String toString() {
		return key;
	}
}
