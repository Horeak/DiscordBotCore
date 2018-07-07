package DiscordBotCore;

public class Setting<T> {
	public enum SettingType{
		Channel,
		Role,
		User,
		State,
		Text
	}
	
	private SettingType type;
	private String key;
	private T defValue;
	
	public Setting( SettingType type, String key ) {
		this.type = type;
		this.key = key;
	}
	
	public Setting( SettingType type, String key, T defValue ) {
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
	
	public T getDefValue() {
		return defValue;
	}
	
	@Override
	public String toString() {
		return key;
	}
}
