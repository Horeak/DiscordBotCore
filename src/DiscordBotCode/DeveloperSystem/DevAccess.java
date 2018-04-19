package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Config.GsonDataManager;
import sx.blah.discord.handle.obj.IUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DevAccess
{
	public static class Data{
		public ConcurrentHashMap<Long, DevUserObject> devList = new ConcurrentHashMap<>();
	}
	
	private static GsonDataManager<Data> data;
	public static GsonDataManager<Data> data() {
		if (data == null) {
			try {
				data = new GsonDataManager<>(Data.class, DiscordBotBase.FilePath + "/devs.json", Data::new);
			} catch (IOException e) {
				System.err.println("Cannot read from config file?");
				e.printStackTrace();
			}
		}
		return data;
	}
	public static void addDev(Long id){
		if(!isDev(id) || isOwner(id) && !data().get().devList.containsKey(id)) {
			data().get().devList.put(id, new DevUserObject());
			
			try {
				data().save();
			} catch (IOException e) {
				DiscordBotBase.handleException(e);
			}
		}
	}
	
	public static void removeDev(Long id){
		if(isDev(id)) {
			data().get().devList.remove(id);
			
			try {
				data().save();
			} catch (IOException e) {
				DiscordBotBase.handleException(e);
			}
		}
	}
	
	public static DevUserObject getDevData( IUser user) {
		if(!isDev(user)) return null;
		
		if(data().get().devList.containsKey(user.getLongID())){
			return data().get().devList.get(user.getLongID());
		}
		
		return null;
	}
	
	public static void updateDevData(IUser user, DevUserObject data){
		if(!isDev(user)) return;
		
		data().get().devList.put(user.getLongID(), data);
		
		try {
			data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
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
		return data().get().devList.containsKey(id) || isOwner(id);
	}
	
	
	public static ArrayList<IUser> getDevs(){
		ArrayList<IUser> users = new ArrayList<>();
		
		for(Long t : data().get().devList.keySet()){
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
		for(Map.Entry<Long, DevUserObject> t : data().get().devList.entrySet()){
			if(t.getValue().notifications){
				IUser temp = DiscordBotBase.discordClient.getUserByID(t.getKey());
				ChatUtils.sendMessage(temp.getOrCreatePMChannel(), message);
			}
		}
	}
	
	public static void msgOwner(String message){
		for(Map.Entry<Long, DevUserObject> t : data().get().devList.entrySet()){
			if(t.getValue().notifications){
				IUser temp = DiscordBotBase.discordClient.getUserByID(t.getKey());
				
				if(isOwner(temp.getLongID())) {
					ChatUtils.sendMessage(temp.getOrCreatePMChannel(), message);
				}
			}
		}
	}
	
}
