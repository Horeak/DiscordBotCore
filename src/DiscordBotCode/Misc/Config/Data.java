package DiscordBotCode.Misc.Config;

import sx.blah.discord.handle.obj.IGuild;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Data {
	private HashMap<Long, CopyOnWriteArrayList<String>> ignoredRoles = new HashMap<>();
	
	public HashMap<Long, CopyOnWriteArrayList<String>> getIgnoredRoles() {
		return ignoredRoles;
	}
	
	public CopyOnWriteArrayList<String> getGuildIgnoredRoles(IGuild guild){
		if(!ignoredRoles.containsKey(guild.getLongID())){
			ignoredRoles.put(guild.getLongID(), new CopyOnWriteArrayList<>());
		}
		
		return ignoredRoles.get(guild.getLongID());
	}
}
