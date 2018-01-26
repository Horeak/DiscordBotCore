package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.Main.ChatUtils;
import sx.blah.discord.handle.obj.IMessage;

public class DevNotifications extends DevCommandBase
{
	@Override
	public String commandPrefix()
	{
		return "devNotif";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(DevAccess.isDev(message.getAuthor())){
			boolean t = false;
			
			if(DevAccess.devNotifications.containsKey(message.getAuthor().getLongID()) && DevAccess.devNotifications.size() > 0){
				t = DevAccess.devNotifications.get(message.getAuthor().getLongID());
			}
			
			DevAccess.devNotifications.put(message.getAuthor().getLongID(), !t);
		}
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " You have now " + (DevAccess.devNotifications.get(message.getAuthor().getLongID()) ? "enabled" : "disabled") + " dev notifications!");
	}
}
