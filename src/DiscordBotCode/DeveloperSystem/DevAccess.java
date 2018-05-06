package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Annotation.DataObject;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DevAccess
{
	@DataObject(file_path = "devs.json", name = "devList")
	public static ConcurrentHashMap<Long, DevUserObject> devList = new ConcurrentHashMap<>();
	

	public static void addDev(Long id){
		if(!isDev(id) || isOwner(id) && !devList.containsKey(id)) {
			devList.put(id, new DevUserObject());
		}
	}
	
	public static void removeDev(Long id){
		if(isDev(id)) {
			devList.remove(id);
		}
	}
	
	public static DevUserObject getDevData( IUser user) {
		if(!isDev(user)) return null;
		
		if(devList.containsKey(user.getLongID())){
			return devList.get(user.getLongID());
		}
		
		return null;
	}
	
	public static void updateDevData(IUser user, DevUserObject data){
		if(!isDev(user)) return;
		devList.put(user.getLongID(), data);
	}
	
	public static boolean isDev( IUser user){
		return isDev(user.getLongID());
	}
	
	public static boolean isOwner(Long id){
		boolean t = false;
		
		try {
			t = DiscordBotBase.discordClient != null && DiscordBotBase.discordClient.getApplicationOwner() != null && DiscordBotBase.discordClient.getApplicationOwner().getLongID() == id;
		}catch (Exception e){
			DiscordBotBase.handleException(e);
		}
		
		return t;
	}
	
	public static boolean isDev(Long id){
		return devList.containsKey(id) || isOwner(id);
	}
	
	
	public static ArrayList<IUser> getDevs(){
		ArrayList<IUser> users = new ArrayList<>();
		
		for(Long t : devList.keySet()){
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
		for(Map.Entry<Long, DevUserObject> t : devList.entrySet()){
			if(t.getValue().notifications){
				IUser temp = DiscordBotBase.discordClient.getUserByID(t.getKey());
				ChatUtils.sendMessage(temp.getOrCreatePMChannel(), message);
			}
		}
	}
	
	public static void msgOwner(String message){
		for(Map.Entry<Long, DevUserObject> t : devList.entrySet()){
			if(t.getValue().notifications){
				IUser temp = DiscordBotBase.discordClient.getUserByID(t.getKey());
				
				if(isOwner(temp.getLongID())) {
					ChatUtils.sendMessage(temp.getOrCreatePMChannel(), message);
				}
			}
		}
	}
	
}
