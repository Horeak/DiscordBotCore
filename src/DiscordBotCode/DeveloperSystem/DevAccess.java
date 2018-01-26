package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DevAccess
{
	public static CopyOnWriteArrayList<Long> devs = new CopyOnWriteArrayList();
	public static ConcurrentHashMap<Long, Boolean> devNotifications = new ConcurrentHashMap<>();
	
	public static File file;
	
	public static void init(){
		file = FileGetter.getFile(DiscordBotBase.FilePath + "/devAccess.txt");
		load();
		
		if(DiscordBotBase.discordClient.getApplicationOwner() != null) {
			if(!devs.contains(DiscordBotBase.discordClient.getApplicationOwner().getLongID()) || !devNotifications.containsKey(DiscordBotBase.discordClient.getApplicationOwner().getLongID())) {
				addDev(DiscordBotBase.discordClient.getApplicationOwner().getLongID());
			}
		}
	}
	
	public static void load(){
		devs.clear();
		devNotifications.clear();
		
		try {
			Files.lines(file.toPath()).forEach(( e ) -> {
				String[] tt = e.split("=");
				
				if(tt.length >= 1) {
					Long id = Long.parseLong(tt[ 0 ]);
					boolean b = Boolean.parseBoolean(tt[ 1 ]);
					
					devs.add(id);
					devNotifications.put(id, b);
				}
				
			});
			
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	
	public static void addDev(Long id){
		if(!isDev(id) || isOwner(id) && !devs.contains(id)) {
			devs.add(id);
			devNotifications.put(id, true);
			FileUtil.addLineToFile(file, id + "=true");
		}
	}
	
	public static void removeDev(Long id){
		if(isDev(id)) {
			devs.remove(id);
			devNotifications.remove(id);
			FileUtil.removeLineFromFile(file, id + "=");
		}
	}
	
	public static boolean isDev( IUser user){
		return isDev(user.getLongID());
	}
	
	public static boolean isOwner(Long id){
		try {
			return DiscordBotBase.discordClient != null && DiscordBotBase.discordClient.getApplicationOwner() != null && DiscordBotBase.discordClient.getApplicationOwner().getLongID() == id;
		}catch (Exception e){
			DiscordBotBase.handleException(e);
		}
		
		return false;
	}
	
	public static boolean isDev(Long id){
		return devs.contains(id) || isOwner(id);
	}
	
	
	public static ArrayList<IUser> getDevs(){
		ArrayList<IUser> users = new ArrayList<>();
		
		for(Long t : devs){
			IUser temp = DiscordBotBase.discordClient.getUserByID(t);
			
			if(temp != null){
				users.add(temp);
			}
		}
		
		return users;
	}
	
	
	public static void msgDevs(String message){
		msgDevs(message, null);
	}
	
	public static void msgDevs(String message, IUser sender){
		for(IUser user : getDevs()){
			if(sender == null || user.getLongID() != sender.getLongID()) {
				if (devNotifications.containsKey(user.getLongID())) {
					if (devNotifications.get(user.getLongID())) {
						ChatUtils.sendMessage(user.getOrCreatePMChannel(), message);
					}
				}
			}
		}
	}
	
}
