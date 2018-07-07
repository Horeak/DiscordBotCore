package DiscordBotCore.Main;

import DiscordBotCore.Extra.FileGetter;
import DiscordBotCore.Extra.FileUtil;
import DiscordBotCore.Misc.Annotation.Init;
import sx.blah.discord.handle.obj.IGuild;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSettings {
	private static ConcurrentHashMap<Long, HashMap<String, String>> values = new ConcurrentHashMap<>();
	
	public static File folder;
	
	@Init
	public static void init(){
		folder = FileGetter.getFolder(DiscordBotBase.FilePath + "/serverSettings/");
		
		if(folder.isDirectory()) {
			for (File fe : folder.listFiles()) {
				if(Utils.isLong(fe.getName())){
					Long id = Long.parseLong(fe.getName());
					
					try {
						Files.lines(fe.toPath()).forEach(( e ) -> {
							if(!e.contains("=")) return;
							
							String[] tt = e.split("=");
							
							if(tt.length == 2){
								if(!values.containsKey(id)){
									values.put(id, new HashMap<>());
								}
								
								values.get(id).put(tt[0], tt[1]);
							}
						});
					} catch (IOException e) {
						DiscordBotBase.handleException(e);
					}
				}
			}
		}
	}
	
	
	public static HashMap<String, String> getMap( IGuild guild){
		return values.getOrDefault(guild.getLongID(), new HashMap<>());
	}
	
	public static String getValue(IGuild guild, String key){
		return getMap(guild).getOrDefault(key, null);
	}
	
	public static void deleteValue(IGuild guild, String key){
		File fe = FileGetter.getFile(folder.getAbsolutePath() + "/" + guild.getLongID());
		FileUtil.removeLineFromFile(fe, key + "=");
	}
	
	public static void saveValue(IGuild guild, String key, String value){
		File fe = FileGetter.getFile(folder.getAbsolutePath() + "/" + guild.getLongID());
		FileUtil.addLineToFile(fe, key + "=" + value);
	}
	
	public static void addValue(IGuild guild, String key, String value){
		if(!values.containsKey(guild.getLongID())){
			values.put(guild.getLongID(), new HashMap<>());
		}
		
		values.get(guild.getLongID()).put(key, value);
		saveValue(guild, key, value);
	}
	
	public static void removeValue(IGuild guild, String key){
		if(!values.containsKey(guild.getLongID())){
			return;
		}
		
		values.get(guild.getLongID()).remove(key);
		deleteValue(guild, key);
	}
	
	public static boolean valueExsists(IGuild guild, String key){
		return values.containsKey(guild.getLongID()) && values.get(guild.getLongID()).containsKey(key);
	}
	
	public static void setValue(IGuild guild, String key, String value){
		removeValue(guild, key);
		addValue(guild, key, value);
	}
}
