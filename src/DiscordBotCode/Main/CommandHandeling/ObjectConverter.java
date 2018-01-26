package DiscordBotCode.Main.CommandHandeling;

import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectConverter
{
	private static final Map<String, Method> CONVERTERS = new HashMap<String, Method>();
	
	static {
		Method[] methods = ObjectConverter.class.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getParameterTypes().length == 1) {
				CONVERTERS.put(method.getParameterTypes()[ 0 ].getName() + "_" + method.getReturnType().getName(), method);
			}
		}
	}
	
	private ObjectConverter() { }
	
	public static <T> T convert( Object from, Class<T> to )
	{
		if (from == null) {
			return null;
		}
		
		if (to.isAssignableFrom(from.getClass())) {
			return to.cast(from);
		}
		
		String converterId = from.getClass().getName() + "_" + to.getName();
		Method converter = CONVERTERS.get(converterId);
		
		if (converter == null) {
			DiscordBotBase.handleException(new UnsupportedOperationException("Cannot convert from " + from.getClass().getName() + " to " + to.getName() + ". Requested converter does not exist."));
			return null;
		}
		
		if(to.isPrimitive()){
			try {
				return (T) converter.invoke(to, from);
			} catch (Exception e) {
				DiscordBotBase.handleException(new RuntimeException("Cannot convert from " + from.getClass().getName() + " to " + to.getName() + ". Conversion failed with " + e.getMessage(), e));
			}
		}
		
		try {
			return to.cast(converter.invoke(to, from));
		} catch (Exception e) {
			DiscordBotBase.handleException(new RuntimeException("Cannot convert from " + from.getClass().getName() + " to " + to.getName() + ". Conversion failed with " + e.getMessage(), e));
		}
		return null;
	}
	
	
	
	
	public static Guild stringToGuild( String value){
		if(Utils.isLong(value)){
			return (Guild) DiscordBotBase.discordClient.getGuildByID(Long.parseLong(value));
		}
		
		return null;
	}

	public static User stringToUser(String value){
		if(Utils.isLong(value)){
			return (User) DiscordBotBase.discordClient.fetchUser(Long.parseLong(value));
		}
		
		return null;
	}
	
	public static Channel stringToChannel( String value){
		if(Utils.isLong(value)){
			return (Channel) DiscordBotBase.discordClient.getChannelByID(Long.parseLong(value));
		}
		
		return null;
	}
	
	public static Message stringToMessage( String value){
		if(Utils.isLong(value)){
			return (Message) DiscordBotBase.discordClient.getMessageByID(Long.parseLong(value));
		}
		
		return null;
	}
	
	public static boolean stringToBoolean(String value) {
		return value.toLowerCase().equals("true");
	}
	
	public static long stringToLong(String value) {
		return Long.parseLong(value);
	}
	
	public static Integer stringToInteger(String value) {
		return Integer.parseInt(value);
	}
	
	
	public static List<Long> stringToLongList(String value){
		ArrayList<Long> list = new ArrayList<>();
		String[] tt = value.substring(1, value.length() - 1).split(",");
		
		for(String t : tt){
			if(Utils.isLong(t)){
				list.add(Long.parseLong(t));
			}
		}
		return list;
	}
	
	public static List<IChannel> stringToChannelList( String value){
		ArrayList<IChannel> list = new ArrayList<>();
		String[] tt = value.substring(1, value.length() - 1).split(",");
		
		for(String t : tt){
			IChannel tmp = stringToChannel(t);
			
			if(tmp != null) {
				list.add(tmp);
			}
		}
		return list;
	}
	
}